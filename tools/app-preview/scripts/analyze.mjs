import { spawnSync } from "node:child_process";

const [video, ballsArg, outFps = "30"] = process.argv.slice(2);
if (!video || !ballsArg) {
  console.error("usage: analyze.mjs <video> <x0,x1,x2,x3@y> [fps]");
  process.exit(1);
}
const [xs, yStr] = ballsArg.split("@");
const centers = xs.split(",").map(Number);
const cy = Number(yStr);
const ringTop = cy - 99;
const pitch = centers[1] - centers[0];
const pad = 160;
const x0 = Math.max(0, Math.floor((Math.min(...centers) - pad) / 2) * 2);
const x1 = Math.max(...centers) + pad;
const y0 = Math.floor((ringTop - 4) / 2) * 2;
const w = Math.round((x1 - x0) / 2) * 2;
const h = Math.ceil((cy - y0 + 20) / 2) * 2;

const res = spawnSync(
  "ffmpeg",
  ["-v", "error", "-i", video, "-vf", `fps=${outFps},crop=${w}:${h}:${x0}:${y0},format=gray`, "-f", "rawvideo", "-"],
  { maxBuffer: 1 << 30 },
);
if (res.status !== 0) {
  console.error(res.stderr.toString());
  process.exit(1);
}
const buf = res.stdout;
const frameSize = w * h;
const count = Math.floor(buf.length / frameSize);
const px = (f, x, y) => buf[f * frameSize + (y - y0) * w + (x - x0)];

const ringX = (f) => {
  let sum = 0;
  let n = 0;
  for (let x = x0; x < x0 + w; x++) {
    if (px(f, x, ringTop) < 110) {
      sum += x;
      n++;
    }
  }
  return n > 6 ? sum / n : null;
};

const fill = (f, cx) => {
  let s = 0;
  let n = 0;
  for (let dy = -12; dy <= 12; dy += 6) {
    for (let dx = -12; dx <= 12; dx += 6) {
      s += px(f, cx + dx, cy + dy);
      n++;
    }
  }
  return Math.round(s / n);
};

const rows = [];
for (let f = 0; f < count; f++) {
  const x = ringX(f);
  rows.push({
    f,
    pos: x === null ? null : +((x - centers[0]) / pitch).toFixed(3),
    fills: centers.map((cx) => fill(f, cx)),
  });
}

const beats = [];
for (let f = 2; f < count; f++) {
  const a = rows[f - 2].pos;
  const b = rows[f - 1].pos;
  const c = rows[f].pos;
  if (a === null || b === null || c === null) continue;
  const still = Math.abs(b - a) < 0.012;
  const moving = Math.abs(c - b) > 0.02;
  if (still && moving) {
    beats.push({ f: f - 0.5, ball: (Math.round(b) + 1) % centers.length });
  }
}
console.log(JSON.stringify({ count, beats, rows }));
