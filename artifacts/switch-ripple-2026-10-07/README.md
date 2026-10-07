# Rounded settings-switch ripple

The shared SettingsSwitch row applied toggleable without a rounded clip, so its indication painted a rectangle inside the sound card. A clip using the existing cornerRadiusMedium token now precedes toggleable. It wraps the ripple without adding padding, scaling, changing layout, or changing callbacks. The shared fix covers Beat haptics, Count-in, Keep screen awake, Background playback, Beat flash, and the iOS Live Activity row.

A temporary native rendering probe held a real label-area press for 800ms at normal and 200% text. Both baseline probes failed the corner check: the top-left backdrop changed by 0.10196078. After the fix, both probes passed: feedback remained visible inside the row, all four corners retained the backdrop, row bounds stayed fixed, and release toggled exactly once. Titles and subtitles remain intact in both captures.

The temporary probe was removed before final verification. The existing 212 native tests and 182 Android shared tests passed, along with Android debug build, iOS arm64 compilation and Wasm compilation. Captures are production components rendered in native raster fixtures, distinct from full UIKit or physical-device screenshots.
