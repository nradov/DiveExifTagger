// SPDX-License-Identifier: LGPL-3.0-or-later
import { depthValue, UNIT_SYSTEMS } from './units.js';

/**
 * Renders a small SVG depth profile with markers for matched photos.
 */
const W = 600;
const H = 110;
const PAD = { l: 34, r: 8, t: 8, b: 18 };

/**
 * @param {import('../model.js').Dive} dive
 * @param {number[]} photoTimes UTC ms of photos matched to this dive
 * @param {'metric'|'statute'} [units] display units for the axis labels
 * @returns {string} SVG markup
 */
export function profileSvg(dive, photoTimes = [], units = 'metric') {
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
  const unit = UNIT_SYSTEMS[units].depth;
  const maxLabel = `${depthValue(maxD, units).toFixed(0)} ${unit}`;
  return `<svg class="profile" viewBox="0 0 ${W} ${H}" role="img" aria-label="Depth profile, max ${maxLabel} over ${minutes} minutes">
    <line class="axis" x1="${PAD.l}" x2="${W - PAD.r}" y1="${y(0)}" y2="${y(0)}"/>
    <path class="depth" d="${d}"/>
    ${markers}
    <text class="label" x="${PAD.l - 4}" y="${y(0) + 4}" text-anchor="end">0 ${unit}</text>
    <text class="label" x="${PAD.l - 4}" y="${y(maxD)}" text-anchor="end">${maxLabel}</text>
    <text class="label" x="${W - PAD.r}" y="${H - 4}" text-anchor="end">${minutes} min</text>
  </svg>`;
}
