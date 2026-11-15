import { test } from 'node:test';
import assert from 'node:assert/strict';

import { parseDiveLog, parseDiveLogFile, findParser } from '../src/formats/index.js';
import { readExif, writeExif, parseExifOffset } from '../src/exif/jpeg-exif.js';
import { photoUtcTime, matchPhoto, interpolate } from '../src/match.js';
import { createZip, readZip } from '../src/zip.js';
import { buildDeflateZip } from './helpers/zip-builder.js';
import { buildDiveFit, FitWriter, toFitTime } from './helpers/fit-writer.js';
import { Decoder, Stream } from '@garmin/fitsdk';
import { buildJpeg, MAKER_NOTE_MAGIC } from './helpers/jpeg-builder.js';

const near = (a, b, eps = 1e-6) => assert.ok(Math.abs(a - b) <= eps, `${a} ≉ ${b}`);

// ------------------------------------------------------------------ FIT

test('FIT: parses a Garmin dive into the dive model', () => {
  const fit = buildDiveFit();
  assert.equal(findParser('x.fit', fit.bytes).id, 'garmin-fit');
  const dives = parseDiveLog('dive.fit', fit.bytes);
  assert.equal(dives.length, 1);
  const d = dives[0];
  assert.equal(d.startTime, fit.start);
  assert.equal(d.endTime, fit.end);
  assert.equal(d.utcOffsetMinutes, 600);
  near(d.entry.lat, fit.entry.lat, 1e-6);
  near(d.entry.lon, fit.entry.lon, 1e-6);
  near(d.exit.lat, fit.exit.lat, 1e-6);
  near(d.maxDepth, 18, 1e-3);
  assert.equal(d.minTemperature, 24);
  assert.equal(d.samples.length, 241);
  assert.equal(d.device, 'Garmin Descent Mk3i');
});

test('FIT: fixture files are valid for the official SDK (header + CRC)', () => {
  const w = new FitWriter();
  const t0 = toFitTime(Date.UTC(2026, 0, 1)) & ~0x1f;
  w.message(20, [[253, 'uint32', t0 + 30]]);
  w.compressed(20, [[92, 'uint32', 5000]], 2); // compressed timestamp header
  const decoder = new Decoder(Stream.fromByteArray(w.build()));
  assert.equal(decoder.isFIT(), true);
  assert.equal(decoder.checkIntegrity(), true);
  const records = [];
  decoder.read({ mesgListener: (num, m) => num === 20 && records.push(m) });
  assert.equal(records.at(-1).depth, 5);
});

test('FIT: rejects non-FIT data', () => {
  assert.throws(() => parseDiveLog('x.fit', new Uint8Array(40)));
});

test('FIT inside a Garmin Connect style ZIP (deflate + data descriptors)', async () => {
  const fit = buildDiveFit();
  const zip = buildDeflateZip([
    { name: '__MACOSX/._21345678901_ACTIVITY.fit', data: new Uint8Array([0, 5, 22, 7]) },
    { name: '21345678901_ACTIVITY.fit', data: fit.bytes },
  ]);
  const dives = await parseDiveLogFile('activity_21345678901.zip', zip);
  assert.equal(dives.length, 1);
  assert.equal(dives[0].startTime, fit.start);
  assert.equal(dives[0].fileName, 'activity_21345678901.zip › 21345678901_ACTIVITY.fit');
  near(dives[0].maxDepth, 18, 1e-3);
});

test('ZIP with several FIT files (stored) yields every dive; plain FIT still works', async () => {
  const a = buildDiveFit({ start: Date.UTC(2026, 10, 14, 1) });
  const b = buildDiveFit({ start: Date.UTC(2026, 10, 14, 4) });
  const zip = createZip([
    { name: 'dives/a.fit', data: a.bytes },
    { name: 'dives/readme.txt', data: new TextEncoder().encode('hi') },
    { name: 'dives/b.FIT', data: b.bytes },
  ]);
  const dives = await parseDiveLogFile('export.zip', zip);
  assert.deepEqual(dives.map((d) => d.startTime).sort(), [a.start, b.start]);
  assert.equal((await parseDiveLogFile('a.fit', a.bytes)).length, 1);
});

test('ZIP without dive logs gives a clear error', async () => {
  const zip = createZip([{ name: 'photo.txt', data: new Uint8Array([1, 2, 3]) }]);
  await assert.rejects(parseDiveLogFile('x.zip', zip), /no supported dive log/);
  const round = await readZip(zip);
  assert.deepEqual([...round[0].data], [1, 2, 3]);
});

