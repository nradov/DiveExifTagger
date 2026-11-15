/**
 * Dive log format adapter: Garmin FIT (Descent series and other Garmin
 * watches that record dive activities), decoded with Garmin's official
 * FIT JavaScript SDK (@garmin/fitsdk).
 *
 * In the browser, the bare "@garmin/fitsdk" specifier is resolved by the
 * import map in index.html. In Node (tests), it resolves from node_modules.
 *
 * Note: the FIT SDK is distributed under Garmin's FIT Protocol License,
 * not under this project's MIT license.
 */
import { Decoder, Stream } from '@garmin/fitsdk';
import { makeDive, isNum } from '../model.js';

// Global message numbers (FIT Profile). Messages are collected by number via
// the decoder's mesgListener so we don't depend on dictionary key names.
const MESG = { FILE_ID: 0, SESSION: 18, LAP: 19, RECORD: 20, ACTIVITY: 34 };

const FIT_EPOCH_MS = 631065600000;
const SEMICIRCLE_TO_DEG = 180 / 2 ** 31;

export default {
  id: 'garmin-fit',
  name: 'Garmin FIT',
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
  const device =
    fileId.productName ||
    (fileId.manufacturer === 'garmin' && typeof fileId.garminProduct === 'string'
      ? `Garmin ${fileId.garminProduct}`
      : fileId.manufacturer === 'garmin'
        ? 'Garmin'
        : null);

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
    return [makeDive({ source: 'garmin-fit', fileName, device, utcOffsetMinutes, samples: records })];
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
      source: 'garmin-fit',
      fileName: sessions.length > 1 ? `${fileName} #${i + 1}` : fileName,
      device,
      startTime: start,
      endTime: end,
      utcOffsetMinutes,
      entry,
      exit,
      samples,
    });
  });
}

/** Date (date_time fields) or raw FIT seconds (e.g. local_date_time) → Unix ms. */
function toMs(v) {
  if (v instanceof Date) return v.getTime();
  if (isNum(v)) return v * 1000 + FIT_EPOCH_MS;
  return NaN;
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
