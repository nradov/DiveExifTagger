// SPDX-License-Identifier: LGPL-3.0-or-later
import { depthValue, UNIT_SYSTEMS } from './units.js';

/**
 * Renders a small SVG depth profile with markers for matched photos.
 */
const W = 600;
const H = 124;
const PAD = { l: 34, r: 8, t: 8, b: 30 };

/** "Nice" tick spacings for the time axis, in minutes. */
const TICK_STEPS = [1, 2, 5, 10, 15, 20, 30, 60, 120];

/**
 * Minute marks for a time axis: the smallest nice spacing that gives at most
 * `maxTicks` intervals. Returns the minutes to label, starting at 0.
 * @param {number} durationMinutes
 * @param {number} [maxTicks]
 */
export function minuteTicks(durationMinutes, maxTicks = 8) {
  const step = TICK_STEPS.find((s) => durationMinutes / s <= maxTicks) ?? Math.ceil(durationMinutes / maxTicks / 60) * 60;
  const ticks = [];
  for (let m = 0; m <= durationMinutes + 1e-9; m += step) ticks.push(m);
  return ticks;
}

/**
 * @param {import('../model.js').Dive} dive
 * @param {number[]} photoTimes UTC ms of photos matched to this dive
 * @param {'metric'|'statute'} [units] display units for the axis labels
 * @returns {string} SVG markup
 */
export function profileSvg(dive, photoTimes = [], units = 'metric') {
  if (dive.activity && !dive.activity.isDive) return trackTimelineSvg(dive, photoTimes);
  const pts = dive.samples.filter((s) => s.depth !== null);
  if (pts.length < 2) return '<p class="muted">No depth samples in this log.</p>';
  const t0 = dive.startTime;
  const t1 = Math.max(dive.endTime, t0 + 1);
  const maxD = Math.max(dive.maxDepth || 1, 1);
  const x = (t) => PAD.l + ((t - t0) / (t1 - t0)) * (W - PAD.l - PAD.r);
  const y = (d) => PAD.t + (d / maxD) * (H - PAD.t - PAD.b);

  const step = Math.max(1, Math.floor(pts.length / 400)); // keep the path small
  let d = `M${x(pts[0].time).toFixed(1)},${y(0).toFixed(1)}`;
  for (let i = 0; i < pts.length; i += step) d += `L${x(pts[i].time).toFixed(1)},${y(pts[i].depth).toFixed(1)}`;
  const last = pts[pts.length - 1];
  d += `L${x(last.time).toFixed(1)},${y(last.depth).toFixed(1)}L${x(last.time).toFixed(1)},${y(0).toFixed(1)}Z`;

  const markers = photoTimes
    .filter((t) => t >= t0 && t <= t1)
    .map((t) => `<line class="photo-mark" x1="${x(t).toFixed(1)}" x2="${x(t).toFixed(1)}" y1="${PAD.t}" y2="${H - PAD.b}"/>`)
    .join('');

  const minutes = Math.round((t1 - t0) / 60000);
  const bottom = H - PAD.b;
  const axisY = bottom + 3;
  const ticks = minuteTicks((t1 - t0) / 60000)
    .map((m) => {
      const tx = x(t0 + m * 60000).toFixed(1);
      // leave room at the right edge for the "min" unit label
      const label = Number(tx) <= W - PAD.r - 28 ? `<text class="label" x="${tx}" y="${axisY + 14}" text-anchor="middle">${m}</text>` : '';
      return `<line class="grid" x1="${tx}" x2="${tx}" y1="${PAD.t}" y2="${bottom}"/><line class="axis" x1="${tx}" x2="${tx}" y1="${axisY}" y2="${axisY + 4}"/>${label}`;
    })
    .join('');
  const unit = UNIT_SYSTEMS[units].depth;
  const maxLabel = `${depthValue(maxD, units).toFixed(0)} ${unit}`;
  return `<svg class="profile" viewBox="0 0 ${W} ${H}" role="img" aria-label="Depth profile, max ${maxLabel} over ${minutes} minutes">
    ${ticks}
    <line class="axis" x1="${PAD.l}" x2="${W - PAD.r}" y1="${y(0)}" y2="${y(0)}"/>
    <line class="axis" x1="${PAD.l}" x2="${W - PAD.r}" y1="${axisY}" y2="${axisY}"/>
    <path class="depth" d="${d}"/>
    ${markers}
    <text class="label" x="${PAD.l - 4}" y="${y(0) + 4}" text-anchor="end">0 ${unit}</text>
    <text class="label" x="${PAD.l - 4}" y="${y(maxD)}" text-anchor="end">${maxLabel}</text>
    <text class="label" x="${W - PAD.r}" y="${axisY + 14}" text-anchor="end">min</text>
  </svg>`;
}

