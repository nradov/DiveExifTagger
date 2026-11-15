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

/** @type {DiveLogParser[]} */
export const PARSERS = [garminFit];

/** File extensions accepted by the file picker. */
export const ACCEPTED_EXTENSIONS = [...new Set(PARSERS.flatMap((p) => p.extensions))];

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
