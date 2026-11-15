/**
 * Registry of dive log format parsers.
 *
 * To add a format, create a module that default-exports an object of the
 * shape below and add it to `PARSERS`. See docs/ADDING_A_FORMAT.md.
 *
 * @typedef {Object} DiveLogParser
 * @property {string} id                 stable identifier, e.g. "garmin-fit"
 * @property {string} name               human readable name
 * @property {string[]} extensions       lower-case, with leading dot
 * @property {(bytes: Uint8Array) => boolean} sniff   content check
 * @property {(bytes: Uint8Array, opts: {fileName: string}) => import('../model.js').Dive[]} parse
 */
import garminFit from './garmin-fit.js';
import { isZip, readZip } from '../zip.js';

/** @type {DiveLogParser[]} */
export const PARSERS = [garminFit];

/**
 * File extensions accepted by the file picker. ZIP archives (as downloaded
 * from Garmin Connect and similar services) are unpacked automatically.
 */
export const ACCEPTED_EXTENSIONS = [...new Set(PARSERS.flatMap((p) => p.extensions)), '.zip'];

/**
 * Find a parser for a file: content sniffing first, extension as fallback.
 * @param {string} fileName
 * @param {Uint8Array} bytes
 */
export function findParser(fileName, bytes) {
  const sniffed = PARSERS.find((p) => {
    try {
      return p.sniff(bytes);
    } catch {
      return false;
    }
  });
  if (sniffed) return sniffed;
  const ext = fileName.toLowerCase().match(/\.[^.]+$/)?.[0];
  return PARSERS.find((p) => p.extensions.includes(ext)) || null;
}

/**
 * Parse a dive log file into dives.
 * @param {string} fileName
 * @param {ArrayBuffer|Uint8Array} data
 */
export function parseDiveLog(fileName, data) {
  const bytes = data instanceof Uint8Array ? data : new Uint8Array(data);
  const parser = findParser(fileName, bytes);
  if (!parser) throw new Error(`Unsupported dive log format: ${fileName}`);
  return parser.parse(bytes, { fileName });
}

/**
 * Parse a dive log file the user selected. ZIP archives are unpacked and
 * every supported log inside them is parsed; other entries are ignored.
 * @param {string} fileName
 * @param {ArrayBuffer|Uint8Array} data
 * @returns {Promise<import('../model.js').Dive[]>}
 */
export async function parseDiveLogFile(fileName, data) {
  const bytes = data instanceof Uint8Array ? data : new Uint8Array(data);
  if (!isZip(bytes)) return parseDiveLog(fileName, bytes);

  const entries = (await readZip(bytes)).filter(
    (e) => !e.name.startsWith('__MACOSX/') && !/(^|\/)\./.test(e.name), // macOS/hidden junk
  );
  const dives = [];
  let supported = 0;
  for (const e of entries) {
    const base = e.name.split('/').pop();
    if (!findParser(base, e.data)) continue;
    supported++;
    dives.push(...parseDiveLog(`${fileName} › ${base}`, e.data));
  }
  if (!supported) throw new Error('ZIP archive contains no supported dive log files');
  return dives;
}
