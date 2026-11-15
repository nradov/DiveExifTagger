// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Which EXIF values to write for a matched photo, given the user's settings.
 */

/**
 * @param {import('./match.js').MatchResult} m
 * @param {number} time  photo time, UTC ms
 * @param {{writePosition:boolean, depthAsAltitude:boolean, writeDiveTags:boolean}} s
 * @returns {Object} tags for writeExif()
 */
export function buildTags(m, time, s) {
  const isDive = m.dive.activity?.isDive !== false;
  const label = isDive ? 'Dive log' : `GPS track (${m.dive.activity?.name || 'activity'})`;
  const tags = { time, processingMethod: `${label}: ${m.dive.device || m.dive.source}` };
  if (s.writePosition && m.lat !== null) Object.assign(tags, { lat: m.lat, lon: m.lon });
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
