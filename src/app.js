/**
 * UI controller. All processing happens locally in the browser.
 */
import { PARSERS, ACCEPTED_EXTENSIONS, parseDiveLogFile } from './formats/index.js';
import { readExif, writeExif, parseExifDate } from './exif/jpeg-exif.js';
import { photoUtcTime, matchPhoto } from './match.js';
import { createZip } from './zip.js';
import { profileSvg } from './ui/profile.js';

const $ = (sel) => document.querySelector(sel);

const state = {
  dives: [],
  /** @type {{id:number, file:File, exif:any, error:string|null, url:string}[]} */
  photos: [],
  tzTouched: false,
  rows: [],
};
let photoId = 0;

// ------------------------------------------------------------------ setup

$('#log-formats').textContent = `Supported: ${PARSERS.map((p) => `${p.name} (${p.extensions.join(', ')})`).join(' · ')}, or a .zip containing them (e.g. a Garmin Connect export)`;
$('#log-input').accept = ACCEPTED_EXTENSIONS.join(',');

const tzSelect = $('#tz-select');
for (let m = -12 * 60; m <= 14 * 60; m += 15) {
  const opt = document.createElement('option');
  opt.value = String(m);
  opt.textContent = `UTC${formatOffset(m)}`;
  tzSelect.append(opt);
}
tzSelect.value = String(-new Date().getTimezoneOffset());
tzSelect.addEventListener('change', () => (state.tzTouched = true));

setupDropzone('#log-drop', '#log-input', addLogs);
setupDropzone('#photo-drop', '#photo-input', addPhotos);
$('#settings').addEventListener('input', render);
$('#settings').addEventListener('change', render);
$('#save-zip').addEventListener('click', saveZip);
$('#save-folder').addEventListener('click', saveFolder);
$('#clear').addEventListener('click', clearAll);
$('#sync-apply').addEventListener('click', applySync);
if ('showDirectoryPicker' in window) $('#save-folder').hidden = false;

function setupDropzone(zoneSel, inputSel, handler) {
  const zone = $(zoneSel);
  const input = $(inputSel);
  input.addEventListener('change', () => {
    handler([...input.files]);
    input.value = '';
  });
  zone.addEventListener('dragover', (e) => {
    e.preventDefault();
    zone.classList.add('over');
  });
  zone.addEventListener('dragleave', () => zone.classList.remove('over'));
  zone.addEventListener('drop', (e) => {
    e.preventDefault();
    zone.classList.remove('over');
    handler([...e.dataTransfer.files]);
  });
}

// ------------------------------------------------------------------ input

async function addLogs(files) {
  for (const file of files) {
    try {
      const dives = await parseDiveLogFile(file.name, await file.arrayBuffer());
      if (!dives.length) throw new Error('no dive data found');
      state.dives.push(...dives);
    } catch (err) {
      toast(`${file.name}: ${err.message}`);
    }
  }
  state.dives.sort((a, b) => a.startTime - b.startTime);
  // Default the camera time zone to the one the dive computer used.
  const tz = state.dives.find((d) => d.utcOffsetMinutes !== null)?.utcOffsetMinutes;
  if (!state.tzTouched && tz != null && tzSelect.querySelector(`option[value="${tz}"]`)) tzSelect.value = String(tz);
  render();
}

async function addPhotos(files) {
  const jpegs = files.filter((f) => /\.jpe?g$/i.test(f.name) || f.type === 'image/jpeg');
  if (jpegs.length < files.length) toast(`Skipped ${files.length - jpegs.length} non-JPEG file(s)`);
  for (const file of jpegs) {
    const photo = { id: ++photoId, file, exif: null, error: null, url: URL.createObjectURL(file) };
    try {
      // EXIF lives at the start of the file; avoid reading huge files twice.
      let exif = null;
      try {
        exif = readExif(new Uint8Array(await file.slice(0, 256 * 1024).arrayBuffer()));
      } catch {
        /* fall through */
      }
      if (!exif?.dateTimeOriginal) exif = readExif(new Uint8Array(await file.arrayBuffer()));
      photo.exif = exif;
    } catch (err) {
      photo.error = err.message;
    }
    state.photos.push(photo);
  }
  state.photos.sort((a, b) => (a.exif?.dateTimeOriginal || '').localeCompare(b.exif?.dateTimeOriginal || ''));
  render();
}

