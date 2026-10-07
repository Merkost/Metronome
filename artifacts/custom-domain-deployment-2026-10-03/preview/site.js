import {
  animate,
  reveal,
  pulse,
  pressable,
  Distances,
  Scales,
} from "./motion.js";
const root = document.documentElement;
const dark = matchMedia("(prefers-color-scheme: dark)");
const themeButtons = document.querySelectorAll(".theme-toggle");
function appearance() {
  return root.dataset.appearance
    ? root.dataset.appearance === "dark"
    : dark.matches;
}
function themeLabels() {
  for (const button of themeButtons) {
    button.setAttribute(
      "aria-label",
      `Use ${appearance() ? "light" : "dark"} appearance`,
    );
    button.setAttribute("aria-pressed", String(appearance()));
  }
}
try {
  const saved = localStorage.getItem("metronome-site-appearance");
  if (["light", "dark"].includes(saved)) root.dataset.appearance = saved;
} catch {}
for (const button of themeButtons)
  button.addEventListener("click", () => {
    root.dataset.appearance = appearance() ? "light" : "dark";
    try {
      localStorage.setItem(
        "metronome-site-appearance",
        root.dataset.appearance,
      );
    } catch {}
    themeLabels();
  });
dark.addEventListener("change", themeLabels);
themeLabels();
document
  .querySelectorAll(".button,.icon-button,.swatch,.sound-choice,.studio-card")
  .forEach((element) =>
    pressable(
      element,
      element.classList.contains("button") ? Scales.subtle : Scales.control,
    ),
  );
for (const item of document.querySelectorAll(".faq-item")) {
  const summary = item.querySelector("summary"),
    answer = item.querySelector(".faq-answer"),
    icon = summary.querySelector(".icon");
  let open = item.open,
    revision = 0;
  summary.setAttribute("aria-expanded", String(open));
  summary.addEventListener("click", (event) => {
    event.preventDefault();
    const id = ++revision;
    open = !open;
    const start = item.open ? answer.getBoundingClientRect().height : 0;
    item.open = true;
    summary.setAttribute("aria-expanded", String(open));
    answer.style.height = "auto";
    const end = open ? answer.scrollHeight : 0;
    answer.style.height = `${start}px`;
    animate(
      icon,
      { transform: open ? "rotate(0deg)" : "rotate(45deg)" },
      { transform: open ? "rotate(45deg)" : "rotate(0deg)" },
      "standard",
    );
    animate(
      answer,
      { height: `${start}px`, opacity: open ? 0 : 1 },
      { height: `${end}px`, opacity: open ? 1 : 0 },
      "emphasized",
      "reveal",
    ).then(() => {
      if (id === revision) {
        item.open = open;
        answer.style.height = "auto";
      }
    });
  });
}
const tabs = [...document.querySelectorAll(".tool-tab")];
function selectTab(index) {
  for (const [i, tab] of tabs.entries()) {
    tab.setAttribute("aria-selected", String(i === index));
    tab.tabIndex = i === index ? 0 : -1;
    const panel = document.getElementById(tab.getAttribute("aria-controls"));
    panel.hidden = i !== index;
    if (i === index) reveal(panel);
  }
  const indicator = document.querySelector(".tab-indicator");
  animate(
    indicator,
    { transform: getComputedStyle(indicator).transform },
    { transform: `translateX(${index * 100}%)` },
    "emphasized",
  );
}
for (const [index, tab] of tabs.entries()) {
  tab.addEventListener("click", () => selectTab(index));
  tab.addEventListener("keydown", (event) => {
    let next = index;
    if (event.key === "ArrowRight") next = (index + 1) % tabs.length;
    else if (event.key === "ArrowLeft")
      next = (index + tabs.length - 1) % tabs.length;
    else if (event.key === "Home") next = 0;
    else if (event.key === "End") next = tabs.length - 1;
    else return;
    event.preventDefault();
    selectTab(next);
    tabs[next].focus();
  });
}
const swatches = document.querySelectorAll(".swatch");
for (const swatch of swatches)
  swatch.addEventListener("click", () => {
    for (const s of swatches)
      s.setAttribute("aria-pressed", String(s === swatch));
    document
      .querySelector(".theme-card")
      .style.setProperty("--brand", swatch.style.getPropertyValue("--swatch"));
    document
      .querySelector(".beat-lab")
      .style.setProperty("--brand", swatch.style.getPropertyValue("--swatch"));
    document.getElementById("theme-name").textContent =
      `${swatch.dataset.name} · preview`;
    document.querySelector(".theme-card-name").textContent =
      swatch.dataset.name;
    document
      .querySelectorAll(".colour-preview span")
      .forEach((element) => pulse(element, 1.04));
  });
