# Settings & Tempo-Sheet UI Bugfixes — Fix Spec & Post-Mortem

Date: 2026-06-12
Status: Fix applied, pending on-device confirmation
Branch: feature/practice-tools-redesign

## Summary

Two visual defects were found by running the app after the iOS settings-parity
work landed:

1. **(Primary) Native `UISwitch` clipped on the right** in every settings toggle —
   the switch's right edge is cut and it has no breathing room from the item end.
   This was *introduced* by the new `PlatformSwitch` iOS actual.
2. **(Secondary) Missing vertical gap in the tempo sheet** — the "Save …" chip in
   `SavedTemposRow` butts directly against the preset chips above it.

Both are pure layout/rendering defects. Neither was caught before release-to-user
because the only gate that could catch them — on-device visual QA — had been
explicitly deferred.

---

## Bug 1 — Native `UISwitch` clipped (primary)

### Symptom
On iOS, the settings toggles (Haptic, Color Flash, Keep Screen Awake, Count-in,
Background Play) render with the switch's right edge cut off and flush against the
item's trailing edge.

### Faulty implementation
`PlatformSwitch` (iOS actual) embedded the `UISwitch` in a `UIKitView` sized to the
control's exact intrinsic dimensions:

```kotlin
modifier = Modifier.size(width = 51.dp, height = 31.dp)
```

### Why it failed (root cause)
- **Zero-margin interop box + clipping.** `UIKitView` hosts the native control in an
  interop container whose frame matches the Compose node. Sized to the *exact*
  intrinsic `51×31`, there is no margin. The native control draws its rounded track
  with sub-pixel anti-aliasing at the very edge of that box; with the interop
  container clipping to its bounds, the rightmost rounded edge is shaved — read by
  the user as "cut."
- **No trailing inset.** As the trailing child of a `SpaceBetween` row the switch
  sat flush to the content's right edge. The Material `Switch` it replaced carried
  internal component padding / a larger touch footprint that masked the absence of
  an explicit inset; the bare `UISwitch` exposed it.
- **Wrong assumption in the plan.** The implementation plan specified the intrinsic
  size on the assumption that "a native control sized to its intrinsic dimensions
  renders fully." That assumption ignored the interop layer's clipping behaviour.

### Why the process didn't catch it
- The two-stage review was **code-level**: it correctly caught a behavioural defect
  (the off→on animation on every appearance) but a clipped-edge is only visible when
  rendered.
- **On-device QA was explicitly deferred** ("can't render from a framework link"),
  so the one gate that would have surfaced this never ran before the user saw it.
- Takeaway: any change that introduces a *native interop view* must be treated as
  requiring a visual pass before it is considered done — a compile/link is not
  sufficient evidence for interop rendering.

### Fix (applied)
```kotlin
modifier = Modifier
    .padding(end = 6.dp)
    .size(width = 56.dp, height = 31.dp)
```
- `56.dp` width gives the `51` control horizontal slack so it is not clipped at its
  bounds; `31.dp` height is kept exact to preserve vertical centring.
- `padding(end = 6.dp)` gives the trailing breathing room the user asked for.

### Fallback if clipping persists on device
If a few dp of slack is insufficient (i.e. the interop container still crops the
control), wrap the `UISwitch` in a plain `UIView` container with the switch added as
a centred subview at its intrinsic size and `clipsToBounds = false`. The container
fills the interop box; the control is never cropped regardless of box size. This is
more code (frame/centring management in the `update` block) and is only warranted if
the simpler slack-based fix proves insufficient.

### Verification
- Compile/link: `:androidApp:assembleDebug` ✅, `:shared:linkDebugFrameworkIosSimulatorArm64` ✅ (warnings only).
- **Device (required):** the switch renders fully (no clipped edge), has trailing
  inset, stays vertically centred, and `onTintColor` matches the active scheme.

---

## Bug 2 — Missing vertical gap in the tempo sheet (secondary)

### Symptom
In the tempo sheet, the "Save {tempo}" chip in `SavedTemposRow` has no vertical
spacing from the preset chips (`FlowRowPresets`) above it; they touch when the
presets wrap.

### Why it failed (root cause)
The sheet's outer `Column` (`TempoTrainerSheet.kt`) had no `verticalArrangement`
and no explicit spacer between `FlowRowPresets` and `SavedTemposRow`:

```kotlin
Column {
    FlowRowPresets(...)
    SavedTemposRow(...)   // <- no gap above this
    Spacer(Modifier.height(spacingSmall))
    HorizontalDivider()
    ...
}
```

Each `FlowRow` manages only its **own internal** wrap spacing
(`verticalArrangement = spacedBy(spacingSmall)`); the gap *between* the two rows was
never set. The earlier `df2d400` ("refined chip padding") tuned the chips' internal
padding — which is why the defect "still" existed: internal chip padding was a
different concern from the inter-row gap, which was never touched.

### Fix (applied)
Insert a spacer between the two rows:
```kotlin
FlowRowPresets(...)
Spacer(Modifier.height(spacingSmall))
SavedTemposRow(...)
```

### Verification
- Compile: `:androidApp:assembleDebug` ✅ (common code).
- **Device/visual:** clear gap between preset chips and the saved/Save chips.

---

## Files touched
- `shared/src/iosMain/kotlin/com/merkost/metronome/screens/PlatformSettingsComponents.ios.kt`
  — `PlatformSwitch` box width + end inset (import `layout.padding`).
- `shared/src/commonMain/kotlin/com/merkost/metronome/screens/TempoTrainerSheet.kt`
  — spacer between `FlowRowPresets` and `SavedTemposRow`.

## Process follow-up
Add "native interop views require a visual/device pass before done" to the iOS
interop checklist; a compile/link is necessary but not sufficient for any
`UIKitView`-hosted control. See [[ios-system-volume-kvo]].
