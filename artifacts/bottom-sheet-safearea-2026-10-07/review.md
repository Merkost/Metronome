# Bottom-sheet safe-area review — 2026-10-07

**Verdict: approved. No actionable production findings.** This review covers the shared safe-area change only.

The public `AppBottomSheet` signature is unchanged. All 12 production callers still use the single wrapper; there is one production `ModalBottomSheet` site.

| Before | After | Why |
| --- | --- | --- |
| Material3 content insets protected text while the sheet surface could reach behind the top cutout. | Top and horizontal `safeDrawing` padding precede the Material3 surface/anchor modifiers. | The surface and available layout constraints respect the safe edges. |
| Material3 spring animations could place the sheet at a negative raw offset. | A standard placement offset cancels only negative rounded pixel offsets. | The guard keeps the surface below its safe top while preserving positive drag/hide travel and the underlying gesture/animation state. |
| The body added navigation-bar padding after Material3 had already consumed bottom insets. | The dialog-local `contentWindowInsets` lambda owns Bottom safe drawing; body navigation padding is removed. | Insets have one owner. The removed padding was normally redundant, rather than evidence of an existing doubled bottom gap. |

Selected Material3 1.9.0 applies the caller modifier before measuring its draggable surface and applies content insets inside the surface. The negative-offset reader is guarded by `hasExpandedState` and evaluated during placement. Bottom insets are evaluated inside the dialog; no extra IME padding is introduced. Shape, typography, 480dp body cap, design spacing, scrolling and dismissal callbacks are preserved.

Independent visual review of accepted UIKit/Metal Simulator captures confirms the baseline surface at y=0 and the fixed iPhone portrait surface at y=186px, below the Island/status region. The final Done action ends at y=2520px, matching the 2622px window minus its 102px bottom safe area. The genuine system accessibility text-size capture reports fontScale 1.8 and retains the final action inside that edge. The iPad capture is portrait and shows a centered sheet/body. The opening measurement contains 32 detected modal frames from 140 recorded frames, with minimum painted top 186px; this is sampled evidence.

Fresh XML independently confirms 219 native tests and 182 Android shared tests passed with zero failures, errors or skips. All 37 rendering cases passed, including four new sheet cases. The saved gate record reports `BUILD SUCCESSFUL in 1m 21s`, covering Android debug, iOS arm64 and Wasm compilation. Source hashes and accepted saved evidence match the reviewed files; `git diff --check` passes. Temporary UIKit launch fixtures and the MainViewController guard are absent from final production source.

The four new tests render the production safe-area helper and body with synthetic insets, simulated anchored placement, semantic bounds and painted-pixel checks. Bounded ScrollBy advances the paused clock and verifies the final action remains inside the content viewport. They also check nested inset consumption, large RTL content, negative-offset correction and changing bottom obstruction ownership. They do not instantiate the real modal dialog; actual-modal evidence is the separate UIKit capture set.

Native software-keyboard behavior remains unverified: hardware-keyboard mode produced IME=0. Native landscape remains unverified because UIWindowScene rejected rotation. The earlier custom-density capture is excluded from accessibility acceptance. No physical-device, exhaustive opening-frame or exhaustive gesture acceptance is claimed. No production edits or Gradle runs were performed by this review.
