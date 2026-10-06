import React from "react";
import { AbsoluteFill, Audio, interpolate, staticFile, useCurrentFrame } from "remotion";
import { BeatRow, BallState, PITCH, BALL } from "./components/BeatRow";
import { BrandMark } from "./components/BrandMark";
import { Kinetic, Line } from "./components/Kinetic";
import { CARD_TOP, PHONE_W, Stage, screenToCanvas } from "./components/Stage";
import { Touches } from "./components/Touch";
import score from "./score.json";
import { BEAT, BLUE, FONT, GROUND, Hue, INK, INK_SOFT, MARGIN, MINT, PINK, VIOLET, W } from "./theme";
import { calm, clamp, easeIn, easeInOut, easeOut, emphasized, expressive, mix, pulse, ramp, snappy } from "./motion";

type SceneKey = "s1" | "s2" | "s3" | "s4";
type Scene = { start: number; end: number; clip: string; frames: number[] };
const scenes = score.scenes as Record<SceneKey, Scene>;
const T = score.timing;
const layout = score.layout;
const clicks: { f: number }[] = score.clicks;

const footage = (key: SceneKey, frame: number) => {
  const s = scenes[key];
  const i = Math.max(0, Math.min(s.frames.length - 1, Math.round(frame - s.start)));
  return `footage/${s.clip}/${String(s.frames[i] + 1).padStart(4, "0")}.jpg`;
};

const COPY: Record<SceneKey, { hue: Hue; lines: Line[]; sub: string }> = {
  s1: {
    hue: VIOLET,
    lines: [
      { text: "Precise timing.", color: INK },
      { text: "Better practice.", color: VIOLET.accent },
    ],
    sub: "A steady pulse you can trust, at any tempo",
  },
  s2: {
    hue: MINT,
    lines: [
      { text: "Build speed", color: INK },
      { text: "automatically", color: MINT.accent },
    ],
    sub: "The tempo climbs bar by bar while you play",
  },
  s3: {
    hue: BLUE,
    lines: [
      { text: "Train your", color: INK },
      { text: "inner clock", color: BLUE.accent },
    ],
    sub: "The click drops out. You keep the time.",
  },
  s4: {
    hue: PINK,
    lines: [
      { text: "Shape", color: INK },
      { text: "every beat", color: PINK.accent },
    ],
    sub: "Tap any beat to accent it, soften it or mute it",
  },
};

const HEAD_X = MARGIN;
const HEAD_Y = 150;
const HEAD_SIZE = 76;
const SUB_Y = 336;

const Eyebrow: React.FC<{ frame: number; hue: Hue; appear: number; beatPulse: number }> = ({ hue, appear, beatPulse }) => (
  <>
    <div
      style={{
        position: "absolute",
        left: HEAD_X,
        top: 84,
        fontFamily: FONT,
        fontWeight: 600,
        fontSize: 22,
        letterSpacing: "0.06em",
        color: hue.accent,
        opacity: appear,
        transform: `translateY(${(1 - appear) * 12}px)`,
      }}
    >
      METRONOME
    </div>
    <div
      style={{
        position: "absolute",
        right: HEAD_X - 6,
        top: 60,
        opacity: appear,
        transform: `scale(${(0.8 + 0.2 * appear) * (1 + 0.05 * beatPulse)})`,
      }}
    >
      <BrandMark size={74} color={hue.accent} />
    </div>
  </>
);

const Sub: React.FC<{ text: string; inAt: number; outAt: number }> = ({ text, inAt, outAt }) => {
  const frame = useCurrentFrame();
  const a = calm(frame, inAt);
  const b = ramp(frame, outAt, outAt + 8, easeIn);
  return (
    <div
      style={{
        position: "absolute",
        left: HEAD_X,
        top: SUB_Y,
        fontFamily: FONT,
        fontWeight: 400,
        fontSize: 29,
        color: INK_SOFT,
        opacity: a * (1 - b),
        transform: `translateY(${(1 - a) * 18 - b * 10}px)`,
        whiteSpace: "nowrap",
      }}
    >
      {text}
    </div>
  );
};

