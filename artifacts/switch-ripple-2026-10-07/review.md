# Independent source and visual review

Approved with no actionable findings.

| Before | After | Why |
| --- | --- | --- |
| Indication painted an unclipped rectangular row. | Existing rounded clip wraps toggleable. | Feedback follows the branded row shape while preserving geometry and callbacks. |

Modifier order is correct: the rounded layer wraps the indication because it precedes toggleable. The 12dp token matches adjacent settings controls. All switch callers reuse the boundary; child iOS track placement, drag handling, RTL, cancellation, merged semantics and keyboard focus are unchanged.

Before/after captures were inspected at normal and 200% text. Ripple feedback is rounded, text remains intact, and the switch retains its placement. Both temporary probes failed before and passed after the fix. Fresh final reports independently confirm 212 native and 182 Android tests with zero failures/errors/skips and successful Android debug, iOS arm64 and Wasm gates. Temporary capture code is absent from final source.