// ------------------------------------------------------------------ EXIF

for (const little of [true, false]) {
  test(`EXIF (${little ? 'II' : 'MM'}): writes GPS, depth and temperature, preserves maker note`, () => {
    const jpeg = buildJpeg({ little });
    const before = readExif(jpeg);
    assert.equal(before.dateTimeOriginal, '2026:11:14 11:20:00');
    assert.equal(before.gps, null);

    const out = writeExif(jpeg, {
      lat: -16.5004321,
      lon: 145.7512345,
      altitude: -12.34,
      time: Date.UTC(2026, 10, 14, 1, 20, 0),
      waterDepth: 12.34,
      temperature: 24.5,
      processingMethod: 'test',
    });
    const after = readExif(out);
    near(after.gps.lat, -16.5004321, 1e-7);
    near(after.gps.lon, 145.7512345, 1e-7);
    near(after.gps.altitude, -12.34, 1e-9);
    near(after.waterDepth, 12.34, 1e-9);
    near(after.temperature, 24.5, 1e-9);
    assert.equal(after.dateTimeOriginal, before.dateTimeOriginal);
    assert.equal(after.make, 'TestCam');
    assert.equal(after.model, 'Housing 1');

    // Maker note bytes are at the same absolute TIFF offset as before.
    const tiffStart = (b) => indexOf(b, [0x45, 0x78, 0x69, 0x66, 0, 0]) + 6;
    const magic = [...new TextEncoder().encode(MAKER_NOTE_MAGIC)];
    assert.equal(indexOf(out, magic) - tiffStart(out), indexOf(jpeg, magic) - tiffStart(jpeg));

    // Image data is untouched.
    const sos = (b) => b.subarray(indexOf(b, [0xff, 0xda]));
    assert.deepEqual(sos(out), sos(jpeg));

    // Writing twice still works (re-tagging an already tagged file).
    const twice = readExif(writeExif(out, { lat: 1, lon: 2 }));
    near(twice.gps.lat, 1, 1e-9);
    near(twice.waterDepth, 12.34, 1e-9);
  });
}

test('EXIF: keeps existing GPS entries (e.g. compass heading) when adding a position', () => {
  // Simulate a camera with a compass (e.g. OM System TG-7): GPS IFD holding only a heading.
  const withHeading = writeExif(buildJpeg(), { altitude: 0 });
  const out = writeExif(addImgDirection(withHeading, 47), { lat: 36.43, lon: -121.93, waterDepth: 40 });
  assert.equal(readImgDirection(out), 47);
  near(readExif(out).gps.lat, 36.43, 1e-9);
});

test('EXIF: creates an EXIF block when the JPEG has none', () => {
  const jpeg = buildJpeg({ exif: false });
  const out = writeExif(jpeg, { lat: 10.25, lon: -20.5, waterDepth: 5 });
  const e = readExif(out);
  near(e.gps.lat, 10.25, 1e-9);
  near(e.gps.lon, -20.5, 1e-9);
  near(e.waterDepth, 5, 1e-9);
  // APP0 (JFIF) stays first
  assert.deepEqual([...out.subarray(0, 4)], [0xff, 0xd8, 0xff, 0xe0]);
});

test('EXIF: offset parsing', () => {
  assert.equal(parseExifOffset('+10:00'), 600);
  assert.equal(parseExifOffset('-05:30'), -330);
  assert.equal(parseExifOffset(''), null);
});

// ------------------------------------------------------------------ matching

test('matching: time conversion, interpolation and location modes', () => {
  const fit = buildDiveFit();
  const [dive] = parseDiveLog('d.fit', fit.bytes);

  // Camera set to local time (UTC+10), 20 min into the dive, camera 30 s slow.
  const t = photoUtcTime(
    { dateTimeOriginal: '2026:11:14 11:19:30', offsetTimeOriginal: null, subSecTimeOriginal: null },
    { cameraUtcOffsetMinutes: 600, clockCorrectionSeconds: 30, preferOffsetTag: true },
  );
  assert.equal(t, fit.start + 20 * 60_000);

  const base = { toleranceMinutes: 30, manualPosition: null };
  const m = matchPhoto(t, [dive], { ...base, locationMode: 'entry' });
  assert.equal(m.phase, 'underwater');
  near(m.depth, 18, 1e-6);
  near(m.lat, fit.entry.lat, 1e-6);

  const mi = matchPhoto(t, [dive], { ...base, locationMode: 'interpolate' });
  near(mi.lat, (fit.entry.lat + fit.exit.lat) / 2, 1e-6);

  const before = matchPhoto(fit.start - 5 * 60_000, [dive], { ...base, locationMode: 'entry' });
  assert.equal(before.phase, 'before');
  assert.equal(before.depth, 0);

  assert.equal(matchPhoto(fit.end + 31 * 60_000, [dive], { ...base, locationMode: 'entry' }), null);

  const manual = matchPhoto(t, [dive], { ...base, locationMode: 'manual', manualPosition: { lat: 1, lon: 2 } });
  assert.deepEqual([manual.lat, manual.lon], [1, 2]);
});

