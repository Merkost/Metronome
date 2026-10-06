import { useLayoutEffect, useRef } from "react";
import { animate, spring } from "motion";
import { appMotion, reducedFeedback } from "./app-motion";
import { useAppMotion } from "./motion-components";

type Palette = Record<string, string>;
const light: Palette = { surface: "#ffffff", "surface-container": "#f2f2f2", primary: "#161616", "on-primary": "#ffffff", "on-surface": "#161616", "on-surface-variant": "#656565", outline: "#dedede", accent: "#161616" };
const dark: Palette = { surface: "#101010", "surface-container": "#242424", primary: "#f8f8f8", "on-primary": "#111111", "on-surface": "#f8f8f8", "on-surface-variant": "#b5b5b5", outline: "#393939", accent: "#f8f8f8" };
const accents = { Mono: [light.primary, dark.primary], Violet: ["#6f55c8", "#b89fff"], Blue: ["#5b6abf", "#c9d5fe"], Mint: ["#1a8a2e", "#9effae"], Pink: ["#a83b66", "#ffcaea"] };
const appearanceFrames = () => {
  const generator = spring({ ...appMotion.standard, keyframes: [0, 100] }); const frames: Keyframe[] = []; let elapsed = 0;
  for (; elapsed <= 2000; elapsed += 1000 / 60) {
    const sample = generator.next(elapsed); const reveal = Math.max(0, Math.min(100, sample.value));
    frames.push({ clipPath: `inset(0 ${100 - reveal}% 0 0)` });
    if (sample.done) break;
  }
  return { frames, duration: elapsed };
};
const appearance = appearanceFrames();
const channels = (hex: string) => [1, 3, 5].map(offset => parseInt(hex.slice(offset, offset + 2), 16));
const interpolate = (from: string, to: string, progress: number) => `#${channels(from).map((channel, index) => Math.round(channel + (channels(to)[index] - channel) * progress).toString(16).padStart(2, "0")).join("")}`;
export function ThemeMotion({ theme, colour }: { theme: string; colour: string }) {
  const reduced = useAppMotion(); const current = useRef<Palette | null>(null); const inversion = useRef(0); const previousDark = useRef(theme === "Dark");
  useLayoutEffect(() => {
    const isDark = theme === "Dark"; const changedAppearance = previousDark.current !== isDark; previousDark.current = isDark;
    const target = { ...(isDark ? dark : light) };
    target.primary = target.accent = (accents[colour as keyof typeof accents] ?? accents.Mono)[isDark ? 1 : 0];
    document.body.dataset.appColour = colour.toLowerCase(); document.body.dataset.reducedMotion = String(reduced);
    const start = current.current ?? target; const fromInvert = current.current ? inversion.current : Number(isDark);
    const apply = (progress: number) => {
      const next: Palette = {};
      Object.keys(target).forEach(key => { next[key] = interpolate(start[key], target[key], progress); document.body.style.setProperty(`--${key}`, next[key]); });
      current.current = next; inversion.current = fromInvert + (Number(isDark) - fromInvert) * progress;
      document.body.style.setProperty("--theme-invert", String(inversion.current));
      if (progress === 1) document.body.dataset.appTheme = isDark ? "dark" : "light";
    };
    if (!current.current || reduced || (changedAppearance && !document.startViewTransition)) { apply(1); return; }
    if (changedAppearance && document.startViewTransition) {
      let cancelled = false; let animation: Animation | undefined;
      document.body.dataset.appearanceTransition = "true";
      const transition = document.startViewTransition(() => { if (!cancelled) apply(1); });
      transition.ready.then(() => {
        if (cancelled) return;
        animation = document.documentElement.animate(appearance.frames, { duration: appearance.duration, easing: "linear", fill: "both", pseudoElement: "::view-transition-new(app-appearance)" });
      }).catch(() => { if (!cancelled) apply(1); });
      transition.finished.then(() => { if (!cancelled) delete document.body.dataset.appearanceTransition; });
      return () => { cancelled = true; animation?.cancel(); transition.skipTransition(); delete document.body.dataset.appearanceTransition; };
    }
    const controls = animate(0, 1, { ...appMotion.calm, onUpdate: apply, onComplete: () => apply(1) });
    return () => controls.stop();
  }, [theme, colour, reduced]);
  return null;
}
