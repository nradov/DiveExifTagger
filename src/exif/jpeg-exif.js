// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Dependency-free JPEG EXIF reader/writer, scoped to what this project needs.
 *
 * Writing strategy ("append and re-point"): the original TIFF block inside
 * the APP1 segment is kept byte-for-byte. New/updated IFDs (IFD0, Exif IFD,
 * GPS IFD) are appended to the end of the block and the pointers to them are
 * updated. Because nothing is moved, every existing absolute offset stays
 * valid, so maker notes, thumbnails and unknown tags survive untouched.
 * The image data (everything after the APP1 segment) is never modified.
 */

const TAG = {
  // IFD0
  DATETIME: 0x0132,
  MAKE: 0x010f,
  MODEL: 0x0110,
  EXIF_IFD: 0x8769,
  GPS_IFD: 0x8825,
  // Exif IFD
  DATETIME_ORIGINAL: 0x9003,
  OFFSET_TIME_ORIGINAL: 0x9011,
  SUBSEC_TIME_ORIGINAL: 0x9291,
  AMBIENT_TEMPERATURE: 0x9400, // Exif 2.31 "Temperature", SRATIONAL °C
  WATER_DEPTH: 0x9403, // Exif 2.31, SRATIONAL metres
  // GPS IFD
  GPS_VERSION: 0x0000,
  GPS_LAT_REF: 0x0001,
  GPS_LAT: 0x0002,
  GPS_LON_REF: 0x0003,
  GPS_LON: 0x0004,
  GPS_ALT_REF: 0x0005,
  GPS_ALT: 0x0006,
  GPS_TIMESTAMP: 0x0007,
  GPS_MAP_DATUM: 0x0012,
  GPS_PROCESSING_METHOD: 0x001b,
  GPS_DATESTAMP: 0x001d,
};

const TYPE = { BYTE: 1, ASCII: 2, SHORT: 3, LONG: 4, RATIONAL: 5, UNDEFINED: 7, SLONG: 9, SRATIONAL: 10, IFD: 13 };
const TYPE_SIZE = { 1: 1, 2: 1, 3: 2, 4: 4, 5: 8, 6: 1, 7: 1, 8: 2, 9: 4, 10: 8, 11: 4, 12: 8, 13: 4 };

const EXIF_HEADER = [0x45, 0x78, 0x69, 0x66, 0x00, 0x00]; // "Exif\0\0"
const MAX_SEGMENT = 0xffff;

export class ExifError extends Error {}

// ---------------------------------------------------------------- JPEG level

/** Split a JPEG into its header segments (up to SOS) and the remainder. */
function scanJpeg(bytes) {
  if (bytes[0] !== 0xff || bytes[1] !== 0xd8) throw new ExifError('Not a JPEG file');
  const segments = [];
  let pos = 2;
  while (pos + 4 <= bytes.length) {
    if (bytes[pos] !== 0xff) throw new ExifError('Corrupt JPEG segment structure');
    const marker = bytes[pos + 1];
    if (marker === 0xff) {
      pos++; // fill byte
      continue;
    }
    if (marker === 0xda || marker === 0xd9) break; // SOS / EOI: image data follows
    const len = (bytes[pos + 2] << 8) | bytes[pos + 3];
    segments.push({ marker, start: pos, end: pos + 2 + len });
    pos += 2 + len;
  }
  return { segments, dataStart: pos };
}

function isExifSegment(bytes, seg) {
  if (seg.marker !== 0xe1) return false;
  return EXIF_HEADER.every((b, i) => bytes[seg.start + 4 + i] === b);
}

function findExif(bytes) {
  const { segments, dataStart } = scanJpeg(bytes);
  const seg = segments.find((s) => isExifSegment(bytes, s)) || null;
  const tiff = seg ? bytes.subarray(seg.start + 10, seg.end) : null;
  return { segments, dataStart, seg, tiff };
}

// ---------------------------------------------------------------- TIFF level

function tiffInfo(tiff) {
  const little = tiff[0] === 0x49 && tiff[1] === 0x49;
  if (!little && !(tiff[0] === 0x4d && tiff[1] === 0x4d)) throw new ExifError('Bad TIFF byte order');
  const view = new DataView(tiff.buffer, tiff.byteOffset, tiff.byteLength);
  if (view.getUint16(2, little) !== 42) throw new ExifError('Bad TIFF magic');
  return { view, little, ifd0: view.getUint32(4, little) };
}

