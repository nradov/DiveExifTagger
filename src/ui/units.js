// SPDX-License-Identifier: LGPL-3.0-or-later
/**
 * Display units for the web page only. Data written to EXIF is always metric.
 */

export const UNIT_SYSTEMS = {
  metric: { label: 'Metric (m, °C)', depth: 'm', temperature: '°C' },
  statute: { label: 'Statute (ft, °F)', depth: 'ft', temperature: '°F' },
};

const FEET_PER_METRE = 1 / 0.3048;

/** Regions that conventionally use feet and °F (United States and territories, Liberia, Myanmar). */
const STATUTE_REGIONS = new Set(['US', 'PR', 'GU', 'VI', 'AS', 'MP', 'UM', 'LR', 'MM']);

/** Default unit system for a BCP 47 locale such as "en-US". */
export function defaultUnits(locale) {
  let region = null;
  try {
    region = new Intl.Locale(locale).maximize().region;
  } catch {
    /* invalid locale: fall through */
  }
  return STATUTE_REGIONS.has(region) ? 'statute' : 'metric';
}

/** Depth in metres → number in the display unit. */
export function depthValue(m, units) {
  return units === 'statute' ? m * FEET_PER_METRE : m;
}

/** Temperature in °C → number in the display unit. */
export function temperatureValue(c, units) {
  return units === 'statute' ? (c * 9) / 5 + 32 : c;
}

/** e.g. "42.7 m" or "140 ft" (feet need no decimals at dive-computer precision). */
export function formatDepth(m, units, digits = units === 'statute' ? 0 : 1) {
  if (m == null) return '–';
  return `${depthValue(m, units).toFixed(digits)} ${UNIT_SYSTEMS[units].depth}`;
}

/** e.g. "20.0 °C" or "68 °F". */
export function formatTemperature(c, units, digits = units === 'statute' ? 0 : 1) {
  if (c == null) return '–';
  return `${temperatureValue(c, units).toFixed(digits)} ${UNIT_SYSTEMS[units].temperature}`;
}