const hueAt = (frame: number): Hue => {
  if (frame < T.s2) return VIOLET;
  if (frame < T.s3) return MINT;
  if (frame < T.s4) return BLUE;
  if (frame < T.end) return PINK;
  return VIOLET;
};

const lastBeatBefore = (frame: number) => {
  let last = -999;
  for (const c of clicks) if (c.f <= frame) last = c.f;
  return last;
};

const ColdOpen: React.FC<{ frame: number }> = ({ frame }) => {
  const p = calm(frame, T.s1 - 2, 22);
  const words = [0, BEAT, BEAT * 2, BEAT * 3];
  const bigSize = 112;
  const sx = mix(1, HEAD_SIZE / bigSize, p);
  const ty = mix(640, HEAD_Y, p);
  const dotsUnitStart = 2.15;
  const dotsUnitEnd = PHONE_W / 440;
  const unit = mix(dotsUnitStart, dotsUnitEnd, p);
  const target = screenToCanvas(layout.ballsX[0], layout.ballsY);
  const rowW = (3 * PITCH + BALL) * unit;
  const startCx = W / 2;
  const startCy = 1180;
  const endCx = screenToCanvas((layout.ballsX[0] + layout.ballsX[3]) / 2, layout.ballsY).x;
  const cx = mix(startCx, endCx, p);
  const cy = mix(startCy, target.y, p);
  const beatIndex = Math.max(0, Math.min(3, Math.floor(frame / BEAT)));
  const slide = beatIndex === 0 ? 0 : beatIndex - 1 + emphasized(frame, beatIndex * BEAT);
  const indicator = frame >= T.s1 ? 3 - 3 * emphasized(frame, T.s1) : slide;
  const handoff = ramp(frame, T.s1 + 14, T.s1 + 22, easeInOut);
  const colorT = ramp(frame, T.s1 + 2, T.s1 + 16, easeInOut);
  const balls = [0, 1, 2, 3].map((i) => {
    const appear = frame >= i * BEAT ? expressive(frame, i * BEAT) : 0;
    const glow = pulse(frame, i * BEAT, 8) * (frame < T.s1 ? 1 : 0);
    const state: BallState = i === 0 ? "high" : "low";
    const dark = i === 0 ? layout.highColor : layout.lowColor;
    const light = i === 0 ? INK : "#3A3A3F";
    return { state, appear, glow, color: lerpColor(light, dark, colorT) };
  });
  const headOut = frame >= T.s1 ? 0 : 0;
  return (
    <>
      <div
        style={{
          position: "absolute",
          left: HEAD_X,
          top: ty,
          transform: `scale(${sx})`,
          transformOrigin: "0 0",
          opacity: 1 - headOut,
        }}
      >
        {frame < T.s2 ? (
          <Kinetic
            lines={COPY.s1.lines}
            size={bigSize}
            inAt={0}
            wordFrames={words}
            punchy
            outAt={T.s2 - 8}
          />
        ) : null}
      </div>
      {handoff < 1 && (
        <div
          style={{
            position: "absolute",
            left: cx - rowW / 2,
            top: cy - (BALL * unit) / 2,
            opacity: 1 - handoff,
          }}
        >
          <BeatRow
            unit={unit}
            indicator={indicator}
            balls={balls}
            high={INK}
            low="#3A3A3F"
            ring={lerpColor(INK, layout.highColor, colorT)}
          />
        </div>
      )}
    </>
  );
};

const lerpColor = (a: string, b: string, t: number) => {
  const pa = [1, 3, 5].map((i) => parseInt(a.slice(i, i + 2), 16));
  const pb = [1, 3, 5].map((i) => parseInt(b.slice(i, i + 2), 16));
  const c = pa.map((v, i) => Math.round(mix(v, pb[i], clamp(t))));
  return `rgb(${c[0]},${c[1]},${c[2]})`;
};

