import React from "react";
import { Phone, phoneOuter } from "./Phone";
import { W } from "../theme";

export const CARD_X = 26;
export const CARD_TOP = 440;
export const CARD_RADIUS = 64;
export const PHONE_W = 640;
export const PHONE_TOP = CARD_TOP + 62;

export const phoneBox = () => {
  const o = phoneOuter(PHONE_W);
  return { left: (W - o.w) / 2, top: PHONE_TOP, w: o.w, h: o.h, bezel: o.bezel };
};

export const screenToCanvas = (fx: number, fy: number) => {
  const b = phoneBox();
  const screenH = (PHONE_W * 2868) / 1320;
  return { x: b.left + b.bezel + fx * PHONE_W, y: b.top + b.bezel + fy * screenH };
};

type Props = {
  card: string;
  src: string;
  x?: number;
  y?: number;
  scale?: number;
  origin?: { x: number; y: number };
  dim?: number;
  overlay?: React.ReactNode;
};

export const Stage: React.FC<Props> = ({ card, src, x = 0, y = 0, scale = 1, origin, dim = 0, overlay }) => {
  const b = phoneBox();
  const o = origin ?? { x: W / 2, y: 1100 };
  return (
    <div
      style={{
        position: "absolute",
        inset: 0,
        transform: `translate(${x}px, ${y}px) scale(${scale})`,
        transformOrigin: `${o.x}px ${o.y}px`,
      }}
    >
      <div
        style={{
          position: "absolute",
          left: CARD_X,
          right: CARD_X,
          top: CARD_TOP,
          height: 1920,
          borderRadius: CARD_RADIUS,
          background: card,
        }}
      />
      <div style={{ position: "absolute", left: b.left, top: b.top }}>
        <Phone width={PHONE_W} src={src}>
          {overlay}
        </Phone>
      </div>
      {dim > 0 && (
        <div
          style={{
            position: "absolute",
            inset: -400,
            background: `rgba(10,10,10,${dim})`,
          }}
        />
      )}
    </div>
  );
};