function clearAll() {
  state.photos.forEach((p) => URL.revokeObjectURL(p.url));
  state.dives = [];
  state.photos = [];
  state.tzTouched = false;
  render();
}

// ------------------------------------------------------------------ settings

function readSettings() {
  const f = $('#settings').elements;
  const lat = parseFloat(f.manualLat.value);
  const lon = parseFloat(f.manualLon.value);
  const manual = Number.isFinite(lat) && Number.isFinite(lon) && Math.abs(lat) <= 90 && Math.abs(lon) <= 180;
  return {
    cameraUtcOffsetMinutes: parseInt(f.cameraUtcOffsetMinutes.value, 10) || 0,
    clockCorrectionSeconds: parseCorrection(f.clockCorrection.value),
    preferOffsetTag: f.preferOffsetTag.checked,
    locationMode: f.locationMode.value,
    manualPosition: manual ? { lat, lon } : null,
    toleranceMinutes: Math.max(0, parseFloat(f.toleranceMinutes.value) || 0),
    writePosition: f.writePosition.checked,
    depthAsAltitude: f.depthAsAltitude.checked,
    writeDiveTags: f.writeDiveTags.checked,
    skipExistingGps: f.skipExistingGps.checked,
  };
}

/** "[-]h:mm:ss", "[-]m:ss" or "[-]seconds" → seconds (NaN-safe). */
export function parseCorrection(s) {
  s = String(s || '').trim();
  if (!s) return 0;
  const sign = s.startsWith('-') ? -1 : 1;
  const parts = s.replace(/^[+-]/, '').split(':').map(Number);
  if (parts.some((n) => !Number.isFinite(n))) return 0;
  return sign * parts.reduce((acc, n) => acc * 60 + n, 0);
}

function applySync() {
  const photo = state.photos.find((p) => String(p.id) === $('#sync-photo').value);
  const shown = $('#sync-time').value; // "HH:MM" or "HH:MM:SS"
  if (!photo?.exif?.dateTimeOriginal || !shown) return toast('Choose a photo and enter the time shown on the dive computer');
  const s = readSettings();
  const wall = parseExifDate(photo.exif.dateTimeOriginal, photo.exif.subSecTimeOriginal);
  const uncorrected = photoUtcTime(photo.exif, { ...s, clockCorrectionSeconds: 0 });

  // The dive computer shows local time in the zone of the nearest dive.
  const dive = nearestDive(uncorrected);
  const diveOffset = dive?.utcOffsetMinutes ?? s.cameraUtcOffsetMinutes;
  const [h, m, sec = 0] = shown.split(':').map(Number);
  const day = new Date(wall);
  let shownWall = Date.UTC(day.getUTCFullYear(), day.getUTCMonth(), day.getUTCDate(), h, m, sec);
  let actual = shownWall - diveOffset * 60_000;
  // Pick the nearest calendar day (handles photos taken around midnight).
  const dayMs = 86_400_000;
  while (actual - uncorrected > dayMs / 2) actual -= dayMs;
  while (uncorrected - actual > dayMs / 2) actual += dayMs;

  const correction = Math.round((actual - uncorrected) / 1000);
  $('#settings').elements.clockCorrection.value = formatCorrection(correction);
  render();
  toast(`Clock correction set to ${formatCorrection(correction)}`);
}

function nearestDive(t) {
  let best = null;
  let dist = Infinity;
  for (const d of state.dives) {
    const x = t < d.startTime ? d.startTime - t : t > d.endTime ? t - d.endTime : 0;
    if (x < dist) [best, dist] = [d, x];
  }
  return best;
}

// ------------------------------------------------------------------ matching