/** Gaps longer than this between fixes are shown as missing GPS. */
const FIX_GAP_MS = 15_000;

/**
 * Timeline for a non-diving activity (a GPS track, e.g. a watch in a towed
 * buoy): where the track has GPS fixes, where it has gaps, and where the
 * photos fall.
 */
export function trackTimelineSvg(dive, photoTimes = []) {
  const TH = 64;
  const top = 10;
  const band = 18;
  const t0 = dive.startTime;
  const t1 = Math.max(dive.endTime, t0 + 1);
  const x = (t) => PAD.l + ((Math.min(Math.max(t, t0), t1) - t0) / (t1 - t0)) * (W - PAD.l - PAD.r);
  const fixes = dive.samples.filter((s) => s.lat !== null).map((s) => s.time);

  // Merge consecutive fixes into covered segments.
  const segments = [];
  for (const t of fixes) {
    const last = segments[segments.length - 1];
    if (last && t - last[1] <= FIX_GAP_MS) last[1] = t;
    else segments.push([t, t]);
  }
  const covered = segments
    .map(([a, b]) => `<rect class="gps" x="${x(a).toFixed(1)}" y="${top}" width="${Math.max(1, x(b) - x(a)).toFixed(1)}" height="${band}"/>`)
    .join('');

  const axisY = top + band + 6;
  const ticks = minuteTicks((t1 - t0) / 60000)
    .map((m) => {
      const tx = x(t0 + m * 60000).toFixed(1);
      const label = Number(tx) <= W - PAD.r - 28 ? `<text class="label" x="${tx}" y="${axisY + 14}" text-anchor="middle">${m}</text>` : '';
      return `<line class="axis" x1="${tx}" x2="${tx}" y1="${axisY}" y2="${axisY + 4}"/>${label}`;
    })
    .join('');
  const markers = photoTimes
    .filter((t) => t >= t0 && t <= t1)
    .map((t) => `<line class="photo-mark" x1="${x(t).toFixed(1)}" x2="${x(t).toFixed(1)}" y1="${top - 4}" y2="${top + band + 4}"/>`)
    .join('');
  const minutes = Math.round((t1 - t0) / 60000);
  const gapCount = segments.length - 1;

  return `<svg class="profile track" viewBox="0 0 ${W} ${TH}" role="img" aria-label="GPS track timeline over ${minutes} minutes with ${fixes.length} fixes and ${gapCount} gaps">
    <rect class="gps-gap" x="${PAD.l}" y="${top}" width="${W - PAD.l - PAD.r}" height="${band}"/>
    ${covered}
    ${markers}
    <text class="label" x="${PAD.l - 4}" y="${top + band / 2 + 4}" text-anchor="end">GPS</text>
    <line class="axis" x1="${PAD.l}" x2="${W - PAD.r}" y1="${axisY}" y2="${axisY}"/>
    ${ticks}
    <text class="label" x="${W - PAD.r}" y="${axisY + 14}" text-anchor="end">min</text>
  </svg>`;
}