function readIfd(tiff, view, offset, little) {
  if (!offset || offset + 2 > tiff.length) return { entries: [], next: 0 };
  const n = view.getUint16(offset, little);
  const entries = [];
  for (let i = 0; i < n; i++) {
    const p = offset + 2 + i * 12;
    if (p + 12 > tiff.length) break;
    entries.push({
      tag: view.getUint16(p, little),
      type: view.getUint16(p + 2, little),
      count: view.getUint32(p + 4, little),
      pos: p,
      raw: tiff.slice(p, p + 12),
    });
  }
  const np = offset + 2 + n * 12;
  const next = np + 4 <= tiff.length ? view.getUint32(np, little) : 0;
  return { entries, next };
}

function entryValue(tiff, view, e, little) {
  const size = (TYPE_SIZE[e.type] || 1) * e.count;
  const at = size <= 4 ? e.pos + 8 : view.getUint32(e.pos + 8, little);
  if (at + size > tiff.length) return undefined;
  switch (e.type) {
    case TYPE.ASCII: {
      const s = new TextDecoder().decode(tiff.subarray(at, at + e.count));
      return s.replace(/\0[\s\S]*$/, '').trim();
    }
    case TYPE.BYTE:
    case TYPE.UNDEFINED:
      return Array.from(tiff.subarray(at, at + e.count));
    case TYPE.SHORT:
      return arr(e.count, (i) => view.getUint16(at + i * 2, little));
    case TYPE.LONG:
    case TYPE.IFD:
      return arr(e.count, (i) => view.getUint32(at + i * 4, little));
    case TYPE.SLONG:
      return arr(e.count, (i) => view.getInt32(at + i * 4, little));
    case TYPE.RATIONAL:
      return arr(e.count, (i) => view.getUint32(at + i * 8, little) / view.getUint32(at + i * 8 + 4, little));
    case TYPE.SRATIONAL:
      return arr(e.count, (i) => view.getInt32(at + i * 8, little) / view.getInt32(at + i * 8 + 4, little));
    default:
      return undefined;
  }
}

function arr(n, f) {
  const out = [];
  for (let i = 0; i < n; i++) out.push(f(i));
  return n === 1 ? out[0] : out;
}

// ---------------------------------------------------------------- reading

/**
 * Read the metadata this app cares about.
 * @param {ArrayBuffer|Uint8Array} input
 * @returns {{
 *   dateTimeOriginal: string|null, offsetTimeOriginal: string|null, subSecTimeOriginal: string|null,
 *   make: string|null, model: string|null,
 *   gps: {lat:number, lon:number, altitude:number|null}|null,
 *   waterDepth: number|null, temperature: number|null
 * }}
 */
export function readExif(input) {
  const bytes = toUint8(input);
  const out = {
    dateTimeOriginal: null,
    offsetTimeOriginal: null,
    subSecTimeOriginal: null,
    make: null,
    model: null,
    gps: null,
    waterDepth: null,
    temperature: null,
  };
  const { tiff } = findExif(bytes);
  if (!tiff) return out;
  const { view, little, ifd0 } = tiffInfo(tiff);
  const get = (ifd, tag) => {
    const e = ifd.entries.find((x) => x.tag === tag);
    return e ? entryValue(tiff, view, e, little) : undefined;
  };

  const i0 = readIfd(tiff, view, ifd0, little);
  out.make = get(i0, TAG.MAKE) || null;
  out.model = get(i0, TAG.MODEL) || null;
  const dateTime = get(i0, TAG.DATETIME) || null;

  const exifPtr = get(i0, TAG.EXIF_IFD);
  if (exifPtr) {
    const ie = readIfd(tiff, view, exifPtr, little);
    out.dateTimeOriginal = get(ie, TAG.DATETIME_ORIGINAL) || null;
    out.offsetTimeOriginal = get(ie, TAG.OFFSET_TIME_ORIGINAL) || null;
    out.subSecTimeOriginal = get(ie, TAG.SUBSEC_TIME_ORIGINAL) || null;
    out.waterDepth = get(ie, TAG.WATER_DEPTH) ?? null;
    out.temperature = get(ie, TAG.AMBIENT_TEMPERATURE) ?? null;
  }
  if (!out.dateTimeOriginal) out.dateTimeOriginal = dateTime;

  const gpsPtr = get(i0, TAG.GPS_IFD);
  if (gpsPtr) {
    const ig = readIfd(tiff, view, gpsPtr, little);
    const lat = get(ig, TAG.GPS_LAT);
    const lon = get(ig, TAG.GPS_LON);
    if (Array.isArray(lat) && Array.isArray(lon)) {
      const latRef = get(ig, TAG.GPS_LAT_REF);
      const lonRef = get(ig, TAG.GPS_LON_REF);
      const alt = get(ig, TAG.GPS_ALT);
      const altRef = get(ig, TAG.GPS_ALT_REF);
      out.gps = {
        lat: dmsToDeg(lat) * (latRef === 'S' ? -1 : 1),
        lon: dmsToDeg(lon) * (lonRef === 'W' ? -1 : 1),
        altitude: typeof alt === 'number' ? alt * (altRef?.[0] === 1 || altRef === 1 ? -1 : 1) : null,
      };
    }
  }
  return out;
}