const lab = document.querySelector(".beat-lab");
if (lab) {
  const slider = lab.querySelector("input[type=range]"),
    readout = lab.querySelector(".tempo-readout"),
    thumb = lab.querySelector(".slider-thumb"),
    play = lab.querySelector(".demo-play"),
    playLabel = lab.querySelector(".play-label"),
    marking = lab.querySelector(".tempo-marking"),
    error = lab.querySelector(".preview-error");
  let tempo = 80,
    sound = "wood",
    playing = false,
    loading = false,
    context,
    timer,
    next = 0,
    beat = 0,
    generation = 0;
  const buffers = new Map(),
    sources = new Set(),
    visuals = new Set();
  function number(value) {
    const previous = String(tempo).padStart(3, " "),
      current = String(value).padStart(3, " "),
      up = value >= tempo;
    [...readout.children].forEach((slot, index) => {
      if (previous[index] === current[index]) return;
      const old = slot.lastElementChild;
      [...slot.children]
        .filter((child) => child !== old)
        .forEach((child) => child.remove());
      const fresh = document.createElement("span");
      fresh.textContent = current[index];
      slot.append(fresh);
      animate(
        old,
        { opacity: 1, transform: "translateY(0px)" },
        {
          opacity: 0,
          transform: `translateY(${up ? -Distances.digit : Distances.digit}px)`,
        },
        "quick",
      ).then(() => old.remove());
      animate(
        fresh,
        {
          opacity: 0,
          transform: `translateY(${up ? Distances.digit : -Distances.digit}px)`,
        },
        { opacity: 1, transform: "translateY(0px)" },
        "standard",
      );
    });
    readout.setAttribute("aria-label", `${value} beats per minute`);
  }
  slider.addEventListener("input", () => {
    const value = Number(slider.value);
    number(value);
    tempo = value;
    const ratio = (value - 40) / 180;
    lab
      .querySelector(".slider")
      .style.setProperty("--progress", `${ratio * 100}%`);
    lab.querySelector(".slider").style.setProperty("--ratio", String(ratio));
    marking.textContent =
      value < 60
        ? "Largo"
        : value < 76
          ? "Adagio"
          : value < 108
            ? "Andante"
            : value < 120
              ? "Moderato"
              : value < 168
                ? "Allegro"
                : "Presto";
  });
  const gripDown = () =>
    animate(
      thumb,
      { transform: "scale(1)" },
      { transform: `scale(${Scales.slider})` },
      "press",
    );
  const gripUp = () =>
    animate(
      thumb,
      { transform: `scale(${Scales.slider})` },
      { transform: "scale(1)" },
      "standard",
    );
  slider.addEventListener("pointerdown", gripDown);
  for (const event of ["pointerup", "pointercancel", "blur"])
    slider.addEventListener(event, gripUp);
  for (const button of lab.querySelectorAll(".sound-choice"))
    button.addEventListener("click", () => {
      sound = button.dataset.sound;
      for (const s of lab.querySelectorAll(".sound-choice"))
        s.setAttribute("aria-pressed", String(s === button));
      if (context) load(sound).catch(() => {});
    });
  async function load(name) {
    if (buffers.has(name)) return buffers.get(name);
    const response = await fetch(
      `/sounds/${name === "classic" ? "metronome.wav" : `${name}.mp3`}`,
    );
    if (!response.ok) throw new Error("Sound unavailable");
    const buffer = await context.decodeAudioData(await response.arrayBuffer());
    buffers.set(name, buffer);
    return buffer;
  }
  function stop() {
    generation++;
    playing = false;
    loading = false;
    clearInterval(timer);
    for (const source of sources)
      try {
        source.stop();
      } catch {}
    sources.clear();
    for (const visual of visuals) clearTimeout(visual);
    visuals.clear();
    lab.dataset.playing = "false";
    playLabel.textContent = "Play a beat";
    play.setAttribute("aria-pressed", "false");
    play.disabled = false;
    lab
      .querySelectorAll(".pulse-dot")
      .forEach((dot) => dot.classList.remove("is-active"));
  }
  function schedule() {
    if (!playing) return;
    while (next < context.currentTime + 0.12) {
      const index = beat % 4,
        buffer = buffers.get(sound);
      if (buffer) {
        const source = context.createBufferSource(),
          gain = context.createGain();
        source.buffer = buffer;
        source.playbackRate.value = index === 0 ? 1.12 : 1;
        gain.gain.value = 0.65;
        source.connect(gain);
        gain.connect(context.destination);
        source.start(next);
        sources.add(source);
        source.onended = () => sources.delete(source);
      }
      const tickGeneration = generation;
      const visual = setTimeout(
        () => {
          visuals.delete(visual);
          if (!playing || tickGeneration !== generation) return;
          lab.querySelectorAll(".pulse-dot").forEach((dot, i) => {
            dot.classList.toggle("is-active", i === index);
            if (i === index) pulse(dot);
          });
        },
        Math.max(0, (next - context.currentTime) * 1000),
      );
      visuals.add(visual);
      next += 60 / tempo;
      beat++;
    }
  }
  play.addEventListener("click", async () => {
    if (playing || loading) {
      stop();
      return;
    }
    const id = ++generation;
    loading = true;
    error.hidden = true;
    playLabel.textContent = "Loading sound…";
    play.disabled = true;
    try {
      context ??= new (window.AudioContext || window.webkitAudioContext)();
      await context.resume();
      await load(sound);
      if (id !== generation) return;
      loading = false;
      playing = true;
      beat = 0;
      next = context.currentTime + 0.06;
      lab.dataset.playing = "true";
      playLabel.textContent = "Stop preview";
      play.setAttribute("aria-pressed", "true");
      play.disabled = false;
      schedule();
      timer = setInterval(schedule, 25);
    } catch {
      if (id === generation) {
        stop();
        error.hidden = false;
        error.textContent =
          "Sound couldn’t load. Try again, or open the web app.";
      }
    }
  });
  document.addEventListener("visibilitychange", () => {
    if (document.hidden) stop();
  });
  window.addEventListener("pagehide", stop);
  reveal(lab);
}