function computeRows(s) {
  return state.photos.map((photo) => {
    const row = { photo, time: null, match: null, status: 'ready', note: '' };
    if (photo.error) return { ...row, status: 'error', note: photo.error };
    row.time = photoUtcTime(photo.exif, s);
    if (row.time === null) return { ...row, status: 'error', note: 'No capture time in EXIF' };
    row.match = matchPhoto(row.time, state.dives, s);
    if (!row.match) return { ...row, status: 'skip', note: 'No dive at this time' };
    if (s.skipExistingGps && photo.exif.gps) return { ...row, status: 'skip', note: 'Already has GPS' };
    const tags = tagsFor(row, s);
    if (!Object.keys(tags).some((k) => k !== 'time' && k !== 'processingMethod'))
      return { ...row, status: 'skip', note: 'Nothing to write' };
    if (row.match.lat === null && s.writePosition) row.note = 'No position (add a fallback)';
    else if (row.match.phase !== 'underwater') row.note = `Surface, ${row.match.phase} dive`;
    return row;
  });
}

function tagsFor(row, s) {
  const m = row.match;
  const tags = { time: row.time, processingMethod: `Dive log: ${m.dive.device || m.dive.source}` };
  if (s.writePosition && m.lat !== null) Object.assign(tags, { lat: m.lat, lon: m.lon });
  if (s.depthAsAltitude && m.depth !== null) tags.altitude = -m.depth;
  if (s.writeDiveTags) {
    if (m.depth !== null) tags.waterDepth = m.depth;
    if (m.temperature !== null) tags.temperature = m.temperature;
  }
  return tags;
}

// ------------------------------------------------------------------ render

function render() {
  const s = readSettings();
  state.rows = computeRows(s);
  renderDives();
  renderTable();
  renderSyncOptions();

  const ready = state.rows.filter((r) => r.status === 'ready').length;
  $('#save-zip').disabled = $('#save-folder').disabled = ready === 0;
  $('#summary').textContent =
    !state.dives.length && !state.photos.length
      ? 'Add dive logs and photos to get started.'
      : `${state.dives.length} dive(s), ${state.photos.length} photo(s) — ${ready} ready to geotag.`;
}

function renderDives() {
  const byDive = new Map();
  for (const r of state.rows) if (r.match) byDive.set(r.match.dive, [...(byDive.get(r.match.dive) || []), r.time]);
  $('#dive-list').innerHTML = state.dives
    .map((d) => {
      const pos = d.entry ? `${d.entry.lat.toFixed(5)}, ${d.entry.lon.toFixed(5)}` : 'no GPS fix';
      const photos = byDive.get(d)?.length || 0;
      return `<article class="dive">
        <header>
          <strong>${esc(fmtLocal(d.startTime, d.utcOffsetMinutes))}</strong>
          <span>${Math.round((d.endTime - d.startTime) / 60000)} min</span>
          <span>max ${d.maxDepth?.toFixed(1) ?? '–'} m</span>
          <span>${d.minTemperature ?? '–'} °C</span>
          <span>${esc(pos)}</span>
          <span class="muted">${photos} photo(s) · ${esc(d.fileName)}</span>
        </header>
        ${profileSvg(d, byDive.get(d))}
      </article>`;
    })
    .join('');
}

function renderTable() {
  $('#photo-table tbody').innerHTML = state.rows
    .map((r) => {
      const m = r.match;
      const pos =
        m && m.lat !== null
          ? `<a href="https://www.openstreetmap.org/?mlat=${m.lat}&mlon=${m.lon}#map=15/${m.lat}/${m.lon}" target="_blank" rel="noopener">${m.lat.toFixed(5)}, ${m.lon.toFixed(5)}</a>`
          : '–';
      return `<tr class="st-${r.status}">
        <td><img src="${r.photo.url}" alt="" loading="lazy" decoding="async" width="56" height="42" /></td>
        <td>${esc(r.photo.file.name)}<br /><small class="muted">${esc(r.photo.exif?.dateTimeOriginal || '')}</small></td>
        <td>${r.time !== null ? esc(new Date(r.time).toISOString().replace('T', ' ').slice(0, 19)) : '–'}</td>
        <td>${m ? esc(fmtLocal(m.dive.startTime, m.dive.utcOffsetMinutes).slice(-5)) : '–'}</td>
        <td>${m?.depth != null ? m.depth.toFixed(1) + ' m' : '–'}</td>
        <td>${m?.temperature != null ? m.temperature.toFixed(1) + ' °C' : '–'}</td>
        <td>${pos}</td>
        <td><span class="badge">${r.status}</span> <small>${esc(r.note)}</small></td>
      </tr>`;
    })
    .join('');
}