// ---------------------------------------------------------------- writing

/**
 * Return a copy of the JPEG with location / depth / temperature written.
 *
 * @param {ArrayBuffer|Uint8Array} input
 * @param {Object} tags
 * @param {number} [tags.lat]            decimal degrees
 * @param {number} [tags.lon]            decimal degrees
 * @param {number} [tags.altitude]       metres; negative = below sea level
 * @param {number} [tags.time]           UTC ms, written as GPSDateStamp/GPSTimeStamp
 * @param {number} [tags.waterDepth]     metres, Exif 2.31 WaterDepth
 * @param {number} [tags.temperature]    °C, Exif 2.31 Temperature
 * @param {string} [tags.processingMethod] free text, e.g. "Garmin Descent dive log"
 * @returns {Uint8Array}
 */
export function writeExif(input, tags) {
  const bytes = toUint8(input);
  const { segments, seg, tiff: oldTiff } = findExif(bytes);

  // A fresh, empty big-endian TIFF header if the image has no EXIF yet.
  const base = oldTiff ? oldTiff.slice() : new Uint8Array([0x4d, 0x4d, 0, 42, 0, 0, 0, 0]);
  const { view, little, ifd0 } = tiffInfo(base);
  const w = new TiffAppender(base, little);

  const i0 = readIfd(base, view, ifd0, little);
  const ifd0Entries = new Map(i0.entries.map((e) => [e.tag, e]));

  // --- GPS IFD (existing entries such as a camera's compass heading are kept;
  // the tags we write replace any older values)
  const hasPosition = isNum(tags.lat) && isNum(tags.lon);
  if (hasPosition || isNum(tags.altitude)) {
    const g = [];
    g.push(newEntry(TAG.GPS_VERSION, TYPE.BYTE, [2, 3, 0, 0]));
    if (hasPosition) {
      g.push(newEntry(TAG.GPS_LAT_REF, TYPE.ASCII, tags.lat < 0 ? 'S' : 'N'));
      g.push(newEntry(TAG.GPS_LAT, TYPE.RATIONAL, degToDmsRationals(Math.abs(tags.lat))));
      g.push(newEntry(TAG.GPS_LON_REF, TYPE.ASCII, tags.lon < 0 ? 'W' : 'E'));
      g.push(newEntry(TAG.GPS_LON, TYPE.RATIONAL, degToDmsRationals(Math.abs(tags.lon))));
    }
    if (isNum(tags.altitude)) {
      g.push(newEntry(TAG.GPS_ALT_REF, TYPE.BYTE, [tags.altitude < 0 ? 1 : 0]));
      g.push(newEntry(TAG.GPS_ALT, TYPE.RATIONAL, [[Math.round(Math.abs(tags.altitude) * 100), 100]]));
    }
    if (isNum(tags.time)) {
      const d = new Date(tags.time);
      g.push(
        newEntry(TAG.GPS_TIMESTAMP, TYPE.RATIONAL, [
          [d.getUTCHours(), 1],
          [d.getUTCMinutes(), 1],
          [d.getUTCSeconds() * 1000 + d.getUTCMilliseconds(), 1000],
        ]),
      );
      g.push(newEntry(TAG.GPS_DATESTAMP, TYPE.ASCII, `${d.getUTCFullYear()}:${pad(d.getUTCMonth() + 1)}:${pad(d.getUTCDate())}`));
    }
    g.push(newEntry(TAG.GPS_MAP_DATUM, TYPE.ASCII, 'WGS-84'));
    if (tags.processingMethod) {
      const text = new TextEncoder().encode(tags.processingMethod);
      g.push(newEntry(TAG.GPS_PROCESSING_METHOD, TYPE.UNDEFINED, [...EXIF_ASCII_PREFIX, ...text]));
    }
    const oldPtrEntry = ifd0Entries.get(TAG.GPS_IFD);
    const oldPtr = oldPtrEntry ? entryValue(base, view, oldPtrEntry, little) : 0;
    const merged = new Map(readIfd(base, view, oldPtr, little).entries.map((e) => [e.tag, e]));
    for (const e of g) merged.set(e.tag, e);
    const gpsOffset = w.appendIfd([...merged.values()], 0);
    ifd0Entries.set(TAG.GPS_IFD, newEntry(TAG.GPS_IFD, TYPE.LONG, [gpsOffset]));
  }

  // --- Exif IFD (existing entries preserved, ours added/replaced)
  if (isNum(tags.waterDepth) || isNum(tags.temperature)) {
    const ptrEntry = ifd0Entries.get(TAG.EXIF_IFD);
    const exifPtr = ptrEntry ? entryValue(base, view, ptrEntry, little) : 0;
    const ie = readIfd(base, view, exifPtr, little);
    const exifEntries = new Map(ie.entries.map((e) => [e.tag, e]));
    if (isNum(tags.waterDepth)) {
      exifEntries.set(TAG.WATER_DEPTH, newEntry(TAG.WATER_DEPTH, TYPE.SRATIONAL, [[Math.round(tags.waterDepth * 100), 100]]));
    }
    if (isNum(tags.temperature)) {
      exifEntries.set(
        TAG.AMBIENT_TEMPERATURE,
        newEntry(TAG.AMBIENT_TEMPERATURE, TYPE.SRATIONAL, [[Math.round(tags.temperature * 10), 10]]),
      );
    }
    const exifOffset = w.appendIfd([...exifEntries.values()], ie.next);
    ifd0Entries.set(TAG.EXIF_IFD, newEntry(TAG.EXIF_IFD, TYPE.LONG, [exifOffset]));
  }

  // --- IFD0, re-pointed from the TIFF header
  const newIfd0 = w.appendIfd([...ifd0Entries.values()], i0.next);
  const tiff = w.finish();
  new DataView(tiff.buffer).setUint32(4, newIfd0, little);

  const segLen = 2 + EXIF_HEADER.length + tiff.length;
  if (segLen > MAX_SEGMENT) throw new ExifError('EXIF block would exceed the 64 KB JPEG segment limit');

  const app1 = new Uint8Array(2 + segLen);
  app1.set([0xff, 0xe1, segLen >> 8, segLen & 0xff, ...EXIF_HEADER]);
  app1.set(tiff, 10);

  // Splice: replace the old APP1, or insert after SOI (and after JFIF APP0 if present).
  let cutStart, cutEnd;
  if (seg) {
    cutStart = seg.start;
    cutEnd = seg.end;
  } else {
    const app0 = segments[0]?.marker === 0xe0 ? segments[0] : null;
    cutStart = cutEnd = app0 ? app0.end : 2;
  }
  const out = new Uint8Array(bytes.length - (cutEnd - cutStart) + app1.length);
  out.set(bytes.subarray(0, cutStart), 0);
  out.set(app1, cutStart);
  out.set(bytes.subarray(cutEnd), cutStart + app1.length);
  return out;
}

