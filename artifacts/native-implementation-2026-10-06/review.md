# Native implementation review — 6 October 2026

Read-only source review of the integrated working tree against the approved `web/design-review/src/Prototype.tsx` and repository constraints. No unresolved P1 or P2 findings remain in the reviewed scope. This is source review, not native visual or physical-device acceptance.

## Corrected findings verified in source

- Main passes the raw beat index, so count-in does not create a phantom active ring. Accent, normal and mute remain independent of the playback ring.
- The custom slider mirrors its complete Canvas track in RTL, including fill, centered pan origin and ticks.
- Main awaits routine pause before auditioning, bounds the wait, and cancels pending previews on selection, dismissal, disposal and background transitions.
- Onboarding hydration preserves the saved click, exposes a retry after required-read failure, and isolates optional What's New loading. Completion waits for persistence, and backgrounding cancels requested automatic playback.
- Preview uses separate platform players and focus controllers. Async load failures reach visible error text and stop the audition; preview does not update practice time.
- Android export regenerates contents after Activity recreation if the prepared snapshot was lost, avoiding a silent empty backup. Android import reads bounded chunks off the UI thread; iOS import preflights size and performs a bounded read off the UI thread.

## Integration review

Practice now exposes actual preset/set counts, timer duration, Today, Streak, Total and the practice-data entry. Rhythm includes count-in. Welcome's sound library previews at its displayed 100 BPM, while pace previews use the chosen pace.

The starter preview does not persist data. Editing keeps candidate presets local; Save validates limits and references, reuses matching saved setups, adds only required candidate presets, and commits presets plus the routine in one DataStore edit. Failure leaves both collections unchanged.

Backup decode validates size, framing, records, duplicate IDs, limits and preset references. Additive import remaps colliding IDs and set references and writes both collections together. It preserves current setups.

The automatic What's New gate requires the installed version to match notes version 1.4.0. Current Android catalog and iOS Info.plist both identify 1.4.0, so manual sheet labels match the highlights. No version bump or publication was performed by this review.

## Evidence and acceptance limits

Existing XML reports inspected from the implementer's test run show zero failures/errors for onboarding (8), preview (11), starter repository (5), backup codec/repository (14), and What's New policy/coordinator (11). `git diff --check` passed during review. This reviewer ran no builds or tests; the implementation agent owns the final compile and link evidence.

Physical Android and iOS acceptance remains open: first-use Start/Skip and saved-once behavior after relaunch; native sound load/loudness/timing, stereo and audio interruptions; background/foreground transitions; real Files/document-provider export/import including rotation and cancellation; VoiceOver/TalkBack labels and actions; large text, reduced motion, RTL, touch targets, keyboard and safe-area layouts. Simulator/framework/Android builds and source review do not establish those results or authorize store publication.

## Addendum — final visual and transition corrections

Re-reviewed the final visual changes. Main's rhythm pill now has one disclosure icon, and Welcome's rounded CTA and mint selection use shared semantic theme roles. Digit direction is remembered for each value change so unrelated recomposition cannot reverse a decrement transition. The active ring snaps to the first valid beat after a hidden/count-in state, then resumes its normal spring movement.

The theme contrast guard composites paired backgrounds over the animated surface, preserves readable foregrounds, checks target semantic alternatives against the 4.5 threshold, and uses centrally defined exact dark/light foreground roles only when neither alternative qualifies. This closes the dynamic-neutral transition gap found during this addendum review. Custom light/dark surface foreground roles are defined centrally. Three meaningful helper tests cover appearance reversal, preservation of readable brand color, and insufficient dynamic-neutral alternatives; their source was inspected, and their final execution belongs to the implementation agent's pending final run.

No unresolved P1 or P2 issues were found in these corrections. `git diff --check` passed again. The physical-device acceptance limits above still apply.

## Final note — preview output and Welcome spacing

Settings now updates audition volume/pan through `updateOutput`, which changes the following clicks without reinitializing the player, restarting the four-beat bar, or reacquiring audio focus. Choosing another sound stops the current audition first. The regression checks all four clicks, one focus request/release, one player release and the unchanged 2,400 ms bar duration. The inspected final preview report has 12 tests and the theme report has 3 tests, both with zero failures/errors. The implementation validation artifact records 182 shared tests with zero failures/errors plus Android, iOS simulator/device source and Wasm compilation, linked simulator framework and a built/launched iOS simulator app.

Welcome's main vertical spacing now uses the shared 16 dp token; its scroll container and inset handling remain intact. No new P1/P2 concerns were found in either change. This final note supersedes the earlier pending status for the theme tests. No builds or tests were executed by this reviewer, and physical-device acceptance remains open.
