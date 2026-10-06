import React from "react";
import { AbsoluteFill } from "remotion";
import { BeatRow } from "../components/BeatRow";
import { Phone, phoneOuter } from "../components/Phone";
import { BLUE, FONT, GROUND, INK, MINT, PINK, VIOLET } from "../theme";

export const HEADER_W = 3840;
export const HEADER_H = 1646;
export const HEADER_SAFE = { x: 1097, y: 493, w: 1646, h: 661 };

const Card: React.FC<{
  x: number;
  y: number;
  w: number;
  color: string;
  phone: string;
  phoneW: number;
}> = ({ x, y, w, color, phone, phoneW }) => {
  const outer = phoneOuter(phoneW);
  return (
    <div
      style={{
        position: "absolute",
        left: x,
        top: y,
        width: w,
        height: HEADER_H - y + 200,
        borderRadius: 120,
        background: color,
        overflow: "hidden",
      }}
    >
      <div style={{ position: "absolute", left: (w - outer.w) / 2, top: 110 }}>
        <Phone width={phoneW} src={phone} />
      </div>
    </div>
  );
};

export const Header: React.FC<{ guides?: boolean }> = ({ guides = false }) => {
  const s = HEADER_SAFE;
  const unit = 2.1;
  const hues = [VIOLET, MINT, BLUE, PINK];
  return (
    <AbsoluteFill style={{ background: GROUND }}>
      <Card x={-150} y={260} w={980} color={MINT.card} phone="stills/tempo.png" phoneW={640} />
      <Card x={3010} y={260} w={980} color={BLUE.card} phone="stills/gap.png" phoneW={640} />
      <div
        style={{
          position: "absolute",
          left: s.x,
          top: s.y,
          width: s.w,
          height: s.h,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: 92,
        }}
      >
        <div style={{ paddingTop: 30 }}>
          <BeatRow
            unit={unit}
            indicator={0}
            balls={hues.map((h) => ({ state: "high" as const, appear: 1, color: h.accent }))}
            high={INK}
            low={INK}
            ring={INK}
          />
        </div>
        <div
          style={{
            fontFamily: FONT,
            fontWeight: 700,
            fontSize: 178,
            lineHeight: 1.02,
            letterSpacing: "-0.02em",
            textAlign: "center",
            color: INK,
          }}
        >
          <div>Precise timing.</div>
          <div style={{ color: VIOLET.accent }}>Better practice.</div>
        </div>
      </div>
      {guides && (
        <div
          style={{
            position: "absolute",
            left: s.x,
            top: s.y,
            width: s.w,
            height: s.h,
            outline: "4px solid #00FF00",
          }}
        />
      )}
    </AbsoluteFill>
  );
};