const EXIF_ASCII_PREFIX = [0x41, 0x53, 0x43, 0x49, 0x49, 0, 0, 0]; // "ASCII\0\0\0"

/** An IFD entry we create (as opposed to a raw one copied from the file). */
function newEntry(tag, type, value) {
  return { tag, type, value };
}

/** Accumulates data appended to the end of an existing TIFF block. */
class TiffAppender {
  constructor(base, little) {
    this.chunks = [base];
    this.length = base.length;
    this.little = little;
  }

  pad() {
    if (this.length % 2) this.push(new Uint8Array(1));
  }

  push(chunk) {
    this.chunks.push(chunk);
    this.length += chunk.length;
  }

  /** Append an IFD (+ its out-of-line values); returns its offset. */
  appendIfd(entries, next) {
    this.pad();
    const little = this.little;
    entries = [...entries].sort((a, b) => a.tag - b.tag);
    const offset = this.length;
    const headLen = 2 + entries.length * 12 + 4;
    const encoded = entries.map((e) => (e.raw ? null : encodeValue(e, little)));
    let dataLen = 0;
    for (const enc of encoded) if (enc && enc.data.length > 4) dataLen += enc.data.length + (enc.data.length % 2);

    const buf = new Uint8Array(headLen + dataLen);
    const v = new DataView(buf.buffer);
    v.setUint16(0, entries.length, little);
    let dataPos = headLen;
    entries.forEach((e, i) => {
      const p = 2 + i * 12;
      if (e.raw) {
        buf.set(e.raw, p); // copied verbatim; its offsets point into the untouched original
        return;
      }
      const { type, count, data } = encoded[i];
      v.setUint16(p, e.tag, little);
      v.setUint16(p + 2, type, little);
      v.setUint32(p + 4, count, little);
      if (data.length <= 4) {
        buf.set(data, p + 8);
      } else {
        v.setUint32(p + 8, offset + dataPos, little);
        buf.set(data, dataPos);
        dataPos += data.length + (data.length % 2);
      }
    });
    v.setUint32(2 + entries.length * 12, next || 0, little);
    this.push(buf);
    return offset;
  }

