# Stable click-sound preview

The preview row sized itself to Preview and then the shorter Stop label. Its edge moved 22px at normal text and 44px at 200% text, changing the neighbouring weighted sound selector and rewrapping its description. All three new native geometry cases failed before the fix.

A reusable SoundPreviewButton now reserves the widest label and fixed icon slot. One shared memoized metrics result provides the exact rendered typography, ceil text dimensions and total width to both the button and its adaptive parent. Label/icon transitions use the existing motion tokens; press/focus feedback changes colour without scaling the control. The active square icon matches the existing Stop action and sound picker. Only the current action is exposed to accessibility during outgoing/incoming text overlap.

At constrained widths and larger text, the sound selector and button stack without reducing font size or splitting words. The breakpoint considers all sound names/descriptions and the shared preview width, so neither selected sound nor preview state changes layout mode. The original preview callback, main-playback gate and audio controller are unchanged.

Final verification: 215 native iOS simulator tests and 182 Android shared tests passed with zero failures/errors/skips. Android debug build, iOS arm64 and Wasm compilation passed. Three new preview cases cover start/stop, automatic finish, rapid reversals, single callbacks, disabled playback, current accessibility action, invariant button/sound text/panel/following-content bounds, and readable 200% dark RTL text. All 33 native rendering cases passed.

Four native raster captures show idle and active production panels at normal light and 200% dark RTL. Temporary capture helpers are absent from committed source. Native speaker/Bluetooth or physical accessibility acceptance was not rerun for this UI-only change.
