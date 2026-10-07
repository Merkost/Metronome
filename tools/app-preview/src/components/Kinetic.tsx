import React from "react";
import { useCurrentFrame } from "remotion";
import { FONT } from "../theme";
import { calm, clamp, easeIn, ramp, snappy } from "../motion";

export type Line = { text: string; color: string };

type Props = {
  lines: Line[];
  size: number;
  weight?: number;
  lineHeight?: number;
  tracking?: number;
  inAt: number;
  outAt?: number;
  stagger?: number;
  wordFrames?: number[];
  punchy?: boolean;
  style?: React.CSSProperties;
};

export const Kinetic: React.FC<Props> = ({
  lines,
  size,
  weight = 700,
  lineHeight = 1.04,
  tracking = -0.02,
  inAt,
  outAt,
  stagger = 2.5,
  wordFrames,
  punchy = false,
  style,
}) => {
  const frame = useCurrentFrame();
  let index = 0;
  return (
    <div
      style={{
        fontFamily: FONT,
        fontWeight: weight,
        fontSize: size,
        lineHeight,
        letterSpacing: `${tracking}em`,
        ...style,
      }}
    >
      {lines.map((line, li) => (
        <div key={li} style={{ display: "block", whiteSpace: "nowrap" }}>
          {line.text.split(" ").map((word, wi) => {
            const i = index++;
            const start = wordFrames ? wordFrames[i] ?? inAt : inAt + i * stagger;
            const enter = punchy ? snappy(frame, start) : calm(frame, start);
            const exitStart = outAt !== undefined ? outAt + i * 1.2 : Infinity;
            const exit = ramp(frame, exitStart, exitStart + 9, easeIn);
            const y = (1 - enter) * 105 - exit * 105;
            const blur = clamp(1 - enter) * 6 + exit * 4;
            const scale = punchy ? 1 + (1 - clamp(enter)) * 0.06 : 1;
            return (
              <span
                key={wi}
                style={{
                  display: "inline-block",
                  overflow: "hidden",
                  verticalAlign: "top",
                  paddingBottom: "0.12em",
                  marginBottom: "-0.12em",
                  marginRight: wi < line.text.split(" ").length - 1 ? "0.24em" : 0,
                }}
              >
                <span
                  style={{
                    display: "inline-block",
                    color: line.color,
                    transform: `translateY(${y}%) scale(${scale})`,
                    transformOrigin: "0% 100%",
                    filter: blur > 0.05 ? `blur(${blur}px)` : undefined,
                  }}
                >
                  {word}
                </span>
              </span>
            );
          })}
        </div>
      ))}
    </div>
  );
};
