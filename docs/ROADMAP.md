# Metronome Roadmap — September 2026 to September 2027

> Status: adopted 2026-09-05. Supersedes the 2026-09-03 roadmap; that document's shipped history is preserved in Appendix C and every one of its open items is accounted for in Appendix B.
> Inputs: `docs/product-review-2026-09.md` (111 verified findings, referenced below by id such as `main#1`, `ios#2`), `docs/competitive-landscape-2026-09.md` (category norms, gap matrix, musician needs), `PRODUCT.md` (principles and non-goals), `DESIGN.md` (design rules), `CHANGELOG.md` (cadence), and the state of `main` on the adoption date.
> Method: three independent roadmap proposals (trust-first, craft-first, market-first) were drafted from the review, sized against the code, and merged by two judges (a solo-developer lens and a musician-and-shopper lens). A separate code-grounded sizing pass estimated every candidate in developer-days and recorded feasibility findings (§12); the merged plan was then reconciled by hand against the repository and that sizing. Effort figures are developer-days for one experienced developer including unit tests and a device check, not store publishing, which is counted in each release's closing item.
> How to use: work top to bottom inside a release; do not pull items forward across releases without moving something back (§7 gives the cut order); tick the exit gates before tagging; re-score the metrics in §3 on the first of every month and at every release; rewrite this document at the 12-month checkpoint (R7-05).

---

## 1. Starting point

**Product.** 1.3.0 (`versionCode 9`) on `main`, which is level with `dev`. 139 shared unit tests pass. Both crash fixes found in Crashlytics (the API < 35 `removeLast` trap and the ViewModel init-order NPE) are on `main`, untagged and unpublished. The site is live at `metronome.merkost.dev` on Cloudflare Pages with brotli, rebuilt on 2026-09-05 in the app's own visual language; the web build is served from `/app/`. Crashlytics is configured on both platforms; Firebase Analytics and Performance are compiled into the Android build with no events defined.

**Market.** Both stores still serve 1.2.1 under the old name *Metronome: Feel the Beat!* with 1.0-era screenshots. Zero App Store ratings in every storefront checked; no rating rendered on Play; `1K+` installs. The app appears in store search for exactly one query, its old name. Play's data-safety form says "No data collected" while the binary ships Firebase. Three product names are in circulation, and the one PRODUCT.md calls "approved next store name" belongs to a live competitor.

**Verdict of the review** (1–10; 5 is a competent generic utility): value 6.5, beauty 6, appeal 5.5, reliability 6, market position 2. Three design critics independently called the UI *clean, not beautiful*. The product is ahead of its distribution, its first-minute breadth, and its own claims, in that order.

**What is already strong and must be protected:** free with no ads, account or paywall on any tool; a drift-free, tested scheduler; Practice Presets and Practice Sets with goals and recovery that no competitor offers free; local stats without an account; iOS Live Activity and Dynamic Island controls; real Android parity from one Kotlin Multiplatform codebase; a disciplined design system.

---

## 2. Strategy

### 2.1 Thesis

The next twelve months turn three deficits into facts in a fixed order, because each one is worth little without the one before it:

1. **Be findable this month.** Every week the corrected listing stays unpublished is a week of zero ratings and no search presence. September is a publish, not a build.
2. **Stop surprising the musician in the first ten minutes.** Tempo that forgets itself, meters that wrap, a Lock Screen that lies, a goal that passes in silence, sounds 16 dB apart. All small, all trust.
3. **Close the table stakes a shopper checks in the first minute.** BPM 20–300, any meter felt the way it is counted, an exact tempo in one gesture, a backup file the musician owns.
4. **Make Android as trustworthy as iOS when the phone locks, and make the beat physical.** The Android service owns playback with lock-screen and headset control; the ball strikes instead of sitting there. This is the release that produces the screenshots the listing converts on.
5. **Earn the precision claim.** Clicks placed on the audio clock on both platforms, measured on real devices before and after, with the method published. Until then the listing says "drift-free scheduling" and nothing more.
6. **Then spend on everyone.** Latency-compensated visuals, Reduce Motion, real semantics, every string a resource, and finally training depth, a sound palette, and the first two languages, all on an engine that has been proven.

Seven releases roughly eight weeks apart, each capped near twenty developer-days of scope, each shipping with a musician-readable What's New, each gated on a physical-device pass. Watches, widgets, polyrhythms, program mode, sync and monetisation stay out of the year by decision, not by neglect (§9).

### 2.2 Positioning

*The free, calm, still-updated metronome that also gives away the structured practice the incumbents charge for.* The vacuum Pulse left when it stopped updating in 2018, filled by an app with the practice depth Pulse never had. Against Soundbrenner (subscription, account, 313 MB), Pro Metronome (features moved behind a paywall, weak Android), Tempo (dated, iOS-first) and Metronome Beats (ads), the durable advantage is honesty: free with everything, local by default, claims that trace to code or to a published measurement.

### 2.3 Pillars and what "done" means in twelve months

| Pillar | Done in twelve months |
|---|---|
| **Trust** | Nothing resets, wraps, lies or stays silent. Background and interruption behaviour matches a written policy on both platforms, verified per release on a named device set. Clicks are scheduled on the audio clock; p95 inter-onset deviation is measured, published with method, and at or below 1.0 ms on Android and 0.5 ms on iOS. Zero unsupported public claims. |
| **Feel** | The beat strikes and returns to rest; the indicator ring lands on the beat at every tempo; captions have a tonal tier; numerals hold still; the first run ends in sound. Design-critic median at or above 8 on the main screen with at least one component that identifies the app with the name covered. |
| **Depth** | Tempo 20–300; any meter 1–16 over /2 /4 /8 /16 with a compound pulse option; exact tempo by hold, type or tap; random, per-beat and progressive gap training; ramps by time with hold and loop; swing and dotted feels; eight loudness-matched sound sets with real accent voices. |
| **Reach** | Lock-screen, notification and headset control on Android; hardware keys so pedals work as keyboards; Spanish and German shipped after native review. Watch and widgets decided and scheduled for the following year with hardware in hand. |
| **Distribution** | One name on both stores and the site; six honest frames; a branded feature graphic; a reconciled data-safety form; every release live on both stores within 14 days of its tag; 120 ratings per store at an average of 4.6 or better. |
| **Foundations** | Engine composition under virtual-time test; a release checklist and an on-device verification protocol run before every tag; a timing rig; every UI string a resource with a guard test; WCAG 2.2 AA stated and met on the main surfaces; the previous roadmap's debt register closed. |

### 2.4 Product priorities (continuity with the previous roadmap)

- **P0 Precision foundation** remains a continuous release gate: timing, audio reliability, accessibility, localisation readiness, maintainable architecture. It is now expressed as exit gates (§4) and continuous tracks (§5) rather than as a backlog.
- **P1 Structured practice** shipped in 1.3.0; this year finishes its running experience (goal feedback, recovery exits, progress, export) rather than widening it.
- **P2 Faster control** lands as exact tempo, an honest door to the tools, hardware keys and Android media controls (1.4.0, 1.5.0); watches and widgets move to the following year.
- **P3 Private insight** lands only as trustworthy stats across midnight and force-quit, and a backup file; history, notes and reminders stay parked (§9).

---

## 3. Outcomes and how they are measured

No account, no in-app analytics events and no telemetry beyond Crashlytics are introduced. Every figure below comes from the store consoles, Crashlytics, a physical rig, a stopwatch or a fixed critique rubric, recorded by hand in `docs/store/metrics.md` and `docs/verification/<version>.md`.