test('matching: OffsetTimeOriginal wins when preferred', () => {
  const exif = { dateTimeOriginal: '2026:11:14 03:00:00', offsetTimeOriginal: '+02:00', subSecTimeOriginal: null };
  assert.equal(
    photoUtcTime(exif, { cameraUtcOffsetMinutes: 600, clockCorrectionSeconds: 0, preferOffsetTag: true }),
    Date.UTC(2026, 10, 14, 1),
  );
  assert.equal(
    photoUtcTime(exif, { cameraUtcOffsetMinutes: 600, clockCorrectionSeconds: 0, preferOffsetTag: false }),
    Date.UTC(2026, 10, 13, 17),
  );
});

test('interpolate skips missing values', () => {
  const s = [
    { time: 0, depth: 0 },
    { time: 10, depth: null },
    { time: 20, depth: 10 },
  ];
  assert.equal(interpolate(s, 10, 'depth'), 5);
  assert.equal(interpolate(s, -5, 'depth'), 0);
});

// ------------------------------------------------------------------ zip

test('zip: produces a valid stored archive', () => {
  const zip = createZip([
    { name: 'a.txt', data: new TextEncoder().encode('hello') },
    { name: 'b.jpg', data: new Uint8Array([1, 2, 3]) },
  ]);
  assert.deepEqual([...zip.subarray(0, 4)], [0x50, 0x4b, 0x03, 0x04]);
  const v = new DataView(zip.buffer, zip.byteOffset);
  const eocd = zip.length - 22;
  assert.equal(v.getUint32(eocd, true), 0x06054b50);
  assert.equal(v.getUint16(eocd + 10, true), 2);
  // CRC-32 of "hello"
  assert.equal(v.getUint32(14, true), 0x3610a686);
});

function indexOf(hay, needle) {
  outer: for (let i = 0; i <= hay.length - needle.length; i++) {
    for (let j = 0; j < needle.length; j++) if (hay[i + j] !== needle[j]) continue outer;
    return i;
  }
  return -1;
}

// --- helpers for the compass heading test (GPSImgDirection 0x0011, RATIONAL)
function gpsIfdLocation(jpeg) {
  const t0 = indexOf(jpeg, [0x45, 0x78, 0x69, 0x66, 0, 0]) + 6;
  const v = new DataView(jpeg.buffer, jpeg.byteOffset + t0);
  const le = jpeg[t0] === 0x49;
  const ifd0 = v.getUint32(4, le);
  const n = v.getUint16(ifd0, le);
  for (let i = 0; i < n; i++) {
    const p = ifd0 + 2 + i * 12;
    if (v.getUint16(p, le) === 0x8825) return { v, le, gps: v.getUint32(p + 8, le), t0 };
  }
  return null;
}
function addImgDirection(jpeg, deg) {
  // Turn the GPSAltitude entry (also a single RATIONAL) into GPSImgDirection in place.
  const out = jpeg.slice();
  const { v, le, gps } = gpsIfdLocation(out);
  const n = v.getUint16(gps, le);
  for (let i = 0; i < n; i++) {
    const p = gps + 2 + i * 12;
    if (v.getUint16(p, le) === 0x0006) {
      v.setUint16(p, 0x0011, le);
      const at = v.getUint32(p + 8, le);
      v.setUint32(at, deg, le);
      v.setUint32(at + 4, 1, le);
    }
  }
  return out;
}
function readImgDirection(jpeg) {
  const { v, le, gps } = gpsIfdLocation(jpeg);
  const n = v.getUint16(gps, le);
  for (let i = 0; i < n; i++) {
    const p = gps + 2 + i * 12;
    if (v.getUint16(p, le) === 0x0011) {
      const at = v.getUint32(p + 8, le);
      return v.getUint32(at, le) / v.getUint32(at + 4, le);
    }
  }
  return null;
}
