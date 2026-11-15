// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Regression tests against real files: a Garmin Connect export of a
 * Garmin Descent Mk2 dive, and five photos from an OM System TG-7 taken
 * during that dive. See test/fixtures/README.md.
 */
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

import { parseDiveLogFile } from '../src/formats/index.js';
import { readExif, writeExif } from '../src/exif/jpeg-exif.js';
import { photoTime, matchPhoto } from '../src/match.js';

const fixture = (name) => new Uint8Array(readFileSync(new URL(`./fixtures/${name}`, import.meta.url)));
const near = (a, b, eps) => assert.ok(Math.abs(a - b) <= eps, `${a} ≉ ${b}`);

// photo -> [UTC time, depth (m)] at that moment in the dive log
const PHOTOS = {
  'PA310402.JPG': ['2026-10-31T18:41:30Z', 42.66],
  'PA310420.JPG': ['2026-10-31T18:45:53Z', 41.06],
  'PA310437.JPG': ['2026-10-31T18:47:35Z', 39.05],
  'PA310461.JPG': ['2026-10-31T18:54:01Z', 38.96],
  'PA310471.JPG': ['2026-10-31T18:56:38Z', 37.1],
};

const dives = await parseDiveLogFile('24921353436.zip', fixture('24921353436.zip'));

test('real Garmin Connect ZIP: Descent Mk2 dive', () => {
  assert.equal(dives.length, 1);
  const [d] = dives;
  assert.equal(d.device, 'Garmin Descent Mk2');
  assert.equal(new Date(d.startTime).toISOString(), '2026-10-31T18:28:58.000Z');
  near((d.endTime - d.startTime) / 60000, 74.2, 0.1);
  near(d.maxDepth, 43.593, 1e-6);
  assert.equal(d.utcOffsetMinutes, -420);
  assert.equal(d.minTemperature, 20);
  near(d.entry.lat, 36.42919, 1e-5);
  near(d.entry.lon, -121.93241, 1e-5);
  near(d.exit.lat, 36.43764, 1e-5);
  assert.equal(d.samples.length, 4053);
});

for (const [name, [utc, depth]] of Object.entries(PHOTOS)) {
  test(`real TG-7 photo ${name}: matched and tagged without losing camera metadata`, () => {
    const jpeg = fixture(name);
    const before = readExif(jpeg);
    assert.equal(before.model.trim(), 'TG-7');
    assert.equal(before.offsetTimeOriginal, '-07:00');

    const pt = photoTime(before, dives, { cameraUtcOffsetMinutes: null, clockCorrectionSeconds: 0, toleranceMinutes: 30 });
    assert.equal(pt.source, 'photo');
    assert.equal(new Date(pt.time).toISOString(), utc.replace('Z', '.000Z'));

    const m = matchPhoto(pt.time, dives, { toleranceMinutes: 30, locationMode: 'entry', manualPosition: null });
    assert.equal(m.phase, 'underwater');
    near(m.depth, depth, 0.01);
    assert.equal(m.temperature, 20);

    const out = writeExif(jpeg, {
      lat: m.lat,
      lon: m.lon,
      altitude: -m.depth,
      time: pt.time,
      waterDepth: m.depth,
      temperature: m.temperature,
    });
    const after = readExif(out);
    near(after.gps.lat, m.lat, 1e-7);
    near(after.gps.lon, m.lon, 1e-7);
    near(after.gps.altitude, -m.depth, 0.006);
    near(after.waterDepth, m.depth, 0.006);
    assert.equal(after.temperature, 20);
    assert.equal(after.dateTimeOriginal, before.dateTimeOriginal);
    assert.equal(after.offsetTimeOriginal, '-07:00');

    // The camera's compass heading (GPSImgDirection) and maker note survive.
    const heading = (b) => tagBytes(b, 0x0011);
    const makerNote = (b) => tagBytes(b, 0x927c);
    assert.ok(heading(jpeg));
    assert.deepEqual(heading(out), heading(jpeg));
    assert.deepEqual(makerNote(out), makerNote(jpeg));

    // Everything after the EXIF segment (image data) is byte-identical.
    assert.deepEqual(afterApp1(out), afterApp1(jpeg));
  });
}

/** Raw value bytes of the first IFD entry with this tag (little-endian TIFF, as the TG-7 writes). */
function tagBytes(jpeg, tag) {
  const t0 = findExif(jpeg);
  const v = new DataView(jpeg.buffer, jpeg.byteOffset + t0);
  const sizes = { 1: 1, 2: 1, 3: 2, 4: 4, 5: 8, 7: 1, 9: 4, 10: 8 };
  const walk = (off, seen = new Set()) => {
    if (!off || seen.has(off)) return null;
    seen.add(off);
    const n = v.getUint16(off, true);
    const subs = [];
    for (let i = 0; i < n; i++) {
      const p = off + 2 + i * 12;
      const tg = v.getUint16(p, true);
      const size = sizes[v.getUint16(p + 2, true)] * v.getUint32(p + 4, true);
      const at = size <= 4 ? p + 8 : v.getUint32(p + 8, true);
      if (tg === tag) return jpeg.slice(t0 + at, t0 + at + size);
      if (tg === 0x8769 || tg === 0x8825) subs.push(v.getUint32(p + 8, true));
    }
    for (const s of subs) {
      const r = walk(s, seen);
      if (r) return r;
    }
    return null;
  };
  return walk(v.getUint32(4, true));
}

function findExif(b) {
  for (let i = 2; i < b.length - 10; i++) {
    if (b[i] === 0x45 && b[i + 1] === 0x78 && b[i + 2] === 0x69 && b[i + 3] === 0x66 && b[i + 4] === 0 && b[i + 5] === 0) return i + 6;
  }
  throw new Error('no EXIF');
}

function afterApp1(b) {
  let p = 2;
  while (!(b[p] === 0xff && b[p + 1] === 0xe1)) p += 2 + ((b[p + 2] << 8) | b[p + 3]);
  return b.subarray(p + 2 + ((b[p + 2] << 8) | b[p + 3]));
}
