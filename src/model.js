// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * The format-neutral dive model. Every dive log parser converts its input
 * into an array of `Dive` objects; nothing downstream (matching, EXIF
 * writing, UI) knows which file format a dive came from.
 *
 * All times are UTC milliseconds since the Unix epoch.
 * Depths are metres (positive = below the surface), temperatures °C,
 * coordinates WGS-84 decimal degrees.
 *
 * @typedef {Object} Position
 * @property {number} lat
 * @property {number} lon
 *
 * @typedef {Object} Sample
 * @property {number} time            UTC ms
 * @property {number|null} depth      metres
 * @property {number|null} temperature °C
 * @property {number|null} lat
 * @property {number|null} lon
 *
 * @typedef {Object} Dive
 * @property {string} id              unique within the session
 * @property {string} source          parser id, e.g. "fit"
 * @property {string} fileName
 * @property {string|null} device     e.g. "Garmin Descent Mk3"
 * @property {number} startTime       UTC ms
 * @property {number} endTime         UTC ms
 * @property {number|null} utcOffsetMinutes  local time offset the dive computer used, if known
 * @property {Position|null} entry    surface position at the start of the dive
 * @property {Position|null} exit     surface position at the end of the dive
 * @property {Sample[]} samples       sorted by time
 * @property {number|null} maxDepth
 * @property {number|null} minTemperature
 * @property {Activity} activity     what kind of recording this is
 *
 * @typedef {Object} Activity
 * @property {boolean} isDive         a scuba or freediving activity with depth data.
 *   Other activities (e.g. a watch in a towed surface buoy recording an open
 *   water swim) are GPS tracks: no depth or temperature is written for them.
 * @property {string|null} sport      as recorded, e.g. "diving", "swimming"
 * @property {string|null} subSport   e.g. "apneaDiving", "openWater"
 * @property {string|null} name       human readable, e.g. "Open Water" or "Multi-Gas"
 */

let counter = 0;

/** Build a Dive with derived fields filled in. */
export function makeDive(props) {
  const samples = [...(props.samples || [])].sort((a, b) => a.time - b.time);
  const depths = samples.map((s) => s.depth).filter(isNum);
  const temps = samples.map((s) => s.temperature).filter(isNum);
  const startTime = props.startTime ?? samples[0]?.time;
  const endTime = props.endTime ?? samples[samples.length - 1]?.time;
  if (!isNum(startTime) || !isNum(endTime)) throw new Error('Dive has no time information');
  return {
    id: props.id ?? `dive-${++counter}`,
    source: props.source,
    fileName: props.fileName ?? '',
    device: props.device ?? null,
    startTime,
    endTime,
    utcOffsetMinutes: props.utcOffsetMinutes ?? null,
    entry: validPos(props.entry),
    exit: validPos(props.exit),
    samples,
    maxDepth: depths.length ? Math.max(...depths) : null,
    minTemperature: temps.length ? Math.min(...temps) : null,
    activity: {
      sport: props.activity?.sport ?? null,
      subSport: props.activity?.subSport ?? null,
      name: props.activity?.name ?? null,
      // Unknown activity type: decide by whether there is depth data.
      isDive: props.activity?.isDive ?? depths.some((d) => d > 0),
    },
  };
}

export function isNum(v) {
  return typeof v === 'number' && Number.isFinite(v);
}

function validPos(p) {
  if (!p || !isNum(p.lat) || !isNum(p.lon)) return null;
  if (Math.abs(p.lat) > 90 || Math.abs(p.lon) > 180) return null;
  if (p.lat === 0 && p.lon === 0) return null; // "null island" = no fix
  return { lat: p.lat, lon: p.lon };
}
