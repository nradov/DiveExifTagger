// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * UI controller. All processing happens locally in the browser.
 */
import { PARSERS, ACCEPTED_EXTENSIONS, parseDiveLogFile } from './formats/index.js';
import { readExif, writeExif, parseExifDate } from './exif/jpeg-exif.js';
import { photoTime, matchPhoto } from './match.js';
import { buildTags, hasPayload } from './tags.js';
import { zipSync } from 'fflate';
import { profileSvg } from './ui/profile.js';
import { defaultUnits, formatDepth, formatTemperature } from './ui/units.js';

const $ = (sel) => document.querySelector(sel);

const state = {
  dives: [],
  /** @type {{id:number, file:File, exif:any, error:string|null, url:string}[]} */
  photos: [],
  rows: [],
  units: loadUnits(),
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

// Display units (page only; EXIF is always metric). Remembered per browser.
for (const radio of document.querySelectorAll('input[name=units]')) {
  radio.checked = radio.value === state.units;
  radio.addEventListener('change', () => {
    state.units = radio.value;
    try {
      localStorage.setItem('units', state.units);
    } catch {
      /* storage unavailable: keep the choice for this visit only */
    }
    render();
  });
}

function loadUnits() {
  try {
    const saved = localStorage.getItem('units');
    if (saved === 'metric' || saved === 'statute') return saved;
  } catch {
    /* ignore */
  }
  return defaultUnits(navigator.language);
}

setupDropzone('#log-drop', '#log-input', addLogs);
setupDropzone('#photo-drop', '#photo-input', addPhotos);
$('#settings').addEventListener('input', render);
$('#settings').addEventListener('change', render);
$('#save-zip').addEventListener('click', saveZip);
$('#save-folder').addEventListener('click', saveFolder);
$('#clear').addEventListener('click', clearAll);
$('#sync-apply').addEventListener('click', applySync);
if ('showDirectoryPicker' in window) $('#save-folder').hidden = false;

// On GitHub Pages (https://<user>.github.io/<repo>/), point the footer at the source repo.
{
  const m = /^([a-z0-9-]+)\.github\.io$/i.exec(location.hostname);
  const repo = location.pathname.split('/').filter(Boolean)[0];
  if (m && repo) $('#repo-link').href = `https://github.com/${m[1]}/${repo}`;
}

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
  render();
}

// ------------------------------------------------------------------ settings

function readSettings() {
  const f = $('#settings').elements;
  const manualMode = f.locationMode.value === 'manual';
  $('#location-hint').hidden = f.locationMode.value !== 'auto';
  // Latitude/longitude are only editable (and only used) in manual mode.
  f.manualLat.disabled = f.manualLon.disabled = !manualMode;
  const lat = parseFloat(f.manualLat.value);
  const lon = parseFloat(f.manualLon.value);
  const manual = manualMode && Number.isFinite(lat) && Number.isFinite(lon) && Math.abs(lat) <= 90 && Math.abs(lon) <= 180;
  return {
    cameraUtcOffsetMinutes: f.cameraUtcOffsetMinutes.value === 'auto' ? null : parseInt(f.cameraUtcOffsetMinutes.value, 10),
    fallbackUtcOffsetMinutes: -new Date().getTimezoneOffset(),
    clockCorrectionSeconds: parseCorrection(f.clockCorrection.value),
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
  const pt = photoTime(photo.exif, state.dives, { ...s, clockCorrectionSeconds: 0 });
  const uncorrected = pt.time;

  // The dive computer shows local time in the zone of the nearest dive.
  const dive = nearestDive(uncorrected);
  const diveOffset = dive?.utcOffsetMinutes ?? pt.offsetMinutes;
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
    row.pt = photoTime(photo.exif, state.dives, s);
    if (!row.pt) return { ...row, status: 'error', note: 'No capture time in EXIF' };
    row.time = row.pt.time;
    row.match = matchPhoto(row.time, state.dives, s);
    if (!row.match) return { ...row, status: 'skip', note: 'No dive at this time' };
    if (s.skipExistingGps && photo.exif.gps) return { ...row, status: 'skip', note: 'Already has GPS' };
    if (!hasPayload(buildTags(row.match, row.time, s))) return { ...row, status: 'skip', note: 'Nothing to write' };
    const m = row.match;
    const isDive = m.dive.activity.isDive;
    if (m.lat === null && s.writePosition) row.note = 'No GPS position in log (use a manual position)';
    else if (!isDive && m.gpsGapSeconds > 120)
      row.note = `Position interpolated across a ${Math.round(m.gpsGapSeconds / 60)}-minute GPS gap`;
    else if (m.phase !== 'underwater') row.note = isDive ? `Surface, ${m.phase} dive` : `${m.phase === 'before' ? 'Before' : 'After'} the activity`;
    return row;
  });
}