  finish() {
    const out = new Uint8Array(this.length);
    let p = 0;
    for (const c of this.chunks) {
      out.set(c, p);
      p += c.length;
    }
    return out;
  }
}

function encodeValue({ type, value }, little) {
  if (type === TYPE.ASCII) {
    const data = new TextEncoder().encode(value + '\0');
    return { type, count: data.length, data };
  }
  if (type === TYPE.BYTE || type === TYPE.UNDEFINED) {
    return { type, count: value.length, data: Uint8Array.from(value) };
  }
  const size = TYPE_SIZE[type];
  const data = new Uint8Array(size * value.length);
  const v = new DataView(data.buffer);
  value.forEach((x, i) => {
    const p = i * size;
    if (type === TYPE.SHORT) v.setUint16(p, x, little);
    else if (type === TYPE.LONG) v.setUint32(p, x, little);
    else if (type === TYPE.SLONG) v.setInt32(p, x, little);
    else if (type === TYPE.RATIONAL) {
      v.setUint32(p, x[0], little);
      v.setUint32(p + 4, x[1], little);
    } else if (type === TYPE.SRATIONAL) {
      v.setInt32(p, x[0], little);
      v.setInt32(p + 4, x[1], little);
    }
  });
  return { type, count: value.length, data };
}

// ---------------------------------------------------------------- helpers

function degToDmsRationals(deg) {
  let d = Math.floor(deg);
  let m = Math.floor((deg - d) * 60);
  let s = Math.round(((deg - d) * 60 - m) * 60 * 10000); // 1/10000 s ≈ 3 mm
  if (s >= 600000) {
    s -= 600000;
    m += 1;
  }
  if (m >= 60) {
    m -= 60;
    d += 1;
  }
  return [
    [d, 1],
    [m, 1],
    [s, 10000],
  ];
}

function dmsToDeg([d, m = 0, s = 0]) {
  return d + m / 60 + s / 3600;
}

/**
 * Parse an EXIF "YYYY:MM:DD HH:MM:SS" string into wall-clock parts.
 * Returns UTC ms *as if the wall-clock time were UTC*; the caller applies
 * the camera's time zone offset.
 */
export function parseExifDate(s, subSec) {
  const m = /^(\d{4}):(\d{2}):(\d{2})[ T](\d{2}):(\d{2}):(\d{2})/.exec(s || '');
  if (!m) return null;
  const ms = subSec ? Math.round(Number(`0.${String(subSec).replace(/\D/g, '')}`) * 1000) : 0;
  return Date.UTC(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +m[6], ms || 0);
}

/** Parse an EXIF OffsetTime string ("+10:00", "-05:30") into minutes. */
export function parseExifOffset(s) {
  const m = /^([+-])(\d{2}):?(\d{2})$/.exec((s || '').trim());
  if (!m) return null;
  return (m[1] === '-' ? -1 : 1) * (+m[2] * 60 + +m[3]);
}

function pad(n) {
  return String(n).padStart(2, '0');
}

function isNum(v) {
  return typeof v === 'number' && Number.isFinite(v);
}

function toUint8(input) {
  if (input instanceof Uint8Array) return input;
  if (input instanceof ArrayBuffer) return new Uint8Array(input);
  throw new TypeError('Expected ArrayBuffer or Uint8Array');
}
