import { loadFont } from "@remotion/google-fonts/Outfit";

const outfit = loadFont("normal", {
  weights: ["400", "500", "600", "700", "800"],
  subsets: ["latin"],
});

export const FONT = outfit.fontFamily;

export const W = 886;
export const H = 1920;
export const FPS = 30;
export const BPM = 120;
export const BEAT = (60 / BPM) * FPS;
export const BAR = BEAT * 4;

export const INK = "#FFFFFF";
export const INK_SOFT = "#E6E6E6";
export const INK_MUTED = "#8C8C92";
export const GROUND = "#0A0A0A";

export type Hue = { accent: string; card: string };

export const VIOLET: Hue = { accent: "#B89FFF", card: "#E6DEFA" };
export const MINT: Hue = { accent: "#9EFFAE", card: "#E1F4E5" };
export const BLUE: Hue = { accent: "#C9D5FE", card: "#E5EAF9" };
export const PINK: Hue = { accent: "#FFCAEA", card: "#F9E5EF" };

export const MARGIN = 64;
