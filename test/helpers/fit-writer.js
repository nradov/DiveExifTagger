// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Tiny FIT encoder used only to build test fixtures.
 * Supports little-endian definitions and the base types used by the tests.
 */
/** Seconds between the Unix epoch and the FIT epoch (1989-12-31T00:00:00Z). */
const FIT_EPOCH_OFFSET = 631065600;

const TYPES = {
  enum: [0x00, 1, 'setUint8'],
  sint8: [0x01, 1, 'setInt8'],
  uint8: [0x02, 1, 'setUint8'],
  uint16: [0x84, 2, 'setUint16'],
  sint32: [0x85, 4, 'setInt32'],
  uint32: [0x86, 4, 'setUint32'],
  float32: [0x88, 4, 'setFloat32'],
  // strings: [num, 'string', value, sizeInBytes]
  string: [0x07, 0, null],
};

const size = ([, t, , n]) => (t === 'string' ? n : TYPES[t][1]);
const key = (num, fields, dev = []) =>
  num + ':' + fields.map((f) => `${f[0]}${f[1]}${size(f)}`).join(',') + '|' + dev.map((f) => `${f[0]}${f[1]}`).join(',');

function writeValues(v, buf, p, fields) {
  for (const f of fields) {
    const [, t, value] = f;
    if (t === 'string') {
      buf.set(new TextEncoder().encode(value).subarray(0, size(f) - 1), p); // NUL-padded
    } else {
      v[TYPES[t][2]](p, value, true);
    }
    p += size(f);
  }
  return p;
}

export const toFitTime = (ms) => Math.round(ms / 1000) - FIT_EPOCH_OFFSET;
export const toSemicircles = (deg) => Math.round((deg * 2 ** 31) / 180);

export class FitWriter {
  constructor() {
    this.parts = [];
    this.defs = new Map(); // key -> local type
  }

  /**
   * @param {number} num global message number
   * @param {Array<[number, keyof TYPES, number|string, number?]>} fields [fieldNum, type, value, (string size)]
   * @param {Array<[number, keyof TYPES, number]>} [devFields] developer fields [fieldNum, type, value],
   *   all for developer data index 0 (declare them first with developerField()).
   */
  message(num, fields, devFields = []) {
    const k = key(num, fields, devFields);
    let local = this.defs.get(k);
    if (local === undefined) {
      local = this.defs.size % 16;
      this.defs.set(k, local);
      const def = [0x40 | (devFields.length ? 0x20 : 0) | local, 0, 0, num & 0xff, num >> 8, fields.length];
      for (const f of fields) def.push(f[0], size(f), TYPES[f[1]][0]);
      if (devFields.length) {
        def.push(devFields.length);
        for (const f of devFields) def.push(f[0], size(f), 0);
      }
      this.parts.push(Uint8Array.from(def));
    }
    const all = [...fields, ...devFields];
    const buf = new Uint8Array(1 + all.reduce((s, f) => s + size(f), 0));
    buf[0] = local;
    writeValues(new DataView(buf.buffer), buf, 1, all);
    this.parts.push(buf);
    return this;
  }

  /** Declare a developer field (developer data index 0), as Suunto's app does. */
  developerField(fieldNum, type, name, units = '') {
    if (!this.devDeclared) {
      this.message(207, [
        [3, 'uint8', 0], // developer_data_index
        [2, 'uint16', 23], // manufacturer_id = suunto
      ]);
      this.devDeclared = true;
    }
    return this.message(206, [
      [0, 'uint8', 0], // developer_data_index
      [1, 'uint8', fieldNum], // field_definition_number
      [2, 'uint8', TYPES[type][0]], // fit_base_type_id
      [3, 'string', name, 16], // field_name
      [8, 'string', units, 8], // units
    ]);
  }

  /** Add a data message using a compressed timestamp header (local type must be 0-3). */
  compressed(num, fields, fitTimestamp) {
    this.message(num, fields); // ensure a definition exists (writes one full message)
    const local = this.defs.get(key(num, fields));
    const buf = new Uint8Array(1 + fields.reduce((s, f) => s + size(f), 0));
    buf[0] = 0x80 | (local << 5) | (fitTimestamp & 0x1f);
    writeValues(new DataView(buf.buffer), buf, 1, fields);
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

/**
 * Build a FIT file shaped like a Suunto app export (e.g. Suunto Ocean):
 * manufacturer = suunto with a product_name string, Suunto developer fields,
 * NO positions in session/lap messages, and GPS fixes only in record
 * messages while the diver is at the surface before and after the dive.
 */
export function buildSuuntoFit({
  start = Date.UTC(2025, 3, 19, 3, 10, 0),
  minutes = 50,
  surfaceSeconds = 60, // GPS-tracked surface time before and after the dive
  entry = { lat: -8.23576, lon: 115.56247 },
  exit = { lat: -8.23581, lon: 115.56212 },
  utcOffsetMinutes = 480,
  maxDepth = 21.7,
} = {}) {
  const w = new FitWriter();
  const t0 = toFitTime(start);
  const dur = minutes * 60;
  w.message(0, [
    [0, 'enum', 4], // type = activity
    [1, 'uint16', 23], // manufacturer = suunto
    [2, 'uint16', 62],
    [4, 'uint32', t0],
    [8, 'string', 'Suunto Ocean', 16], // product_name
  ]);
  w.developerField(0, 'float32', 'max_depth', 'm');
  w.developerField(1, 'uint16', 'surface_time', 's');
  for (let s = -surfaceSeconds; s <= dur + surfaceSeconds; s += 5) {
    const underwater = s >= 0 && s <= dur;
    const f = Math.min(Math.max(s / dur, 0), 1);
    const depth = !underwater ? 0 : f < 0.1 ? (f / 0.1) * maxDepth : f > 0.85 ? ((1 - f) / 0.15) * maxDepth : maxDepth;
    const fields = [
      [253, 'uint32', t0 + s],
      [92, 'uint32', Math.round(depth * 1000)],
      [13, 'sint8', 29],
    ];
    if (!underwater) {
      const p = s < 0 ? entry : exit;
      fields.push([0, 'sint32', toSemicircles(p.lat)], [1, 'sint32', toSemicircles(p.lon)]);
    }
    w.message(20, fields);
  }
  w.message(19, [
    [253, 'uint32', t0 + dur + surfaceSeconds],
    [2, 'uint32', t0 - surfaceSeconds],
  ]);
  w.message(
    18,
    [
      [253, 'uint32', t0 + dur + surfaceSeconds],
      [2, 'uint32', t0 - surfaceSeconds],
      [5, 'enum', 53], // sport = diving
      [7, 'uint32', (dur + 2 * surfaceSeconds) * 1000],
    ],
    [
      [0, 'float32', maxDepth],
      [1, 'uint16', 3600],
    ],
  );
  w.message(34, [
    [253, 'uint32', t0 + dur + surfaceSeconds],
    [5, 'uint32', t0 + dur + surfaceSeconds + utcOffsetMinutes * 60],
  ]);
  return { bytes: w.build(), start, end: start + dur * 1000, entry, exit, surfaceSeconds };
}