| # | Metric | Baseline (2026-09-05) | 6 months (Mar 2027) | 12 months (Sep 2027) | How measured |
|---|---|---|---|---|---|
| M1 | Ratings count and average per store | App Store: 0 in US, GB, AU, DE, CA. Play: below render threshold; `1K+` installs. Ranks for one query: its old name. | ≥ 30 ratings per store, average ≥ 4.5; top 100 on the US App Store for "metronome practice" | ≥ 120 ratings per store, average ≥ 4.6; no unanswered 1–3 star review older than 7 days; a top-50 appearance for "metronome practice" or "practice metronome" on either store | Consoles read on the 1st of each month; iTunes Search API (term, country, limit 200) and a manual Play web search logged in `docs/store/rank-log.csv`. The existing restrained review prompt is the only in-product ask. |
| M2 | Tag-to-live parity (days from git tag to both stores serving the release) | Stores serve 1.2.1 (16 Jun); 1.3.0 untagged; gap 82 days and counting | Every tag from 1.3.0 live on both stores within 14 days; rename, category and frames live by 3 Oct 2026 | Median ≤ 7 days across seven releases (21 for the staged 1.6.0); no release skipped on either platform; no crash fix unshipped longer than 14 days | Tag date against each console's release date, recorded in the CHANGELOG entry. Measurable today. |
| M3 | Crash-free users per released version, both platforms | Android: 7 fatal events in 90 days across 3 issues, 6 on the unreleased 1.3.0 (8) from emulators; iOS: 0 | ≥ 99.8% on Android for every production version within 30 days; 0 fatal issues per iOS version; any crash on the current release patched within 14 days | ≥ 99.9% on Android across 1.5.0–1.8.0; 0 open fatal issues on either platform; Play ANR rate ≤ 0.2% | Crashlytics per version on both platforms; Play vitals; Xcode Organizer as cross-check. Read weekly, snapshotted per release. |
| M4 | Measured timing: p95 inter-onset deviation (wired) and visual/haptic offset (per route) on a fixed device set | Never measured. Architecture estimate ±5–10 ms inter-onset; visuals lead audio by ~40–80 ms on Android speaker, ~10 ms iOS, 100–250 ms Bluetooth | Nothing published; the rig exists and a baseline on the old audio paths is recorded on 3 Android and 2 iOS devices before any renderer ships | p95 ≤ 1.0 ms Android, ≤ 0.5 ms iOS at 60/120/220/300 BPM incl. sixteenths; visual and haptic offset ≤ 10 ms speaker and wired, ≤ 25 ms Bluetooth; dated within 30 days of the latest release on `docs/timing.html` with method | Loopback recording of headphone output at 48 kHz analysed by `tools/timing-rig`; visual offset by 240 fps video against audio; haptic by contact microphone. Manual, per release from 1.6.0. |
| M5 | Interruption and background matrix pass rate on physical devices | Unknown; the matrix has never been run (previous roadmap listed it as a pending gate) | Protocol written in the 1.3.1 cycle; 100% of scenarios match the documented policy on 2 Android (one ≤ API 30, one ≥ API 34) and 2 iOS devices for 1.4.0 and 1.5.0 | 100% on 3 Android (three OEMs) and 3 iOS devices for every release from 1.6.0, results public in `docs/verification/`; zero store reviews in the year citing the click stopping on lock or on a notification | `docs/verification/protocol.md` run before every tag, PASS/FAIL per scenario with a linked issue or recorded policy for every FAIL. |
| M6 | First-minute parity checklist (leading indicator, self-scored against the landscape §5.1) | 1 of 9 (only "free with everything, no ads, no account") | 6 of 9 after 1.5.0: range, meters, persisted state, exact tempo, lock-screen control on both platforms, export | 8 of 9 after 1.8.0 (sounds and swing added); the watch row reported as open, not hidden | `docs/store/parity-checklist.md` re-scored at every release. |
| M7 | Unsupported public claims | Four known: "never drifts" on the feature graphic and OG card; implied measured precision; "countdown timer and stopwatch" sold as a usable pair; Play "No data collected". Three product names | Zero at 1.3.0, 1.3.1, 1.4.0 and 1.5.0 by the claims gate; one canonical name everywhere by 12 Sep 2026 | Zero at every release; the only precision statement anywhere is the measured, dated, method-linked figure | Claims gate in `docs/release-checklist.md`: banned phrases grepped in `docs/`, `artifacts/` and store copy; every listing claim walked back to code or a verification file. Measurable today. |
| M8 | Design-critic score (the review's three-critic screenshot rubric, median) | 6, 6.5, 5.5 (median 6): "clean, not beautiful"; nothing identifies the app with the name covered | Median ≥ 7 on the main screen, light and dark, after 1.5.0 | Median ≥ 8 after 1.8.0; no critic below 7.5; at least one critic naming a component that identifies the app with the name covered | Re-run the same prompt and rubric from the review §4–5 on fresh device captures after 1.5.0, 1.7.0 and 1.8.0; record in `docs/design-critique-<date>.md`. A release does not upload if the median drops. |

Supporting counter kept in the CHANGELOG "Under the hood" line: shared unit tests, from 139 today to at least 160 (1.3.1), 185 (1.4.0), 200 (1.5.0), 215 (1.6.0), 235 (1.7.0), 260 (1.8.0).

---

## 4. Release train

| Release | Codename | Window | Live on both stores by | Theme | Item-days |
|---|---|---|---|---|---|
| 1.3.0 | Doorstep | Sep 2026 | 3 Oct 2026 | Publish what exists under the right name, with honest assets and true data declarations | 7.5 |
| 1.3.1 | Even Keel | Oct–Nov 2026 | 14 Nov 2026 | Nothing resets, wraps, lies or stays silent; sounds matched; engine under test | 15 |
| 1.4.0 | First Minute | Dec 2026–Jan 2027 | 23 Jan 2027 | Range, meters, exact tempo, an honest door, a backup file | 18.5 |
| 1.5.0 | Locked Screen | Feb–Mar 2027 | 20 Mar 2027 | Android owns playback; iOS never runs silent; the beat strikes | 19.5 |
| 1.6.0 | Clockwork | Mar–May 2027 | 15 May 2027 (staged) | Clicks on the audio clock, measured before and after | 18 |
| 1.7.0 | In Time | May–Jul 2027 | 10 Jul 2027 | Latency-compensated visuals, published measurement, Reduce Motion, semantics, strings, timer truth | 17 |
| 1.8.0 | Inner Clock | Jul–Sep 2027 | 18 Sep 2027 | Gap and ramp modes, swing and dotted, eight sound sets, Spanish and German, the checkpoint | 22.5 |

**Rules of the train.** Fixed eight-week cadence; a release ships with fewer items rather than later. Each release has a written cut order (§7); verification and publish items are never cut. Every release that touches audio ships through a staged rollout. Dependencies are upgraded only in the first week of a cycle. Any fatal crash on the current release is patched within 14 days regardless of the train. Items over three days get a short spec in `docs/superpowers/specs/` before code.

Pillars: **trust · feel · depth · reach · distribution · foundations.**

---

### 4.1 Release 1.3.0 "Doorstep" — September 2026

*Publish what exists under the right name, with honest assets and true data declarations. No product code beyond one regression test and one dependency change.*

**Goals**
- Both stores serve 1.3.0 (versionCode 9, both crash fixes) as *Metronome: Practice & Tempo* with the corrected six-frame light set, App Store primary category Music, and a branded feature graphic that claims only what the code supports.
- The Play data-safety form, the App Store privacy label, `docs/privacy.html` and the shipped SDKs say the same thing.
- A release checklist exists so every later release passes the same claims and device gates.

| Id | Item | Pillar | Days | Closes | Depends on |
|---|---|---|---|---|---|
| R1-01 | Publish 1.3.0 (versionCode 9) with a regression test for the init-order crash | distribution | 1.5 | review §2.3, §6.4 | – |
| R1-02 | Corrected, honest listings on both stores under one name | distribution | 3.5 | store#1–3, #5–11, a11y#13, §6.6 | R1-01, D1 |
| R1-03 | Data declarations that match the binaries on both platforms | trust | 1 | §6.6, previous roadmap debt (privacy) | D2 |
| R1-04 | Site residuals and a way to share | distribution | 1 | a11y#13, §2.1 | R1-02 |
| R1-05 | Release checklist, version 1 | foundations | 0.5 | previous roadmap P0 gates | – |

**R1-01 · Publish 1.3.0 (versionCode 9).** Tag `v1.3.0` on `main` (`gradle/libs.versions.toml` already reads versionCode 9 / 1.3.0; correct the CHANGELOG line that says versionCode 8). Per D3: TestFlight and Play open testing in the first week, production within 14 days. Add `shared/src/commonTest/.../viewModels/MetronomeViewModelInitTest.kt` constructing the ViewModel with a recovered paused session and asserting `stopGradualTempo` cannot NPE; the test must fail on the parent of 1a8c177 and pass on the release commit. Confirm `AppNavigator.goBack` uses `removeAt(lastIndex)`. Build the release AAB and the iOS archive; CHANGELOG entry with per-store messages (Play under 500 characters).
*Acceptance:* ≥ 140 tests, 0 failures. Play Console shows 1.3.0 (9) at 100% production and App Store Connect shows 1.3.0 current within 14 days of the tag. Back press on an API 26–34 physical device does not crash; Crashlytics shows no new issue in the first 7 days. Tag exists; CHANGELOG carries both store messages and live dates.

**R1-02 · Corrected, honest listings.** Rename to *Metronome: Practice & Tempo* on both stores; App Store primary category Music, secondary Utilities; subtitle, keywords and description from `docs/aso-strategy.md`; Play short and full descriptions rewritten (remove "hold you back" and "Fill the Beat"). Fix the five-frame light set in `artifacts/aso-screenshots/final`: frame 01 capitalised with a category-bearing sub-line; frame 03 cropped below the "Off" header with the duplicated estimate masked; frame 02 a plausible stopwatch value; frame 04 an unsliced Finish button; frame 05 with the sound picker cropped out until R2-08 lands. Add the Lock Screen / Live Activity frame as frame 6, captured within the first three minutes of playback (the 180 s stale state is fixed in R2-03). Feature graphic and OG card gain the mark and wordmark; "never drifts" becomes "drift-free scheduling". Archive the eight-frame dark set; update `docs/aso-strategy.md` to the six-frame narrative and PRODUCT.md:41-43 to the one name; keep the old name as a keyword for one cycle.
*Acceptance:* both listings show the new name, the Music category and six frames within 14 days of R1-01 going live. Searching every uploaded asset and both descriptions for "never drifts", "Feel the Beat", "Fill the Beat", "hold you back" and any emoji returns nothing; frame 03 shows no "Off" label and exactly one estimate line. Every claim in the frames traces to a shipped behaviour. `grep -r "BPM & Practice"` returns nothing outside CHANGELOG history.

**R1-03 · Data declarations that match the binaries.** Per D2: remove the `firebase-perf` plugin and dependency and `firebase-analytics` from `androidApp/build.gradle.kts`; keep Crashlytics on both platforms. Play data-safety: Crash logs, collected, not shared, not linked to identity. App Store privacy label: Crash Data, not linked. `docs/privacy.html`: crash reports only, both platforms. PRODUCT.md: replace the "must be reconciled" sentence with the reconciled statement.
*Acceptance:* `unzip -l` of the release AAB shows no `com/google/firebase/perf` or `analytics` classes. Play data safety lists exactly Crash logs; the App Store label lists exactly Crash Data; `privacy.html` says the same. PRODUCT.md and this roadmap no longer list the reconciliation as debt.

**R1-04 · Site residuals and a way to share.** The site already resolves with brotli and the dead App Store id and process comment are gone. Remaining: below 820px the sticky bar's CTA detects the OS and opens the matching store; both store buttons carry an App Store campaign token and a Play referrer; request a Wayback snapshot; verify that `docs/_headers` is applied (the live `composeApp.js` is served with `max-age=14400`, not the 300 s the file specifies); add a *Share* row (system share sheet with the site URL) beside the existing *Rate the App* row in Settings, with strings in `composeResources`. No prompt or nag; the review-prompt policy is unchanged.
*Acceptance:* on an iPhone and an Android phone the compact CTA opens the correct store in one tap with attribution; a Wayback snapshot exists; Share works on both platforms and is announced with an action label.

**R1-05 · Release checklist, version 1.** `docs/release-checklist.md`: version bump locations, test command and count, the claims gate (banned phrases; every listing claim walked back to code or a verification file), the device matrix (Android API 26 and latest, iPhone, iPad, one Bluetooth route), TalkBack and VoiceOver smoke, per-store What's New from CHANGELOG, the screenshot-refresh rule (whenever a framed surface changes), tagging and the tag-to-live log. Ticked copies live in `docs/verification/<version>.md`.
*Acceptance:* the checklist exists and its ticked copy for 1.3.0 is filed; every later release links its completed checklist.

**Exit gates**
- Both listings live under the new name with 1.3.0 (9); App Store primary category is Music.
- Crashlytics shows zero events for the `removeLast` and `stopGradualTempo` issues 7 days after 100% rollout; the init-order regression test is green.
- `grep -rn "never drifts" docs artifacts` returns nothing; no asset implies a measured precision figure; one product name across PRODUCT.md, `docs/aso-strategy.md`, `docs/index.html` and both stores.
- Play data safety = Crash logs only; App Privacy label = Crash Data only; the AAB contains no Analytics or Performance classes.
- `docs/release-checklist.md` committed and its ticked copy for 1.3.0 filed.

**What's New theme:** Practice Presets and Practice Sets: save a complete setup, line them up with time or bar goals, and pick up exactly where you left off, now under a new name.

---

### 4.2 Release 1.3.1 "Even Keel" — October to November 2026

*Nothing resets, wraps, lies or stays silent in the first ten minutes of use; every click sound is as loud as every other; the engine loop is under test so everything after this is safe to change.*

**Goals**
- Tempo and accents survive a relaunch; every meter fits on one row; the Lock Screen keeps telling the truth for as long as you play; reaching a practice goal is something you feel.
- Three sounds at one loudness with no pre-roll, auditionable while stopped, with no emoji anywhere.
- Practice time survives a force-quit and midnight.
- `MetronomeEngine`'s composition is under virtual-time test with at least 15 scenarios.

| Id | Item | Pillar | Days | Closes | Depends on |
|---|---|---|---|---|---|
| R2-01 | Persist BPM and the per-meter beat pattern; persist colour scheme by name | trust | 1.5 | main#1, main#9 (accents), §3.6 | – |
| R2-02 | Every meter on one row | trust | 1 | main#2 | – |
| R2-03 | The Live Activity never lies | trust | 1 | ios#2 | – |
| R2-04 | Hide the iOS Background Play switch until it does something | trust | 0.5 | settings#1 | – |
| R2-05 | Practice Set targets you can feel, and a way out of a recovered session | trust | 2 | presets#1, #2, #4, #8 | – |
| R2-06 | Quick-adjust buttons that do what they say at the edges | trust | 0.5 | main#6 | – |
| R2-07 | Credit practice time continuously and split it at midnight | trust | 1 | settings#7, previous roadmap debt (midnight, streak) | – |
| R2-08 | Three sounds at one loudness, no pre-roll, auditionable, and no idle iOS engine | trust | 2.5 | settings#2–4, android#7, ios#5, ios#7, craft#7, store#9 | – |
| R2-09 | `MetronomeEngine` under virtual-time test | foundations | 3 | android#6 | – |
| R2-10 | On-device verification protocol v1 and the device pass | foundations | 2 | previous roadmap P0 gates, review App. B | R2-01…R2-09 |

**R2-01 · Persist BPM and the per-meter beat pattern.** `AppDatastoreImpl.kt` gains `BPM` (int) and `BEAT_PATTERN` (string keyed by meter); `MetronomeViewModel.init` (:714-721) restores BPM, then meter, then that meter's stored pattern when its length matches, otherwise the meter default; slider writes debounced at 300 ms; on meter change restore that meter's remembered pattern. Migrate `colorScheme` from ordinal to name with a one-time read (`AppDatastoreImpl.kt:58,105`). Tests against `InMemoryPreferencesDataStore`. Presets and recovered sessions keep precedence.
*Acceptance:* set 137 BPM, 5/4, beat 3 muted; force-quit; relaunch shows the same. Switch 4/4 → 3/4 → 4/4: the 4/4 pattern returns; a stored 7-entry pattern against 4/4 falls back to the default (test). A 5-second slider drag produces a handful of DataStore writes (fake clock). Reordering the scheme enum in a test does not change the restored scheme.

**R2-02 · Every meter on one row.** `MetronomeBalls.kt:213` and `MainScreen.kt:359-361`: derive dot diameter (40dp, 32dp compact) and spacing from measured width and beat count; keep 48dp invisible hit boxes by letting them overlap instead of reserving 48dp of layout per dot; wrap only at 9 or more beats (a rule R3-02 relies on). Ring animation changes belong to R4-04.
*Acceptance:* 5/4, 6/8 and 7/8 render on one row at 360dp and 393dp (Compose UI test on bounds, plus emulator and iPhone 15 screenshots). Every beat target measures ≥ 48×48dp; TalkBack and VoiceOver still hit every beat individually. At fontScale 2.0 the row still fits on 360dp.

**R2-03 · The Live Activity never lies.** `MetronomeLiveActivityManager.swift:62`: no `staleDate` while playing; set one only on pause or stop per the existing dismissal policy. `MetronomeLiveActivityWidget.swift:105-110`: the Dynamic Island honours `isStale` identically to the Lock Screen so the two never disagree.
*Acceptance:* 15 minutes of unchanged playback on a physical iPhone keeps BPM, timer and Play/Pause on the Lock Screen. After Stop the activity ends within the existing window; after a force-quit within the system stale window. Island and Lock Screen show the same state throughout a play/pause/stop sequence.

**R2-04 · Hide the iOS Background Play switch.** In the iOS actual of `PlatformSettingsComponents`, omit the row and show a static caption that playback continues in the background; keep the preference key; Android unchanged; update the 2026-06-12 settings-parity spec.
*Acceptance:* no Background Play row on iOS; the caption is present; Android's switch behaves as before. No setting on iOS claims to control something it does not.

**R2-05 · Practice Set targets you can feel, and a way out of a recovered session.** `PracticeSessionController.kt:150-173` emits a target-reached event that fires a confirm haptic and, when count-in sound is on, one accent click through `MetronomePlayer.play`; wire the existing `discardRecovery()` to a *Discard* action in the recovery strip and sheet; Next on the last step becomes Finish; skip the finish confirmation once the last target is reached; progress rendered as m:ss with the `StatusStrip` progress layer for timed steps. This item defines the ViewModel → engine cue seam (a `SharedFlow` of one-shot cues consumed by `MetronomeEngine`, which owns the player) that R2-08's audition and R6-05's timer chime reuse. Tests in `PracticeSessionControllerTest`.
*Acceptance:* reaching a 1:00 target fires exactly one haptic within 100 ms of the boundary. Discard neither marks the session Completed nor changes the Practice Again target, and unlocks the set. On the last step with its target reached, Next reads Finish and needs no dialog. The strip reads "0:37 of 5:00" with a proportional fill.

**R2-06 · Quick-adjust buttons that do what they say.** `MetronomeViewModel.kt:259-277` clamps ±5 into range and exposes enabled state for ×2 and ÷2; `Buttons.kt` renders disabled states (alpha and semantics) and suppresses the haptic when nothing changes, including ±1 at the limits.
*Acceptance:* at 218, +5 yields 220; at 111, ×2 is disabled and announced as such; at 40, −1 produces no haptic (fake `HapticProvider`). Unit tests cover all four edges.

**R2-07 · Credit practice time continuously.** `MetronomeViewModel.kt:205-207`: a 60-second tick while playing commits elapsed time, today's total and streak to DataStore, attributing each tick to the current calendar day; the remainder is committed on pause; streaks re-evaluate on the day boundary while the app stays open. Closes both midnight debts from the previous roadmap.
*Acceptance:* force-quit after 3 minutes of playback: at least 3 minutes credited on relaunch. A session from 23:58 to 00:04 credits 2 minutes to day one and 4 to day two (injected clock). Streak updates at midnight without a relaunch.

**R2-08 · Three sounds at one loudness.** Normalise wood, click and classic to equal short-term loudness within 1 dB and fix Classic's right channel; export all three as 48 kHz 16-bit WAV to `shared/src/androidMain/res/raw/`, `iosApp/iosApp/Resources/` (Copy Bundle Resources) and `docs/app/sounds`, removing the 46 ms MP3 pre-roll and the duplicate `wood.mp3` project reference; `MetronomePlayer` gains `preview()` so tapping a sound in Settings while stopped auditions it; `ClickSound.kt:3-7` emoji replaced with Lucide glyphs; `MetronomePlayerIos.kt:120-166`: after a sound switch while stopped, stop the engine and deactivate the session.
*Acceptance:* `ffprobe start_time` is 0 for every sample; peak and short-term loudness match within 1 dB across sounds and channels (report in `docs/audio/`). Tapping a sound while stopped plays it once on both platforms; switching mid-play shows no phase jump in a recording. No emoji code points in `shared/src`; the web build shows glyphs. After a sound switch while stopped on iOS, `AVAudioEngine.isRunning` is false within 1 s and the session is inactive.

**R2-09 · `MetronomeEngine` under virtual-time test.** Inject `TimeSource.WithComparableMarks` and the `CoroutineDispatcher`; `delayUntil` becomes testable with `runTest` virtual time and a `TestTimeSource`; a `FakeMetronomePlayer` records (event, time) pairs; `MetronomeEngineTest.kt` covers count-in, meter-change restart, gap muting, stale skip, bar counting, sub-click offsets at 60, 137 and 220 BPM, and the recovered-session start path. No behaviour change on device.
*Acceptance:* ≥ 15 new engine tests; all existing timing tests unchanged and green; total ≥ 160; suite under 10 seconds; verification results identical to 1.3.0 on the named devices.

**R2-10 · Verification protocol v1 and the device pass.** `docs/verification/protocol.md`: the interruption and audio matrix (call, notification sound, assistant, headphone unplug, Bluetooth connect and disconnect, 30-minute lock, task swipe, 8 silent bars, sound switch mid-play, 220 BPM sixteenths, count-in plus meter change, Live Activity 15 minutes) with the expected behaviour per platform and a results template; run it for 1.3.1 on 2 Android (one ≤ API 30, one ≥ API 34) and 2 iOS devices; TalkBack and VoiceOver smoke on the main screen and session sheet; retake frame 05 without emoji; both store messages; tag.
*Acceptance:* protocol committed; `docs/verification/1.3.1.md` records every scenario as PASS/FAIL with a linked issue or recorded policy for every FAIL; checklist linked from the release PR; frame 05 uploaded; Play message under 500 characters.

**Exit gates**
- All items pass their acceptance on physical devices, not only simulators; protocol v1 committed with 1.3.1 results for 2 Android and 2 iOS devices.
- Tests ≥ 160 and green; ≥ 15 engine scenario tests.
- Crashlytics shows 0 fatal events on 1.3.1 after 7 days on internal and TestFlight tracks.
- Claims checklist ticked; frame 05 retaken.

**What's New theme:** Your tempo and accents now stay exactly where you left them, every time signature fits on one row, the Lock Screen stays live for as long as you play, and reaching a practice goal is something you feel.

---

### 4.3 Release 1.4.0 "First Minute" — December 2026 to January 2027

*The table stakes a shopper checks in the first sixty seconds: 20–300 BPM, any meter felt the way you count it, an exact tempo in one gesture, an honest door to the practice tools, and your practice data as a file you own.*

**Goals**
- Range and meters stop being the first thing a review mentions; 6/8 can be felt as a dotted quarter.
- Setting 132 BPM takes one gesture: hold, type, or tap with a listening state.
- Every preset, set, statistic and setting can be exported and restored with no account.
- A new user reaches presets and sets in two taps from the instrument.

| Id | Item | Pillar | Days | Closes | Depends on |
|---|---|---|---|---|---|
| R3-01 | Tempo 20 to 300 | depth | 1.5 | gap: BPM range; main#9 | R2-08, D6 |
| R3-02 | Meters that mean what they say | depth | 5 | main#9, tempo#5, gap: meters, review Q2 | R2-01, R2-02, R2-09, D7 |
| R3-03 | Exact tempo, fast | depth | 3 | main#5, main#7, a11y#3 | R3-01 |
| R3-04 | Your practice as a file | trust | 4 | gap: export/backup; previous roadmap P3 export | R3-02 |
| R3-05 | An honest door to the practice tools | feel | 1.5 | main#3, main#4, presets#5, craft#5 | – |
| R3-06 | Hardware keys so pedals work as keyboards | reach | 1 | gap: pedal/keyboard; previous roadmap P2B | – |
| R3-07 | Device pass, meter and tempo frames, store messages | foundations | 2.5 | – | R3-01…R3-06 |

**R3-01 · Tempo 20 to 300.** `MetronomeConstants.kt` `MIN_BPM` 20, `MAX_BPM` 300; tempo-name table extended (Grave below 40, Prestissimo above 200); slider detents, ±5 and ×2/÷2 clamps re-tested; `Pendulum.kt` swing model extended past its 240 assumption; preset and set codecs accept the range; trainer steppers and tap tempo bounded; `MetronomePlayerAndroid` `maxStreams` 4 → 8 as the interim voice budget with the trimmed WAV tails until R5-02; `TempoMathTest` extended.
*Acceptance:* 20 and 300 reachable by slider, buttons and tap; the pendulum swings correctly at both; 25 BPM reads Grave. Existing presets load unchanged; a preset at 300 round-trips. 60 s at 300 BPM sixteenths on a Pixel 6a-class device and an iPhone 12-class device drops no click (count from a recording).

**R3-02 · Meters that mean what they say.** `TimeSignature.kt` becomes numerator 1–16 over denominator {2, 4, 8, 16}; the meter dropdown becomes a meter `AppBottomSheet` with a common list (2/4, 3/4, 4/4, 5/4, 6/8, 7/8, 9/8, 12/8, 2/2, 5/8, 3/8) plus a custom stepper; default groupings 7/8 = 2+2+3, 5/8 = 3+2, 9/8 = 3+3+3, 12/8 = 3+3+3+3; per D7 a per-meter pulse option for x/8 meters (eighth at the shown BPM, or dotted quarter with subdivided eighths), defaulting to eighth for 1.3.x continuity; accents remembered per meter (R2-01); subdivision labels relative to the beat unit; `PracticePresetCodec.kt` and `PracticeSetCodec.kt` bumped with tolerant decoding of the old enum names and fixture tests from real 1.3.x payloads; balls on one row up to 8, two designed rows above.
*Acceptance:* any 1–16 over /2 /4 /8 /16 can be chosen, plays, persists and shows in the meter pill. 12/8 in dotted-quarter pulse produces 4 pulses per bar with 3 sub-clicks each at the displayed BPM (`SubdivisionTimingTest`); 7/8 defaults to accents on 1, 3 and 5. Every 1.3.x preset and set decodes unchanged; switching 4/4 → 7/8 → 4/4 restores the user's 4/4 accents. The sheet announces the selected meter to screen readers.

**R3-03 · Exact tempo, fast.** `Buttons.kt:157-165`: hold-to-repeat with acceleration on ±1 reusing `ValueStepper`'s logic; tap on the readout opens a numeric `AppBottomSheet` (`AppTextField`, clamps to range, Enter applies, empty leaves the tempo unchanged); Tap Tempo shows a listening state ("tap 2 more") with a rolling six-tap window, a 2-second timeout and an optional auto-start toggle (default off); the plus, minus and gear controls announce actions ("Increase tempo", "Decrease tempo", "Settings"), not icon names.
*Acceptance:* 80 → 132 in one hold of at most 3 seconds; releasing stops within one step. A typed 137 applies and persists; 999 clamps to 300; 5 clamps to 20. After the first tap the listening state is visible; a BPM appears after the third; a 2-second pause resets the window (unit test covers 2, 3, 6 and 9 taps). VoiceOver and TalkBack say the action names.

**R3-04 · Your practice as a file.** Settings › *Back up practice data* writes one versioned JSON envelope (presets, sets, stats, settings) through the platform document picker (Android `ACTION_CREATE_DOCUMENT`, iOS `UIDocumentPickerViewController` via `PlatformActions`); *Restore* merges by id with keep-both on name clash and never deletes; unknown fields tolerated; a `PracticeBackupCodec` with round-trip tests on the R3-02 codec version; delete-all with confirmation; `privacy.html` and PRODUCT.md updated. No network, no account, no sharing links.
*Acceptance:* export, wipe app data, import restores every preset, set, favourite flag, order, statistic and setting on both platforms. A malformed file is rejected with a message and no partial import; a newer envelope with unknown fields imports without error. Nothing leaves the device without the user choosing a destination.

**R3-05 · An honest door to the practice tools.** `MainScreen.kt`: the tempo pill becomes a chevron-less summary ("Andante · 4/4 · Quarter") that opens the tempo sheet; a small *Practice* entry (Lucide icon button in the top bar) opens the sheet at presets and sets; the coach mark names Practice Sets; Practice Again offers the last three completed sets; the meter pill stays the only chevron dropdown until R3-02's sheet replaces it.
*Acceptance:* the summary pill has no chevron, a button role and a spoken "Opens tempo tools" label; a new user reaches presets and sets in two taps; Practice Again lists up to three sets, most recent first.

**R3-06 · Hardware keys so pedals work as keyboards.** Key handling in `AppNavigation.kt`: space toggles play/pause, up/down ±1, left/right ±5, Enter taps tempo; AirTurn and PageFlip pedals that present as keyboards work on Android and iPad with no pairing UI. Out: pedal SDKs, MIDI, proximity control.
*Acceptance:* a Bluetooth keyboard's space bar toggles playback on Android and iPad; arrows change tempo by 1 and 5; keys do not fire while a text field is focused.

*Sequencing inside the release:* R3-03 (tap-to-type on the readout) lands before R3-05 (the Practice entry in the top bar), because both edit the same app-bar and readout region of `MainScreen.kt`.

**R3-07 · Device pass, meter and tempo frames, store messages.** Protocol on 2 Android (one Samsung) and 2 iOS at 20, 120 and 300 BPM with every subdivision, Bluetooth, an incoming call and 30 minutes in the background; TalkBack and VoiceOver traversal of the meter sheet, numeric entry and the summary pill; retake frames 01 and 03 where the pill label changed; parity checklist re-scored; both store messages; tag; staged rollout.
*Acceptance:* `docs/verification/1.4.0.md` complete with device names and OS versions; tests ≥ 185; frames uploaded; `docs/store/parity-checklist.md` re-scored; 1.4.0 live at 100% within 14 days of the tag.

**Exit gates**
- Codec migration tests pass against 1.3.x preset and set fixtures; export → wipe → import round-trip verified on both platforms.
- Sixteenths at 300 BPM drop no click on 2 Android devices and 1 iPhone.
- TalkBack and VoiceOver announce meter selection, numeric entry and the summary pill.
- Verification matrix pass; tests ≥ 185; no Crashlytics issue in the first 7 days.

**What's New theme:** Any time signature from 1 to 16 felt the way you count it, tempos from 20 to 300, hold or type your way to an exact BPM, and a backup of your presets and practice data that needs no account.

---

### 4.4 Release 1.5.0 "Locked Screen" — February to March 2027

*Android as trustworthy as iOS when the phone locks, iOS that never runs silent, and a beat that strikes instead of sitting there. This is the release that produces the refreshed frames and the first critic re-run.*

**Goals**
- Android playback survives screen lock and task swipe, pauses politely for a call and resumes on the beat, ducks for a notification, and can be controlled from the lock screen, notification and headset.
- iOS never runs an idle engine, never animates a beat it is not playing, and produces audio from the Dynamic Island after a cold launch.
- The ball strikes, the ring lands on the beat, captions have a tonal tier, numerals hold still, the slider loses its stray pixel, and nothing new animates without a Reduce Motion off-switch.

| Id | Item | Pillar | Days | Closes | Depends on |
|---|---|---|---|---|---|
| R4-01 | The Android service owns playback | trust | 5 | android#3, §6.2, §3.1 phone-locked complaint | – |
| R4-02 | Graceful audio focus on both platforms | trust | 2.5 | android#2, ios#7 (ShouldResume), review Q5 | R4-01, D4 |
| R4-03 | iOS audio hygiene: no silent playback, audio from a cold launch | trust | 3 | ios#4, ios#6, ios#7 | – |
| R4-04 | Strike, not hold; ring on the beat; halo retired; instrument centred; motion gate | feel | 3.5 | motion#1–3, motion#14 (gate), craft#1, craft#4, firstrun#1, firstrun#8 | D14 |
| R4-05 | Tonal tier and a designed dark ladder | feel | 1.5 | motion#4–6, craft#7, craft#8 | – |
| R4-06 | Numerals that hold still, and a slider without a stray pixel | feel | 2 | motion#11, #15, #16, craft#2, craft#3 | – |
| R4-07 | Device pass on three OEMs, frame refresh, first critic re-run | foundations | 2 | store#11 | R4-01…R4-06 |

**R4-01 · The Android service owns playback.** `MetronomeService.kt` and `MainActivity.kt:49`: `startForegroundService` with `foregroundServiceType mediaPlayback` on Play from the foreground Activity, `stopSelf` on stop, `onTaskRemoved` stops cleanly (D4 keeps "stop on swipe" so nothing plays without a visible owner), a partial wake lock while playing (add `WAKE_LOCK`), `MediaSessionCompat` with play/pause/stop and a MediaStyle notification, an `ACTION_AUDIO_BECOMING_NOISY` receiver that pauses on headphone unplug; the engine remains a Koin single the service drives.
*Acceptance:* screen off for 30 minutes: the click continues and the bar count matches wall clock within one beat. Swipe from Recents: click stops within 1 s and the notification clears. Lock-screen, notification and wired-headset buttons toggle play/pause; unplugging pauses. Verified on API 26, 30, 34 and 35+; foreground-service start complies with Android 14+ rules.

**R4-02 · Graceful audio focus on both platforms.** Per D4: `AndroidAudioFocusController.kt:12-18` drops `setWillPauseWhenDucked(true)`; the common, tested `PlaybackFocusPolicy.kt` maps CAN_DUCK to a click-gain dip to about 40%, TRANSIENT to pause with auto-resume on GAIN within 30 s, LOSS to stop; `IosAudioFocusController` honours the interruption-ended `ShouldResume` flag within the same window. Update the decision record in `docs/metronome-phase2-plan.md`.
*Acceptance:* a notification sound mid-practice dips the click and restores it; the bar count is unaffected. A 10-second call pauses and resumes on the correct beat boundary on both platforms. Another media app starting stops the metronome. `PlaybackFocusPolicyTest` covers every transition for both platforms.

**R4-03 · iOS audio hygiene.** `MetronomePlayerIos.kt:96-97` reports engine failure to the ViewModel, which stops the timeline and shows a quiet "Audio unavailable" `StatusStrip` instead of animating; observe `AVAudioEngineConfigurationChangeNotification` and `AVAudioSessionMediaServicesWereResetNotification` and rebuild the graph; resolve the engine at Koin start (`CommonModule.kt:64-67`) so `TogglePlaybackIntent` produces audio from a cold launch; one owner of session activation; a typed `timerKind` replacing six string comparisons.
*Acceptance:* Play from the Dynamic Island after a force-quit produces audio within 1 s. A media-services reset recovers within 2 s of the next Play; no beat animates without audio. If the engine cannot start, the UI shows the unavailable strip and the ring does not travel. No string comparisons for timer kind remain.

**R4-04 · Strike, not hold; ring on the beat; halo retired; instrument centred; motion gate.** `MetronomeBalls.kt:190-200`: a one-shot `Animatable` keyed on the beat index (1.0 → ~1.16 with the press spring, back with the standard spring; HIGH peaks higher); rest returns fully to 1.0 (D14); the light-theme radial glow at :235-252 replaced by a crisp expanding 1dp stroke ring. `MainScreen.kt:264`: indicator-ring stiffness derived from beat duration so settle time stays under 35% of the beat, crossfade above ~160 BPM. `Buttons.kt:192-204`: the 2.6 s reversing halo tween replaced by a beat-1-keyed impulse at 5–8% alpha inside `pressScale`. `MainScreen.kt:321-336`: the content stack centred in the scroll region with 1:2:1 spacers. A `MotionPreferences` interface with Koin implementations (Android animator scale and `areAnimatorsEnabled`, iOS `UIAccessibility.isReduceMotionEnabled` with its notification, wasm `prefers-reduced-motion`) feeding a CompositionLocal that snaps `AppAnimations` and disables the strike ring, halo and flash; the full semantics and haptic-split work stays in R6-03. Out: the triangle accent (D14 prototype only), tablet scaling.
*Acceptance:* at 60 BPM the ball is back at rest within 250 ms of the strike and at rest for at least 60% of the beat (frame capture); at 220 BPM the ring has settled before the next beat. No `InfiniteTransition` or repeatable tween runs outside the pendulum; nothing animates while stopped; the light-theme LOW beat shows no grey smudge. With Reduce Motion on, no spring is perceptible and the strike is a plain state change. On an iPhone 15 and an iPad the leftover band is split equally above and below the instrument.

**R4-05 · Tonal tier and a designed dark ladder.** `ColorSchemes.kt` (all ten schemes): `onSurfaceVariant` at about 65% ink; a distinct `tertiary`; `outline`, `outlineVariant`, `scrim` and `error` set explicitly; `Color.DarkGray` (:40-45) and `LightGray.copy(0.5f)` replaced with a designed ladder per DESIGN.md neutrals (#000 canvas, #1A1A1A surface, #222 control rest, #2B2B2B inactive track, #333 LOW beat, white at 12% hairlines); Settings' `ElevatedCard` becomes a flat container and the gradient swatches become solid pairs. DESIGN.md names the ladder values.
*Acceptance:* a script over `ColorSchemes.kt` asserts caption text between 4.5:1 and 12:1 on surface in every scheme, light and dark. No `Color.DarkGray`, `Color.LightGray`, `ElevatedCard` or `Brush.horizontalGradient` in `shared/`. Dark-theme screenshot: controls brighter than inactive containers; dropdown borders not lavender.

**R4-06 · Numerals that hold still, and a slider without a stray pixel.** `Type.kt` defines `tempoDisplay`, `dataDisplay`, bold `titleLarge` and `labelLarge`-selected per DESIGN.md; `fontFeatureSettings "tnum"` on the BPM readout, `TimerChip.kt`, `StatusStrip.kt`, `PracticeTimerSheet.kt` and `TempoTrainerSheet.kt` figures (and `.monospacedDigit` in the Swift widget); those five files lose their inline overrides; the readout animates only the changed digit and not on every integer during a drag. `AppSlider.kt:41-64`: remove the M3 stop-indicator dot and thumb-track gap and use a 28dp round thumb through `SliderDefaults` parameters, keeping semantics, the 48dp target and per-BPM detent haptics. The remaining inline type overrides migrate through the hygiene track; the fully custom slider is next year.
*Acceptance:* 100 → 101 does not change the readout's width; timers do not jitter horizontally in a 10 s recording. No inline `fontSize` in the five files; the widget uses `monospacedDigit`. Frame capture shows no stop dot or thumb gap in any scheme; TalkBack and VoiceOver still adjust by step and announce the value.

**R4-07 · Device pass on three OEMs, frame refresh, first critic re-run.** Protocol on Pixel (Android 15+), Samsung (One UI) and a budget OEM plus 2 iPhones with the focus, call, headset, task-swipe and 30-minute screen-off scenarios; frame capture of the strike and ring; retake every frame that shows the instrument; a Play frame showing the notification and lock-screen controls; run the three-critic rubric and record `docs/design-critique-2027-03.md`; both store messages; tag; staged rollout.
*Acceptance:* `docs/verification/1.5.0.md` complete including Samsung foreground-service behaviour; tests ≥ 200; frames uploaded on both stores; critic median recorded; live within 14 days of the tag.

**Exit gates**
- Verification matrix 100% pass on 3 Android devices (Pixel, Samsung, one budget OEM) and 2 iPhones, including focus, call and Android 14+ foreground-service start rules.
- Strike and ring verified by frame capture at 60, 120 and 220 BPM; nothing animates while stopped; Reduce Motion verified on both platforms.
- Design-critic median ≥ 7 on the main screen, light and dark, recorded before upload.
- Tests ≥ 200; `PlaybackFocusPolicyTest` covers every transition for both platforms; no Crashlytics issue in the first 7 days.

**What's New theme:** The click keeps going when your phone locks, pauses politely for a call and comes back on the beat, plays and pauses from the lock screen or your headphones on Android, and the beat now strikes instead of glowing.

---

### 4.5 Release 1.6.0 "Clockwork" — March to May 2027

*Clicks placed on the audio clock itself on both platforms, behind a fallback flag, measured on real devices before and after so the improvement is a number and not a claim.*

**Goals**
- A scheduled-playback contract with `BeatTimeline` still the musical authority, under test with a scheduling fake.
- Android streams and mixes clicks at computed frame offsets; iOS schedules at `AVAudioTime`; the accent is a pre-rendered buffer, not a runtime rate change.
- A loopback rig reports sub-millisecond p95 deviation and the before-and-after figures are written down.
- No dropped clicks at 300 BPM sixteenths; no standby wake-up after silent gap bars.

| Id | Item | Pillar | Days | Closes | Depends on |
|---|---|---|---|---|---|
| R5-01 | Scheduled-playback contract and lookahead loop | trust | 3.5 | android#1, ios#1, android#8, review Q9 | R2-09, D5 |
| R5-02 | Android `AudioTrack` streaming renderer | trust | 6 | android#1, android#4, android#7 | R5-01, R2-08, R4-01 |
| R5-03 | iOS `AVAudioTime` renderer | trust | 4 | ios#1 | R5-01, R4-03 |
| R5-04 | Timing rig v1, with a before-and-after baseline | foundations | 2 | §6.1 no measurement; gap: published figure | – |
| R5-05 | Staged rollout, fallback flag, device pass | foundations | 2.5 | process | R5-02, R5-03, R5-04 |

**R5-01 · Scheduled-playback contract and lookahead loop.** Per D5: `MetronomePlayer.kt` gains a relative-time contract, `schedule(event, dueIn: Duration)` plus `outputLatency(): Duration`, that Android, iOS and the web player can all implement (each converts the due time to its own clock: `AudioTrack` frame position, `AVAudioPlayerNode` sample time from `lastRenderTime`, Web Audio `currentTime`), plus `cancelPending()` so queued clicks are dropped on a tempo or meter change (the one semantic every backend must honour), while `play()` stays for auditions; `MetronomeEngine.kt` adds a lookahead loop that walks `BeatTimeline` 150–200 ms ahead and schedules events, keeping monotonic time for UI and haptic emission; `beatEvents()` unchanged; the R2-09 engine tests extended with a scheduling fake; the old players ignore the time and play immediately so either path can ship; `docs/app/webAudio.js` passes `when` to `src.start` and the "sample accurate" comment in `MetronomePlayerWasm.kt` is corrected.
*Acceptance:* with the fake player every scheduled `at` equals anchor + n × interval + offset exactly over 1000 beats at 173 BPM. A live tempo change takes effect within one beat; mute, gap, count-in and stale-skip behaviours hold under test. `grep -rn "sample accurate" shared docs` returns nothing untrue.

**R5-02 · Android `AudioTrack` streaming renderer.** New `MetronomePlayerAndroidTrack.kt`: `AudioTrack` `MODE_STREAM` at the device's native rate with `PERFORMANCE_MODE_LOW_LATENCY`, a mixer thread writing fixed blocks with clicks summed at frame offsets derived from `AudioTrack.getTimestamp()`; WAV samples from R2-08 decoded and resampled once at load; the accent as a separately rendered buffer; continuous silence during gap bars so the output never enters standby; SoundPool kept behind a hidden *Legacy audio engine* setting for one release; underrun count logged as a Crashlytics custom key. Device validation depends on R4-01: a continuously writing audio thread behind a bound-only service with no wake lock is exactly what task removal and Doze kill. Oboe only if the rig misses the target (D5); delete the `oboe` and `latency` branches.
*Acceptance:* at 300 BPM sixteenths with the 757 ms wood tail no click is dropped. `getUnderrunCount()` stays 0 over 10 minutes on 3 devices; the re-entry click after 8 silent bars shows no wake-up delay in a recording. Rig-measured p95 inter-onset deviation ≤ 1.0 ms wired on the device set; CPU under 3% while playing; battery over 30 minutes within 10% of the SoundPool path.

**R5-03 · iOS `AVAudioTime` renderer.** `MetronomePlayerIos.kt:106-108`: `scheduleBuffer(buffer, at: AVAudioTime(sampleTime:atRate:))` derived from `playerNode.lastRenderTime` plus the lookahead (player sample time, not a host-time conversion of the Kotlin monotonic mark, which the sizing pass found fragile across route changes); `setPreferredIOBufferDuration(0.005)`; at least two buffers in reserve; the per-click `varispeed.rate` mutation and the `AVAudioPlayerNodeBufferInterrupts` option are incompatible with queued buffers, so the accent becomes a second pre-rendered buffer on its own player node; re-anchor on the configuration-change observer from R4-03; sound switch and count-in flush and reschedule from the next deadline; the player node started once per graph; the serialized build-then-swap retained.
*Acceptance:* rig-measured p95 ≤ 0.5 ms wired on 2 iPhones. Tempo change applies within one beat; a mid-play sound switch produces no double click and no gap longer than one beat. No regression in the matrix, including AirPods connect and disconnect.

**R5-04 · Timing rig v1.** `tools/timing-rig/`: a script that takes a WAV recorded from the device headphone output (USB audio interface or a second phone), detects click onsets by envelope threshold, and reports mean interval, p50/p95/max deviation and drift over N minutes; `docs/verification/timing-method.md` describes the setup honestly (onset deviation and drift, not absolute latency); baseline figures recorded on the SoundPool and `atTime = null` paths **before** R5-02 and R5-03 land, then the new figures, on at least 3 Android and 2 iOS devices.
*Acceptance:* the rig reproduces a synthetic WAV's known intervals within 0.05 ms. `docs/verification/1.6.0.md` holds before-and-after figures for every device, including SoundPool versus `AudioTrack` on the same device.

**R5-05 · Staged rollout, fallback flag, device pass.** Play staged rollout 10% → 50% → 100% over two weeks gated on crash-free users ≥ 99.5% and vitals; a TestFlight external group of at least 5 musicians for two weeks; the legacy toggle documented in `support.html`; protocol on 3 Android and 3 iOS devices plus three Bluetooth routes and a screen-off soak; both store messages; tag.
*Acceptance:* no audio-related crash or ANR in the 10% cohort for 5 days before widening; ≥ 5 external testers report no dropouts; `docs/verification/1.6.0.md` complete; tests ≥ 215; live within 21 days of the tag.

**Exit gates**
- Rig baseline recorded on the old paths, then p95 ≤ 1.0 ms Android and ≤ 0.5 ms iOS on the named device set with the new renderers.
- Zero `AudioTrack` underruns over 10 minutes on each Android device; no dropouts on AirPods over 10 minutes.
- Engine tests green with the scheduled fake; tests ≥ 215; matrix pass; the web build still runs with honest wording.
- Staged rollout completed as specified; the SoundPool fallback documented.

**What's New theme:** Every click is now placed on the audio clock itself, so the beat is as steady at 300 BPM sixteenths as at 60, and the timing is measured on real phones, not promised.

---

### 4.6 Release 1.7.0 "In Time" — May to July 2027

*What you see and feel lands with what you hear, the listing says exactly what was measured, and the app is honest for every musician: Reduce Motion honoured everywhere, real semantics, every string a resource, timers and trainers that do what their labels say.*

**Goals**
- Visual beat and haptic compensated for measured output latency per route, re-anchored on route change.
- A public timing page with method and dated figures, and one honest sentence in both store descriptions.
- WCAG 2.2 AA met on the main surfaces with VoiceOver and TalkBack; 100% of UI text externalised and guarded by a test.
- Countdown, stopwatch, count-in and the trainers behave exactly as their labels say.

| Id | Item | Pillar | Days | Closes | Depends on |
|---|---|---|---|---|---|
| R6-01 | Latency-compensated visuals and haptics | trust | 3 | android#5, ios#3 | R5-02, R5-03 |
| R6-02 | Publish the measurement | distribution | 1.5 | §6.6, gap: published figure, store#8 | R6-01, R5-04, D9 |
| R6-03 | Real semantics, honest haptic settings, accessibility statement | trust | 3 | a11y#5–7, firstrun#7, review Q8 | R4-04, D10, D15 |
| R6-04 | Every string a resource | foundations | 3 | a11y#1, #2, #8, #9, tempo#8 | – |
| R6-05 | Timer and trainer truth | trust | 3 | tempo#1, #2, #6, #7, #9, main#11 | R2-09 |
| R6-06 | A first run that ends in sound | feel | 1.5 | main#10, settings#8, firstrun#2–5 | R4-04 |
| R6-07 | Device pass, accessibility traversal, timing capture | foundations | 2 | – | R6-01…R6-06 |

**R6-01 · Latency-compensated visuals and haptics.** `MetronomeEngine` emits the UI beat and haptic at scheduled audio time minus output latency, where output latency comes from `AudioTrack.getTimestamp()` and `AudioManager` on Android and `AVAudioSession.outputLatency` plus `ioBufferDuration` on iOS; re-read on route change (`AudioDeviceCallback`, `AVAudioSessionRouteChange`); a Bluetooth default of about 150 ms when the platform reports zero; a read-only figure under Settings › About › Timing. No user-facing calibration slider unless measurement proves unreliable.
*Acceptance:* 240 fps capture shows absolute visual-to-audio offset ≤ 10 ms on speaker and wired on 4 devices and ≤ 25 ms on Bluetooth; re-aligns within 1 s of a route change. Haptic within the same envelope (contact microphone). Figures in `docs/verification/1.7.0.md`.

**R6-02 · Publish the measurement.** `docs/timing.html`: method, device table, dated before-and-after figures for inter-onset deviation and visual offset, what is not measured; per D9 one sentence in both store descriptions ("Timing measured on real devices; method and figures at metronome.merkost.dev/timing"), never a bare number or a comparison; PRODUCT.md "Evidence on hand" updated; the feature graphic stays at "drift-free scheduling".
*Acceptance:* the page is live before the store copy referencing it is submitted; every number on the page traces to a row in `docs/verification/*.md`; store copy contains no "never", "perfect" or microsecond claim; the claims checklist passes.

**R6-03 · Real semantics, honest haptic settings, accessibility statement.** `AppChip` and the meter sheet gain selected state and role; the readout becomes a polite live region; strips and the timer chip get descriptions; both strip stop buttons say what they stop; auto-size floors raised to 12sp; *Haptic feedback* split into Beat haptics and Control haptics with beat haptics on for new installs (D15); the animated splash and coach transitions honour the R4-04 motion gate; an accessibility sentence added to both descriptions once the checklist passes (D10).
*Acceptance:* VoiceOver and TalkBack traversal of main, tempo sheet, meter sheet and settings announces every control's purpose and state (checklist in `docs/verification/a11y.md`). No text below 12sp at 200% scale. With Control haptics off no chip, slider or button vibrates; with Beat haptics off no beat vibrates. With Reduce Motion on, the splash is a static mark and no `InfiniteTransition` runs anywhere.

**R6-04 · Every string a resource.** Externalise `SettingsScreen` (~38 literals), `TempoTrainerSheet` (~32), `PracticeTimerSheet` (14), `CoachMarks` (7) and every model enum label into `composeResources` `strings.xml` with plurals; remove inline duplicates of existing resources (`AppDialog.kt:19`, `SettingsScreen.kt:154`); mirror directional icons for RTL readiness; a guard test that scans `shared/src/commonMain` for hard-coded `Text` literals and fails on any; a pseudo-locale run in CI.
*Acceptance:* the guard test passes; `strings.xml` holds ≥ 260 entries; "1 bar" and "2 bars" come from plurals; a pseudo-locale run at 1.3× text scale shows no English leak and no truncation on main, settings or the tempo sheet.

**R6-05 · Timer and trainer truth.** Countdown anchored to a monotonic deadline (no 1000 ms subtraction), pause preserves remaining exactly; end-of-timer chime through `MetronomePlayer.play` plus haptic; "+5 min" extends the finished timer; stopwatch gains reset and shows its per-session figure in the timer sheet; tempo trainer *Edit settings* gets Apply and Cancel without restarting the ramp; gap trainer keeps phase across pause; count-in choice of 1 or 2 bars in the tempo sheet with the count-in state visible before Play; minutes rounded rather than truncated.
*Acceptance:* a 10-minute countdown ends within 100 ms of wall clock across three pauses; +5 after Done extends. Stopwatch reset works and its value appears in the sheet. Editing a running ramp then cancelling leaves BPM and bar count untouched; resuming a gap cycle keeps its phase. 1.9 minutes displays as "2 min"; count-in state is visible before Play.

**R6-06 · A first run that ends in sound.** Gate onboarding on splash completion (`AppNavigation.kt:45-47`, `MetronomeViewModel.kt:723-726`); one splash of about 700 ms with a spring fade and the Reduce Motion path, the platform launch screens showing the same static mark; coach cards anchored below their spotlight using the balls' own bounds; step 2's spotlight covers the slider; the last step's action is Play and starts playback; Skip moves into the app bar; sets named in the coach mark.
*Acceptance:* on a fresh install the undimmed instrument is visible for ≥ 600 ms before the first coach card; the tour's final tap starts the click; time to first sound ≤ 6 seconds including the tour. iPad shows Skip in the app bar with no overlap; each card is within 48dp of its spotlight. Under Reduce Motion the splash is static and the coach transitions crossfade.

*Sequencing inside the release:* R6-04 (strings) lands first, so that R6-05 and R6-06 add their new copy as resources rather than as literals to be swept later.

**R6-07 · Device pass, accessibility traversal, timing capture.** Protocol on the 3-and-3 device set; full TalkBack and VoiceOver traversal filed in `docs/verification/a11y.md`; 240 fps visual-offset capture on speaker, wired, AirPods and a Bluetooth speaker; rig re-run; second critic re-run recorded; both store messages; tag; staged rollout.
*Acceptance:* `docs/verification/1.7.0.md` complete with offset and rig figures; a11y checklist filed; `docs/design-critique-2027-07.md` recorded; tests ≥ 235; live within 14 days of the tag.

**Exit gates**
- Visual and haptic offsets within targets on 4 devices across speaker, wired and Bluetooth; rig figures no worse than 1.6.0.
- Timing page live and dated before the store copy referencing it is submitted; claims checklist passes with the new sentence.
- VoiceOver and TalkBack checklist complete on both platforms; string guard green; pseudo-locale run clean.
- Matrix pass; tests ≥ 235; no Crashlytics issue in the first 7 days.

**What's New theme:** The light and the buzz now land exactly with the click, the timing is measured on real phones and we show you how, Reduce Motion is honoured, full VoiceOver and TalkBack support, and the timer and trainers finish what they start.

---

### 4.7 Release 1.8.0 "Inner Clock" — July to September 2027

*Training depth on the proven engine, a sound palette worth it, and the first two languages; the 12-month checkpoint.*

**Goals**
- Random, per-beat, custom and progressive gap training; ramps by time with hold, loop and stop at target.
- Swing and dotted feels with a sub-click volume, rig-verified.
- Eight loudness-matched sound sets with real accent and sub voices, auditionable from the instrument.
- Spanish and German shipped only after native review; Portuguese and Japanese only if reviewers are booked.
- Year-end measurement refresh, honest assets, and a written checkpoint against every metric in §3.

| Id | Item | Pillar | Days | Closes | Depends on |
|---|---|---|---|---|---|
| R7-01 | Trainer modes: gap and ramp | depth | 4 | tempo#3, tempo#4, gap: gap-trainer modes | R6-05 |
| R7-02 | Swing and dotted feel with a sub-click volume | depth | 3 | tempo#5, gap: subdivisions | R5-01, R3-02 |
| R7-03 | Eight sound sets with real accent voices | depth | 8 | settings#2, settings#6, §3.5, gap: sounds | R5-02, R5-03, D8 |
| R7-04 | Spanish and German, with Portuguese and Japanese behind native review | reach | 3.5 | a11y#1, a11y#9, previous roadmap P0 localisation | R6-04, D11 |
| R7-05 | Year-end measurement, claims refresh and checkpoint | distribution | 2 | §6.6, store hygiene | R7-02, R7-03 |
| R7-06 | Device pass, frames in three languages, store messages | foundations | 2 | – | R7-01…R7-04 |

**R7-01 · Trainer modes: gap and ramp.** `GapTrainerConfig` gains modes: fixed (existing), random per bar at 10/25/50/75%, random per beat, custom play and mute counts up to 16, progressive (mute grows every N cycles); the mute decision is a pure function tested with a seeded RNG. `GradualTempoConfig` gains steps by seconds alongside bars, custom step 1–20 BPM, hold at target for N bars, loop, stop at target; the `StatusStrip` shows the mode; config captured in presets and set steps; virtual-time tests. Out: scoring or "did you come back on time".
*Acceptance:* at 50% per bar over 200 bars the mute rate is between 45% and 55% (seeded test); per-beat random works with subdivisions and count-in; resume keeps phase. A 60 → 120 ramp at +5 every 30 seconds completes in 6 minutes ± 1 second under virtual time; stop at target stops playback and credits practice time correctly. Every mode announces itself in the strip and round-trips through presets.

**R7-02 · Swing and dotted feel with a sub-click volume.** `Subdivision` gains SWING (ratio 50/58/66/75%, default 66%) and DOTTED (dotted eighth plus sixteenth); `beatEvents()` places the off-clicks through the R5-01 contract; sub-click gain becomes a setting (default 35%) replacing the fixed value; labels follow the beat unit from R3-02; preset codec bump with tolerant decode and fixtures; `BeatEventsTest` and `SubdivisionTimingTest` extended. Out: quintuplets, sextuplets, the per-subdivision accent grid.
*Acceptance:* swing 66% at 120 BPM places off-beat onsets 333 ms after the beat on the rig within 0.5 ms; dotted at 3/4 of the beat (engine test). Ratio and sub-click gain persist and are captured in presets; 1.7.0 presets decode; the UI shows the ratio as a chip row.

**R7-03 · Eight sound sets with real accent voices.** Per D8: eight designed sets (accent, normal, sub sample each: Wood, Click, Bell, Rim, Soft for headphones, Studio, Low, Clave; no hi-hat) sourced under CC0 or from a paid pack and authored as 48 kHz WAV loudness-matched at authoring (authoring and licensing, not code, are the dominant cost; the estimate is the low end of the sizing range and assumes samples are sourced, not recorded), replacing the runtime 1.4× pitch shift; `ClickSound.kt` becomes a data-driven list with Lucide glyphs; a sound sheet reachable from the instrument with audition on tap while stopped, volume and pan; Xcode Copy Bundle Resources and `docs/app/sounds` updated; sound choice captured in presets.
*Acceptance:* all 24 samples within 1 dB short-term loudness of each other; no `rate = 1.4f` or `playbackRate` remains in either player. The accent is identifiable blind against normal for every set by the developer and three musicians. Switching sets mid-play produces no gap or double click; package growth ≤ 1.5 MB per platform. Sound change from the instrument in two taps; audition works stopped on both platforms.

**R7-04 · Spanish and German.** Per D11: ES and DE resources with a musical-terms glossary in `docs/l10n/glossary.md`, plural rules, locale-aware numbers, pseudo-RTL verification; machine draft plus native-musician review before any locale ships; localised store listings (subtitle, description, What's New) for ES and DE; pt-BR and JA ship in the same release only if their reviewers are booked by D11's date, otherwise in the next train.
*Acceptance:* every screen renders without truncation at 1.3× font scale in each shipped locale; no English visible in a manual traversal; per-locale coverage ≥ 98% in the guard test; native review signed off in `docs/l10n/`; store listing localisation live for ES and DE.

**R7-05 · Year-end measurement, claims refresh and checkpoint.** Re-run the rig and the visual-offset capture on the device set with one new device per platform; update `docs/timing.html` and `docs/verification/1.8.0.md`; refresh the frames where the UI changed (sound sheet, trainer strip); re-run the claims checklist; third critic re-run; `docs/store/metrics.md` reviewed against every §3 target with a one-line reason per miss; rewrite this document for the following year with inputs for widgets, watch, the custom slider, polyrhythm and the monetisation decision.
*Acceptance:* timing page dated within 30 days of release with the enlarged device set; screenshots match the shipped UI; zero unsupported claims; a checkpoint document exists with each metric's actual versus target; this roadmap reflects reality.

**R7-06 · Device pass, frames in three languages, store messages.** Protocol on the 3-and-3 device set including swing at 60 and 240 BPM and every sound on speaker, wired and AirPods; TalkBack and VoiceOver on the new trainer controls in two locales; frames regenerated where surfaces changed, in en, es and de; `targetSdk` bumped to the Play requirement in force in August 2027; both store messages in three languages; tag; staged rollout.
*Acceptance:* `docs/verification/1.8.0.md` complete; tests ≥ 260; frames live in three languages; live within 14 days of the tag.

**Exit gates**
- Rig checks for swing and dotted offsets recorded; seeded RNG and virtual-time ramp tests green; tests ≥ 260.
- Loudness table for all 24 samples recorded; blind accent test passed by the developer and three musicians.
- Per-locale coverage ≥ 98% with native review sign-off in `docs/l10n/`; store listings live for ES and DE.
- Monetisation decision D12 recorded before this release ships; timing page re-dated; claims checklist ticked; checkpoint document written.

**What's New theme:** Random and progressive gap training, tempo ramps by time, swing and dotted feels, eight new click sounds each with a real accent voice, and the app now speaks Spanish and German.

---

## 5. Continuous tracks

| Track | Cadence | What |
|---|---|---|
| Store hygiene | Every review answered within 7 days; monthly snapshot on the 1st; per-release refresh; quarterly keyword review | What's New per store from the CHANGELOG (Play under 500 characters); screenshots refreshed whenever a framed surface changes (1.3.1, 1.4.0, 1.5.0, 1.8.0); ratings, installs, retention proxies and rank appended to `docs/store/metrics.md` and `docs/store/rank-log.csv`; the icon PPO experiment from `docs/aso-strategy.md` run once ratings reach 25; one Play listing experiment or App Store PPO per quarter; data-safety form, privacy label and `privacy.html` re-checked whenever a dependency changes; one canonical name enforced; `docs/aso-strategy.md` is the single source of truth for the frame narrative. |
| Device verification | Per release, before the tag | `docs/verification/protocol.md` on the named device set (2 Android spanning old and new API plus 2 iOS from 1.3.1, growing to 3 and 3 from 1.6.0), results in `docs/verification/<version>.md`, every FAIL linked to an issue or a recorded policy; from 1.6.0 the timing rig and from 1.7.0 the visual-offset capture join the protocol; launch-to-first-click recorded from 1.7.0. |
| Tests | Every pull request | Every fix carries a regression test; engine changes tested under `TestTimeSource`; codec changes carry fixture-based migration tests from every shipped version; the hard-coded-string guard runs from 1.7.0; counts recorded in the CHANGELOG. |
| Accessibility | Per release for changed screens; full traversal at 1.7.0 and 1.8.0 | TalkBack and VoiceOver traversal of every changed screen, 200% text scale, Reduce Motion from 1.5.0, 48dp targets preserved, a greyscale check that state never depends on colour; checklist in `docs/verification/a11y.md`; conformance target per D10 and a listing sentence once R6-03 passes. |
| Localisation | String freeze two weeks before each release from 1.8.0 | Externalisation guard green from 1.7.0; glossary in `docs/l10n/glossary.md`; shipped locales updated per release with native review; ES and DE store listings kept in step; plurals and locale numbers verified; pt-BR and JA added only with booked reviewers. |
| Docs and claims | Per release | CHANGELOG entry with per-store messages before the tag; this roadmap's status; PRODUCT.md evidence, names and claims reconciled; DESIGN.md figures kept equal to code (ladder and typography in 1.5.0, motion figures, slider); the release checklist ticked and filed; `privacy.html` matches the SDK set; specs in `docs/superpowers/specs/` for any item over three days. |
| Crash and vitals triage | Weekly; patch within 14 days | Crashlytics on both platforms, Play vitals and Xcode Organizer read weekly; any fatal on the current release patched within 14 days regardless of the train; iOS focus-controller non-fatals reviewed monthly; staged rollouts for every audio-touching release. |
| Design-system hygiene and ViewModel decomposition | Opportunistic, scoped to files a release already touches; never a release of its own | Raw `Button`/`FilledTonalButton`/`OutlinedButton` sites and inline type overrides in files a release edits migrate to a shared `AppPrimaryButton` / `AppSecondaryButton` / `AppTonalButton` family (52 and 48dp, pressScale, haptic) and `Type.kt` roles; `FilterChip` → `AppChip`; one app-bar style; one empty state; `AppDialog` everywhere; the session strip in the `StatusStrip` language; one duplicate or dead token deleted per release. `MetronomeViewModel.kt` split into timer, trainer, session and preset state holders as each area is changed, each extraction with tests. |
| Design critique | After 1.5.0, 1.7.0 and 1.8.0 | Re-run the review's three-critic screenshot rubric on fresh device captures, light and dark, phone and iPad, including the cover-the-name identity test; record in `docs/design-critique-<date>.md`; a release does not upload if the median drops. |

---

## 6. Decisions required

Each has a recommended default that the plan assumes. Deciding differently changes the items that cite it.

| Id | Question | Recommended default | Decide by | Why |
|---|---|---|---|---|
| D1 | Which product name is canonical? | **Metronome: Practice & Tempo** everywhere; update PRODUCT.md and archive the alternative; keep the old name as a keyword for one cycle | 2026-09-12 | *BPM & Practice* is a live, ranking competitor; every asset, campaign link and localised listing keys off one name. |
| D2 | Reconcile Play's "No data collected" with the shipped Firebase SDKs by disclosure, or by removal? | Remove Analytics and Performance from the Android build; keep Crashlytics on both platforms; declare Crash logs / Crash Data; `privacy.html` says crash reports only | 2026-09-08 (before the 1.3.0 AAB is built) | No analytics event exists and nothing on this roadmap consumes Perf; the smaller, truer statement costs nothing, shrinks the AAB, and every metric here uses Crashlytics, the consoles or a rig. Deleting iOS Crashlytics would lose the only iOS crash signal. |
| D3 | Ship 1.3.0 (versionCode 9) now, or wait for 1.3.1's trust fixes? | Publish 1.3.0 now: TestFlight and Play open testing in week one, production within 14 days; 1.3.1 in November | 2026-09-08 | Both crash fixes are on `main`; every unpublished week is a week of zero ratings; the trust fixes are worth a second What's New. |
| D4 | Android audio-focus and task-swipe policy: keep "stop on any loss, never resume"? | Duck to ~40% on CAN_DUCK, pause on TRANSIENT and resume on GAIN within 30 s, stop only on permanent LOSS; iOS honours `ShouldResume` in the same window; task swipe stops playback | 2027-01-15 | The recorded phase-2 decision never weighed a notification sound ending a practice session, the most-cited mid-practice surprise on Android; the policy is a pure tested function so reversing is cheap. |
| D5 | Android renderer: pure-Kotlin `AudioTrack` or Oboe via NDK? Contract change once for all players? Stale branches? | `AudioTrack` streaming with `PERFORMANCE_MODE_LOW_LATENCY` behind one `schedule(event, at)` contract; Oboe only if the rig shows p95 above 1 ms after 1.6.0; delete the `oboe` (no unique commits) and `latency` (one 2023 commit) branches | 2027-03-15 | Keeps the build pure Kotlin for one developer; the audit's own advice is to measure before escalating; the contract change is cheap before the sound palette and expensive after. |
| D6 | Widen BPM to 20–300 in 1.4.0, before the renderer? | Yes, with `maxStreams` 8 and trimmed WAV tails as the interim voice budget; re-verified on the renderer in 1.6.0 | 2026-11-20 | 40–220 is the narrowest of any credible competitor and narrower than Google's widget; the engine handles fractional intervals; the voice-stealing risk is testable on device before the tag. |
| D7 | Should x/8 meters be felt as eighths (current) or as a dotted-quarter pulse, and which is default? | Both, as a per-meter pulse option remembered per meter, defaulting to eighth for 1.3.x continuity | 2026-11-20 | Existing presets and captions assume eighths; classical and drummer feedback wants the dotted quarter; an option preserves both without a silent semantic change. |
| D8 | Keep the 1.4× pitch-shifted accent, or move to real accent samples? | Placeholder: normalise loudness now, pre-render the accent buffer in 1.6.0, ship real accent and sub samples per set in 1.8.0 | 2027-05-01 | A pitch-shifted copy is the audible equivalent of a stock slider; pianists ask for a distinct beat-1 voice. |
| D9 | How to publish the timing figure? | p95 inter-onset deviation and visual offset per route, before and after, on ≥ 3 Android and 2 iOS devices, dated, with method, on `docs/timing.html`; store copy says "measured on real devices" and links, never a bare number or a comparison | 2027-05-15 | PRODUCT.md forbids unverifiable benchmarks; a method-linked figure with a before-and-after delta is harder to attack than a bigger number. |
| D10 | Which accessibility conformance target does the product state, and when? | WCAG 2.2 AA as interpreted for mobile, stated in PRODUCT.md and both store descriptions once R6-03's traversal checklist passes | 2027-05-01 | Time Guru, Smart Metronome and Gap Click list accessibility as a feature and teachers recommend on it; the listing can claim it only once there is a checklist to stand behind. |
| D11 | Localisation scope and reviewers? | ES and DE in 1.8.0 with paid native-musician review against a glossary; pt-BR and JA only if reviewers are booked by this date | 2027-06-01 | Wrong musical terms damage trust faster than English-only; two well-reviewed locales beat four unreviewed ones. |
| D12 | Monetisation in these twelve months? | None; record the decision in `docs/monetisation-decision.md` before 1.8.0 development starts, with a written rule that nothing already shipped ever moves behind a paywall; a tip jar in About is the only candidate for the following year | 2027-06-15 | Free-with-everything is the only durable advantage against incumbents with 20K–180K ratings; nothing in this plan needs revenue to execute; the decision must precede the swing and sound work a later tier might cover. |
| D13 | Capacity assumption and cut order? | Confirm about ten developer-days a month; the per-release cut orders in §7 apply automatically; if the honest number is under eight, drop R7-03 to four sets, R7-04 to ES only, and move R6-06 to the following year | 2026-09-12 | The plan totals ~116 item-days; a three-month gap already happened between 1.2.1 and 1.3.0; the cut order must be agreed before the first train. |
| D14 | Does the accented beat become the mark's triangle, and does the held 1.12 scale survive as a cursor? | The strike returns fully to rest and the indicator ring is the only cursor; the triangle accent is prototyped behind a build flag during the 1.5.0 cycle and ships only if the March critique reads it as signal, not ornament | 2027-02-01 | Two scale states on one dot dilute the strike; the triangle amends DESIGN.md's shapes rule, so it earns its place by critique rather than by schedule. |
| D15 | New-install defaults for haptics, flash and count-in? | Beat haptics on for new installs from 1.7.0; colour flash and count-in remain off; existing users unchanged | 2027-05-01 | Every physical channel is off today, so the first play is a 5 KB sample and a scaled dot; haptics on matches category defaults without adding visual noise. |
| D16 | Watch companions and widgets: which first, and is hardware available? | Out of these twelve months; decide the order (Apple Watch remote first, Wear OS second) and buy or borrow one watch per platform by this date so a two-day haptic-latency spike can open the next roadmap | 2027-07-01 | Each companion is a new surface with its own timing problems; the phone engine must be measured and proven first; the MediaSession service and the Live Activity bridge are the seams both would build on. |

---

## 7. Capacity, cut order and slip policy

**Assumption (D13):** one developer at about ten developer-days a month, roughly 113–120 days from 5 September 2026 to 30 September 2027, against 118 item-days planned (the two over-budget releases, 1.4.0 and 1.8.0, carry the cut that brings them to budget), with each window budgeted at its calendar length × 2.25 days a week. Device verification and store publishing are counted inside each release's closing item rather than hidden.

| Release | Item-days | Budget | Cut order (first cut first) | Never cut |
|---|---|---|---|---|
| 1.3.0 | 7.5 | ~7 | R1-04 (site residuals, Share) → 1.3.1 | R1-01, R1-02, R1-03: the release *is* the listing |
| 1.3.1 | 15 | ~15 | R2-08's iOS idle-engine half → 1.5.0; R2-05's m:ss progress layer → 1.4.0 | R2-09, R2-10: 1.4.0 changes the engine |
| 1.4.0 | 18.5 | ~17 | R3-06 → later; R3-05 → 1.5.0; R3-04 → 1.5.0 (fixture tests still guard the meter migration) | R3-02 codec migration tests |
| 1.5.0 | 19.5 | ~18 | R4-06 slider hygiene → 1.7.0; R4-05 → 1.7.0; R4-06 numerals → 1.7.0 | R4-01, R4-02, R4-04's motion gate: the March What's New is the Android lock-screen story and nothing new animates without an off-switch |
| 1.6.0 | 18 | ~18 | R5-03 → 1.7.0 (iOS is already within one render quantum; Android has the audible problem). Nothing else moves; the release is delayed instead, because a half-landed renderer is worse than a late one | R5-04 baseline before any renderer commit |
| 1.7.0 | 17 | ~18 | R6-06 → 1.8.0; R6-05 → 1.8.0 | R6-02, R6-04: the timing page is the trust story and strings gate localisation |
| 1.8.0 | 22.5 | ~20–21 | Two of the eight sound sets (brings the release to budget); pt-BR and JA (already conditional); R7-01's ramp half | R7-02 swing: the last high-importance shopper gap |

**Slip policy.** A release ships with fewer items rather than later. If a cut would remove the release's What's New theme, the theme is rewritten from what remains, not the date moved. Patch releases (x.y.1) are allowed for any crash on the current release and ship within 14 days. If two consecutive releases each lose more than a quarter of their scope, D13 is re-opened and the following release is dropped from the train rather than every release shrinking silently.

**What was cut from the proposals to make the year fit:** both watch companions (~15 days) and both widget stacks (~7 days) with a dated decision instead; the fully custom slider (4 days) replaced by a 1-day hygiene pass; the button-family and settings-restructure project (~4.5 days) moved to the hygiene track; tablet scaling (2.5 days) dropped; Practice Set state on the Lock Screen (2.5 days) and richer set library rows (2 days) dropped; gap modes and ramp-by-time merged into one item; the triangle accent turned into a critique-gated prototype; localisation trimmed to ES and DE. Moved earlier than any proposal: the engine test (November, before meters change the engine), export (January, in the same release as the meter migration), the Android service and focus policy (March). Moved later than the trust-first proposal: the renderers (May, after ratings exist and the device set has grown to three OEMs).

---

## 8. Risks

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Solo-developer bandwidth falls below the assumed ten days a month and releases slip into each other or arrive unfinished | high | high | Fixed eight-week trains with a written cut order; verification and publish items never cut; ship with fewer items rather than later; patch releases allowed; capacity confirmed at D13 before the first train |
| The 1.6.0 renderer regresses audio on OEMs the device set does not cover (underruns, Bluetooth stutter, route-change silence, standby) | medium | high | SoundPool behind a hidden setting for one release; Play staged rollout 10/50/100 over two weeks gated on crash-free users and vitals; a TestFlight external group; three OEMs in the set by 1.5.0; underrun count as a Crashlytics key; the iOS renderer moves to 1.7.0 first if Android slips |
| App Review flags iOS background-audio behaviour, or the Play data-safety correction triggers a policy hold | low | high | R2-08 stops the idle engine before any renderer work; the smaller truthful declaration is filed with the 1.3.0 build and kept identical to `privacy.html`; the listing describes background playback plainly |
| The rename and category change disrupt the one query the app ranks for, and ratings stay below the render threshold for reasons outside engineering | medium | high | Keep the old name as a keyword for one cycle; target the long-tail terms the landscape shows are winnable; Share row and the existing restrained prompt; every release carries a musician-readable What's New; if March 2027 shows under 20 ratings per store, add a one-time rate card after the first completed Practice Set, still no nag |
| Codec migrations (meters in 1.4.0, swing in 1.8.0, the backup envelope) corrupt presets or sets for users upgrading from 1.3.x | medium | high | Fixture-based migration tests from real payloads of every shipped version; tolerant decoding as the rule; export lands in the same release as the meter migration so a user can back up first; staged rollout |
| Android 14–16 foreground-service and wake-lock rules block or kill the service on some devices, especially Samsung | medium | medium | Start the service only from the foreground Play tap; test on API 34, 35 and 36 and a Samsung in the 1.5.0 matrix; hold the wake lock only while playing; document the OEM battery-optimisation caveat in `support.html` |
| The measured figure is worse than competitors' marketing claims and publishing it looks like a weakness | medium | medium | Publish method + figure + what is not measured, with a before-and-after delta; the claim is "measured, on real devices, here is how", never "the smallest number" |
| Craft critics and breadth shoppers both stay unsatisfied for part of the year (custom slider and button family deferred; swing and sounds last) | medium | medium | Strike, tonal tier, numerals and slider hygiene land in March and refresh every frame; range, meters and exact tempo land in January; store copy never claims breadth it lacks; if two or more reviews in a month cite sounds or swing, R7-03 moves ahead of R6-06 |
| iOS `AVAudioTime` scheduling loses its reserve buffers on a route change and the click stalls or doubles | medium | medium | Re-anchor the lookahead on the configuration-change observer landed in R4-03; flush and reschedule from the next deadline; AirPods connect and disconnect are in the matrix |
| Machine-translated musical terms read wrong to native musicians and produce low ratings in the storefronts meant to grow installs | medium | medium | Glossary first; a locale ships only after native-musician review is recorded; coverage guard test; a locale's listing pulled back to English if its storefront rating underperforms |
| Sound-set authoring and licensing take longer than the code (the sizing pass put R7-03 at 8–14 days if samples must be found or recorded) | medium | medium | Source CC0 or paid packs early in the 1.7.0 cycle; the cut order drops to six sets, then four; the normalisation pipeline from R2-08 is reused unchanged |
| Toolchain drift (Kotlin, Compose Multiplatform, compileSdk, Xcode) breaks builds mid-release | medium | medium | Upgrade dependencies only in the first week of a cycle; pin versions; the release checklist includes a clean build of both platforms from a fresh clone |
| The `MetronomePlayer` contract change breaks the web demo or leaves it claiming a precision it does not have | medium | low | Pass `when` to `src.start` in the same release and correct the comment; the web build is a demo, not a metric |

---

## 9. Parking lot: deliberately excluded, with the trigger that brings each back

| Item | Why not this year | Trigger to schedule |
|---|---|---|
| Apple Watch and Wear OS companions | The most-requested platform feature in reviews, but each is a new surface with its own audio and haptic timing problems and needs hardware the developer does not own; not before the phone engine is measured and proven | D16 decided and hardware in hand; first item of the next roadmap, Apple Watch remote first |
| Home-screen and lock-screen widgets | The Live Activity plus the MediaSession notification cover the lock-screen story this year | 1.8.0 shipped; the Android widget rides on the R4-01 service |
| A fully custom `AppSlider` and a rotary tempo dial | The stop dot, thumb gap and bar thumb are removed cheaply in R4-06 | First feel item of the following year |
| Button family, app-bar, empty-state and dialog consolidation as a project | Handled through the hygiene track in files each release already touches | Never as release scope |
| Composition scaling on expanded height; tablet and landscape layouts | DESIGN.md asks for evidence first; only the vertical centring ships | iPad or tablet share visible in the consoles |
| Polyrhythm UI | Gated on the unified scheduler, which lands in 1.6.0 with no cycle left inside the year | First depth candidate for the next roadmap |
| Gig setlists and program mode (song names, big display, meter and tempo changes inside a piece) | A second list concept beside goal-based Practice Sets | Export files show how large libraries get; reviews ask for gig use |
| Cloud sync, accounts, sharing links | PRODUCT.md non-goals; the account-free answer is the local export file | A specific user need and a written privacy contract |
| Ableton Link, MIDI clock, pedal SDKs, proximity control | Only hardware keys and the free MediaSession controls ship | Stage-use reviews after 1.5.0 |
| Monetisation of any kind | D12; free-with-everything is the only durable advantage | The D12 record, and only a tip jar |
| Tuner, recorder, drum machine, drones | PRODUCT.md and review non-goals; roundups punish kitchen-sink apps | Never |
| Quintuplets, sextuplets, the per-subdivision accent and mute grid, voice counting | Swing and dotted cover the common asks | Reviews after 1.8.0 |
| Practice Set state on the Lock Screen and Android notification; set library previews and totals | Low cost but not this year's trust or shopper item | The next time the Live Activity or the sets library is touched |
| Practice history calendar, session notes, local reminders (previous roadmap P3) | Unproven retention effect; the no-pressure rule limits what reminders can do | Ratings and reviews show demand after 1.8.0 |
| Portuguese and Japanese | Conditional, not scheduled: only with booked native reviewers | D11 |
| Web demo work beyond hosting (service worker, size, accessibility tree, persistence) | The browser build is a demo; only its timing wording is in scope | Site analytics showing meaningful `/app/` traffic |
| A new icon or visual identity | Only the PPO test of the two strategy variants; the triangle accent is a critique-gated prototype | D14 and the PPO result |
| Custom analytics events or any telemetry beyond Crashlytics | The plan measures with consoles, Crashlytics, stopwatches, recordings and a fixed rubric | A decision to change PRODUCT.md's local-by-default principle, which is not expected |
| Reorder drag polish, the half-animated theme switch, cold-start set build-up | Low-severity feel items | When their screens are next touched |

---

## 10. Explicit non-goals (unchanged)

- No account requirement for core practice.
- No subscription pressure, advertisements, or artificial feature friction; nothing already shipped ever moves behind a paywall.
- No leaderboards, levels, badges, celebratory clutter, or "10,000-hour" mechanics.
- No dashboard replacing the metronome as the primary surface.
- No cloud synchronisation until there is a specific user need and a privacy contract.
- No tuner or recorder expansion before the core timing and structured-practice experience is excellent.
- No published precision number that was not measured on real devices with a published method.

---

## 11. Technical debt register

Carried from the previous roadmap and extended by the review. Each entry names the item that closes it.

| Debt | Closed by |
|---|---|
| Sessions spanning midnight credit all time to the pause day | R2-07 |
| Streaks do not re-evaluate at midnight while the app stays open | R2-07 |
| Practice time credited only on pause (lost on force-quit) | R2-07 |
| Privacy policy and Play data declaration disagree | R1-03 |
| Onboarding coach marks reference beat circles and mis-target the slider | R6-06 (the pendulum clause is practically unreachable and is dropped) |
| BPM and accent pattern not persisted | R2-01 |
| Colour scheme persisted by enum ordinal | R2-01 |
| Meters wrap into two rows on common phone widths | R2-02 |
| Live Activity stale after 180 s of steady playback | R2-03 |
| iOS Background Play switch is a no-op | R2-04 |
| Sounds differ ~16 dB in loudness; MP3 pre-roll; duplicate `wood.mp3` reference | R2-08 |
| Idle iOS engine and active session after a sound switch while stopped | R2-08 |
| `MetronomeEngine` loop untested and non-injectable | R2-09 |
| Physical-device audio, TalkBack and VoiceOver never verified (release gate pending since 1.3.0) | R2-10, R6-07 |
| Bound-only Android service, no wake lock, Stop-only notification | R4-01 |
| All Android focus losses stop playback; no resume on either platform | R4-02 |
| iOS silent-failure path; no reset observers; lazy engine for App Intents; string-typed `timerKind`; two session owners | R4-03 |
| Held beat state, ring lag, out-of-time halo, light-theme glow | R4-04 |
| `onSurfaceVariant` full ink; `tertiary == primary`; outline roles unset; `Color.DarkGray` ladder | R4-05 |
| Type scale in 67 inline overrides; no tabular numerals; stock slider artefacts | R4-06 (rest through the hygiene track) |
| Wall-clock-triggered clicks (SoundPool, `atTime = null`); wasm comment claims sample accuracy | R5-01, R5-02, R5-03 |
| Visuals and haptics lead audio with no latency compensation | R6-01 |
| No reduce-motion handling | R4-04 gate, R6-03 completion |
| ~half of UI text hard-coded; inline duplicates of resources; no plurals; RTL arrows | R6-04 |
| Chips and meter pill without selection semantics; readout without live region; icon-name labels | R3-03, R6-03 |
| Countdown drift; stopwatch cannot reset; "+5" starts a fresh timer; trainer edit restarts the ramp; gap phase lost on resume | R6-05 |
| Stale `oboe` and `latency` branches | R5-02 (delete) |
| `MetronomeViewModel.kt` at 762 lines | Hygiene track, opportunistic |
| No shared button family; `FilterChip`; two app-bar styles; two empty states; two dialog surfaces; duplicated one-offs; dead tokens | Hygiene track, one per release |
| DESIGN.md motion and press figures stale | Docs and claims track (1.5.0) |
| Practice Set state absent from the Lock Screen; set library rows show only name and dots | Parked (§9) |

---

## 12. Technical feasibility notes

Findings from the code-grounded sizing pass that shape how items are built. Each was checked against the repository on the adoption date.

| Topic | Verdict | What it means for the plan |
|---|---|---|
| Android sample-accurate renderer: pure-Kotlin `AudioTrack` versus Oboe/NDK | Pure-Kotlin `AudioTrack` `MODE_STREAM` with `PERFORMANCE_MODE_LOW_LATENCY` is sufficient and the right first step; Oboe is optional headroom, not a requirement | R5-02 stays pure Kotlin; D5 escalates to Oboe only on a measured miss |
| iOS `scheduleBuffer(at:)` lookahead from Kotlin/Native | Feasible entirely from the existing AVFAudio cinterop; use sample time from the player node's timeline (`lastRenderTime` → `playerTime(forNodeTime:)`) rather than converting a Kotlin monotonic mark to mach host time | R5-03 as written |
| A shared `MetronomePlayer` contract all three players can implement | Feasible with one relative-time contract: `schedule(event, dueIn)` plus `outputLatency()`; each player maps the due time onto its own clock (frame position, node sample time, Web Audio `currentTime`) | R5-01 as written; the web player passes `when` to `start()` |
| Android `MediaSession` + started foreground service + wake lock | Feasible with platform APIs only (no Media3 needed); the manifest is already most of the way there (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `foregroundServiceType`); every start is user-initiated so Android 14+ start rules are satisfied | R4-01 as written; needs `WAKE_LOCK` |
| Wear OS and watchOS companions | Wear OS: feasible as a new Android-only module reusing the shared model and timeline. watchOS: feasible only as a SwiftUI app with its own scheduler, or after a Compose-free `:core` module split, because `shared/build.gradle.kts` puts Compose in `commonMain`; an on-watch Kotlin loop also needs the injectable beat loop from R2-09 | Input to D16 and the next roadmap; not this year |
| Compose Multiplatform 1.12.0 web accessibility for `ComposeViewport` | An accessibility root exists on web since 1.9.0 and 1.12.0 fixed it being 0×0, but it is undocumented and beta-grade | Do not promise screen-reader support on the web demo (parked, §9) |
| Toolchain currency | Kotlin 2.4.10 is the newest published (2.4.20 planned September 2026); Compose Multiplatform 1.12.0 is the newest stable; Koin 4.2.2 is current; AGP 9.3.2 is one minor behind. The resolved material3 artifact is 1.9.0 | Upgrade only in the first week of a cycle (§8) |
| Replacing the M3 slider thumb and track | Feasible with the public slot APIs (`@ExperimentalMaterial3Api`); the default thumb also draws a desktop/web focus ring that the custom thumb must handle | R4-06 hygiene now; the fully custom control next year |
| Tabular numerals via `fontFeatureSettings` | Feasible on Android, iOS and wasm with `TextStyle(fontFeatureSettings = "tnum")`; every font in play carries the feature; the readout's `TextAutoSize.StepBased` re-measures per step and becomes stable once widths stop changing | R4-06 as written |
| Reduce-motion abstraction (interface + Koin, CompositionLocal) | Feasible; the platform observers are small; the cost is in how `AppAnimations` consumes the flag (about half a day for a global snap flag, about a day and a half for a fully compositional design); the pendulum and splash use tweens and must be special-cased; a runtime toggle on iOS needs the notification observer | The global flag ships as R4-04's gate; the compositional completion is R6-03 |
| Persisting BPM and per-meter accents via `AppDatastoreImpl` | Feasible in about half a day; the only subtlety is init ordering against session recovery, which needs the ViewModel test harness from R1-01 | R2-01 depends on R1-01's harness |
| A `MetronomeViewModel` test harness in `commonTest` | Feasible in about half a day with `Dispatchers.setMain` or an injected scope; reset in `@AfterTest` to avoid leaks between tests | Built in R1-01, reused by every ViewModel item |
| Sound palette sourcing | Code risk is low; the effort is authoring and licensing (CC0 or paid packs), which the sizing pass put at 8–14 days for eight sets | R7-03 at the low end of the range with a cut order to six or four sets |
| Codec migrations | Decoding a stored meter name that fails silently drops the preset today; every codec bump must ship with fixture tests from real payloads of every shipped version and tolerant decoding | R3-02, R3-04, R7-02 carry fixture tests; export lands with the first migration |

## Appendix A. Traceability: review findings to roadmap items

High and medium findings from `docs/product-review-2026-09.md` Appendix A. Low findings are closed opportunistically by the item that touches their file.

| Finding | Item | Finding | Item | Finding | Item |
|---|---|---|---|---|---|
| main#1 | R2-01 | tempo#1 | R6-05 | android#1 | R5-01, R5-02 |
| main#2 | R2-02 | tempo#2 | R6-05 | android#2 | R4-02 |
| main#3 | R3-05 | tempo#3 | R7-01 | android#3 | R4-01 |
| main#4 | R3-05 | tempo#4 | R7-01 | android#4 | R5-02 |
| main#5 | R3-03 | tempo#5 | R3-02, R7-02 | android#5 | R6-01 |
| main#6 | R2-06 | presets#1 | R2-05 | android#6 | R2-09 |
| main#7 | R3-03 | presets#2 | R2-05 | android#7 | R2-08 |
| main#9 | R2-01, R3-01, R3-02 | presets#4 | R2-05 | android#8 | R5-01 |
| main#10 | R6-06 | presets#8 | R2-05 | ios#1 | R5-03 |
| settings#1 | R2-04 | motion#1 | R4-04 | ios#2 | R2-03 |
| settings#3 | R2-08 | motion#2 | R4-04 | ios#3 | R6-01 |
| settings#7 | R2-07 | motion#3 | R4-04 | ios#4 | R4-03 |
| settings#8 | R6-06 | motion#4 | R4-05 | ios#5 | R2-08 |
| a11y#1 | R6-04, R7-04 | motion#5 | R4-05 | ios#6 | R4-03 |
| a11y#2 | R6-04 | motion#7 | hygiene track | ios#7 | R2-08, R4-02, R4-03 |
| a11y#3 | R3-03 | motion#8–10 | hygiene track | craft#1 | R4-04 |
| a11y#4 | R4-04, R6-03 | motion#11 | R4-06 | craft#2 | R4-06 |
| a11y#5 | R6-03 | motion#13 | hygiene track | craft#3 | R4-06 |
| a11y#6 | R6-03 | motion#14 | R4-04, R6-03 | craft#4 | R4-04 |
| a11y#10 | parked (§9) | motion#15 | R4-06 | craft#5 | R3-05 |
| a11y#11 | parked (§9) | motion#16 | R4-06 | craft#6 | hygiene track |
| a11y#12 | R3-07 / store track | motion#17 | parked (§9) | craft#7 | R2-08, R4-05 |
| a11y#13 | R1-02, R1-04 | motion#18 | parked (§9) | craft#8 | R4-05 |
| store#1–3, #5–11 | R1-02, R4-07 | firstrun#1 | R4-04 | firstrun#2–5 | R6-06 |
| store#4 | store track (PPO) | firstrun#6 | D14 prototype | firstrun#7 | R6-03, D15 |
| firstrun#8 | R4-04 | | | | |

## Appendix B. Where the previous roadmap's open items went

| Previous item | Now |
|---|---|
| P0: on-device audio checks across BPM, subdivisions, sounds, interruptions, Bluetooth, background | R2-10 protocol; run every release |
| P0: physical-device output latency as a release gate | R5-04 rig, R6-01 compensation, M4 |
| P0: validate iOS build-then-swap and Android load-then-swap/focus on devices | R2-10, R4-02 |
| P0: VoiceOver and TalkBack traversal, text scaling, reduced motion, compact screens | R4-04 (motion gate), R6-03, R6-07; accessibility track |
| P0: move hard-coded English into resources, then ES, DE, PT, JA | R6-04, R7-04 (PT and JA conditional per D11) |
| P0: split `MetronomeViewModel` into focused state holders | Hygiene track, opportunistic; the engine seam extracted in R2-09 |
| P2A: long-press shortcuts | R3-03 (hold-to-repeat) |
| P2A: Android home-screen widget on the Live Activity seam | Parked (§9), rides on R4-01 |
| P2B: hardware keyboard and Bluetooth pedal shortcuts | R3-06 (keys); pedal SDKs parked |
| P2B: Apple Watch and Wear OS controls | D16; next roadmap |
| P2B: lock-screen and widget shortcuts | R4-01 (Android MediaSession); widgets parked |
| P2B: Ableton Link and MIDI clock | Parked (§9) |
| P3: local session history, per-preset time, trends, notes, calendar, reminders | Parked (§9); stats made trustworthy by R2-07 |
| P3: export and delete controls | R3-04 |
| Polyrhythms after the unified scheduler | Parked; scheduler lands in R5-01 |
| More click sounds and per-sound accent pitch | R7-03 |
| Set sharing preserving local-first use | R3-04 (file export); links parked |

## Appendix C. Shipped before this roadmap (1.0.0 to 1.3.0)

**Practice tools redesign (1.1.0–1.2.1).** Practice timer with bottom-sheet configuration, mode-aware chip, extend and restart, custom durations. Tempo sheet with presets, saved tempos, subdivisions, tempo trainer and gap trainer in a single-expanded accordion. Ascending and descending tempo trainer with persisted configuration. Gap trainer with play and muted bar cycles. Eighth, triplet and sixteenth subdivisions. Per-beat accent and mute. Saved tempo bookmarks. Daily, streak and total practice statistics stored locally. Count-in, pendulum display, keep-screen-awake, UI haptics, multiple colour schemes. Android and iOS background playback. Android load-then-swap click switching, serialized iOS audio graphs, predictable focus and interruption stops. iOS Live Activity and Dynamic Island controls. Shared Compose component and motion system. Native in-app review prompt after a qualifying practice pause. Accessibility semantics, 48dp targets, responsive typography, adaptive 480dp content width. Typed Navigation 3 back stack with saved state, entry-scoped ViewModels, directional and predictive-back transitions. Drift-free cumulative monotonic scheduling with fractional intervals (1.2.1).

**Structured practice foundation (1.3.0, on `main`, unpublished at adoption).** Stable, editable, named Practice Presets capturing BPM, meter, subdivision, accents and mutes, and count-in. Lossless migration from saved tempos, with favourites, recents, reordering, rename, duplicate, apply, delete and validation states. Ordered Practice Sets with optional duration or bar targets per step. Previous, pause, resume, next, finish, recovery and replacement-conflict handling. Safe preset changes at playback boundaries and one restrained active-practice strip. Practice Again for the most recently completed set. Review-prompt suppression while any practice tool is active. What's New sheet. 139 unit tests. Designs in `docs/superpowers/specs/2026-09-01-practice-presets-foundation-design.md` and `2026-09-01-practice-sets-design.md`.
