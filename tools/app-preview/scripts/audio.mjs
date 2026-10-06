import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const score = JSON.parse(readFileSync(join(root, "src/score.json"), "utf8"));

const SR = 48000;
const FPS = 30;
const LEN = Math.ceil((score.durationInFrames / FPS) * SR);
const L = new Float32Array(LEN);
const R = new Float32Array(LEN);
const sendL = new Float32Array(LEN);
const sendR = new Float32Array(LEN);

const loadF32 = (name) => {
  const b = readFileSync(join(root, "public/audio", `${name}.f32`));
  return new Float32Array(b.buffer, b.byteOffset, b.byteLength / 4);
};
const wood = loadF32("wood");

const at = (frame) => Math.round((frame / FPS) * SR);

const put = (i, l, r, send) => {
  if (i < 0 || i >= LEN) return;
  L[i] += l;
  R[i] += r;
  sendL[i] += l * send;
  sendR[i] += r * send;
};

const sample = (buf, frame, { rate = 1, gain = 1, pan = 0, send = 0.18 } = {}) => {
  const start = at(frame);
  const n = Math.floor(buf.length / rate);
  const gl = gain * Math.cos(((pan + 1) * Math.PI) / 4) * Math.SQRT2;
  const gr = gain * Math.sin(((pan + 1) * Math.PI) / 4) * Math.SQRT2;
  for (let k = 0; k < n; k++) {
    const p = k * rate;
    const i0 = Math.floor(p);
    const fr = p - i0;
    const v = (buf[i0] ?? 0) * (1 - fr) + (buf[i0 + 1] ?? 0) * fr;
    put(start + k, v * gl, v * gr, send);
  }
};

const thump = (frame, gain = 0.5) => {
  const start = at(frame);
  const n = Math.floor(SR * 0.6);
  let phase = 0;
  for (let k = 0; k < n; k++) {
    const t = k / SR;
    const f = 42 + 58 * Math.exp(-t / 0.045);
    phase += (2 * Math.PI * f) / SR;
    const env = (1 - Math.exp(-t / 0.002)) * Math.exp(-t / 0.16);
    const v = Math.sin(phase) * env * gain;
    put(start + k, v, v, 0.05);
  }
};

let seed = 7;
const noise = () => {
  seed = (seed * 1664525 + 1013904223) >>> 0;
  return seed / 2147483648 - 1;
};

const riser = (fromFrame, toFrame, gain = 0.16) => {
  const s = at(fromFrame);
  const e = at(toFrame);
  let lpL = 0;
  let lpR = 0;
  let bpL = 0;
  let bpR = 0;
  for (let i = s; i < e; i++) {
    const t = (i - s) / (e - s);
    const cutoff = 200 + 5200 * t * t;
    const a = 1 - Math.exp((-2 * Math.PI * cutoff) / SR);
    lpL += a * (noise() - lpL);
    lpR += a * (noise() - lpR);
    const hp = 1 - Math.exp((-2 * Math.PI * 120) / SR);
    bpL += hp * (lpL - bpL);
    bpR += hp * (lpR - bpR);
    const env = Math.pow(t, 2.2) * gain;
    put(i, (lpL - bpL) * env, (lpR - bpR) * env, 0.3);
  }
};

const midi = (m) => 440 * Math.pow(2, (m - 69) / 12);

const chord = (frame, notes, { gain = 0.11, attack = 0.05, hold = 1.2, release = 1.8 } = {}) => {
  const start = at(frame);
  const total = Math.floor(SR * (attack + hold + release));
  notes.forEach((m, ni) => {
    const f0 = midi(m);
    const detune = [0.9985, 1.0015];
    const phases = [ni * 1.3, ni * 2.1 + 0.7];
    for (let k = 0; k < total; k++) {
      const t = k / SR;
      let env;
      if (t < attack) env = t / attack;
      else if (t < attack + hold) env = 1 - 0.25 * ((t - attack) / hold);
      else env = 0.75 * Math.exp(-(t - attack - hold) / (release / 3.2));
      const bright = Math.exp(-t / 1.6);
      for (let c = 0; c < 2; c++) {
        const f = f0 * detune[c];
        const ph = phases[c] + (2 * Math.PI * f * k) / SR;
        const v =
          Math.sin(ph) +
          0.32 * bright * Math.sin(2 * ph) +
          0.12 * bright * Math.sin(3 * ph) +
          0.05 * bright * Math.sin(4 * ph);
        const g = (env * gain * v) / Math.sqrt(notes.length);
        if (c === 0) put(start + k, g * (ni % 2 ? 0.8 : 1), 0, 0.45);
        else put(start + k, 0, g * (ni % 2 ? 1 : 0.8), 0.45);
      }
    }
  });
};

