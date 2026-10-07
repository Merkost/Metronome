import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const edit = JSON.parse(readFileSync(join(root, "scripts/edit.json"), "utf8"));
const clip = (name) => JSON.parse(readFileSync(join(root, "captures", `${name}.json`), "utf8"));

const T = edit.timing;
const BEAT = 15;
const scenes = {};
const clicks = [];
const taps = [];

const linear = (s, startOut, endOut, beatSrc, beatOut) => {
  const offset = beatSrc - beatOut;
  const frames = [];
  for (let o = startOut; o < endOut; o++) frames.push(Math.ceil(o + offset));
  return { offset, frames };
};

const stateAt = (states, f) => {
  let cur = states[0].pattern;
  for (const s of states) if (s.f <= f) cur = s.pattern;
  return cur;
};

const addClicks = (data, toOut, fromOut, toOutEnd, states, opts = {}) => {
  for (const b of data.beats) {
    const raw = toOut(b.f);
    const o = raw === null ? null : opts.grid ? Math.round(raw / BEAT) * BEAT : raw;
    if (o === null || o < fromOut - 0.01 || o >= toOutEnd - 0.01) continue;
    if (opts.muted?.(b.f)) continue;
    const pattern = states ? stateAt(states, b.f) : "HLLL";
    const kind = pattern[b.ball];
    if (kind === "M") continue;
    clicks.push({ f: +o.toFixed(3), accent: kind === "H", gain: opts.gain?.(b) ?? 1 });
  }
};

for (const f of [0, 15, 30, 45]) clicks.push({ f, accent: f === 0, gain: 1 });

{
  const c = clip("c1");
  const { offset, frames } = linear(c, T.s1 - 8, T.s2 + 16, edit.s1.beatSrc, T.s1);
  scenes.s1 = { start: T.s1 - 8, end: T.s2 + 16, clip: "c1", frames };
  addClicks(c, (f) => f - offset, T.s1, T.s2, null, { grid: true });
}

{
  const c = clip("c2");
  const keys = edit.s2.downbeats;
  const seg = (T.s3 - T.s2) / (keys.length - 1);
  const map = (o) => {
    const t = Math.max(0, Math.min(keys.length - 1 - 1e-9, (o - T.s2) / seg));
    const k = Math.floor(t);
    const s = t - k;
    const D = keys[k + 1] - keys[k];
    const g = seg * (2 * s ** 3 - 3 * s ** 2 + s) + D * (3 * s ** 2 - 2 * s ** 3);
    return keys[k] + g;
  };
  const frames = [];
  for (let o = T.s2 - 8; o < T.s3; o++) frames.push(Math.round(o < T.s2 ? keys[0] - (T.s2 - o) : map(o)));
  scenes.s2 = { start: T.s2 - 8, end: T.s3, clip: "c2", frames };
  const inverse = (src) => {
    if (src < keys[0] || src > keys[keys.length - 1]) return null;
    let lo = T.s2;
    let hi = T.s3;
    for (let i = 0; i < 40; i++) {
      const mid = (lo + hi) / 2;
      if (map(mid) < src) lo = mid;
      else hi = mid;
    }
    return (lo + hi) / 2;
  };
  const downs = new Set(keys.map((k) => Math.round(k * 2)));
  addClicks(c, inverse, T.s2, T.s3, null, {
    gain: (b) => (downs.has(Math.round(b.f * 2)) ? 1 : 0.55),
  });
}

{
  const c = clip("c3");
  const { offset, frames } = linear(c, T.s3, T.s4 + 16, edit.s3.beatSrc, T.s3);
  scenes.s3 = { start: T.s3, end: T.s4 + 16, clip: "c3", frames };
  addClicks(c, (f) => f - offset, T.s3, T.s4, null, {
    grid: true,
    muted: (f) => f - offset >= T.gap - 0.5,
  });
}

{
  const c = clip("c4");
  const { offset, frames } = linear(c, T.s4 - 6, T.end + 18, edit.s4.beatSrc, T.s4);
  scenes.s4 = { start: T.s4 - 6, end: T.end + 18, clip: "c4", frames };
  addClicks(c, (f) => f - offset, T.s4, T.final, edit.s4.states, { grid: true });
  for (const t of edit.s4.taps) taps.push({ frame: +(t.f - offset).toFixed(2), x: t.x / 440, y: t.y / 956 });
}

clicks.push({ f: T.final, accent: true, gain: 1.1 });
clicks.sort((a, b) => a.f - b.f);

const score = {
  durationInFrames: edit.durationInFrames,
  timing: T,
  layout: edit.layout,
  scenes,
  taps,
  clicks,
  thumps: [
    { f: T.s1, gain: 0.45 },
    { f: T.s3, gain: 0.7 },
    { f: T.s4, gain: 0.55 },
    { f: T.final, gain: 0.75 },
  ],
  risers: [{ from: T.s2 + 4, to: T.s3, gain: 0.2 }],
  chords: [
    {
      f: T.final,
      notes: [48, 55, 62, 64, 71],
      opts: { gain: 0.1, attack: 0.03, hold: 0.6, release: 1.4 },
    },
  ],
};

writeFileSync(join(root, "src/score.json"), JSON.stringify(score, null, 1));
console.log(`clicks ${clicks.length}, taps ${taps.length}`);
console.log(clicks.map((c) => `${c.f}${c.accent ? "!" : ""}`).join(" "));
