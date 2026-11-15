// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Dive log format adapter: FIT files from dive computers, decoded with
 * Garmin's official FIT JavaScript SDK (@garmin/fitsdk).
 *
 * Known producers:
 *  - Garmin Descent series (and other Garmin watches that record dives):
 *    entry/exit positions in the session (and lap) messages.
 *  - Suunto (e.g. Ocean, Nautic), exported from the Suunto app: no positions
 *    in session/lap messages; GPS fixes appear only in record messages while
 *    at the surface, and the files carry Suunto developer fields (which the
 *    SDK handles and we ignore). Entry/exit come from the first/last fix.
 *
 * In the browser, the bare "@garmin/fitsdk" specifier is resolved by the
 * import map in index.html. In Node (tests), it resolves from node_modules.
 *
 * Note: the FIT SDK is distributed under Garmin's FIT Protocol License,
 * not under this project's LGPL license.
 */
import { Decoder, Stream } from '@garmin/fitsdk';
import { makeDive, isNum } from '../model.js';

// Global message numbers (FIT Profile). Messages are collected by number via
// the decoder's mesgListener so we don't depend on dictionary key names.
const MESG = { FILE_ID: 0, SPORT: 12, SESSION: 18, LAP: 19, RECORD: 20, ACTIVITY: 34 };

const FIT_EPOCH_MS = 631065600000;
const SEMICIRCLE_TO_DEG = 180 / 2 ** 31;

export default {
  id: 'fit',
  name: 'FIT (Garmin, Suunto)',
  extensions: ['.fit'],
  sniff: (bytes) => Decoder.isFIT(Stream.fromByteArray(bytes)),
  parse,
};

/**
 * @param {Uint8Array|ArrayBuffer} input
 * @param {{fileName?: string}} [opts]
 * @returns {import('../model.js').Dive[]}
 */
