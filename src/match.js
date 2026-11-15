// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Matching photos to dives and interpolating dive data at the photo's time.
 */
import { parseExifDate, parseExifOffset } from './exif/jpeg-exif.js';
import { isNum } from './model.js';

/**
 * @typedef {Object} TimeSettings
 * @property {number|null} cameraUtcOffsetMinutes  manual camera time zone; null = automatic
 * @property {number} clockCorrectionSeconds       added to camera time (fixes clock drift)
 * @property {number} [fallbackUtcOffsetMinutes]   last resort, e.g. the browser's own zone
 *
 * @typedef {Object} PhotoTime
 * @property {number} time            UTC ms
 * @property {number} offsetMinutes   time zone used for the camera's local time
 * @property {'manual'|'photo'|'dive computer'|'this computer'} source
 */

/**
 * Convert a photo's EXIF capture time (local wall-clock time) to UTC.
 *
 * Time zone, in order of preference:
 *   1. a zone the user picked manually,
 *   2. the photo's own OffsetTimeOriginal / OffsetTime tag,
 *   3. the dive computer's zone, assuming camera and dive computer were set to
 *      the same zone (tried for each dive's zone, keeping the one that lands the
 *      photo in a dive with that zone),
 *   4. `fallbackUtcOffsetMinutes`.
 *
 * @param {{dateTimeOriginal:string|null, offsetTimeOriginal:string|null, subSecTimeOriginal:string|null}} exif
 * @param {import('./model.js').Dive[]} dives
 * @param {TimeSettings & Partial<MatchSettings>} s
 * @returns {PhotoTime|null}
 */
export function photoTime(exif, dives, s) {
  const wall = parseExifDate(exif.dateTimeOriginal, exif.subSecTimeOriginal);
  if (wall === null) return null;
  const correction = (s.clockCorrectionSeconds || 0) * 1000;
  const at = (offsetMinutes, source) => ({ time: wall - offsetMinutes * 60_000 + correction, offsetMinutes, source });

  if (isNum(s.cameraUtcOffsetMinutes)) return at(s.cameraUtcOffsetMinutes, 'manual');

  const tag = parseExifOffset(exif.offsetTimeOriginal);
  if (tag !== null) return at(tag, 'photo');

  const zones = [...new Set(dives.map((d) => d.utcOffsetMinutes).filter(isNum))];
  for (const z of zones) {
    const t = at(z, 'dive computer');
    if (matchPhoto(t.time, dives, { toleranceMinutes: 0, ...s })?.dive.utcOffsetMinutes === z) return t;
  }
  if (zones.length) return at(zones[0], 'dive computer');
  return at(s.fallbackUtcOffsetMinutes ?? 0, 'this computer');
}

/**
 * @typedef {Object} MatchSettings
 * @property {number} toleranceMinutes   photos this long before/after a dive still match it (surface shots)
 * @property {'entry'|'exit'|'interpolate'|'manual'} locationMode
 * @property {{lat:number, lon:number}|null} manualPosition  used for 'manual', and as fallback when a dive has no GPS
 */

/**
 * @typedef {Object} MatchResult
 * @property {import('./model.js').Dive} dive
 * @property {'underwater'|'before'|'after'} phase
 * @property {number|null} depth
 * @property {number|null} temperature
 * @property {number|null} lat
 * @property {number|null} lon
 */

/**
 * Find the dive a photo belongs to and the conditions at that moment.
 * @param {number} time UTC ms
 * @param {import('./model.js').Dive[]} dives
 * @param {MatchSettings} s
 * @returns {MatchResult|null}
 */
export function matchPhoto(time, dives, s) {
  const tol = (s.toleranceMinutes || 0) * 60_000;
  let best = null;
  let bestDist = Infinity;
  for (const d of dives) {
    if (time < d.startTime - tol || time > d.endTime + tol) continue;
    const dist = time < d.startTime ? d.startTime - time : time > d.endTime ? time - d.endTime : 0;
    if (dist < bestDist) {
      best = d;
      bestDist = dist;
    }
  }
  if (!best) return null;

  const phase = time < best.startTime ? 'before' : time > best.endTime ? 'after' : 'underwater';
  const depth = phase === 'underwater' ? interpolate(best.samples, time, 'depth') : 0;
  const temperature = interpolate(best.samples, time, 'temperature');
  const position = locate(best, time, phase, s);
  return { dive: best, phase, depth, temperature, lat: position?.lat ?? null, lon: position?.lon ?? null };
}

function locate(dive, time, phase, s) {
  if (s.locationMode === 'manual') return s.manualPosition;
  const { entry, exit } = dive;
  let p = null;
  if (s.locationMode === 'exit') p = exit || entry;
  else if (s.locationMode === 'interpolate' && entry && exit) {
    const span = dive.endTime - dive.startTime;
    const f = phase === 'before' ? 0 : phase === 'after' || span <= 0 ? 1 : (time - dive.startTime) / span;
    p = { lat: entry.lat + (exit.lat - entry.lat) * f, lon: entry.lon + (exit.lon - entry.lon) * f };
  } else if (phase === 'after') p = exit || entry;
  else p = entry || exit;
  return p || s.manualPosition || null;
}

/**
 * Linear interpolation of a sample field at `time`. Samples with a null
 * value for the field are ignored. Outside the sampled range the nearest
 * value is returned.
 */
export function interpolate(samples, time, key) {
  const pts = samples.filter((x) => isNum(x[key]));
  if (!pts.length) return null;
  if (time <= pts[0].time) return pts[0][key];
  if (time >= pts[pts.length - 1].time) return pts[pts.length - 1][key];
  let lo = 0;
  let hi = pts.length - 1;
  while (hi - lo > 1) {
    const mid = (lo + hi) >> 1;
    if (pts[mid].time <= time) lo = mid;
    else hi = mid;
  }
  const a = pts[lo];
  const b = pts[hi];
  const f = b.time === a.time ? 0 : (time - a.time) / (b.time - a.time);
  return a[key] + (b[key] - a[key]) * f;
}
