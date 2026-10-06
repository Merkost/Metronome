import React from "react";
import { Img, staticFile } from "remotion";

export const SCREEN_W = 1320;
export const SCREEN_H = 2868;

type Props = {
  width: number;
  src: string;
  zoom?: { scale: number; x: number; y: number };
  children?: React.ReactNode;
  shadow?: number;
};

export const Phone: React.FC<Props> = ({ width, src, zoom, children, shadow = 1 }) => {
  const screenW = width;
  const screenH = (width * SCREEN_H) / SCREEN_W;
  const bezel = width * 0.034;
  const outerW = screenW + bezel * 2;
  const outerH = screenH + bezel * 2;
  const screenRadius = width * 0.135;
  const outerRadius = screenRadius + bezel;
  const z = zoom ?? { scale: 1, x: 0.5, y: 0.5 };
  return (
    <div
      style={{
        position: "relative",
        width: outerW,
        height: outerH,
        borderRadius: outerRadius,
        background: "#0B0B0C",
        boxShadow: `0 0 0 ${width * 0.004}px #2A2A2E, 0 ${width * 0.06}px ${width * 0.16}px rgba(0,0,0,${0.28 * shadow})`,
      }}
    >
      <div
        style={{
          position: "absolute",
          left: bezel,
          top: bezel,
          width: screenW,
          height: screenH,
          borderRadius: screenRadius,
          overflow: "hidden",
          background: "#FFFFFF",
        }}
      >
        <div
          style={{
            position: "absolute",
            inset: 0,
            transform: `scale(${z.scale})`,
            transformOrigin: `${z.x * 100}% ${z.y * 100}%`,
          }}
        >
          <Img
            src={staticFile(src)}
            style={{ width: "100%", height: "100%", display: "block" }}
          />
          {children}
        </div>
        <div
          style={{
            position: "absolute",
            left: "50%",
            top: screenW * 0.026,
            width: screenW * 0.285,
            height: screenW * 0.084,
            marginLeft: -(screenW * 0.285) / 2,
            borderRadius: screenW,
            background: "#000",
          }}
        />
      </div>
    </div>
  );
};

export const phoneOuter = (width: number) => {
  const bezel = width * 0.034;
  return {
    w: width + bezel * 2,
    h: (width * SCREEN_H) / SCREEN_W + bezel * 2,
    bezel,
  };
};