export function parse(input, { fileName = '' } = {}) {
  const bytes = input instanceof Uint8Array ? input : new Uint8Array(input);
  const decoder = new Decoder(Stream.fromByteArray(bytes));
  if (!decoder.isFIT()) throw new Error('Not a FIT file');
  const intact = decoder.checkIntegrity();

  const byNum = new Map();
  const { errors } = decoder.read({
    applyScaleAndOffset: true, // depth in m, elapsed time in s
    convertTypesToStrings: true,
    convertDateTimesToDates: true,
    expandComponents: true,
    expandSubFields: true,
    mesgListener: (num, mesg) => {
      if (!byNum.has(num)) byNum.set(num, []);
      byNum.get(num).push(mesg);
    },
  });
  const get = (num) => byNum.get(num) || [];

  const records = get(MESG.RECORD)
    .filter((r) => r.timestamp != null)
    .map((r) => ({
      time: toMs(r.timestamp),
      depth: num(r.depth),
      temperature: num(r.temperature),
      lat: semi(r.positionLat),
      lon: semi(r.positionLong),
    }))
    .filter((r) => isNum(r.time))
    .sort((a, b) => a.time - b.time);

  if (!records.length && !get(MESG.SESSION).length) {
    const why = errors?.[0]?.message || (intact ? 'no dive data found' : 'file is corrupt or truncated');
    throw new Error(why);
  }

  const fileId = get(MESG.FILE_ID)[0] || {};
  // e.g. "Suunto Ocean" (Suunto writes product_name), "Garmin Descent Mk2"
  const maker = typeof fileId.manufacturer === 'string' ? prettyProduct(fileId.manufacturer) : null;
  const device =
    fileId.productName ||
    (maker && typeof fileId.garminProduct === 'string' ? `${maker} ${prettyProduct(fileId.garminProduct)}` : maker);

  // activity.localTimestamp − activity.timestamp = the watch's UTC offset
  const activity = get(MESG.ACTIVITY)[0];
  let utcOffsetMinutes = null;
  if (activity && activity.localTimestamp != null && activity.timestamp != null) {
    const diffSec = (toMs(activity.localTimestamp) - toMs(activity.timestamp)) / 1000;
    if (isNum(diffSec)) utcOffsetMinutes = Math.round(diffSec / 900) * 15;
  }

  const laps = get(MESG.LAP);
  const sessions = get(MESG.SESSION)
    .filter((s) => s.startTime != null)
    .sort((a, b) => toMs(a.startTime) - toMs(b.startTime));

  // No session messages (e.g. the watch crashed mid-dive): one dive from records.
  if (!sessions.length) {
    return [makeDive({ source: 'fit', fileName, device, utcOffsetMinutes, samples: records, activity: activityOf({}, get(MESG.SPORT)[0]) })];
  }

  return sessions.map((s, i) => {
    const start = toMs(s.startTime);
    let end = isNum(s.totalElapsedTime)
      ? start + s.totalElapsedTime * 1000
      : s.timestamp != null
        ? toMs(s.timestamp)
        : null;
    const nextStart = sessions[i + 1] ? toMs(sessions[i + 1].startTime) : Infinity;
    const samples = records.filter((r) => r.time >= start && r.time < Math.min(nextStart, (end ?? Infinity) + 1));
    if (end === null) end = samples.length ? samples[samples.length - 1].time : start;

    const sessionLaps = laps.filter((l) => {
      const t = l.startTime != null ? toMs(l.startTime) : NaN;
      return t >= start && t <= end;
    });
    const firstLap = sessionLaps[0] || {};
    const lastLap = sessionLaps[sessionLaps.length - 1] || {};
    const firstFix = samples.find((r) => r.lat !== null);
    const lastFix = [...samples].reverse().find((r) => r.lat !== null);

    const entry =
      pos(s.startPositionLat, s.startPositionLong) ||
      pos(firstLap.startPositionLat, firstLap.startPositionLong) ||
      (firstFix && { lat: firstFix.lat, lon: firstFix.lon });
    const exit =
      pos(s.endPositionLat, s.endPositionLong) ||
      pos(lastLap.endPositionLat, lastLap.endPositionLong) ||
      (lastFix && { lat: lastFix.lat, lon: lastFix.lon });

    return makeDive({
      source: 'fit',
      fileName: sessions.length > 1 ? `${fileName} #${i + 1}` : fileName,
      device,
      startTime: start,
      endTime: end,
      utcOffsetMinutes,
      entry,
      exit,
      samples,
      activity: activityOf(s, get(MESG.SPORT)[0]),
    });
  });
}

/**
 * What kind of activity a session records. Dive computers record scuba and
 * freediving sessions with sport = "diving" (sub sports single/multi-gas,
 * gauge, CCR, apnea, apnea hunting). Anything else, e.g. an open water swim
 * from a watch in a towed buoy, is treated as a GPS track.
 */
function activityOf(session, sportMesg = {}) {
  const sport = session.sport ?? sportMesg.sport ?? null;
  const subSport = session.subSport ?? sportMesg.subSport ?? null;
  const name = session.sportProfileName || sportMesg.name || (typeof subSport === 'string' && subSport !== 'generic' ? prettyProduct(subSport) : null) || (typeof sport === 'string' ? prettyProduct(sport) : null);
  return {
    sport: sport == null ? null : String(sport),
    subSport: subSport == null ? null : String(subSport),
    name,
    // Without a sport, let makeDive decide from the depth data.
    isDive: sport == null ? undefined : sport === 'diving',
  };
}

/** Date (date_time fields) or raw FIT seconds (e.g. local_date_time) → Unix ms. */
function toMs(v) {
  if (v instanceof Date) return v.getTime();
  if (isNum(v)) return v * 1000 + FIT_EPOCH_MS;
  return NaN;
}

/** "descentMk3i" → "Descent Mk3i" */
function prettyProduct(name) {
  return name.replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, (c) => c.toUpperCase());
}

function num(v) {
  return isNum(v) ? v : null;
}

function semi(v) {
  return isNum(v) ? v * SEMICIRCLE_TO_DEG : null;
}

function pos(lat, lon) {
  return isNum(lat) && isNum(lon) ? { lat: semi(lat), lon: semi(lon) } : null;
}