for (const c of score.clicks) {
  sample(wood, c.f, {
    rate: c.accent ? 1.4 : 1,
    gain: (c.accent ? 0.95 : 0.78) * (c.gain ?? 1),
    pan: 0,
    send: 0.16,
  });
}
for (const t of score.thumps ?? []) thump(t.f, t.gain ?? 0.5);
for (const r of score.risers ?? []) riser(r.from, r.to, r.gain);
for (const c of score.chords ?? []) chord(c.f, c.notes, c.opts);

const reverb = (inp, combs, allpasses, decay) => {
  const out = new Float32Array(LEN);
  for (const d of combs) {
    const buf = new Float32Array(d);
    let idx = 0;
    let lp = 0;
    for (let i = 0; i < LEN; i++) {
      const y = buf[idx];
      lp = y * 0.62 + lp * 0.38;
      buf[idx] = inp[i] + lp * decay;
      idx = (idx + 1) % d;
      out[i] += y / combs.length;
    }
  }
  for (const d of allpasses) {
    const buf = new Float32Array(d);
    let idx = 0;
    for (let i = 0; i < LEN; i++) {
      const b = buf[idx];
      const x = out[i];
      const y = -x + b;
      buf[idx] = x + b * 0.5;
      out[i] = y;
      idx = (idx + 1) % d;
    }
  }
  return out;
};

const k = SR / 44100;
const wetL = reverb(sendL, [1116, 1188, 1277, 1356, 1422, 1491].map((d) => Math.round(d * k * 1.6)), [556, 441, 341].map((d) => Math.round(d * k)), 0.84);
const wetR = reverb(sendR, [1139, 1211, 1300, 1379, 1445, 1514].map((d) => Math.round(d * k * 1.6)), [579, 464, 364].map((d) => Math.round(d * k)), 0.84);

const outL = new Float32Array(LEN);
const outR = new Float32Array(LEN);
for (let i = 0; i < LEN; i++) {
  outL[i] = L[i] + wetL[i] * 0.55;
  outR[i] = R[i] + wetR[i] * 0.55;
}

let peak = 0;
for (let i = 0; i < LEN; i++) peak = Math.max(peak, Math.abs(outL[i]), Math.abs(outR[i]));
const norm = peak > 0 ? 0.89 / peak : 1;

const fadeOut = Math.floor(SR * 0.35);
const pcm = Buffer.alloc(LEN * 4);
for (let i = 0; i < LEN; i++) {
  const f = i > LEN - fadeOut ? (LEN - i) / fadeOut : 1;
  const l = Math.max(-1, Math.min(1, outL[i] * norm * f));
  const r = Math.max(-1, Math.min(1, outR[i] * norm * f));
  pcm.writeInt16LE(Math.round(l * 32767), i * 4);
  pcm.writeInt16LE(Math.round(r * 32767), i * 4 + 2);
}
const header = Buffer.alloc(44);
header.write("RIFF", 0);
header.writeUInt32LE(36 + pcm.length, 4);
header.write("WAVE", 8);
header.write("fmt ", 12);
header.writeUInt32LE(16, 16);
header.writeUInt16LE(1, 20);
header.writeUInt16LE(2, 22);
header.writeUInt32LE(SR, 24);
header.writeUInt32LE(SR * 4, 28);
header.writeUInt16LE(4, 32);
header.writeUInt16LE(16, 34);
header.write("data", 36);
header.writeUInt32LE(pcm.length, 40);
writeFileSync(join(root, "public/audio/soundtrack.wav"), Buffer.concat([header, pcm]));
console.log(`soundtrack.wav ${(LEN / SR).toFixed(2)}s peak-normalised x${norm.toFixed(2)}`);
