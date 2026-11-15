// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Which EXIF values to write for a matched photo, given the user's settings.
 */

/**
 * True when the photo already has a position measured at the time it was
 * taken: GPSStatus "A" (measurement in progress), as written for positions
 * from a surface GPS track, and by many cameras and phones with live GPS.
 * A later dive pass must not replace it with the dive computer's entry/exit
 * point, which was measured before or after the dive.
 * @param {{gps: object|null, gpsStatus: string|null}} [existing] readExif() result
 */
export function hasMeasuredPosition(existing) {
  return Boolean(existing?.gps && existing.gpsStatus === 'A');
}

/**
 * Supports processing the same photos twice, in either order: once with the
 * dive computer's log (depth, temperature) and once with a surface GPS track
 * (position). Each pass only replaces the tags it owns; everything else in
 * the photo is kept.
 *
 * Positions are written with standard Exif values only:
 *   GPSProcessingMethod "GPS" (or "MANUAL" for a typed-in position), and
 *   GPSStatus "A" for a position measured at the photo's time (GPS track) or
 *   "V" (measurement interrupted) otherwise: a dive computer's entry/exit
 *   point was its last/next fix while the diver was underwater, and a manual
 *   position isn't a measurement at all.
 *
 * @param {import('./match.js').MatchResult} m
 * @param {number} time  photo time, UTC ms
 * @param {{writePosition:boolean, depthAsAltitude:boolean, writeDiveTags:boolean, locationMode?:string}} s
 * @param {object} [existing] the photo's current metadata (readExif() result)
 * @returns {Object} tags for writeExif()
 */
export function buildTags(m, time, s, existing) {
  const isDive = m.dive.activity?.isDive !== false;
  const manual = s.locationMode === 'manual';
  const tags = { time };
  const keepMeasured = isDive && !manual && hasMeasuredPosition(existing);
  if (s.writePosition && m.lat !== null && !keepMeasured) {
    Object.assign(tags, manual
      ? { lat: m.lat, lon: m.lon, processingMethod: 'MANUAL', gpsStatus: 'V' }
      : { lat: m.lat, lon: m.lon, processingMethod: 'GPS', gpsStatus: isDive ? 'V' : 'A' });
  }
  // Depth and temperature are only ever written for dives.
  if (isDive) {
    if (s.depthAsAltitude && m.depth !== null) tags.altitude = -m.depth;
    if (s.writeDiveTags) {
      if (m.depth !== null) tags.waterDepth = m.depth;
      if (m.temperature !== null) tags.temperature = m.temperature;
    }
  }
  return tags;
}

/** Tags that would change the photo (time and the processing note alone don't count). */
export function hasPayload(tags) {
  return Object.keys(tags).some((k) => k !== 'time' && k !== 'processingMethod');
}