function renderSyncOptions() {
  const sel = $('#sync-photo');
  const current = sel.value;
  sel.innerHTML = state.photos
    .filter((p) => p.exif?.dateTimeOriginal)
    .map((p) => `<option value="${p.id}">${esc(p.file.name)} (${esc(p.exif.dateTimeOriginal)})</option>`)
    .join('');
  if (current) sel.value = current;
}

// ------------------------------------------------------------------ output

async function* taggedFiles() {
  const s = readSettings();
  for (const r of state.rows.filter((x) => x.status === 'ready')) {
    try {
      const data = writeExif(new Uint8Array(await r.photo.file.arrayBuffer()), tagsFor(r, s));
      yield { name: r.photo.file.name, data, date: new Date(r.photo.file.lastModified) };
    } catch (err) {
      toast(`${r.photo.file.name}: ${err.message}`);
    }
  }
}

async function saveZip() {
  await busy('#save-zip', async () => {
    const files = [];
    for await (const f of taggedFiles()) files.push(f);
    if (!files.length) return;
    const blob = new Blob([createZip(files)], { type: 'application/zip' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `geotagged-${new Date().toISOString().slice(0, 10)}.zip`;
    a.click();
    setTimeout(() => URL.revokeObjectURL(a.href), 10_000);
    toast(`Saved ${files.length} photo(s)`);
  });
}

async function saveFolder() {
  let dir;
  try {
    dir = await window.showDirectoryPicker({ mode: 'readwrite' });
  } catch {
    return; // cancelled
  }
  await busy('#save-folder', async () => {
    let n = 0;
    for await (const f of taggedFiles()) {
      const name = await freeName(dir, f.name); // never overwrite originals
      const handle = await dir.getFileHandle(name, { create: true });
      const w = await handle.createWritable();
      await w.write(f.data);
      await w.close();
      n++;
    }
    toast(`Wrote ${n} photo(s) to “${dir.name}”`);
  });
}

async function freeName(dir, name) {
  const exists = async (n) => {
    try {
      await dir.getFileHandle(n);
      return true;
    } catch {
      return false;
    }
  };
  if (!(await exists(name))) return name;
  const [, stem, ext] = /^(.*?)(\.[^.]*)?$/.exec(name);
  for (let i = 1; ; i++) {
    const n = `${stem}-geotagged${i > 1 ? '-' + i : ''}${ext || ''}`;
    if (!(await exists(n))) return n;
  }
}

async function busy(sel, fn) {
  const btn = $(sel);
  const label = btn.textContent;
  btn.disabled = true;
  btn.textContent = 'Working…';
  try {
    await fn();
  } finally {
    btn.textContent = label;
    btn.disabled = false;
  }
}

// ------------------------------------------------------------------ utils

function toast(msg) {
  const el = document.createElement('div');
  el.className = 'toast';
  el.setAttribute('role', 'status');
  el.textContent = msg;
  document.body.append(el);
  setTimeout(() => el.remove(), 5000);
}

function formatOffset(m) {
  const sign = m < 0 ? '-' : '+';
  const a = Math.abs(m);
  return `${sign}${String(Math.floor(a / 60)).padStart(2, '0')}:${String(a % 60).padStart(2, '0')}`;
}

function formatCorrection(sec) {
  const sign = sec < 0 ? '-' : '+';
  let a = Math.abs(sec);
  const h = Math.floor(a / 3600);
  a %= 3600;
  const m = Math.floor(a / 60);
  const s = a % 60;
  return `${sign}${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function fmtLocal(t, offsetMinutes) {
  const off = offsetMinutes ?? 0;
  const iso = new Date(t + off * 60_000).toISOString();
  return `${iso.slice(0, 10)} ${iso.slice(11, 16)}`;
}

function esc(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}

render();
