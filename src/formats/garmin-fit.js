/**
 * Dive log format adapter: Garmin FIT (Descent series and other Garmin
 * watches that record dive activities).
 *
 * Message / field numbers come from the FIT SDK Profile.xlsx.
 */
import { decodeFit, isFit, FIT_EPOCH_OFFSET, TIMESTAMP_FIELD } from './fit/decoder.js';
import { makeDive, isNum } from '../model.js';

const MSG = {
  FILE_ID: 0,
  SESSION: 18,
  LAP: 19,
  RECORD: 20,
  DEVICE_INFO: 23,
  ACTIVITY: 34,
};

// file_id fields
const FILE_ID = { MANUFACTURER: 1, PRODUCT: 2, PRODUCT_NAME: 8 };
// session fields
const SESSION = {
  START_TIME: 2,
  START_LAT: 3,
  START_LON: 4,
  SPORT: 5,
  TOTAL_ELAPSED: 7, // s * 1000
  END_LAT: 38,
  END_LON: 39,
};
// lap fields
const LAP = { START_TIME: 2, START_LAT: 3, START_LON: 4, END_LAT: 5, END_LON: 6 };
// record fields
const RECORD = { LAT: 0, LON: 1, TEMPERATURE: 13, DEPTH: 92 /* m * 1000 */ };
// activity fields
const ACTIVITY = { LOCAL_TIMESTAMP: 5 };

const SEMICIRCLE_TO_DEG = 180 / 2 ** 31;
const GARMIN_MANUFACTURER = 1;

export default {
  id: 'garmin-fit',
  name: 'Garmin FIT',
  extensions: ['.fit'],
  /** Cheap check used to pick a parser when the extension is ambiguous. */
  sniff: (bytes) => isFit(bytes),
  parse,
};

/**
 * @param {ArrayBuffer|Uint8Array} bytes
 * @param {{fileName?: string}} [opts]
 * @returns {import('../model.js').Dive[]}
 */
export function parse(bytes, { fileName = '' } = {}) {
  const { messages } = decodeFit(bytes);
  const by = (num) => messages.filter((m) => m.num === num).map((m) => m.fields);

  const fileId = by(MSG.FILE_ID)[0] || {};
  const device =
    fileId[FILE_ID.PRODUCT_NAME] ||
    (fileId[FILE_ID.MANUFACTURER] === GARMIN_MANUFACTURER ? 'Garmin' : null);

  const activity = by(MSG.ACTIVITY)[0];
  let utcOffsetMinutes = null;
  if (activity && isNum(activity[ACTIVITY.LOCAL_TIMESTAMP]) && isNum(activity[TIMESTAMP_FIELD])) {
    const diff = activity[ACTIVITY.LOCAL_TIMESTAMP] - activity[TIMESTAMP_FIELD];
    utcOffsetMinutes = Math.round(diff / 900) * 15; // time zones are multiples of 15 min
  }

  const records = by(MSG.RECORD)
    .filter((r) => isNum(r[TIMESTAMP_FIELD]))
    .map((r) => ({
      time: fitTime(r[TIMESTAMP_FIELD]),
      depth: isNum(r[RECORD.DEPTH]) ? r[RECORD.DEPTH] / 1000 : null,
      temperature: isNum(r[RECORD.TEMPERATURE]) ? r[RECORD.TEMPERATURE] : null,
      lat: semi(r[RECORD.LAT]),
      lon: semi(r[RECORD.LON]),
    }))
    .sort((a, b) => a.time - b.time);

  const laps = by(MSG.LAP);
  let sessions = by(MSG.SESSION).filter((s) => isNum(s[SESSION.START_TIME]));

  // No session messages (e.g. a truncated file): treat all records as one dive.
  if (!sessions.length) {
    if (!records.length) return [];
    return [makeDive({ source: 'garmin-fit', fileName, device, utcOffsetMinutes, samples: records })];
  }

  sessions = sessions.sort((a, b) => a[SESSION.START_TIME] - b[SESSION.START_TIME]);

  return sessions.map((s, i) => {
    const start = fitTime(s[SESSION.START_TIME]);
    let end = isNum(s[SESSION.TOTAL_ELAPSED])
      ? start + s[SESSION.TOTAL_ELAPSED]
      : isNum(s[TIMESTAMP_FIELD])
        ? fitTime(s[TIMESTAMP_FIELD])
        : null;
    const nextStart = sessions[i + 1] ? fitTime(sessions[i + 1][SESSION.START_TIME]) : Infinity;
    const samples = records.filter((r) => r.time >= start && r.time < Math.min(nextStart, (end ?? Infinity) + 1));
    if (end === null) end = samples.length ? samples[samples.length - 1].time : start;

    const sessionLaps = laps.filter((l) => {
      const t = isNum(l[LAP.START_TIME]) ? fitTime(l[LAP.START_TIME]) : NaN;
      return t >= start && t <= end;
    });
    const firstLap = sessionLaps[0] || {};
    const lastLap = sessionLaps[sessionLaps.length - 1] || {};
    const firstFix = samples.find((r) => r.lat !== null);
    const lastFix = [...samples].reverse().find((r) => r.lat !== null);

    const entry =
      pos(s[SESSION.START_LAT], s[SESSION.START_LON]) ||
      pos(firstLap[LAP.START_LAT], firstLap[LAP.START_LON]) ||
      (firstFix && { lat: firstFix.lat, lon: firstFix.lon });
    const exit =
      pos(s[SESSION.END_LAT], s[SESSION.END_LON]) ||
      pos(lastLap[LAP.END_LAT], lastLap[LAP.END_LON]) ||
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

function fitTime(t) {
  return (t + FIT_EPOCH_OFFSET) * 1000;
}

function semi(v) {
  return isNum(v) ? v * SEMICIRCLE_TO_DEG : null;
}

function pos(lat, lon) {
  return isNum(lat) && isNum(lon) ? { lat: semi(lat), lon: semi(lon) } : null;
}