const Count: React.FC<{ frame: number }> = ({ frame }) => {
  const beats = [0, 1, 2, 3].map((i) => T.gap + i * BEAT);
  const out = ramp(frame, T.s4 - 6, T.s4, easeIn);
  return (
    <div
      style={{
        position: "absolute",
        left: HEAD_X,
        top: SUB_Y - 6,
        display: "flex",
        gap: 34,
        fontFamily: FONT,
        fontWeight: 700,
        fontSize: 44,
        opacity: 1 - out,
      }}
    >
      {beats.map((b, i) => {
        const a = frame >= b ? expressive(frame, b) : 0;
        const hot = pulse(frame, b, 10);
        return (
          <div
            key={i}
            style={{
              color: hot > 0.2 ? BLUE.accent : "rgba(201,213,254,0.55)",
              opacity: a,
              transform: `translateY(${(1 - a) * 22}px) scale(${1 + 0.18 * hot})`,
              transformOrigin: "50% 80%",
            }}
          >
            {i + 1}
          </div>
        );
      })}
    </div>
  );
};

const EndCard: React.FC<{ frame: number }> = ({ frame }) => {
  const f = frame - T.end;
  const drawStart = T.end + 2;
  const draw = [0, 1, 2, 3].map((i) => ramp(frame, drawStart + i * 4, drawStart + i * 4 + 20, easeInOut));
  const fill = ramp(frame, T.final - 3, T.final + 4, easeOut);
  const pop = frame >= T.final ? expressive(frame, T.final) : 0;
  const markScale = 1 + 0.06 * pulse(frame, T.final, 12);
  const word = calm(frame, T.final + 2);
  const tag = calm(frame, T.final + 8);
  const dots = [VIOLET, MINT, BLUE, PINK];
  const dotsIn = calm(frame, T.final + 12);
  const markColor = lerpColor(INK, VIOLET.accent, clamp(pop));
  if (f < 0) return null;
  return (
    <AbsoluteFill style={{ alignItems: "center", justifyContent: "center" }}>
      <div style={{ transform: `translateY(-90px) scale(${markScale})` }}>
        <BrandMark size={250} color={markColor} draw={draw} fill={fill} />
      </div>
      <div
        style={{
          position: "absolute",
          top: 1040,
          width: "100%",
          textAlign: "center",
          fontFamily: FONT,
          fontWeight: 700,
          fontSize: 92,
          letterSpacing: "-0.03em",
          color: INK,
          opacity: word,
          transform: `translateY(${(1 - word) * 30}px)`,
        }}
      >
        Metronome
      </div>
      <div
        style={{
          position: "absolute",
          top: 1150,
          width: "100%",
          textAlign: "center",
          fontFamily: FONT,
          fontWeight: 400,
          fontSize: 34,
          color: INK_SOFT,
          opacity: tag,
          transform: `translateY(${(1 - tag) * 20}px)`,
        }}
      >
        No ads. No account. Just practice.
      </div>
      <div style={{ position: "absolute", top: 1290, display: "flex", gap: 26 }}>
        {dots.map((h, i) => {
          const a = calm(frame, T.final + 12 + i * 3);
          return (
            <div
              key={i}
              style={{
                width: 22,
                height: 22,
                borderRadius: 11,
                background: h.accent,
                opacity: a * dotsIn,
                transform: `scale(${a})`,
              }}
            />
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

export const AppPreview: React.FC = () => {
  const frame = useCurrentFrame();
  const hue = hueAt(frame);
  const beatPulse = pulse(frame, lastBeatBefore(frame), 8);

  const riseP = calm(frame, T.s1 - 2, 22);
  const s1Y = (1 - riseP) * 1500;
  const s1Push = ramp(frame, T.s1, T.s2, easeInOut);

  const s2In = snappy(frame, T.s2 - 6);
  const s2Zoom = ramp(frame, T.s2 + 4, T.s3, easeIn);
  const bpm = screenToCanvas(0.5, layout.bpmY);

  const s3In = frame >= T.s3 ? snappy(frame, T.s3) : 0;
  const silence = ramp(frame, T.gap - 2, T.gap + 6, easeOut) * (1 - ramp(frame, T.s4 - 4, T.s4, easeIn));

  const s4In = snappy(frame, T.s4 - 4);
  const s4Zoom = calm(frame, T.s4 + 8, 30);
  const balls = screenToCanvas((layout.ballsX[0] + layout.ballsX[3]) / 2, layout.ballsY);

  const outP = ramp(frame, T.end - 2, T.end + 12, easeIn);

  const headlineFor = (key: SceneKey, inAt: number, outAt: number, punchy = false) => (
    <div style={{ position: "absolute", left: HEAD_X, top: HEAD_Y }}>
      <Kinetic lines={COPY[key].lines} size={HEAD_SIZE} inAt={inAt} outAt={outAt} punchy={punchy} />
    </div>
  );

  const eyebrowIn = calm(frame, T.s1 + 4);
  const eyebrowOut = ramp(frame, T.end - 2, T.end + 8, easeIn);

  return (
    <AbsoluteFill style={{ background: GROUND, overflow: "hidden" }}>
      <Audio src={staticFile("audio/soundtrack.wav")} />

      {frame < T.s2 + 14 && frame >= T.s1 - 4 && (
        <Stage
          card={VIOLET.card}
          src={footage("s1", frame)}
          y={s1Y}
          scale={(1 + 0.025 * s1Push) * (1 - 0.06 * clamp(s2In))}
          origin={{ x: W / 2, y: CARD_TOP }}
          dim={0.5 * clamp(s2In)}
        />
      )}

      {frame >= T.s2 - 6 && frame < T.s3 && (
        <Stage
          card={MINT.card}
          src={footage("s2", Math.max(frame, T.s2))}
          y={(1 - s2In) * 1500}
          scale={1 + 0.3 * s2Zoom}
          origin={{ x: bpm.x, y: bpm.y }}
        />
      )}

      {frame >= T.s3 && frame < T.s4 + 14 && (
        <Stage
          card={BLUE.card}
          src={footage("s3", Math.min(frame, T.s4 - 1))}
          x={-(clamp(s4In) * 0.35) * W * (frame >= T.s4 - 4 ? 1 : 0)}
          scale={(1.06 - 0.06 * clamp(s3In)) * (1 - 0.06 * (frame >= T.s4 - 4 ? clamp(s4In) : 0))}
          origin={{ x: W / 2, y: 1100 }}
          dim={0.22 * silence + (frame >= T.s4 - 4 ? 0.5 * clamp(s4In) : 0)}
        />
      )}

      {frame >= T.s4 - 4 && frame < T.end + 16 && (
        <Stage
          card={PINK.card}
          src={footage("s4", Math.max(frame, T.s4))}
          x={(1 - s4In) * W}
          y={outP * 1700}
          scale={1 + 0.42 * s4Zoom}
          origin={{ x: balls.x, y: balls.y }}
          overlay={<Touches taps={score.taps} size={150} />}
        />
      )}

      {frame < T.s2 + 2 && <ColdOpen frame={frame} />}

      {frame >= T.s2 - 2 && frame < T.s3 + 2 && headlineFor("s2", T.s2, T.s3 - 4)}
      {frame >= T.s3 - 2 && frame < T.s4 + 2 && headlineFor("s3", T.s3, T.s4 - 6, true)}
      {frame >= T.s4 - 2 && frame < T.end + 12 && headlineFor("s4", T.s4, T.end - 4)}

      {frame >= T.s1 && frame < T.s2 && <Sub text={COPY.s1.sub} inAt={T.s1 + 8} outAt={T.s2 - 8} />}
      {frame >= T.s2 && frame < T.s3 && <Sub text={COPY.s2.sub} inAt={T.s2 + 6} outAt={T.s3 - 6} />}
      {frame >= T.s3 && frame < T.gap + 2 && <Sub text={COPY.s3.sub} inAt={T.s3 + 6} outAt={T.gap - 6} />}
      {frame >= T.gap - 2 && frame < T.s4 && <Count frame={frame} />}
      {frame >= T.s4 && frame < T.end + 4 && <Sub text={COPY.s4.sub} inAt={T.s4 + 6} outAt={T.end - 6} />}

      {frame >= T.s1 && frame < T.end + 10 && (
        <Eyebrow frame={frame} hue={hue} appear={eyebrowIn * (1 - eyebrowOut)} beatPulse={beatPulse} />
      )}

      <EndCard frame={frame} />
    </AbsoluteFill>
  );
};
