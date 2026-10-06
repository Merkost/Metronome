import React from "react";

export type BallState = "high" | "low" | "mute";

type Props = {
  unit: number;
  indicator: number;
  indicatorAlpha?: number;
  balls: { state: BallState; appear: number; glow?: number; color?: string }[];
  high: string;
  low: string;
  ring: string;
};

export const BALL = 40;
export const PITCH = 80;
export const RING = 72;
export const RING_STROKE = 5;

export const rowWidth = (count: number, unit: number) => ((count - 1) * PITCH + BALL) * unit;

export const BeatRow: React.FC<Props> = ({
  unit,
  indicator,
  indicatorAlpha = 1,
  balls,
  high,
  low,
  ring,
}) => {
  const n = balls.length;
  const width = rowWidth(n, unit);
  const ringSize = RING * unit;
  return (
    <div style={{ position: "relative", width, height: BALL * unit }}>
      {balls.map((b, i) => {
        const cx = (BALL / 2 + i * PITCH) * unit;
        const size = (BALL - 4) * unit;
        const fill = b.color ?? (b.state === "high" ? high : b.state === "low" ? low : "transparent");
        const s = b.appear * (1 + 0.12 * (b.glow ?? 0));
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              left: cx - size / 2,
              top: (BALL * unit - size) / 2,
              width: size,
              height: size,
              borderRadius: "50%",
              background: fill,
              border: b.state === "mute" ? `${1.5 * unit}px solid ${low}` : undefined,
              boxSizing: "border-box",
              transform: `scale(${s})`,
              opacity: Math.min(1, b.appear * 1.4),
            }}
          />
        );
      })}
      <div
        style={{
          position: "absolute",
          left: (BALL / 2 + indicator * PITCH) * unit - ringSize / 2,
          top: (BALL * unit) / 2 - ringSize / 2,
          width: ringSize,
          height: ringSize,
          borderRadius: "50%",
          border: `${RING_STROKE * unit}px solid ${ring}`,
          boxSizing: "border-box",
          opacity: indicatorAlpha,
          transform: `scale(${0.6 + 0.4 * indicatorAlpha})`,
        }}
      />
    </div>
  );
};
