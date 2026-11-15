/**
 * Minimal, dependency-free decoder for the Garmin/ANT FIT binary format.
 *
 * It turns a FIT file into a flat list of messages:
 *   { num: <global message number>, fields: { <field number>: value } }
 *
 * It deliberately knows nothing about message *meaning* (that lives in the
 * format adapters, e.g. ../garmin-fit.js). It handles:
 *   - 12 and 14 byte file headers
 *   - little and big endian definition messages
 *   - compressed timestamp headers
 *   - developer data fields (skipped)
 *   - array fields
 *   - several FIT files chained together in one file
 *
 * Reference: FIT SDK "Flexible and Interoperable Data Transfer Protocol".
 */

/** Seconds between the Unix epoch and the FIT epoch (1989-12-31T00:00:00Z). */
export const FIT_EPOCH_OFFSET = 631065600;

/** Field number used by every FIT message for its timestamp. */
export const TIMESTAMP_FIELD = 253;

// base type number (low 5 bits) -> [size in bytes, reader, invalid value]
const BASE_TYPES = {
  0x00: [1, 'getUint8', 0xff], // enum
  0x01: [1, 'getInt8', 0x7f], // sint8
  0x02: [1, 'getUint8', 0xff], // uint8
  0x03: [2, 'getInt16', 0x7fff], // sint16
  0x04: [2, 'getUint16', 0xffff], // uint16
  0x05: [4, 'getInt32', 0x7fffffff], // sint32
  0x06: [4, 'getUint32', 0xffffffff], // uint32
  0x07: [1, null, null], // string (null terminated UTF-8)
  0x08: [4, 'getFloat32', null], // float32 (invalid = all 0xFF bits)
  0x09: [8, 'getFloat64', null], // float64
  0x0a: [1, 'getUint8', 0x00], // uint8z
  0x0b: [2, 'getUint16', 0x0000], // uint16z
  0x0c: [4, 'getUint32', 0x00000000], // uint32z
  0x0d: [1, 'getUint8', 0xff], // byte
  0x0e: [8, 'getBigInt64', 0x7fffffffffffffffn], // sint64
  0x0f: [8, 'getBigUint64', 0xffffffffffffffffn], // uint64
  0x10: [8, 'getBigUint64', 0n], // uint64z
};

export class FitError extends Error {}

/** True if the bytes look like a FIT file. */
export function isFit(bytes) {
  const b = toUint8(bytes);
  if (b.length < 12) return false;
  const headerSize = b[0];
  if (headerSize !== 12 && headerSize !== 14) return false;
  return b[8] === 0x2e && b[9] === 0x46 && b[10] === 0x49 && b[11] === 0x54; // ".FIT"
}

/**
 * Decode a FIT file.
 * @param {ArrayBuffer|Uint8Array} input
 * @returns {{ messages: Array<{num:number, fields:Object<number, any>}> }}
 */
export function decodeFit(input) {
  const bytes = toUint8(input);
  const view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
  const messages = [];
  let pos = 0;

  // A file may contain several FIT "files" back to back.
  while (pos + 12 <= bytes.length) {
    if (!isFit(bytes.subarray(pos))) {
      if (messages.length) break; // trailing garbage after a valid file
      throw new FitError('Not a FIT file (missing ".FIT" signature)');
    }
    const headerSize = bytes[pos];
    const dataSize = view.getUint32(pos + 4, true);
    const start = pos + headerSize;
    const end = start + dataSize;
    if (end > bytes.length) throw new FitError('FIT file is truncated');
    decodeRecords(view, start, end, messages);
    pos = end + 2; // skip file CRC
  }
  return { messages };
}

function decodeRecords(view, pos, end, out) {
  const definitions = new Map(); // local message type -> definition
  let lastTimestamp = 0;

  while (pos < end) {
    const header = view.getUint8(pos++);

    if (header & 0x80) {
      // Compressed timestamp header: data message with implied timestamp.
      const local = (header >> 5) & 0x03;
      const offset = header & 0x1f;
      let ts = (lastTimestamp & ~0x1f) + offset;
      if (offset < (lastTimestamp & 0x1f)) ts += 0x20;
      lastTimestamp = ts >>> 0;
      const def = definitions.get(local);
      if (!def) throw new FitError(`Data for undefined local message ${local}`);
      const msg = readData(view, pos, def);
      pos += def.size;
      if (msg.fields[TIMESTAMP_FIELD] === undefined) msg.fields[TIMESTAMP_FIELD] = lastTimestamp;
      out.push(msg);
      continue;
    }

    const local = header & 0x0f;

    if (header & 0x40) {
      // Definition message
      const hasDev = (header & 0x20) !== 0;
      pos++; // reserved
      const little = view.getUint8(pos++) === 0;
      const num = view.getUint16(pos, little);
      pos += 2;
      const count = view.getUint8(pos++);
      const fields = [];
      let size = 0;
      for (let i = 0; i < count; i++) {
        const f = {
          num: view.getUint8(pos),
          size: view.getUint8(pos + 1),
          type: view.getUint8(pos + 2) & 0x1f,
        };
        pos += 3;
        fields.push(f);
        size += f.size;
      }
      if (hasDev) {
        const devCount = view.getUint8(pos++);
        for (let i = 0; i < devCount; i++) {
          size += view.getUint8(pos + 1);
          pos += 3;
        }
      }
      definitions.set(local, { num, little, fields, size });
      continue;
    }

    // Normal data message
    const def = definitions.get(local);
    if (!def) throw new FitError(`Data for undefined local message ${local}`);
    const msg = readData(view, pos, def);
    pos += def.size;
    const ts = msg.fields[TIMESTAMP_FIELD];
    if (typeof ts === 'number') lastTimestamp = ts;
    out.push(msg);
  }
}

function readData(view, pos, def) {
  const fields = {};
  for (const f of def.fields) {
    const value = readField(view, pos, f, def.little);
    if (value !== undefined) fields[f.num] = value;
    pos += f.size;
  }
  return { num: def.num, fields };
}

function readField(view, pos, f, little) {
  const bt = BASE_TYPES[f.type];
  if (!bt) return undefined;
  const [size, reader, invalid] = bt;

  if (f.type === 0x07) {
    const raw = new Uint8Array(view.buffer, view.byteOffset + pos, f.size);
    const nul = raw.indexOf(0);
    const s = new TextDecoder().decode(nul >= 0 ? raw.subarray(0, nul) : raw);
    return s.length ? s : undefined;
  }

  if (f.size % size !== 0) return undefined; // malformed field; skip it

  const n = f.size / size;
  const values = [];
  for (let i = 0; i < n; i++) {
    let v = view[reader](pos + i * size, little);
    if (isInvalid(v, f.type, invalid)) v = undefined;
    values.push(v);
  }
  if (n === 1) return values[0];
  return values.every((v) => v === undefined) ? undefined : values;
}

function isInvalid(v, type, invalid) {
  if (type === 0x08 || type === 0x09) return Number.isNaN(v);
  return v === invalid;
}

function toUint8(input) {
  if (input instanceof Uint8Array) return input;
  if (input instanceof ArrayBuffer) return new Uint8Array(input);
  if (ArrayBuffer.isView(input)) return new Uint8Array(input.buffer, input.byteOffset, input.byteLength);
  throw new TypeError('Expected ArrayBuffer or Uint8Array');
}
