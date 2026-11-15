/**
 * Tiny FIT encoder used only to build test fixtures.
 * Supports little-endian definitions and the base types used by the tests.
 */
import { FIT_EPOCH_OFFSET } from '../../src/formats/fit/decoder.js';

const TYPES = {
  enum: [0x00, 1, 'setUint8'],
  sint8: [0x01, 1, 'setInt8'],
  uint8: [0x02, 1, 'setUint8'],
  uint16: [0x84, 2, 'setUint16'],
  sint32: [0x85, 4, 'setInt32'],
  uint32: [0x86, 4, 'setUint32'],
};

export const toFitTime = (ms) => Math.round(ms / 1000) - FIT_EPOCH_OFFSET;
export const toSemicircles = (deg) => Math.round((deg * 2 ** 31) / 180);

export class FitWriter {
  constructor() {
    this.parts = [];
    this.defs = new Map(); // key -> local type
  }

  /**
   * @param {number} num global message number
   * @param {Array<[number, keyof TYPES, number]>} fields [fieldNum, type, value]
   */
  message(num, fields) {
    const key = num + ':' + fields.map(([n, t]) => `${n}${t}`).join(',');
    let local = this.defs.get(key);
    if (local === undefined) {
      local = this.defs.size % 16;
      this.defs.set(key, local);
      const def = [0x40 | local, 0, 0, num & 0xff, num >> 8, fields.length];
      for (const [n, t] of fields) def.push(n, TYPES[t][1], TYPES[t][0]);
      this.parts.push(Uint8Array.from(def));
    }
    const size = fields.reduce((s, [, t]) => s + TYPES[t][1], 0);
    const buf = new Uint8Array(1 + size);
    const v = new DataView(buf.buffer);
    buf[0] = local;
    let p = 1;
    for (const [, t, value] of fields) {
      v[TYPES[t][2]](p, value, true);
      p += TYPES[t][1];
    }
    this.parts.push(buf);
    return this;
  }

  /** Add a data message using a compressed timestamp header (local type must be 0-3). */
  compressed(num, fields, fitTimestamp) {
    this.message(num, fields); // ensure a definition exists (writes one full message)
    const key = num + ':' + fields.map(([n, t]) => `${n}${t}`).join(',');
    const local = this.defs.get(key);
    const size = fields.reduce((s, [, t]) => s + TYPES[t][1], 0);
    const buf = new Uint8Array(1 + size);
    const v = new DataView(buf.buffer);
    buf[0] = 0x80 | (local << 5) | (fitTimestamp & 0x1f);
    let p = 1;
    for (const [, t, value] of fields) {
      v[TYPES[t][2]](p, value, true);
      p += TYPES[t][1];
    }
    this.parts.push(buf);
    return this;
  }

  build() {
    const dataSize = this.parts.reduce((s, p) => s + p.length, 0);
    const out = new Uint8Array(14 + dataSize + 2);
    const v = new DataView(out.buffer);
    out[0] = 14;
    out[1] = 0x20;
    v.setUint16(2, 2132, true);
    v.setUint32(4, dataSize, true);
    out.set([0x2e, 0x46, 0x49, 0x54], 8);
    v.setUint16(12, crc(out.subarray(0, 12)), true);
    let p = 14;
    for (const part of this.parts) {
      out.set(part, p);
      p += part.length;
    }
    v.setUint16(p, crc(out.subarray(0, p)), true);
    return out;
  }
}

const CRC_TABLE = [
  0x0000, 0xcc01, 0xd801, 0x1400, 0xf001, 0x3c00, 0x2800, 0xe401, 0xa001, 0x6c00, 0x7800, 0xb401, 0x5000, 0x9c01, 0x8801,
  0x4400,
];
function crc(bytes) {
  let c = 0;
  for (const b of bytes) {
    let t = CRC_TABLE[c & 0xf];
    c = (c >> 4) & 0x0fff;
    c = c ^ t ^ CRC_TABLE[b & 0xf];
    t = CRC_TABLE[c & 0xf];
    c = (c >> 4) & 0x0fff;
    c = c ^ t ^ CRC_TABLE[(b >> 4) & 0xf];
  }
  return c;
}

/**
 * Build a realistic single-dive FIT file.
 * @returns {{bytes: Uint8Array, start: number, end: number, entry: {lat,lon}, exit: {lat,lon}}}
 */
export function buildDiveFit({
  start = Date.UTC(2026, 10, 14, 1, 0, 0), // 2026-11-14 01:00Z
  minutes = 40,
  entry = { lat: -16.5, lon: 145.75 },
  exit = { lat: -16.501, lon: 145.752 },
  utcOffsetMinutes = 600,
  maxDepth = 18,
} = {}) {
  const w = new FitWriter();
  const t0 = toFitTime(start);
  const dur = minutes * 60;
  w.message(0, [
    [0, 'enum', 4], // type = activity
    [1, 'uint16', 1], // manufacturer = garmin
    [2, 'uint16', 4223],
    [4, 'uint32', t0],
  ]);
  // depth profile: descend, flat, ascend; one sample every 10 s
  for (let s = 0; s <= dur; s += 10) {
    const f = s / dur;
    const depth = f < 0.1 ? (f / 0.1) * maxDepth : f > 0.85 ? ((1 - f) / 0.15) * maxDepth : maxDepth;
    const temp = Math.round(27 - (depth / maxDepth) * 3);
    w.message(20, [
      [253, 'uint32', t0 + s],
      [92, 'uint32', Math.round(depth * 1000)],
      [13, 'sint8', temp],
    ]);
  }
  w.message(19, [
    [253, 'uint32', t0 + dur],
    [2, 'uint32', t0],
    [3, 'sint32', toSemicircles(entry.lat)],
    [4, 'sint32', toSemicircles(entry.lon)],
    [5, 'sint32', toSemicircles(exit.lat)],
    [6, 'sint32', toSemicircles(exit.lon)],
  ]);
  w.message(18, [
    [253, 'uint32', t0 + dur],
    [2, 'uint32', t0],
    [3, 'sint32', toSemicircles(entry.lat)],
    [4, 'sint32', toSemicircles(entry.lon)],
    [5, 'enum', 53], // sport = diving
    [7, 'uint32', dur * 1000],
  ]);
  w.message(34, [
    [253, 'uint32', t0 + dur],
    [5, 'uint32', t0 + dur + utcOffsetMinutes * 60],
  ]);
  return { bytes: w.build(), start, end: start + dur * 1000, entry, exit };
}
