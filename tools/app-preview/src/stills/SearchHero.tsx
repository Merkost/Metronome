import React from "react";
import { AbsoluteFill } from "remotion";
import { BrandMark } from "../components/BrandMark";
import { Phone, phoneOuter } from "../components/Phone";
import { BLUE, FONT, GROUND, INK, INK_SOFT, MINT, VIOLET } from "../theme";

export const SEARCH_W = 3840;
export const SEARCH_H = 2560;
export const SEARCH_SAFE = { x: 836, y: 765, w: 2168, h: 1030 };

const FEATURES = [
  { label: "Tempo trainer", hue: MINT },
  { label: "Gap trainer", hue: BLUE },
  { label: "Practice sets", hue: VIOLET },
];

const SideCard: React.FC<{ left: number; top: number; color: string; src: string }> = ({ left, top, color, src }) => (
  <div
    style={{
      position: "absolute",
      left,
      top,
      width: 900,
      height: 2400,
      borderRadius: 150,
      background: color,
      overflow: "hidden",
    }}
  >
    <div style={{ position: "absolute", left: (900 - phoneOuter(620).w) / 2, top: 150 }}>
      <Phone width={620} src={src} shadow={0.6} />
    </div>
  </div>
);

export const SearchHero: React.FC<{ guides?: boolean }> = ({ guides = false }) => {
  const s = SEARCH_SAFE;
  const phoneW = 640;
  const outer = phoneOuter(phoneW);
  const phoneLeft = s.x + s.w - outer.w - 10;
  const phoneTop = 690;
  return (
    <AbsoluteFill style={{ background: GROUND }}>
      <SideCard left={3290} top={860} color={MINT.card} src="stills/tempo.png" />
      <SideCard left={-560} top={1000} color={BLUE.card} src="stills/gap.png" />
      <div
        style={{
          position: "absolute",
          left: phoneLeft - 120,
          top: phoneTop - 110,
          width: outer.w + 240,
          height: 2400,
          borderRadius: 150,
          background: VIOLET.card,
        }}
      />
      <div style={{ position: "absolute", left: phoneLeft, top: phoneTop }}>
        <Phone width={phoneW} src="stills/main.png" />
      </div>
      <div
        style={{
          position: "absolute",
          left: s.x,
          top: s.y,
          width: phoneLeft - 120 - s.x - 40,
          height: s.h,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          fontFamily: FONT,
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 26, marginBottom: 46 }}>
          <BrandMark size={104} color={VIOLET.accent} />
          <div style={{ color: VIOLET.accent, fontWeight: 600, fontSize: 60, letterSpacing: "0.06em" }}>
            METRONOME
          </div>
        </div>
        <div style={{ color: INK, fontWeight: 700, fontSize: 156, lineHeight: 1.02, letterSpacing: "-0.02em" }}>
          <div>Precise timing.</div>
          <div style={{ color: VIOLET.accent }}>Better practice.</div>
        </div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 24, marginTop: 70 }}>
          {FEATURES.map((f) => (
            <div
              key={f.label}
              style={{
                display: "flex",
                alignItems: "center",
                gap: 22,
                padding: "24px 42px 26px 36px",
                borderRadius: 999,
                background: "#1A1A1D",
                color: INK_SOFT,
                fontSize: 64,
                fontWeight: 500,
                letterSpacing: "-0.01em",
              }}
            >
              <div style={{ width: 28, height: 28, borderRadius: 14, background: f.hue.accent }} />
              {f.label}
            </div>
          ))}
        </div>
      </div>
      {guides && (
        <div style={{ position: "absolute", left: s.x, top: s.y, width: s.w, height: s.h, outline: "4px solid #00FF00" }} />
      )}
    </AbsoluteFill>
  );
};
