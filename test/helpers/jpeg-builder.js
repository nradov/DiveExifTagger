/**
 * Builds small JPEG-structured files with an EXIF block, for tests.
 * The "image data" is fake; only the segment structure matters here.
 */

/**
 * @param {Object} o
 * @param {boolean} [o.little]
 * @param {string} [o.dateTimeOriginal]
 * @param {string} [o.offsetTimeOriginal]
 * @param {boolean} [o.makerNote]  include a maker note whose content embeds an absolute offset
 * @param {boolean} [o.exif]       false = no APP1 at all
 */
export function buildJpeg({
  little = true,
  dateTimeOriginal = '2026:11:14 11:20:00',
  offsetTimeOriginal = null,
  makerNote = true,
  exif = true,
  app0 = true,
} = {}) {
  const parts = [[0xff, 0xd8]];
  if (app0) parts.push([0xff, 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0, 0, 1, 0, 1, 0, 0]);
  if (exif) {
    const tiff = buildTiff({ little, dateTimeOriginal, offsetTimeOriginal, makerNote });
    const len = 2 + 6 + tiff.length;
    parts.push([0xff, 0xe1, len >> 8, len & 0xff, 0x45, 0x78, 0x69, 0x66, 0, 0, ...tiff]);
  }
  // DQT-ish dummy segment, then SOS + fake scan data + EOI
  parts.push([0xff, 0xdb, 0x00, 0x04, 0x00, 0x01]);
  parts.push([0xff, 0xda, 0x00, 0x02, 0x12, 0x34, 0x56, 0xff, 0x00, 0x78, 0xff, 0xd9]);
  return Uint8Array.from(parts.flat());
}

export const MAKER_NOTE_MAGIC = 'MKNT';

function buildTiff({ little, dateTimeOriginal, offsetTimeOriginal, makerNote }) {
  const buf = new Uint8Array(1024);
  const v = new DataView(buf.buffer);
  const u16 = (p, x) => v.setUint16(p, x, little);
  const u32 = (p, x) => v.setUint32(p, x, little);
  buf.set(little ? [0x49, 0x49] : [0x4d, 0x4d], 0);
  u16(2, 42);
  u32(4, 8);

  let data = 400; // out-of-line values start here
  const putAscii = (s) => {
    const at = data;
    const b = new TextEncoder().encode(s + '\0');
    buf.set(b, at);
    data += b.length + (b.length % 2);
    return [at, b.length];
  };

  // IFD0 at 8: Make, Model, ExifIFD pointer
  const exifIfdAt = 100;
  const ifd0 = [];
  const [makeAt, makeLen] = putAscii('TestCam');
  ifd0.push([0x010f, 2, makeLen, makeAt]);
  const [modelAt, modelLen] = putAscii('Housing 1');
  ifd0.push([0x0110, 2, modelLen, modelAt]);
  ifd0.push([0x8769, 4, 1, exifIfdAt]);
  writeIfd(8, ifd0);

  // Exif IFD
  const ex = [];
  const [dtoAt, dtoLen] = putAscii(dateTimeOriginal);
  ex.push([0x9003, 2, dtoLen, dtoAt]);
  if (offsetTimeOriginal) {
    const [o, l] = putAscii(offsetTimeOriginal);
    ex.push([0x9011, 2, l, o]);
  }
  if (makerNote) {
    // maker note = magic + absolute offset (pointing to itself), like many vendors do
    const at = data;
    buf.set(new TextEncoder().encode(MAKER_NOTE_MAGIC), at);
    u32(at + 4, at);
    data += 8;
    ex.push([0x927c, 7, 8, at]);
  }
  writeIfd(exifIfdAt, ex);

  return buf.slice(0, data);

  function writeIfd(at, entries) {
    entries.sort((a, b) => a[0] - b[0]);
    u16(at, entries.length);
    entries.forEach(([tag, type, count, value], i) => {
      const p = at + 2 + i * 12;
      u16(p, tag);
      u16(p + 2, type);
      u32(p + 4, count);
      u32(p + 8, value);
    });
    u32(at + 2 + entries.length * 12, 0);
  }
}
