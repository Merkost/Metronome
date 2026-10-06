const spring = (stiffness: number, ratio: number) => ({ type: "spring" as const, stiffness, damping: 2 * ratio * Math.sqrt(stiffness), mass: 1 });
export const appMotion = {
  press: spring(1600, 1),
  quick: spring(1100, 1),
  standard: spring(700, 1),
  emphasized: spring(340, .94),
  calm: spring(190, 1),
  expressive: spring(520, .66),
  navigation: spring(400, 1)
};
export const appScale = { subtle: .985, surface: .975, control: .94, enter: .96, exit: .98, pulse: 1.04, beat: 1.15, slider: 1.12 };
export const reducedFeedback = { duration: .12, ease: "easeOut" as const };
export const instant = { duration: 0 };
export const appDistance = { reveal: 12, number: 24, toastExit: 4, sliderHint: 6 };
export const appVisibility = { hiddenThreshold: .001 };
export const appShape = { rest: 43, playing: 26 };