// ------------------------------------------------------------------ render

function render() {
  updateDiveOnlyOptions();
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
      : `${state.dives.length} log(s), ${state.photos.length} photo(s) — ${ready} ready to geotag.`;
}

/**
 * Depth and temperature only exist for dives. When only non-diving
 * activities (GPS tracks) are loaded, switch those options off and explain;
 * restore the user's choices once a dive is loaded again.
 */
function updateDiveOnlyOptions() {
  const f = $('#settings').elements;
  const tracks = state.dives.filter((d) => !d.activity.isDive);
  const onlyTracks = tracks.length > 0 && tracks.length === state.dives.length;
  for (const box of [f.depthAsAltitude, f.writeDiveTags]) {
    if (onlyTracks && !box.disabled) {
      box.dataset.wasChecked = String(box.checked);
      box.checked = false;
      box.disabled = true;
    } else if (!onlyTracks && box.disabled) {
      box.checked = box.dataset.wasChecked !== 'false';
      box.disabled = false;
    }
  }
  const note = $('#track-note');
  const names = [...new Set(tracks.map((d) => d.activity.name || 'non-diving'))].join(', ');
  note.hidden = tracks.length === 0;
  note.textContent = onlyTracks
    ? `Depth and temperature are off: the loaded log (${names}) isn't a dive. Positions come from its GPS track.`
    : `Depth and temperature are written only for dives, not for the ${names} activity.`;
}

function renderDives() {
  const byDive = new Map();
  for (const r of state.rows) if (r.match) byDive.set(r.match.dive, [...(byDive.get(r.match.dive) || []), r.time]);
  $('#dive-list').innerHTML = state.dives
    .map((d) => {
      const pos = d.entry ? `${d.entry.lat.toFixed(5)}, ${d.entry.lon.toFixed(5)}` : 'no GPS fix';
      const photos = byDive.get(d)?.length || 0;
      const fixes = d.samples.filter((x) => x.lat !== null).length;
      const details = d.activity.isDive
        ? `<span>max ${formatDepth(d.maxDepth, state.units)}</span>
          <span>${formatTemperature(d.minTemperature, state.units, 0)}</span>`
        : `<span>${esc(d.activity.name || 'Activity')} (not a dive)</span>
          <span>GPS track, ${fixes} fixes</span>`;
      return `<article class="dive">
        <header>
          <strong>${esc(fmtDiveTime(d.startTime, d.utcOffsetMinutes))}</strong>
          <span class="muted">${esc(fmtZone(d.utcOffsetMinutes))}</span>
          <span>${Math.round((d.endTime - d.startTime) / 60000)} min</span>
          ${details}
          <span>${esc(pos)}</span>
          <span class="muted">${photos} photo(s) · ${esc(d.fileName)}</span>
        </header>
        ${profileSvg(d, byDive.get(d), state.units)}
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
        <td>${
          r.pt
            ? `${esc(new Date(r.time).toISOString().replace('T', ' ').slice(0, 19))}<br /><small class="muted tz-src">camera on UTC${formatOffset(r.pt.offsetMinutes)}, ${TZ_SOURCE[r.pt.source]}</small>`
            : '–'
        }</td>
        <td>${m ? esc(fmtDiveTime(m.dive.startTime, m.dive.utcOffsetMinutes, { timeStyle: 'short' })) : '–'}</td>
        <td>${formatDepth(m?.depth, state.units)}</td>
        <td>${formatTemperature(m?.temperature, state.units)}</td>
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
      const data = writeExif(new Uint8Array(await r.photo.file.arrayBuffer()), buildTags(r.match, r.time, s));
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
    // level 0 = store: JPEGs are already compressed
    const zip = zipSync(Object.fromEntries(files.map((f) => [f.name, [f.data, { level: 0, mtime: f.date }]])));
    const blob = new Blob([zip], { type: 'application/zip' });
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

const TZ_SOURCE = {
  manual: 'set manually',
  photo: 'from photo',
  'dive computer': 'from dive computer',
  'this computer': 'this computer’s zone',
};

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

/**
 * Format a dive time as the dive's local wall-clock time (the dive computer's
 * UTC offset), using the browser's locale conventions. The offset is applied
 * by shifting the instant and formatting it as UTC, which works in every
 * browser regardless of support for offset time zones in Intl.
 */
function fmtDiveTime(t, offsetMinutes, style = { dateStyle: 'medium', timeStyle: 'short' }) {
  const shifted = new Date(t + (offsetMinutes ?? 0) * 60_000);
  return new Intl.DateTimeFormat(undefined, { ...style, timeZone: 'UTC' }).format(shifted);
}

function fmtZone(offsetMinutes) {
  return offsetMinutes == null ? 'UTC' : `UTC${formatOffset(offsetMinutes)}`;
}

function esc(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}

render();
