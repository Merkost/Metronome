import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { AnimatePresence, motion, useMotionValue, useSpring, useTransform } from "motion/react";
import { GripVertical } from "lucide-react";
import { appMotion, appScale, appDistance, reducedFeedback } from "./app-motion";
import { useAppMotion } from "./motion-components";

type BrandedSliderProps = {
  label: string;
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  step?: number;
  unit?: string;
  formatValue?: (value: number) => string;
  endLabels?: [string, string];
  centred?: boolean;
  onDragChange?: (dragging: boolean) => void;
};
export default function BrandedSlider({ label, value, onChange, min = 0, max = 100, step = 1, unit = "", formatValue, endLabels, centred = false, onDragChange }: BrandedSliderProps) {
  const reduced = useAppMotion(); const [dragging, setDragging] = useState(false); const [focused, setFocused] = useState(false);
  const fraction = Math.max(0, Math.min(1, (value - min) / (max - min)));
  const target = useMotionValue(fraction); const progress = useSpring(target, appMotion.standard);
  const rail = useRef<HTMLDivElement>(null); const railWidth = useMotionValue(0);
  const thumbTransform = useTransform([progress, railWidth], ([position, width]: number[]) => `translate(${position * width - 15}px, -50%)`);
  const hintTransform = useTransform([progress, railWidth], ([position, width]: number[]) => `translateX(${Math.max(28, Math.min(width - 28, position * width))}px)`);
  const fillTransform = useTransform(progress, position => `translateY(-50%) scaleX(${centred ? (position - .5) * 2 : position})`);
  const text = formatValue ? formatValue(value) : `${value}${unit ? ` ${unit}` : ""}`;
  useLayoutEffect(() => { if (dragging || reduced) { target.jump(fraction); progress.jump(fraction); } else target.set(fraction); }, [fraction, dragging, reduced, target, progress]);
  useLayoutEffect(() => { if (!rail.current) return; const observer = new ResizeObserver(entries => railWidth.jump(entries[0].contentRect.width)); observer.observe(rail.current); railWidth.jump(rail.current.clientWidth); return () => observer.disconnect(); }, [railWidth]);
  useEffect(() => { onDragChange?.(dragging); }, [dragging, onDragChange]);
  return <div className={`custom-slider ${endLabels ? "with-end-labels" : ""}`} data-scroll-drag="ignore" data-dragging={dragging} data-motion-preset="standard">
    <div ref={rail} className="custom-slider-rail" aria-hidden="true"><span className="custom-slider-track"/><motion.span className="custom-slider-fill" style={{ left: centred ? "50%" : 0, width: centred ? "50%" : "100%", transformOrigin: "left center", transform: fillTransform }}/>{centred && <span className="custom-slider-centre"/>}<motion.span className="custom-slider-thumb-position" style={{ left: 0, transform: thumbTransform }}><motion.span className="custom-slider-thumb" animate={{ transform: `scale(${!reduced && (dragging || focused) ? appScale.slider : 1})` }} transition={appMotion.press}><GripVertical size={15}/></motion.span></motion.span><motion.span className="custom-slider-hint-position" style={{ transform: hintTransform }}><AnimatePresence>{(dragging || focused) && <motion.span className="custom-slider-hint" initial={{ opacity: 0, transform: `translate(-50%, ${reduced ? 0 : appDistance.sliderHint}px)` }} animate={{ opacity: 1, transform: "translate(-50%, 0px)" }} exit={{ opacity: 0, transform: `translate(-50%, ${reduced ? 0 : appDistance.toastExit}px)` }} transition={reduced ? reducedFeedback : appMotion.quick}>{text}</motion.span>}</AnimatePresence></motion.span></div>
    <input className="custom-slider-input" type="range" aria-label={label} aria-valuetext={text} min={min} max={max} step={step} value={value} data-scroll-drag="ignore" onFocus={() => setFocused(true)} onBlur={() => { setFocused(false); setDragging(false); }} onPointerDown={() => setDragging(true)} onPointerUp={() => setDragging(false)} onPointerCancel={() => setDragging(false)} onLostPointerCapture={() => setDragging(false)} onChange={event => { const next = Number(event.target.value); if (dragging || reduced) { const position = (next - min) / (max - min); target.jump(position); progress.jump(position); } onChange(next); }}/>
    {endLabels && <div className="custom-slider-end-labels" aria-hidden="true"><span>{endLabels[0]}</span><span>{endLabels[1]}</span></div>}
  </div>;
}
