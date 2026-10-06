export const Motion = Object.freeze({
  press: { stiffness: 1600, damping: 1 },
  quick: { stiffness: 1100, damping: 1 },
  standard: { stiffness: 700, damping: 1 },
  emphasized: { stiffness: 340, damping: 0.94 },
  calm: { stiffness: 190, damping: 1 },
  expressive: { stiffness: 520, damping: 0.66 },
  navigation: { stiffness: 400, damping: 1 },
});
export const Distances = Object.freeze({ reveal: 12, digit: 24 });
export const Scales = Object.freeze({
  subtle: 0.985,
  surface: 0.975,
  control: 0.94,
  beat: 1.15,
  slider: 1.12,
});
const reduced = matchMedia("(prefers-reduced-motion: reduce)");
const running = new Set();
const tracks = new WeakMap();
const curves = new Map();
export function spring(name = "standard") {
  if (curves.has(name)) return curves.get(name);
  const { stiffness, damping } = Motion[name];
  const omega = Math.sqrt(stiffness),
    values = [];
  for (let time = 0; time <= 1.6; time += 1 / 120) {
    const progress =
      damping < 1
        ? 1 -
          Math.exp(-damping * omega * time) *
            (Math.cos(omega * Math.sqrt(1 - damping * damping) * time) +
              (damping / Math.sqrt(1 - damping * damping)) *
                Math.sin(omega * Math.sqrt(1 - damping * damping) * time))
        : 1 - (1 + omega * time) * Math.exp(-omega * time);
    values.push(progress);
    if (
      time > 0.08 &&
      Math.abs(1 - progress) < 0.0007 &&
      Math.abs(progress - values[values.length - 2]) < 0.0002
    )
      break;
  }
  values[values.length - 1] = 1;
  const result = { values, duration: ((values.length - 1) * 1000) / 120 };
  curves.set(name, result);
  return result;
}
export function animate(
  element,
  from,
  to,
  name = "standard",
  channel = "transform",
) {
  let map = tracks.get(element);
  if (!map) {
    map = new Map();
    tracks.set(element, map);
  }
  const previous = map.get(channel);
  if (previous) {
    const current = getComputedStyle(element);
    from = Object.fromEntries(
      Object.keys(from).map((key) => [key, current[key]]),
    );
    previous.cancel();
    running.delete(previous);
  }
  if (reduced.matches) {
    Object.assign(
      element.style,
      to,
      to.transform?.startsWith("scale(") ? { transform: "none" } : {},
    );
    return Promise.resolve();
  }
  const { values, duration } = spring(name);
  const animation = element.animate([from, to], {
    duration,
    easing: `linear(${values.map((v) => v.toFixed(5)).join(",")})`,
    fill: "both",
  });
  map.set(channel, animation);
  running.add(animation);
  return animation.finished.then(
    () => {
      if (map.get(channel) === animation) {
        Object.assign(element.style, to);
        animation.cancel();
        map.delete(channel);
      }
      running.delete(animation);
    },
    () => {
      running.delete(animation);
    },
  );
}
export function reveal(element) {
  return animate(
    element,
    { opacity: 0, transform: `translateY(${Distances.reveal}px)` },
    { opacity: 1, transform: "translateY(0px)" },
    "emphasized",
  );
}
export function pulse(element, scale = Scales.beat) {
  return animate(
    element,
    { transform: `scale(${scale})` },
    { transform: "scale(1)" },
    "expressive",
  );
}
export function pressable(element, scale = Scales.control) {
  let pressed = false;
  const down = () => {
    pressed = true;
    animate(
      element,
      { transform: "scale(1)" },
      { transform: `scale(${scale})` },
      "press",
    );
  };
  const up = () => {
    if (!pressed) return;
    pressed = false;
    animate(
      element,
      { transform: `scale(${scale})` },
      { transform: "scale(1)" },
      "press",
    );
  };
  element.addEventListener("pointerdown", down);
  for (const event of ["pointerup", "pointercancel", "pointerleave", "blur"])
    element.addEventListener(event, up);
}
reduced.addEventListener("change", () => {
  if (reduced.matches) {
    for (const animation of running)
      try {
        animation.finish();
      } catch {}
    running.clear();
  }
});
document.documentElement.style.setProperty(
  "--color-duration",
  `${Math.round(spring("standard").duration)}ms`,
);
