# Sound preview layout review — 2026-10-07

**Source, visual and runtime verdict: approved. No remaining actionable findings.**

| Before | After | Why |
| --- | --- | --- |
| Changing Preview to Stop resized the button and reflowed the weighted sound picker; the baseline moved its edge 22px normally and 44px at 200% text. | Shared memoized metrics reserve the widest label, its measured height and a fixed icon slot. Label/icon transitions and press/focus feedback preserve the outer bounds. | Starting, stopping and automatically finishing a preview leave the button, sound text, panel and following controls in place. |
| The horizontal layout split Studio and electronic within words at narrow width with large RTL text. | A breakpoint using every sound's text requirements and the shared preview width stacks the picker and preview when needed. | Full words remain readable at the requested font size; selected sound and preview state cannot change layout mode. |

Measurement and rendering consume the same label style and ceil native text dimensions. The control retains its minimum 48dp target, Material colour roles and consistent disabled foreground alpha. Animated child labels/icons clear their semantics, while one button exposes only the current action. The Square icon matches Stop; the picker chevron mirrors in RTL. The original audio callback and playback-disable gate are preserved.

The four saved captures match the inspected images byte for byte. Idle and active bounds visibly match in [normal light](light-idle.png) and [200% dark RTL](2xfont-dark-rtl-idle.png), including their [light](light-active.png) and [RTL](2xfont-dark-rtl-active.png) active states. Text and surrounding controls remain intact; Studio stays on one line and electronic wraps only between words.

The final log reports `BUILD SUCCESSFUL in 1m 28s`. Fresh XML independently confirms 215 native tests, 182 Android shared tests and all 33 native rendering cases passed with zero failures, errors or skips. Android debug build, iOS arm64 compilation and Wasm compilation passed. The three new preview cases cover start/stop, automatic finish, rapid reversals, single callbacks, disabled playback, current-only accessibility actions, stable geometry and readable 200% dark RTL text.

All three source hashes match `validation.json`; temporary exporters are absent and `git diff --check` passed. This change and its new acceptance evidence concern UI layout. Native speaker/Bluetooth audition and physical accessibility acceptance were not rerun; the audio controller and icon-only picker previews remain unchanged.
