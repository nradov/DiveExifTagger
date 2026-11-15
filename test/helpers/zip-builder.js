/**
 * Builds DEFLATE-compressed ZIPs with data descriptors (general purpose flag
 * bit 3), like many real-world archivers, for testing the ZIP reader.
 */
import { deflateRawSync } from 'node:zlib';
import { crc32 } from '../../src/zip.js';

/** @param {{name: string, data: Uint8Array}[]} files */
export function buildDeflateZip(files) {
  const enc = new TextEncoder();
  const parts = [];
  const central = [];
  let offset = 0;
  for (const f of files) {
    const name = enc.encode(f.name);
    const comp = new Uint8Array(deflateRawSync(f.data));
    const crc = crc32(f.data);
    const local = new Uint8Array(30 + name.length);
    const lv = new DataView(local.buffer);
    lv.setUint32(0, 0x04034b50, true);
    lv.setUint16(4, 20, true);
    lv.setUint16(6, 0x0008, true); // sizes/CRC in data descriptor, zero here
    lv.setUint16(8, 8, true);
    lv.setUint16(26, name.length, true);
    local.set(name, 30);
    const desc = new Uint8Array(16);
    const dv = new DataView(desc.buffer);
    dv.setUint32(0, 0x08074b50, true);
    dv.setUint32(4, crc, true);
    dv.setUint32(8, comp.length, true);
    dv.setUint32(12, f.data.length, true);

    const c = new Uint8Array(46 + name.length);
    const cv = new DataView(c.buffer);
    cv.setUint32(0, 0x02014b50, true);
    cv.setUint16(4, 20, true);
    cv.setUint16(6, 20, true);
    cv.setUint16(8, 0x0008, true);
    cv.setUint16(10, 8, true);
    cv.setUint32(16, crc, true);
    cv.setUint32(20, comp.length, true);
    cv.setUint32(24, f.data.length, true);
    cv.setUint16(28, name.length, true);
    cv.setUint32(42, offset, true);
    c.set(name, 46);

    parts.push(local, comp, desc);
    central.push(c);
    offset += local.length + comp.length + desc.length;
  }
  const cdSize = central.reduce((s, c) => s + c.length, 0);
  const end = new Uint8Array(22);
  const ev = new DataView(end.buffer);
  ev.setUint32(0, 0x06054b50, true);
  ev.setUint16(8, files.length, true);
  ev.setUint16(10, files.length, true);
  ev.setUint32(12, cdSize, true);
  ev.setUint32(16, offset, true);
  const all = [...parts, ...central, end];
  const out = new Uint8Array(all.reduce((s, x) => s + x.length, 0));
  let p = 0;
  for (const x of all) {
    out.set(x, p);
    p += x.length;
  }
  return out;
}
