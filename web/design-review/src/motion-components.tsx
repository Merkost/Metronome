import { createContext, useContext, useId, useLayoutEffect, useRef, useState, type ReactNode } from "react";
import { AnimatePresence, LayoutGroup, MotionConfig, animate, motion, useIsPresent, useMotionValue, useReducedMotion, useTransform, type HTMLMotionProps } from "motion/react";
import { ChevronDown } from "lucide-react";
import { appMotion, appScale, appDistance, appVisibility, instant, reducedFeedback } from "./app-motion";

const MotionPreference = createContext(false);
export function useAppMotion() { return useContext(MotionPreference); }
export function AppMotionProvider({ reduced, children }: { reduced: boolean; children: ReactNode }) {
  const preferReduced = Boolean(useReducedMotion()) || reduced;
  return <MotionPreference.Provider value={preferReduced}><MotionConfig reducedMotion={preferReduced ? "always" : "never"} transition={appMotion.standard}>{children}</MotionConfig></MotionPreference.Provider>;
}
export function Pressable({ children, feedback = "surface", transition = appMotion.press, ...props }: HTMLMotionProps<"button"> & { feedback?: "subtle" | "surface" | "control" }) {
  const reduced = useAppMotion();
  return <motion.button {...props} data-motion-press={feedback} whileTap={reduced ? undefined : { scale: appScale[feedback], transition: appMotion.press }} transition={transition}>{children}</motion.button>;
}
export function PressableLink({ children, feedback = "surface", transition = appMotion.press, ...props }: HTMLMotionProps<"a"> & { feedback?: "subtle" | "surface" | "control" }) {
  const reduced = useAppMotion();
  return <motion.a {...props} data-motion-press={feedback} whileTap={reduced ? undefined : { scale: appScale[feedback], transition: appMotion.press }} transition={transition}>{children}</motion.a>;
}
export function AnimatedReveal({ show, id, children, className = "" }: { show: boolean; id?: string; children: ReactNode; className?: string }) {
  const reduced = useAppMotion(); const generatedId = useId(); const panelId = id ?? generatedId; const panel = useRef<HTMLDivElement>(null); const lastContent = useRef(children);
  if (show || (children !== null && children !== undefined && children !== false)) lastContent.current = children;
  useLayoutEffect(() => {
    if (show || !panel.current?.contains(document.activeElement)) return;
    const trigger = Array.from(document.querySelectorAll<HTMLElement>("[aria-controls]")).find(element => element.getAttribute("aria-controls") === panelId);
    if (trigger) trigger.focus(); else (document.activeElement as HTMLElement)?.blur();
  }, [show, panelId]);
  return <motion.div ref={panel} id={panelId} className="motion-reveal" data-motion-reveal={show ? "open" : "closed"} aria-hidden={!show} inert={!show} initial={false} animate={{ height: show ? "auto" : 0, opacity: show ? 1 : 0 }} transition={{ height: reduced ? instant : appMotion.emphasized, opacity: reduced ? reducedFeedback : appMotion.emphasized }}><motion.div className={`motion-reveal-content ${className}`} initial={false} animate={{ transform: `translateY(${reduced || show ? 0 : -appDistance.reveal}px)` }} transition={reduced ? instant : appMotion.emphasized}>{show ? children : lastContent.current}</motion.div></motion.div>;
}
export function DisclosureChevron({ open, size = 20 }: { open: boolean; size?: number }) {
  const reduced = useAppMotion();
  return <motion.span className="motion-chevron" aria-hidden="true" initial={false} animate={{ transform: `rotate(${open ? 180 : 0}deg)` }} transition={reduced ? instant : appMotion.emphasized}><ChevronDown size={size}/></motion.span>;
}
function BoundedSwap({ identity, children, className = "", direction = 0, initialEmpty = false }: { identity: string | number; children: ReactNode; className?: string; direction?: number; initialEmpty?: boolean }) {
  const reduced = useAppMotion();
  const [frames, setFrames] = useState([{ identity: initialEmpty ? null : identity, content: initialEmpty ? null : children }, { identity: null as string | number | null, content: null as ReactNode }]);
  const content = useRef(frames); const previous = useRef({ identity: initialEmpty ? null : identity, reduced });
  const opacityA = useMotionValue(initialEmpty ? 0 : 1); const opacityB = useMotionValue(0); const yA = useMotionValue(0); const yB = useMotionValue(0); const scaleA = useMotionValue(1); const scaleB = useMotionValue(1);
  const transformA = useTransform([yA, scaleA], ([y, scale]: number[]) => `translateY(${y}px) scale(${scale})`); const transformB = useTransform([yB, scaleB], ([y, scale]: number[]) => `translateY(${y}px) scale(${scale})`);
  const controls = useRef<ReturnType<typeof animate>[]>([]);
  useLayoutEffect(() => {
    if (previous.current.identity === identity && previous.current.reduced === reduced) return;
    const layers = [{ opacity: opacityA, y: yA, scale: scaleA }, { opacity: opacityB, y: yB, scale: scaleB }];
    controls.current.forEach(control => control.stop());
    let incoming = content.current.findIndex(frame => frame.identity === identity);
    if (incoming < 0) {
      incoming = opacityA.get() >= opacityB.get() ? 1 : 0;
      content.current = content.current.map((frame, index) => index === incoming ? { identity, content: children } : frame);
      layers[incoming].opacity.jump(0); layers[incoming].y.jump(reduced ? 0 : direction * appDistance.number); layers[incoming].scale.jump(reduced || direction ? 1 : appScale.enter);
      setFrames(content.current);
    }
    if (previous.current.identity !== identity && layers[incoming].opacity.get() <= appVisibility.hiddenThreshold) { layers[incoming].y.jump(reduced ? 0 : direction * appDistance.number); layers[incoming].scale.jump(reduced || direction ? 1 : appScale.enter); }
    controls.current = layers.flatMap((layer, index) => {
      const active = index === incoming;
      if (reduced && direction) { layer.opacity.jump(active ? 1 : 0); layer.y.jump(0); layer.scale.jump(1); return []; }
      return [animate(layer.opacity, active ? 1 : 0, reduced ? reducedFeedback : active ? appMotion.standard : appMotion.quick), animate(layer.y, reduced || active ? 0 : -direction * appDistance.number, reduced ? instant : active ? appMotion.standard : appMotion.quick), animate(layer.scale, reduced || active || direction ? 1 : appScale.exit, reduced ? instant : active ? appMotion.standard : appMotion.quick)];
    });
    previous.current = { identity, reduced };
  }, [identity, children, direction, reduced, opacityA, opacityB, yA, yB, scaleA, scaleB]);
  useLayoutEffect(() => () => { controls.current.forEach(control => control.stop()); previous.current.identity = null; }, []);
  const layers = [{ opacity: opacityA, y: yA, scale: scaleA }, { opacity: opacityB, y: yB, scale: scaleB }];
  return <span className={`motion-swap ${className}`} data-swap-value={identity}><span className="motion-swap-sizer">{children}</span>{frames.map((frame, index) => <motion.span key={index} className="motion-swap-frame" aria-hidden="true" inert style={{ opacity: layers[index].opacity, transform: reduced ? "none" : index === 0 ? transformA : transformB }}>{frame.identity === identity ? children : frame.content}</motion.span>)}</span>;
}
export function AnimatedSwap({ identity, children, className = "", direction = 0, continuous = false }: { identity: string | number; children: ReactNode; className?: string; direction?: number; continuous?: boolean }) {
  if (continuous) return <span className={`motion-swap ${className}`}><span className="motion-swap-static">{children}</span></span>;
  return <BoundedSwap identity={identity} direction={direction} className={className}>{children}</BoundedSwap>;
}
export function AnimatedNumber({ value }: { value: number }) {
  const previous = useRef(value); const width = useRef(String(value).length); const previousWidth = width.current; const nextWidth = Math.max(previousWidth, String(value).length);
  const direction = Math.sign(value - previous.current); const digits = String(value).padStart(nextWidth, " ").split("");
  useLayoutEffect(() => { previous.current = value; width.current = nextWidth; }, [value, nextWidth]);
  return <span className="motion-number" data-number-value={value} data-motion-preset="standard"><span className="motion-number-sizer">{value}</span><span className="motion-number-digits" aria-hidden="true">{digits.map((digit, index) => { const place = nextWidth - index - 1; return <span className="motion-digit-place" key={place} data-digit-place={place} data-digit-value={digit.trim()}><BoundedSwap identity={digit} direction={direction || 1} initialEmpty={place >= previousWidth} className="motion-digit">{digit}</BoundedSwap></span>; })}</span></span>;
}
export function AnimatedChoices({ values, selected, onChange, className = "choices" }: { values: string[]; selected: string; onChange: (value: string) => void; className?: string }) {
  const id = useId(); const reduced = useAppMotion();
  return <LayoutGroup id={id}><div className={className}>{values.map(value => <Pressable key={value} initial={false} animate={{ "--choice-selected": value === selected ? 1 : 0 }} transition={reduced ? reducedFeedback : appMotion.emphasized} className={value === selected ? "selected" : ""} aria-pressed={value === selected} onClick={() => onChange(value)}>{value === selected && <motion.span aria-hidden="true" className="choice-fill" layoutId="selection" transition={reduced ? instant : appMotion.emphasized}/>}<span className="choice-label">{value}</span></Pressable>)}</div></LayoutGroup>;
}
export function AnimatedItem({ children, className = "" }: { children: ReactNode; className?: string }) {
  const reduced = useAppMotion(); const present = useIsPresent();
  return <motion.div className={`motion-item ${className}`} aria-hidden={!present || undefined} inert={!present} layout={reduced ? false : "position"} initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }} exit={{ height: 0, opacity: 0 }} transition={{ height: reduced ? instant : appMotion.emphasized, opacity: reduced ? reducedFeedback : appMotion.quick, layout: reduced ? instant : appMotion.emphasized }}><div className="motion-item-content">{children}</div></motion.div>;
}
export function AnimatedToast({ message }: { message: string }) {
  const reduced = useAppMotion();
  return <AnimatePresence>{message && <motion.div key={message} className="toast" role="status" initial={{ opacity: 0, y: reduced ? 0 : appDistance.reveal, scale: reduced ? 1 : appScale.enter }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, y: reduced ? 0 : appDistance.toastExit, scale: reduced ? 1 : appScale.exit }} transition={reduced ? reducedFeedback : appMotion.standard}>{message}</motion.div>}</AnimatePresence>;
}

export function AnimatedTile({ children, className = "" }: { children: ReactNode; className?: string }) {
  const reduced = useAppMotion(); const present = useIsPresent();
  return <motion.div className={className} aria-hidden={!present || undefined} inert={!present} layout={reduced ? false : "position"} initial={{ opacity: 0, scale: reduced ? 1 : appScale.enter }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0, scale: reduced ? 1 : appScale.exit }} transition={reduced ? reducedFeedback : appMotion.emphasized}>{children}</motion.div>;
}
