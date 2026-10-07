import { Easing, interpolate, spring } from "remotion";
import { FPS } from "./theme";

export const clamp = (v: number, lo = 0, hi = 1) => Math.min(hi, Math.max(lo, v));

const appSpring = (stiffness: number, ratio: number) => (frame: number, delay = 0, durationInFrames?: number) =>
  spring({
    frame: frame - delay,
    fps: FPS,
    config: { stiffness, damping: 2 * ratio * Math.sqrt(stiffness), mass: 1, overshootClamping: false },
    durationInFrames,
  });

export const quick = appSpring(1100, 1);
export const snappy = appSpring(700, 1);
export const emphasized = appSpring(340, 0.94);
export const calm = appSpring(190, 1);
export const expressive = appSpring(520, 0.66);

export const easeOut = Easing.bezier(0.16, 1, 0.3, 1);
export const easeIn = Easing.bezier(0.7, 0, 0.84, 0);
export const easeInOut = Easing.bezier(0.65, 0, 0.35, 1);

export const ramp = (
  frame: number,
  from: number,
  to: number,
  easing: (t: number) => number = easeOut,
) =>
  interpolate(frame, [from, to], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing,
  });

export const mix = (a: number, b: number, t: number) => a + (b - a) * t;

export const pulse = (frame: number, at: number, decay = 9) => {
  const d = frame - at;
  if (d < 0) return 0;
  return Math.exp(-d / (decay / 3));
};
