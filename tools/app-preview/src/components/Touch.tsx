import React from "react";
import { useCurrentFrame } from "remotion";
import { clamp, easeOut, ramp, snappy } from "../motion";

export type Tap = { frame: number; x: number; y: number };

export const Touches: React.FC<{ taps: Tap[]; size: number }> = ({ taps, size }) => {
  const frame = useCurrentFrame();
  return (
    <>
      {taps.map((t, i) => {
        const d = frame - t.frame;
        if (d < -4 || d > 22) return null;
        const press = snappy(frame, t.frame - 4);
        const release = ramp(frame, t.frame + 3, t.frame + 20, easeOut);
        const dotScale = 0.7 + 0.3 * press - 0.2 * release;
        const ring = ramp(frame, t.frame, t.frame + 18, easeOut);
        const dotAlpha = clamp(press) * (1 - release);
        return (
          <React.Fragment key={i}>
            <div
              style={{
                position: "absolute",
                left: `${t.x * 100}%`,
                top: `${t.y * 100}%`,
                width: size,
                height: size,
                marginLeft: -size / 2,
                marginTop: -size / 2,
                borderRadius: "50%",
                background: "rgba(20,20,24,0.16)",
                border: `${size * 0.045}px solid rgba(255,255,255,0.9)`,
                boxShadow: "0 6px 18px rgba(0,0,0,0.18)",
                transform: `scale(${dotScale})`,
                opacity: dotAlpha,
              }}
            />
            <div
              style={{
                position: "absolute",
                left: `${t.x * 100}%`,
                top: `${t.y * 100}%`,
                width: size,
                height: size,
                marginLeft: -size / 2,
                marginTop: -size / 2,
                borderRadius: "50%",
                border: `${size * 0.03}px solid rgba(20,20,24,0.35)`,
                transform: `scale(${1 + ring * 0.9})`,
                opacity: d >= 0 ? (1 - ring) * 0.8 : 0,
              }}
            />
          </React.Fragment>
        );
      })}
    </>
  );
};
