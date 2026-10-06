# Metronome — Product Review, September 2026

> Status: review complete; no code changed.
> Scope: value, beauty, appeal, reliability and market position of `Metronome: Practice & Tempo` 1.3.0 (versionCode 9, branch `dev` at c374edd), against the metronome category on both stores.
> Method: 8 parallel code readers (one per subsystem) + 5 research agents (competitors, platform defaults, store standing, musician needs) + 3 screenshot critics → 111 high/medium findings → adversarial verification by two independent readers per area (code-truth lens and user-impact lens, tiebreak on disagreement). Verification completed for the main surface, tempo sheet, presets/sets and settings; the remaining areas were verified by hand in this session against the cited lines. Every finding in Appendix A carries its verification mark.
> Companion: `docs/competitive-landscape-2026-09.md` (the competitor detail this document summarises).
> Evidence standard: no claim here is from memory. Store, DNS and crash facts were fetched on 2026-09-05; code claims cite `file:line` on `dev`; the 139 shared unit tests were run and pass.

---

## 1. Executive summary

**Where it stands.** This is a technically serious, well-built app with essentially no market footprint. Both store listings still show the 1.2.1 build under the old name, with zero ratings on the App Store, no rating on Play, `1K+` installs, and a marketing site whose domain has never resolved. The 1.3.0 release, the rename and the new screenshot set exist only in the repository. Meanwhile the category is owned by incumbents with 20K–180K ratings each, and the two named differentiators (`tempo trainer`, `gap trainer`) are already contested terms. Honest position: zero-to-one, with the product ahead of its distribution.

**The product itself** is better than its footprint. It is free with no ads, no account, and every practice tool unlocked, which is the single most repeated reason musicians give five stars and the thing they punish Soundbrenner and Pro Metronome for. The beat scheduler is genuinely drift-free and tested. Practice Presets and Practice Sets are a structured-practice model no competitor offers free. The Live Activity and Dynamic Island controls are rare in the category. The design system is disciplined: one motion vocabulary, no hard-coded colours, Lucide only, 48dp targets everywhere.

**What holds it back** splits into four groups:

1. **Table-stakes gaps a shopper notices in the first minute.** BPM range 40–220 is the narrowest of any credible competitor (Google's own search widget goes to 218; Tempo 10–800; Metronome Beats 1–900). Six fixed meters, no 9/8, 12/8, 2/2 or custom. Three click sounds against a category norm of 12–32. No swing or dotted subdivisions. BPM and accent pattern reset to 80 and defaults on every cold launch.
2. **Feel.** Three independent design critics scored it 6, 6.5 and 5.5 out of 10 and agreed on the verdict: *clean, not beautiful*. A very well-executed Material 3 template in monochrome. The beat pulse is a held selection state, not a strike; the indicator ring cannot arrive on the beat above ~150 BPM; captions have no tonal tier because `onSurfaceVariant` is pure ink in every scheme; the tempo control is the stock M3 slider; numerals are proportional; the first run is a 1.5 s splash, three coach cards, and silence.
3. **Trust details.** 5/4, 6/8 and 7/8 wrap into two rows of dots on ordinary phones. The iOS Lock Screen reports "Session ended" after 3 minutes of normal playback. A reached time or bar target in a Practice Set gives no haptic or sound. The iOS Background Play switch does nothing. Any Android notification sound stops the metronome (a recorded decision, but one that costs mid-practice). The scheduler is drift-free but not jitter-free: clicks are triggered from a coroutine and handed to SoundPool / `scheduleBuffer(atTime = null)`, so per-click onset error is milliseconds, not microseconds, and nothing has been measured on a device.
4. **Distribution.** Old name, old screenshots, Utilities category, a Play data-safety declaration that contradicts the shipped Crashlytics, a feature graphic with no brand mark that claims "never drifts", a website with no DNS record, and two crash fixes sitting unreleased on `dev`.

**Scorecard** (1–10, where 5 is a competent generic utility and 10 is the best in the category):

| Dimension | Score | Why |
|---|---|---|
| Value | 6.5 | Free, complete practice toolkit, structured practice nobody else has; loses points for BPM range, meters, sounds, subdivisions, and non-persisted tempo |
| Beauty | 6 | Disciplined system, correct hierarchy, one good motion signature (play morph); every surface is still a stock component in monochrome |
| Appeal | 5.5 | Nothing identifies the app with the name covered; first run ends in silence; store presence unbranded and stale |
| Reliability | 6 | Drift-free, tested scheduling and robust session recovery; ms-class jitter, silent-failure paths, unshipped crash fixes, no device measurements |
| Market position | 2 | No ratings, no search presence, no reachable site; everything that would change this is written but not published |

**The five moves that matter most**, in order:

1. Ship what exists: 1.3.0 (versionCode 9, with both crash fixes) to both stores, the site via the Cloudflare Pages branch plus a DNS record, the rename, the fixed 5-frame screenshot set, and a reconciled data-safety form. Nothing else counts until people can find and rate the app.
2. Fix the six trust details that make the current build look unfinished (§7, items 4–10). All are small.
3. Make the beat physical: a strike instead of a held state, a ring that lands on the beat, a tonal tier for captions, tabular numerals, a first run that ends in sound. This is the gap between 6 and 8 and it is not decoration.
4. Close the table-stakes gaps a shopper checks first: BPM 20–300 (or wider), a beats-per-bar picker with compound meters, persisted tempo, normalised and auditioned sounds.
5. Then earn the precision claim: a sample-accurate renderer on both platforms, latency compensation for visuals and haptics, and a measured jitter figure the listing can state.

---

## 2. Where it stands

### 2.1 Store and web footprint (fetched 2026-09-05)

| Surface | State |
|---|---|
| App Store `id6761737690` | Named **Metronome: Feel the Beat!**, subtitle "Metronome for musicians". Version **1.2.1** (16 Jun 2026). "Not enough ratings" in US, GB, AU, DE, CA. Primary category **Utilities**, secondary Music. 37.9 MB. Three iPhone frames, all 1.0-era UI; none show trainers, presets, sets or Live Activities. |
| Google Play `com.merkost.metronome` | Same old name. **1K+** downloads. No rating section rendered (below Play's threshold). Category Music & Audio. Five 1.0.1-era frames, one of which reads "Metronome: Fill the Beat!". Full description still contains the inverted claim "a sleek design that will hold you back as you play". Data safety declares **No data collected** while the Android build ships Firebase Crashlytics, Analytics and Performance (`androidApp/build.gradle.kts:87-89`). |
| `metronome.merkost.dev` | **NXDOMAIN.** The `merkost.dev` zone is at Cloudflare with no record for the subdomain. GitHub Pages is configured for `main:/docs` with this CNAME, so `merkost.github.io/Metronome/` 301s into the dead domain. Wayback has zero snapshots: the site has never been publicly reachable. `main` still links the dead App Store id `6480380648` (404). |
| Web build `/app/` | Same fate as the site. 15.3 MB raw (8.6 + 6.2 MB wasm + 0.5 MB JS), ~5.1 MB gzipped. |
| Repository | `dev` is 71 commits ahead of `main`, at 1.3.0 versionCode 9. Branch `origin/chore/cloudflare-pages` (today) adds a GitHub Actions deploy to Cloudflare Pages with brotli, which would take the wasm to ~4.5 MB on the wire and fix the hosting; it still needs the DNS record. |

> **Addendum, later on 2026-09-05.** After this section was written, `dev` was merged into `main` (the branches are now identical), the Cloudflare Pages workflow was merged and a DNS record created: `metronome.merkost.dev` now resolves (Cloudflare) and serves the rebuilt site and `/app/` over HTTPS. The dead App Store id and the design-process comment are gone from the published site. The stores still carry 1.2.1 under the old name, 1.3.0 is still untagged and unpublished, and Crashlytics is configured on iOS as well as Android (`iosApp/iosApp/iOSApp.swift:8`), which §6.6 and the plan in §7 should be read with. The roadmap in `docs/ROADMAP.md` starts from this later state.

### 2.2 Search visibility

Checked via the iTunes Search API (US, top ~190 results) and Play web search (top ~30):

- Not present for `metronome`, `metronome app`, `metronome practice`, `practice metronome`, `tempo trainer`, or `gap trainer metronome` on either store.
- The App Store results are dominated by Smart Metronome & Tuner (4.71 / 59.7K ratings), Pro Metronome (4.71 / 20.3K), Metronome Ϟ (4.65 / 37K), Soundbrenner (4.7 / 48K), TonalEnergy (4.8 / 58K). Play by Metronome Beats (4.76 / 182K), Soundbrenner (4.59 / 90K), Pro Metronome, Smart Metronome, Tack.
- On the open web, `gap trainer` belongs to Gap Click by Benny Greb; `tempo trainer` is owned by golf and fitness apps; `practice sets` has no product search intent at all (only how-to articles).
- The app appears for exactly one query: its own old name.
- The name PRODUCT.md still lists as "approved next store name", **Metronome: BPM & Practice**, is already a live competitor (`id6748179477`) that ranks for "metronome practice app". `docs/aso-strategy.md`'s **Metronome: Practice & Tempo** avoids the collision and should be treated as canonical; PRODUCT.md:41-43 needs updating.

### 2.3 Crash health and release state

Firebase Crashlytics, project `metronome-29a74`, 2026-06-07 → 2026-09-05:

| Platform | Fatal events | Issues | Notes |
|---|---|---|---|
| Android | 7 | 3 open | 6 on **1.3.0 (8)** (unreleased), 1 on 1.1.0 (5). 0 ANRs, 0 non-fatals. Single-digit impacted users. |
| iOS | 0 | 0 | |

The three issues: `AppNavigator.goBack` `NoSuchMethodError removeLast()` (5 events, 2 users; the API < 35 `SequencedCollection` trap), `MetronomeViewModel.stopGradualTempo` NPE (1 event; a ViewModel init-order bug on the session-recovery path), and an `ArrayIndexOutOfBounds` in the engine on 1.1.0. Both 1.3.0 crashes came from emulators within hours of their fix commits (b594b33 and 1a8c177 on 2–3 Sep), so they read as the developer's own testing. **Both fixes are on `dev` and neither has shipped**; the stores still serve 1.2.1. The `removeLast` crash would hit every back press on any real device below Android 15 running 1.3.0 (8).

Release cadence from CHANGELOG: 1.1.0 (11 Jun), 1.2.0 (13 Jun), 1.2.1 (15 Jun), 1.3.0 (3 Sep, unpublished). The 139 shared unit tests pass (`./gradlew :shared:testDebugUnitTest`, 25 suites, 0 failures). The release AAB is 7.8 MB, up 17% from 1.2.1.

### 2.4 Competitive position in one table

Full detail, prices, ratings and sources are in `docs/competitive-landscape-2026-09.md`. The short version:

| | Metronome (this app) | Soundbrenner | Pro Metronome | Tempo (Frozen Ape) | Metronome Beats | Tack | Time Guru |
|---|---|---|---|---|---|---|---|
| Price | Free, no IAP | Free + $7.99/mo or $59.99/yr | Free + $3.99 or $0.99 unlocks | $2.99 one-time | Free + ads; Pro $9.99 | Free, open source | $2.99 one-time |
| Ratings | 0 / 0 | 4.7 · 48K / 4.6 · 90K | 4.7 · 20K / 3.6 · 22K | 4.8 · 4.9K / 3.7 · 1.8K | 4.7 · 5.4K / 4.8 · 182K | – / 4.9 · 1.6K | 4.5 · 450 / 4.6 · 700 |
| BPM range | 40–220 | 20–400 | wide | 10–800 | 1–900 | wide | 5–300 |
| Meters | 6 fixed | custom | additive, custom | 35 | any | any | chains to 16/x |
| Sounds | 3 | 20+ | 13 + voice | 14 + voice | custom + pitch | several | 35 + voice (5 languages) |
| Subdivisions | 8th, triplet, 16th | yes | dotted, poly (paid) | patterns | up to 16, swing, poly | swing, poly | patterns |
| Trainers | tempo + gap, free | both paid | both paid | Coach + Automator | speed trainer | tempo | random/pattern mute |
| Structured practice | Presets + Sets with goals, recovery | tracker, goals (account) | playlists | setlists, Gig mode | songs w/ sections (Pro) | song library | chains |
| Lock screen | Live Activity + Dynamic Island | widgets | widgets | – | media controls | Wear OS | lock-screen player |
| Watch | – | – (hardware instead) | Apple Watch | Apple Watch | Apple Watch | Wear OS | – |
| Design | calm monochrome, clean | polished, brand-heavy, "bloated" | dense pro panel | dated LEDs | utilitarian | Material You, praised | "isn't pretty" |

### 2.5 Advantages and gaps

**What this app has that the field does not, or charges for:**

- Free with no ads, no account, no paywall on trainers, presets, haptics or flash. Every 2025–26 roundup rewards exactly this and penalises Soundbrenner (10-song cap, nag screens) and Pro Metronome (subdivisions and trainers paywalled; past purchases invalidated).
- Practice Sets with time or bar goals, pause/resume, recovery and Practice Again. The closest analogues are Tempo's setlists plus Automator, Smart Metronome's Program Mode and BackBeat's Sequence Builder; none combine goals with session recovery, and none are free.
- Local daily/streak/total stats without an account. Soundbrenner's tracker is its headline feature and requires an account and a subscription.
- iOS Live Activity and Dynamic Island play/pause. No researched competitor lists it.
- One KMP codebase with real Android parity. Pro Metronome's Android build is rated 3.6 and its own description calls it "far from perfect"; Tempo's Android is 3.7 with fewer features; Time Guru's and Metronomics' Android builds lag; Tack, Tempo Advance, ONYX and Dr. Betotte are single-platform.
- Actively maintained in a field of dormant apps (Pulse 2018, Metronomics Android 2022, Dr. Betotte and Gap Click 2023).
- Stereo pan, count-in on both platforms, Material You plus five schemes, a browser build.

**What the field has that a shopper will check for and not find:**

| Gap | Importance | Who has it |
|---|---|---|
| BPM range 40–220 | high | every competitor; Google search widget 40–218 |
| Six fixed meters, no 9/8, 12/8, 2/2, custom numerator, additive groupings | high | Tempo (35), Time Guru, Tack, Metronomics, Pro Metronome, True (1–13) |
| Three sounds, no voice count, no pitch | high | 12–35 sets is the norm |
| No swing, dotted, quintuplet subdivisions; no per-subdivision accent | high | Tack, True, Beats, Pro Metronome, Metronomics, Takt |
| No published timing figure | medium | Pro Metronome ±20 µs, Smart Metronome 1/44,100 s, ONYX "lab-tested", Korg ±0.02% |
| No watch app | high (most-requested platform feature in reviews) | Tack (Wear OS), Tempo, Pro Metronome, True, Pulse, Metronomics |
| No gig setlists (song names, notes, big display), no export/backup | medium | Tempo, Beats Pro, Soundbrenner, Time Guru |
| Gap trainer fixed N-on/N-off only; no random mute, no scoring | medium | Time Guru, Takt, Metronomics, Gap Click |
| No pedal / keyboard / media-button control | medium | Tempo, Pro Metronome, Soundbrenner, Beats |
| No polyrhythm | low–medium | Tempo Advance, Tack, Beats, Pro Metronome |
| No home-screen widget | medium | Soundbrenner, Pro Metronome, True |

---

## 3. Value

### 3.1 What musicians ask for, and whether the app answers

From App Store and Play reviews of the top apps, Reddit (r/drums, r/Guitar, r/piano), Piano World, TalkBass, Gearspace and teacher blogs (`research-musician-needs`):

| Complaint about metronome apps | Frequency | Does this app solve it? |
|---|---|---|
| Ads during practice, inappropriate ads | very common | **Yes.** The strongest driver of 5-star reviews and recommendations. |
| Subscription pressure; basics paywalled; features moved behind a wall later | very common | **Yes.** Keep it so; the angriest reviews in the category are about features that used to be free. |
| Account required; libraries vanish | common | **Yes**, with the flip side: no export or backup, so a lost phone loses presets and stats. |
| Stops or loses controls when the phone locks | very common | **Yes** on iOS (background audio, Live Activity). On Android the foreground notification has only a Stop action, no play/pause and no MediaSession (`MetronomeService.kt:80-97`). |
| Timing drift, stutter at high BPM, visuals out of sync | common | **Partially.** Drift is solved; jitter and visual/haptic lead are not (§6.1). No measured figure. |
| Not loud enough over drums, piano, brass | common | **Partially.** Wood is loud; Classic peaks ~16 dB quieter and its right channel is 6 dB below its left (§3.5). |
| Too complicated, bloated, marketing home screens | very common | **Largely yes.** The main surface is tempo, beats, play. |
| Settings do not persist | common | **No, for the setting that matters most.** BPM and accent pattern reset every launch (§3.2). |
| Setlists that cannot be edited, reordered, duplicated | common | **Partially.** Presets can; sets cannot be duplicated or favourited. No gig setlist with song names. |
| Limited time signatures, 6/8 as six quarters | common | **No.** Six fixed meters; x/8 meters click the eighth at the shown BPM with no dotted-quarter pulse. |
| Missing or paywalled subdivisions (dotted, quintuplet, swing) | common | **Partially.** Three free subdivisions; nothing else. |
| Fiddly exact tempo; cannot type a BPM | common | **Mostly.** Slider, ±1, ±5, ×2, ÷2, tap, presets. No hold-to-repeat, no typing, ~2 dp per BPM on the slider. |
| Click on 2 and 4, half-time, off-beat | common | **Mostly.** Per-beat mute gives 2-and-4; ÷2 gives half time; off-beat-only is impossible (mute is per beat, not per subdivision); no swing. |
| Watch / wrist haptics | common | **No.** |
| Voice counting for beginners and singers | occasional | **No.** |
| Crashes, slow launches | common | Unreleased 1.3.0 had two crashes, both fixed on `dev`; iOS has zero in 90 days. |

What makes musicians recommend an app: a clean free tier ("no gimmicks"), restraint ("everything you need, very little you don't"), longevity, gap and speed trainers that "revolutionized my practicing", a timer inside the metronome, loud pleasant clicks, sensible defaults, a responsive developer, and privacy. This app is well aligned with most of that list; the misses are loudness, sound variety and the watch.

### 3.2 The instrument (main surface)

Verified by two independent readers (V2) unless marked.

- **BPM and accent pattern are lost on every cold launch** (V2, high). `MetronomeViewModel.init` (:714-721) restores time signature and subdivision only; there is no BPM key in `AppDatastoreImpl.kt:20-50`; `MetronomeState.kt:18` defaults to 80. The ViewModel is a Koin single with no saved state, and init overwrites `beats` with the meter default even if kept in memory. A musician opens the app to "Andante · Eighths" with their meter intact and their tempo gone: a half-restored state. Only presets and a recovered Practice Set bring BPM back.
- **5/4, 6/8 and 7/8 wrap into two rows** (V2, high; the impact reader called it *understated*). Each dot occupies a 48dp hit box (`MetronomeBalls.kt:213`); with ≤5 beats spacing is 32dp (`MainScreen.kt:359-361`), so 5/4 needs 368dp but a 393pt iPhone gives 357dp → rows of 4 + 1. On the most common Android width (360dp) even 6/8 wraps to 5 + 1. The indicator ring then travels diagonally every bar. This is a regression from the 1.3.0 accessibility pass that enlarged the invisible hit area.
- **The only door to subdivisions, trainers, presets and sets is a chip labelled with an Italian tempo word** (V2; deliberate placement per DESIGN.md L192 and the presets spec, but the *label* is not). The identical chevron pill one row up is a real dropdown (meter), so two look-alike pills behave differently. Once found it is one tap away; the cost is discovery, not use.
- **No fast, exact way to reach a BPM** (V2, medium). No hold-to-repeat on ±1 (`Buttons.kt:157-165`), no drag-on-number, no numeric entry; 80 → 132 is at best 12 taps or a slider guess at ~2dp per BPM. Long-press shortcuts are on the ROADMAP (P2A); typing is not.
- **Quick-adjust buttons silently no-op at the edges, with a haptic tick** (V2, medium). `MetronomeViewModel.kt:259-277`: +5 at 216–220 does nothing, ×2 does nothing above 110 (half the range), ÷2 below 80; the ±1 buttons tick at 40 and 220 too. Clamp ±5 and render ×2/÷2 disabled.
- **Tap Tempo has no listening state** (V2, medium; "no feedback" was overstated: each tap ticks and press-scales). Nothing changes until the third tap, the interval list is unbounded within a run, and tapping never starts playback.
- **Meter coverage and compound-time semantics** (V2, medium). Six meters; 6/8 and 7/8 are six and seven eighth-note pulses at the displayed BPM ("6 beats" in the dropdown), so a 6/8 player sets three times their felt tempo and lives with six dots. 7/8 defaults to 3+3+1 (2+2+3 and 3+2+2 are standard). Custom accents are wiped on meter change and not persisted (they are captured in presets).
- **Coach marks teach things they do not show and block trying them** (V2, low after verification). Step 2 says "drag the slider" while the spotlight ends above it (`MainScreen.kt:381-387` vs :486); the scrim swallows every touch. The pendulum clause in ROADMAP debt is practically unreachable (dots are the default and Settings is blocked during the tour).
- Refuted or deliberate: "six 70dp discs compete with Play" (the −/+ pair is a 7% wash, the quick row a 1dp 50% outline; CHANGELOG 1.2.0 records the whisper-tint decision); "the Andante pill promises a dropdown" is a taste call, though the pill/dropdown inconsistency stands.

### 3.3 Practice tools: trainers, timer, subdivisions

- **Tempo Trainer**: step 1/2/3/5 BPM every 1/2/4/8 bars, start/target steppers with hold-to-repeat, live estimate, two-way stop (keep vs reset), persisted config. Missing (V2, medium): time-based steps, custom values, hold-at-target, loop, stop-at-target. "Edit settings" on an active ramp has no cancel; the only visible exit, "Start trainer", snaps BPM back to the start and zeroes the bar count (`TempoTrainerSheet.kt:231`, `MetronomeViewModel.kt:484-485`); collapsing the section cancels, but nothing says so.
- **Gap Trainer**: fixed play 2/4/8, mute 1/2/4 bar cycles, live edits, status strip. Missing (V2, medium): random or probability muting, custom N, progressive muting, "did you come back on time" feedback. Time Guru, Takt and Metronomics all sell exactly this. Pause/resume resets the cycle to "bar 1" and replays count-in on every resume; both are recorded decisions in the precision spec, but the first one changes what the player was about to hear.
- **Subdivisions**: eighths, triplets, sixteenths, always LOW at a fixed 35% gain. Missing (V2, medium): swing ratio, dotted, quintuplets, per-subdivision accent grid, sub-click volume. In 6/8 and 7/8 "Eighths" produces sixteenths and "16ths" produces 32nds because the beat is the eighth.
- **Practice timer**: countdown drifts against the wall clock (subtracts 1000 ms per `delay(1000)`; V2, low) and each pause loses up to a second; the stopwatch is correct. Timer end gives only a haptic and the metronome keeps playing; "+5 min" after Done starts a fresh timer instead of extending.
- **Stopwatch** (V2, medium): the chip accumulates across every play/pause for the whole process lifetime, has no reset or lap, and its value never appears in the timer sheet; the store copy sells "countdown timer and stopwatch".
- Copy: "1 bars", "every 1 bars", minutes truncated (1.9 → "≈ 1 min") (V2, low).

### 3.4 Presets and Sets

The strongest engineering in the app: referential integrity on delete, bar-boundary application during playback, 5-second checkpoints with paused recovery, versioned tolerant codecs, lossless migration of saved tempos, optimistic concurrency on set edits, 63 tests.

- **A reached time or bar target gives no feedback** (V2, high). `PracticeSessionController.kt:150-173` only sets a flag; the label turns primary. The musician is looking at the instrument, not the phone, so goals pass unnoticed. The standalone timer and the tempo trainer both fire a confirm haptic; the set target is the one goal that stays silent.
- **A recovered session can only be exited via Finish, which counts as Completed** (V2, medium; worse than stated). Sessions never expire, so any set the musician simply stops and closes comes back on next launch as "Recovered session · paused", binds the main Play button to session resume, locks the set against edit/delete, and after Finish becomes the Practice Again target. `discardRecovery()` exists with no caller.
- **Progress is whole-minute text with no visual** (V2, medium): "0 of 5 min" for the first 59 seconds; open-ended steps show nothing; the trainer strips on the same screen draw a progress layer.
- **Next on the last step is a disabled "Set complete"**, the strip has no Finish, and the finish confirmation appears even after the last target is reached (V2, medium; contradicts the sets spec).
- **The set library row shows name, dots and "N steps"** (V2, low): no first-step preview, no total time, no last-practised date although both timestamps are stored.
- Deliberate and confirmed as such: no per-step notes/repeat/rest (spec P1B exclusions), entering the management screens pauses playback (presets spec L135), three taps to start a set from the tempo pill (sets spec L215-217). The library-row preview and total duration were in the spec and were not built.
- The Live Activity carries the practice timer but nothing about a running set (step x of y, next step).

### 3.5 Sound

- Three single samples; the accent is the same sample at 1.4× playback rate; sub-clicks are the same sample at 35%. ROADMAP defers "more sounds and per-sound accent pitch" to Later, so the palette size is a decision; the following are not:
- **Levels differ by ~10 dB peak and ~16 dB short-term loudness** (verified by the reviewer with afconvert; V2, medium). Wood peaks 0.95, Click 0.58, Classic 0.31; Classic's right channel is a further 6 dB down, so at centre pan it sounds lopsided. A player on Classic may not hear the click over an acoustic instrument at 100% in-app volume.
- **Wood and Click are 24 kHz MP3 with a 46 ms encoder pre-roll** (`ffprobe start_time=0.046042`; V1). If a device's SoundPool decode path does not honour the gapless header, those two sounds land ~46 ms later than Classic: a constant offset and a phase jump on sound switch. Unverified on device. Ship WAV.
- **No audition when stopped** (V2, low): tapping a sound in Settings while the metronome is stopped plays nothing.
- **Emoji tiles** 🪵 👆 🔔 in the picker (`ClickSound.kt:3-7`): the only emoji in the product, against CLAUDE.md's Lucide-only rule and the owner's recorded "no emoji" direction; likely empty boxes on the web build (Roboto has no emoji coverage).

### 3.6 Settings

- **Background Play switch is a no-op on iOS and shows the wrong state** (V2, medium; deliberately deferred in the settings-parity spec). Defaults to off while audio always continues in the background; a user who turns it off cannot make the click stop short of force-quitting, which also loses the practice segment.
- **Practice time is credited only on pause** (`MetronomeViewModel.kt:205-207`; V2, medium): a force-quit, crash or OS kill while playing drops the whole segment and can break the streak. Midnight splitting and streak re-evaluation are already in ROADMAP debt.
- Information architecture: Color Flash and Beat Display sit under "Practice" while an "Appearance" header exists; Background Play and Live Activity have no header; Count-in is Settings-only and applying a preset silently rewrites the global toggle (V2).
- The 1.5 s splash on every cold start (900 ms stroke + 600 ms ink, `AnimatedSplash.kt:33-50`) is a recent deliberate addition (a71ee16); its length, its hard cut, and the absence of a reduce-motion path are not decisions. It runs on top of the platform launch screen, so users see two splashes.
- Colour scheme is persisted by enum ordinal (`AppDatastoreImpl.kt:58,105`) while every other enum uses name; reordering the enum silently changes every user's theme.

---

## 4. Beauty

### 4.1 What works

- One shape language (circle / pill / 28dp sheet), one motion vocabulary (`AppAnimations.kt`: seven stiffness tiers, damping 1.0 or 0.94, adopted in 22 of 33 UI files, only three tweens in the whole app), 48dp targets enforced at the component layer, zero hard-coded colours outside `ui/theme`, zero Material icons.
- The BPM readout owns the room (62sp ExtraBold, bounded auto-size), exactly as DESIGN.md asks.
- The play button's circle-to-rounded-square morph with icon crossfade is the one detail that feels like an instrument rather than a widget.
- Impulse-shaped motion where it exists is correct (flash attacks Quick, decays Standard; `pulseOnChange` does the same).
- Slider detent haptics, one per BPM, give the tempo control a machined feel even though its visuals are stock.
- Navigation has depth without decoration (parallax push, predictive back).
- Dark mode is a faithful semantic reversal; the white play disc on black is confident.
- Sheets feel native: drag handle, 28dp radius, 480dp cap.

### 4.2 Verdict: clean, not beautiful

All three critics, independently, reached the same phrase. Premium apps in this price band (Things, Flighty, Halide, Apple's own Clock) share four traits this app has none of, and all four are reachable without a redesign:

1. **A tuned numeral.** `Type.kt` overrides only `bodyLarge`; the DESIGN.md scale (Tempo Display 62/64, Data Display 45/52, bold Title Large) is reconstructed inline at 67 call sites. No `fontFeatureSettings = "tnum"` anywhere, so 100 → 101 shifts glyph widths and every timer jitters horizontally.
2. **A custom tempo control.** `AppSlider` only recolours ticks (`AppSlider.kt:41-64`); the M3 1.12 expressive slider ships verbatim: 16dp track, 4×44dp bar thumb, a gap cut around it, a stop-indicator dot that looks like a stray pixel in every screenshot. It reads as Android on iOS and as a scrubber, not a tempo dial.
3. **A designed dark ladder.** The dark scheme is `Color.Black` plus `Color.DarkGray` for `primaryContainer`, `surfaceVariant`, `secondaryContainer` and `tertiaryContainer` (`ColorSchemes.kt:40-45`), so inactive elements are the brightest greys on screen, brighter than the controls. Light uses `LightGray.copy(0.5f)`, which composites differently over tinted surfaces.
4. **Composition that treats empty space as intentional.** The content stack is pinned to the top of a `weight(1f)` scroll container with the bar docked below (`MainScreen.kt:321-336`); whatever is left over becomes an undesigned band: ~11% of an iPhone, ~36% of an iPad, where the phone layout sits centred at phone size on an 834×1194pt canvas.

### 4.3 Motion: the beat is not physical

- **The beat pulse is a held selection state, not a strike** (V1, high). `beatScale` and `glowAlpha` are `animateFloatAsState` targets on `isActive` (`MetronomeBalls.kt:190-200`): the active ball springs to 1.12 and *stays there* until the next beat. At 60 BPM it sits enlarged for ~850 of 1000 ms. It says "the cursor moved", not "something hit". The optional colour flash already has the right attack-decay envelope; the ball does not.
- **The indicator ring cannot arrive on the beat above ~150 BPM** (V1, high). It animates with `AppAnimations.Emphasized` (stiffness 340, damping 0.94; `MainScreen.kt:264`), which settles in roughly 270 ms; the beat at 220 BPM is 273 ms. At 120 BPM the ring spends over half of every beat in transit. The primary visual timing cue lags the audio by design.
- **The play-button halo is the only continuous animation and it is out of time with the music**: a 2600 ms reversing tween (`Buttons.kt:192-204`) beside balls that pulse on the beat: two rhythms on one screen. Solid rect at 5–14% alpha (no cross-platform blur), so it reads as a faint grey border.
- **The active LOW beat reads as a grey smudge in light theme**: a radial black gradient at 0.26 alpha behind a `primaryContainer` ball inside a 5dp black ring (`MetronomeBalls.kt:235-252`); visible in `docs/screenshots/app-main.png`.
- The tempo readout slides and pulses on every integer during a slider drag (≈120 transitions per second of drag) and slides all digits, not only the changed one.
- Reorder hops one row per 44dp of drag with no lift or translation; sets and steps have no drag at all.
- Theme change is half-animated: chips and balls tween to the new palette while backgrounds and text snap.
- No reduce-motion handling on any platform (grep for `ReduceMotion`, `areAnimatorsEnabled`, `UIAccessibility` returns nothing in `shared/`, `androidApp/`, `iosApp/`). PRODUCT.md:73 names it a requirement; the UI audit listed it unresolved.

### 4.4 Design-system compliance

- **`onSurfaceVariant` is pure ink in every one of the ten schemes** (`ColorSchemes.kt:49,72,94,116,...`), so captions, subtitles, section headers and "paused" labels render at maximum contrast. Hierarchy survives only through size and weight. This is the single biggest reason the default scheme reads flat. **`tertiary == primary` in every scheme**, so the pendulum's accent colour and the trainer strip's accent are invisible as distinctions. `outline`, `outlineVariant`, `scrim` and `error` are never set, so M3's lavender-grey `#CAC4D0` leaks into dropdown borders.
- **No shared primary/secondary button.** DESIGN.md specifies 52dp primary pills and 48dp secondary; the code has 26 raw `Button`/`FilledTonalButton`/`OutlinedButton` sites of which 15 apply `sheetButtonHeight`. Result: 52dp primaries in the tempo/timer/what's-new sheets, 40dp in the session sheet and presets list.
- A stock `FilterChip` for the step target selector (`PracticeSetComponents.kt:679`) where DESIGN.md says `AppChip` owns selectable options; `CenterAlignedTopAppBar` on presets/sets versus `TopAppBar` on main/settings; two empty-state designs; `AppDialog` versus raw `AlertDialog`; an `ElevatedCard` (the app's only shadow) and horizontal-gradient swatches in Settings, both listed under Don't in DESIGN.md.
- The practice-session strip is a 16dp bordered card with three rows and a 40dp tonal button, next to the 48dp pill `StatusStrip` the trainers use: DESIGN.md defines the status strip as "not a dashboard card"; the session strip is that card.
- Duplicated one-offs: two `StepDots`, `PresetActionIcon` ≡ `SetIconButton`, `SectionCaption` re-implemented inline in four places, three ways of putting text in `MySecondaryButton`. Dead: `OutlinedCircle`, `PresetMenuItem`, `appearScale`, elevation tokens, `cornerRadiusSmall`.
- DESIGN.md's motion figures are stale (600/0.8 chip spring and 96% press versus 700/1.0 and 97.5% in code).

---

## 5. Appeal

### 5.1 First run

Score 5.5. The sequence a musician experiences: blank platform launch screen → 1.5 s brand mark → hard cut into an instrument already dimmed to 18% under the coach-mark scrim (onboarding starts in the ViewModel init with no dependency on the splash, `MetronomeViewModel.kt:723-726`; `AppNavigation.kt:45-47` simply stops composing the splash) → three cards → "Got it" → silence. Beat haptics, colour flash and count-in all default off (`AppDatastoreImpl.kt:64,211,339`), so the first play is a 5 KB wood sample and a scaled dot. The undimmed instrument is never shown before the tutorial, and the tutorial never reaches sound.

On iPad the Skip pill lands on the app-bar actions, and the steps 1–2 card is parked at the screen bottom ~1300px from the spotlight it describes (`CoachMarks.kt:184-188`).

### 5.2 Identity

Cover the app name in any screenshot and nothing identifies the product: grey pill chips, outlined circles, a stock slider, a divider, a Lucide gear. The character carriers are the numeral, the play morph and the Italian marking. The two components with mechanical character are hidden: the pendulum (bob slides with BPM, weight recolours on accent) is behind a Settings toggle and defaults off; the brand mark's triangle-and-striker geometry appears only in the splash. The visual critics' cheapest suggestion: render the accented beat as the mark's triangle silhouette so the first beat of every bar is the icon, and put the mark, static, in Settings' About row.

### 5.3 Store presence

Score 6.5. The 5-frame light set (`artifacts/aso-screenshots/final`) converts better than the 8-frame dark set and not narrowly: headlines twice the size, device at 80% of canvas, exploded callouts that survive the three-frame search thumbnail, a tighter narrative. Frame 03 ("Climb 80 → 120, two BPM at a time") is the model the others should copy. All copy claims check out in code.

Defects to fix before upload (verified by viewing the assets):

- Frame 01 sub-line starts lowercase ("it stays exactly where you put it") and neither first frame names the category.
- Frame 03's exploded trainer card shows the state label **Off** directly under "Climb", and "80 bars · ≈ 3 min" appears twice (card and phone behind it).
- Frame 02's stopwatch reads 9:41, identical to the status-bar time; frame 04's "Finish session" button is sliced by the home indicator.
- The feature graphic and OG card carry **no mark or wordmark**, and state "A metronome that never drifts", which escalates the approved "drift-resistant scheduling" to an absolute the audio path cannot guarantee.
- Closing frames prove "No ads" with a Settings screen that shows the emoji sound picker.
- The planned Lock Screen / Live Activity frame (aso-strategy.md:193) was dropped for a generic "resume" frame; it is the app's rarest feature.
- The icon is the category's cliché silhouette drawn black-on-white; the strategy's own PPO experiment (inverted tile, or one Melrose Violet weight dot) has not been run.
- Two screenshot sets and a strategy document that still specifies the eight-frame narrative: two sources of truth for whoever uploads next.

### 5.4 Landing page and web build

The landing page (`docs/index.html`, the "practice log" concept) is unusually well made: real UI crops, a tempo scale with a full `aria-label`, dark palette, reduced-motion handling, JSON-LD, correct store id, a 37 KB self-hosted font, an 81 KB hero. Its problems are strategic, not craft:

- The h1 "Built like an instrument." names neither the category nor a benefit; the category appears only in the bar wordmark and the meta title.
- The hero's largest numbers (42 minutes / 14 hours / 9-day streak) are labelled "An example entry", so the page's central proof proves nothing, while the real proof (monotonic scheduling, real crops) sits lower and smaller. There is no social proof at all.
- Below 820px the sticky bar sends phone visitors to an in-page anchor rather than the matching store; no OS detection.
- Three product names in circulation (page: Practice & Tempo; live listing: Feel the Beat!; PRODUCT.md: BPM & Practice).
- Support and privacy pages are a different visual system (`#0a0a0a`, system font).
- A ~1 KB design-process comment including a "seed key" ships in the production HTML (`docs/index.html:506-512`).
- The web app: 15.3 MB before anything interactive, no accessibility tree (single canvas), in-memory DataStore, and audio buffers fetched after start so an early Play is dropped. The Cloudflare Pages branch fixes hosting and compression; it does not change the download.

---

## 6. Reliability and trust

### 6.1 The timing engine: drift-free is not jitter-free

What is genuinely good (V1, cross-checked against tests): `BeatTimeline` advances `deadline = deadline + interval` on a monotonic clock and re-anchors only when a full interval late (`BeatTimeline.kt:13-17`); the interval is `60_000.0 / bpm` as a fractional Duration (`MetronomeState.kt:27-28`); sub-clicks are exact fractions of the beat; stale clicks are skipped rather than burst; `BeatTimelineTest` proves 1000 beats stay exactly 200 s from the anchor and `MetronomeTimingTest` proves zero cumulative drift over 240 beats at 173 BPM. The old integer-division and open-loop-delay bugs from the precision audit are gone.

What the architecture cannot do: every click is triggered at the wall-clock instant a coroutine wakes (`MetronomeEngine.kt:82-115`: `delayUntil(eventDeadline)` then `player.play(...)`; `delayUntil` is `kotlinx.coroutines.delay`, which rounds up to whole milliseconds). On Android that call is `SoundPool.play()` on a pool built without any low-latency flag (`MetronomePlayerAndroid.kt:19-26`); AOSP's SoundPool creates its AudioTrack on the normal mixer and starts it synchronously, so each onset is quantised to the next mixer cycle. On iOS it is `AVAudioPlayerNode.scheduleBuffer(buffer, atTime = null, ...)` (`MetronomePlayerIos.kt:106-108`): "play at the next render cycle", not at a sample time. There is no lookahead, no `AVAudioTime`, no `setPreferredIOBufferDuration`, no `AudioTrack`/Oboe/AAudio, no latency measurement anywhere (repo-wide grep). Realistic per-click onset error is ~0.2–3 ms from the timer path plus 0–20 ms of mixer quantisation on Android and one render quantum on iOS, i.e. ±5–10 ms typical inter-onset variation, against ~20 µs for a sample-accurate scheduler. This is exactly the audit's "Tier 1"; "Tier 2" was never built. The browser player's header comment claims it is "sample accurate" while the shim calls `src.start()` with no `when` argument (`MetronomePlayerWasm.kt:6-14`, `docs/app/webAudio.js:56`).

Consequences a musician can perceive:

- The visual beat and the haptic fire *before* the audio call (`MetronomeEngine.kt:92-95` then :107-113) with no compensation for output latency: on a phone speaker the light leads the click by ~40–80 ms on Android and ~10 ms on iOS; on Bluetooth by 100–250 ms. Neither platform reads output latency or observes route changes for this.
- Silent gap-trainer bars let the Android output go to standby (3–6 s), so the re-entry click, the one the exercise asks the player to nail, pays HAL wake-up latency.
- The engine loop is a 90-line `collectLatest` with a hard-coded dispatcher and `TimeSource.Monotonic`; it has no test (grep of `commonTest` for `MetronomeEngine` returns nothing). Its parts are tested; its composition (count-in, meter-change restart, gap muting, stale skip, bar counting) is not.
- No on-device measurement exists. ROADMAP lists it as a release gate; PRODUCT.md forbids fabricated benchmarks; the store copy's "precise timing" therefore rests on architecture, not data, while three competitors print a number.

### 6.2 Android audio and background

- **Audio focus: any transient or may-duck request stops the session permanently** (V1; recorded decision in `docs/metronome-phase2-plan.md`: "stop, don't duck"). `AndroidAudioFocusController.kt:12-18` maps `LOSS`, `LOSS_TRANSIENT` and `LOSS_TRANSIENT_CAN_DUCK` all to stop, with `setWillPauseWhenDucked(true)`, and there is no `AUDIOFOCUS_GAIN` branch. A navigation prompt, an assistant response or a notification sound ends the practice session and nothing resumes. The decision was documented; its cost mid-practice was not weighed against the alternative (duck to a lower gain, resume on gain).
- **Background playback rests on a bound-only service with no wake lock** (V1). `MainActivity.kt:49` binds with `BIND_AUTO_CREATE`; nothing calls `startForegroundService`, `unbindService` or `onTaskRemoved`; the engine is a Koin single independent of the service. Swiping the task from Recents or a configuration change destroys the service with the Activity. No `WAKE_LOCK` permission; with the screen off during long silent bars the CPU can idle.
- **Notification has only a Stop action** (`MetronomeService.kt:80-97`): no play/pause, no `MediaSession`, so no lock-screen or headset control, which is the most-cited "phone locked" complaint on Android.
- No `ACTION_AUDIO_BECOMING_NOISY` receiver: unplugging headphones sends the click to the speaker (iOS stops on `OldDeviceUnavailable`).
- MP3 pre-roll and mixed sample rates (§3.5); the accent is a 1.4× resample on every downbeat; wood's 757 ms tail against `maxStreams(4)` steals voices at sixteenths above ~200 BPM.
- Haptic IPC (`Vibrator.vibrate`, a synchronous binder call) sits between the deadline and the audio call on the downbeat.

### 6.3 iOS audio and Live Activity

- **Lock Screen reports "Session ended" after 3 minutes of normal continuous playback** (V1, high). `staleDate` is `now + 180 s` on every push (`MetronomeLiveActivityManager.swift:62`); with tempo and meter unchanged the only changing state is the 1 s tick, which the observer's whole-second rounding and `distinctUntilChanged` deliberately filter, so no update is ever sent; after 180 s `context.isStale` flips and `LockScreenView` replaces BPM, timer and the Play/Pause button with "Session ended" while the click keeps going (`MetronomeLiveActivityWidget.swift:105-110`). The Dynamic Island view has no stale branch, so island and Lock Screen disagree. Battery design is otherwise excellent (self-updating timers, 1.5 s debounce).
- **Silent failure**: if the engine cannot (re)start, `playInternal` returns early (`MetronomePlayerIos.kt:96-97`) while the loop keeps advancing the timeline, updating the index and firing haptics: a running metronome with no sound. Neither `AVAudioEngineConfigurationChangeNotification` nor `AVAudioSessionMediaServicesWereResetNotification` is observed; after a media-services reset the lazy per-click retry cannot recover.
- **Changing the sound while stopped leaves a running engine and an active Playback session**, including in the background (`switchSoundInternal` → `createGraph` → `startEngine` + `player.play()` regardless of playing state, :120-166): battery cost and a pattern App Review associates with background-audio abuse.
- **Play/Pause from the Live Activity after a cold launch flips state but produces no audio**: the engine is a lazy Koin single created when `MainScreen` composes (`CommonModule.kt:64-67`, `MainScreen.kt:136`); an `AudioPlaybackIntent` can launch the process without a scene.
- Two owners of session activation (player and focus controller); the focus controller discards its `NSError`s; interruption-ended `ShouldResume` is ignored (recorded as "predictable stop"); a duplicate `wood.mp3` reference in the Xcode project; string-typed `timerKind` compared against literals in six places; the widget uses `.heavy .rounded` SF and `.accentColor`, neither in DESIGN.md.

### 6.4 Crashes

See §2.3. Two real crashes on the unreleased build, both fixed on `dev`, neither shipped. No unit test guards the ViewModel init-order regression that caused the NPE.

### 6.5 Accessibility and localisation

- **About half of visible UI text is hard-coded English**, concentrated in the pre-1.3 screens (SettingsScreen ~38 literals vs 2 resource lookups, TempoTrainerSheet ~32 vs 6, PracticeTimerSheet 14 vs 0, CoachMarks 7 vs 1, plus every model enum label), against 144 resource strings and 158 `stringResource` calls. Some words exist in resources and are re-typed inline (`AppDialog.kt:19` "Cancel", `SettingsScreen.kt:154` "Back"). No plurals. Localisation (ROADMAP P0) cannot start until this is done.
- **The ± tempo buttons and the Settings gear are announced by their Lucide vector names** ("Minus", "Plus", "Settings"; `Buttons.kt:168`, `MainScreen.kt:311`) rather than actions.
- `AppChip` and the meter dropdown have no `selected`/role semantics, so VoiceOver/TalkBack cannot say which theme or meter is current; the BPM readout and count-in have no live region; the status strips are clickable surfaces with no description; "− 5 / ÷ 2 / × 2 / + 5" depend on symbol pronunciation; both strips' stop buttons say only "Stop".
- Auto-size floors of 8sp on the title and Tap Tempo label defeat the user's text-scale choice.
- No reduce-motion handling (§4.3). Directional arrows do not mirror for RTL.
- The web build has no accessibility tree at all.
- What is good: 48dp targets centralised, beat balls with state descriptions and outline-not-colour for mute, value-bearing slider labels, native `UISwitch` with native accessibility, five `fontScale ≥ 1.3` reflow branches, merged semantics on rows, and all 1.3.0 text externalised.

### 6.6 Honesty of claims

Supported by code: free, no ads, no account, offline, drift-resistant scheduling, background playback, Live Activities. Not supported: "never drifts" (feature graphic, OG card), any implication of measured precision, "countdown timer and stopwatch" as a usable pair, and Play's "No data collected" while Crashlytics, Analytics and Perf are compiled in. The privacy policy already describes the Firebase telemetry; the Play form does not.

---

## 7. Prioritised plan

Effort: S = a day or less, M = up to a week, L = multi-week. Each item names the findings it closes (Appendix A ids).

### Now: ship what exists, then fix what looks unfinished (target: 1.3.1 within weeks)

| # | Item | Effort | Closes |
|---|---|---|---|
| 1 | **Release 1.3.0 (versionCode 9) to both stores** with the removeLast and init-order fixes; tag it; add a unit test for ViewModel init with a recovered session. | S | crash issues |
| 2 | **Bring the site up**: merge `chore/cloudflare-pages`, create the DNS record at Cloudflare, stop GitHub Pages redirecting into the dead domain, retire the `main` copy with the dead App Store id. | S | store-standing |
| 3 | **Publish the corrected listings**: rename to *Metronome: Practice & Tempo* on both stores; App Store primary category → Music; upload the 5-frame set after fixing frame 01 (capitalise, category-bearing sub-line), frame 03 (crop the card below the "Off" header, mask the duplicated estimate), frames 02/04 (plausible stopwatch, unsliced button); add the mark + wordmark to the feature graphic and OG card; change "never drifts" to "drift-free scheduling"; add the Lock Screen frame as frame 6; rewrite Play's description; reconcile the data-safety form with the shipped Firebase SDKs (or remove Analytics/Perf); archive the dark set; update aso-strategy.md and PRODUCT.md:41-43 to the single name. | M | store-presence#1-8, a11y#12-13 |
| 4 | **Persist BPM and the per-beat pattern** (DataStore keys, restore in init, debounce slider writes). | S | main#1 |
| 5 | **Keep every meter on one row**: derive dot size and spacing from measured width and beat count; wrap only at ≥9 beats. | S | main#2 |
| 6 | **Fix the Live Activity stale state**: omit `staleDate` while playing or push a keep-alive; make the Dynamic Island honour `isStale` the same way. | S | ios#2 |
| 7 | **Hide the iOS Background Play switch** (or implement it: deactivate the session on background when off). | S | settings#1 |
| 8 | **Practice Set target feedback + recovery exits**: confirm haptic (and optional accent click) on target reached; wire `discardRecovery()` to a Discard action; Next → Finish on the last step; no confirmation once the last target is reached; m:ss progress with the existing strip progress layer. | S | presets#1,2,4,8 |
| 9 | **Clamp ±5 at the range edges, disable ×2/÷2 when they cannot apply, no haptic on a no-op.** | S | main#6 |
| 10 | **Credit practice time periodically** (every 60 s tick), not only on pause. | S | settings#7 |
| 11 | **Normalise the three samples**, trim the MP3 pre-roll, ship WAV/CAF, audition on tap when stopped, replace emoji with Lucide glyphs. | S–M | settings#3,4, android#7 |

### Next: make the beat physical and the surface premium (1.4)

| # | Item | Effort | Closes |
|---|---|---|---|
| 12 | **Strike, not hold**: drive the ball from a one-shot `Animatable` keyed on the beat (1.0 → ~1.16 with `press()`, back with `standard()`, HIGH peaks higher); drop the light-theme radial glow; a crisp expanding stroke ring instead. | S | motion#1, visual-first-run#1, visual-craft#4 |
| 13 | **Ring arrives on the beat**: `Quick` or `Press` for `indicatorIndex`, or derive stiffness from beat duration so settle time stays under ~35% of the beat; crossfade above ~160 BPM. | S | motion#2 |
| 14 | **Retire or beat-sync the halo**; fade it in/out and place it inside `pressScale`. | S | motion#3 |
| 15 | **Tonal tier**: `onSurfaceVariant` at ~65% ink per scheme; a distinct `tertiary` (or stop using it); set `outline`, `outlineVariant`, `scrim`; replace `Color.DarkGray` / `LightGray.copy(0.5f)` with a designed ladder (#000 / #222 control rest / #333 LOW beat / #2B2B2B inactive track, white at 12% hairlines). | S | motion#4,5,6, visual-craft#7 |
| 16 | **Typography in `Type.kt`**: tempoDisplay, dataDisplay, bold titleLarge, labelLarge-selected; `tnum` on every numeral; remove the 67 inline copies. | M | motion#11, visual-craft#3 |
| 17 | **AppSlider in the instrument language**: 4–6dp round-capped track, 28dp circular thumb with a 1dp ring, no gap, no stop dot, symmetric pan ticks; keep the detents and semantics. | M | motion#15, visual-craft#2 |
| 18 | **Composition**: centre the instrument vertically in the scroll region (`BoxWithConstraints` + `Arrangement.Center` or 1:2:1 spacers); scale the instrument on expanded height (tempo to ~120sp, balls 56dp, play 110dp) and bring the action row into the stack on tablets. | M | visual-craft#1,9 |
| 19 | **First run**: gate onboarding on splash completion; shorten the splash to ~700 ms with a fade-out (spring, not tween); anchor coach cards below their spotlight; report the balls' own bounds; extend step 2's spotlight over the slider; end the last step in Play; move Skip into the app bar. | S | settings#8, visual-first-run#2,3,4,5, main#10 |
| 20 | **One button family**: `AppPrimaryButton`/`AppSecondaryButton`/`AppTonalButton` at 52/48dp with pressScale and haptic; migrate the 26 raw sites; `FilterChip` → `AppChip`; one app-bar style; one empty-state; one dialog surface; session strip in the `StatusStrip` language; delete the duplicates and dead tokens. | M | motion#7-10,12,13 |
| 21 | **An honest door**: relabel the tempo pill as a summary without a chevron ("Andante · 4/4 · Quarter") that opens the sheet, and add a small "Practice" entry (top bar or long-press) that opens the sheet at presets/sets; name sets in the coach mark; Practice Again for the last two or three sets. | S–M | main#3,4, presets#5 |
| 22 | **Exact tempo, fast**: hold-to-repeat with acceleration on ±1 (reuse `ValueStepper`'s logic); tap-to-type on the readout; tap-tempo listening state ("tap 2 more") with a rolling 4–8 tap window and optional auto-start. | M | main#5,7 |
| 23 | **Reduce-motion abstraction** (interface + Koin, CompositionLocal) that snaps `AppAnimations`, disables halo/flash/parallax; finish string externalisation for the pre-1.3 screens; real semantics for chips, meter pill, ± buttons, readout live region, strips and timer chip; raise auto-size floors to 12sp. | M | motion#14, a11y#1-8 |
| 24 | **Trainer polish**: cancel/apply on Edit without restarting; keep gap phase across pause; count-in choice (1 or 2 bars) in the tempo sheet; fix pluralisation and rounding; timer end chime and correct "+5"; stopwatch reset and per-session figure. | S–M | tempo#1,2,6,7,8,9 |

### Later: breadth, precision, platforms (1.5+)

| # | Item | Effort | Closes |
|---|---|---|---|
| 25 | **Meters and range**: beats-per-bar picker (1–16) with /2 /4 /8 /16, presets for 9/8, 12/8, 2/2, 5/8, 7/8 groupings (2+2+3 default), a dotted-quarter pulse option for x/8, remembered accents per meter; BPM 20–300 (the engine already handles fractional intervals; the pendulum already assumes 240). | M | main#9, tempo#5, gaps |
| 26 | **Subdivisions**: swing with ratio, dotted-eighth+sixteenth, quintuplets/sextuplets, a per-subdivision accent/mute grid, sub-click volume; relabel relative to the meter's beat unit. | L | tempo#5 |
| 27 | **Sample-accurate rendering on both platforms**: Android `AudioTrack` MODE_STREAM (or Oboe) with clicks mixed at computed frame offsets 100–200 ms ahead; iOS `scheduleBuffer(at: AVAudioTime)` from a lookahead loop with `setPreferredIOBufferDuration`; keep `BeatTimeline` as the musical authority; then compensate visuals and haptics with measured output latency (re-read on route change; Bluetooth default ~150 ms); then measure inter-onset jitter on real devices and print the figure in the listing. Add an engine test with `TestTimeSource`. | L | android#1,4,5,6, ios#1,3, gaps |
| 28 | **Android service owns playback**: `startForegroundService` on play, `stopSelf` on stop, `onTaskRemoved`, a partial wake lock while playing, `MediaSession` with play/pause for lock screen and headsets, `BECOMING_NOISY` stop, and a graceful focus policy (duck on `CAN_DUCK`, pause on `TRANSIENT`, resume on `GAIN`; stop only on permanent loss). | M | android#2,3 |
| 29 | **iOS audio hygiene**: stop the engine after a sound switch when not playing; observe configuration-change and media-services-reset; surface "audio unavailable" to the UI instead of animating silently; resolve the engine at Koin start so the Dynamic Island toggle works from a cold launch; single owner of session activation; honour `ShouldResume`; typed `timerKind`; widget styled to DESIGN.md. | M | ios#4,5,6,7 |
| 30 | **Gap trainer modes**: random with probability (10/25/50/75%), per-bar or per-beat, custom N; time-based tempo-trainer steps, hold and loop; trainer config inside a preset or set step. | M | tempo#3,4 |
| 31 | **Sound palette**: 8–12 designed sound pairs with a distinct accent voice and sub voice; a voice count; a preview sheet reachable from the instrument. | L | settings#2, gaps |
| 32 | **Wear OS and watchOS companions** (play/pause, tempo, wrist haptic), then home-screen widgets; extend the Live Activity with the running set. | L | ROADMAP Later, gaps |
| 33 | **Export/import of presets and sets** as a local file (no accounts); optional local reminders. | M | gaps |
| 34 | **Localisation** ES/DE/PT/JA once item 23 lands; plurals; RTL mirroring. | M | a11y#1,9 |
| 35 | **Web demo**: brotli via Cloudflare, a service worker, start audio decode on first pointer-down, keep a heading and store links in the DOM, honest size copy. | M | a11y#10,11 |

### On money

Nothing above requires monetisation, and the review found no case for ads or subscriptions: both are the top 1-star triggers in the category. If revenue becomes a goal, the options consistent with PRODUCT.md are a tip jar in About, a one-time "Pro" unlock limited to genuinely new advanced work (polyrhythms, swing, random mute, voice packs, watch, desktop) with nothing existing ever moved behind it, paid sound packs, or a separately sold desktop build (the Wasm build makes this cheap). Decide this before item 26, not after.

---

## 8. Do not do

- Do not add ads, accounts, subscriptions, or move any shipped feature behind a paywall. It is the product's only durable advantage today.
- Do not add a tuner, recorder or drum machine. Roundups already punish "kitchen sink" apps; the roadmap's non-goals are right.
- Do not turn the main surface into a dashboard or move practice stats onto it.
- Do not rename to "Metronome: BPM & Practice"; that name is taken by a ranking competitor.
- Do not publish a precision number until it is measured on real devices; do not keep "never drifts" in any asset.
- Do not answer the feel gap with ornament: gradients, glow, celebration, or a breathing idle animation. The gap is a strike instead of a hold, a tonal tier, a tuned numeral, and one repeated identity shape.
- Do not add cloud sync before export/import exists and a privacy contract is written.
- Do not build polyrhythms before the unified scheduler (ROADMAP already says so).
- Do not ship the 8-frame dark screenshot set.

---

## 9. Open questions for the developer

1. Is resetting to 80 BPM on launch a deliberate "fresh start", or an oversight? Nothing in the specs mentions it. The review treats it as a defect.
2. Is 6/8 meant to be felt in six at the displayed BPM, or should the readout express the dotted-quarter beat? The "6 beats" caption suggests the former; classical and drummer feedback wants the latter as an option.
3. Is the 220 BPM ceiling an engine limit or a product decision? The pendulum already assumes 240 and the engine handles fractional intervals.
4. Is the 1.4× pitch-shifted accent a sonic identity to keep, or a placeholder until real accent samples exist?
5. Is the "stop on any focus loss, never resume" Android policy still the intended trade-off now that the cost mid-practice is clear?
6. Is the held 1.12 ball scale intended as a "current beat" cursor? The answer decides whether to add a strike or keep the hold and add a separate strike.
7. Is `onSurfaceVariant` = full ink a contrast decision (AAA on captions) or an oversight when the schemes were defined?
8. Are UI micro-haptics meant to ignore the "Haptic Feedback" setting, whose subtitle says "Vibrate on each beat"?
9. Is the team willing to take on Oboe/NDK, or is a pure-Kotlin `AudioTrack` streaming renderer the ceiling? Should the `MetronomePlayer` contract change once for both platforms?
10. Does Play's "No data collected" get corrected by disclosing Firebase, or by removing Analytics and Perf and keeping only Crashlytics?
11. Which product name is canonical: the page and aso-strategy say Practice & Tempo; PRODUCT.md says BPM & Practice.

---

## Appendix A. Verified findings register

Marks: **V2** = confirmed by two independent verifiers (code-truth and impact lenses) or by tiebreak; **V1** = single code reader, then checked by hand against the cited lines in this session; **P** = partially true, corrected in the text; **R** = refuted; **D** = deliberate decision on record (still listed where the cost is real). Severity is the post-verification judgement.

### Main surface
| id | Finding | Mark | Sev |
|---|---|---|---|
| main#1 | BPM and accent pattern not persisted across launches | V2 | high |
| main#2 | 5/4, 6/8, 7/8 dots wrap into two rows on 360–393dp phones | V2 | high |
| main#3 | Tools reachable only via the tempo-name pill; label does not hint | V2/D (placement deliberate, label not) | medium |
| main#4 | Tempo pill chevron mirrors a real dropdown one row up | V2/P | low |
| main#5 | No hold-to-repeat, drag, or numeric entry for BPM | V2 (long-press on ROADMAP) | medium |
| main#6 | Quick-adjust no-ops at the edges with a haptic tick | V2 | medium |
| main#7 | Tap Tempo has no listening state; unbounded window | V2/P | medium |
| main#8 | Six 70dp circles compete with Play | R/D (whisper-tint decision, 1.2.0) | – |
| main#9 | Six meters; compound-time semantics; 7/8 = 3+3+1; accents reset on meter change | V2/P (accents kept in presets) | medium |
| main#10 | Coach marks spotlight excludes the slider; scrim blocks trying | V2 | low |
| main#11 | Count-in invisible until Play; presets rewrite the global toggle | V2/P | low |

### Tempo sheet and trainers
| id | Finding | Mark | Sev |
|---|---|---|---|
| tempo#1 | Edit settings on an active ramp has no cancel; Start restarts the ramp | V2/P (collapsing cancels, undiscoverable) | medium |
| tempo#2 | Stopwatch cannot be reset and never appears in its sheet | V2 | medium |
| tempo#3 | Gap trainer is fixed bar cycles only | V2/P (per-beat mute exists statically) | medium |
| tempo#4 | No time-based steps, custom values, hold, loop, stop-at-target; 5 s auto-clear | V2/P (auto-clear deferred while sheet open) | medium |
| tempo#5 | Minimal subdivisions; labels wrong in compound meters | V2 | medium |
| tempo#6 | Gap phase resets and count-in replays on resume | V2/D (precision spec) | low |
| tempo#7 | Countdown drifts (fixed 1000 ms per tick) | V2 | low |
| tempo#8 | "1 bars", truncated minutes | V2 | low |
| tempo#9 | Count-in Settings-only, fixed one bar | V2/D (per-setup via presets) | low |

### Presets and sets
| id | Finding | Mark | Sev |
|---|---|---|---|
| presets#1 | Target reached gives no haptic or sound | V2 | high |
| presets#2 | Recovered session exits only via Finish → Completed; sessions never expire | V2 | medium |
| presets#3 | Step model too thin (notes, repeats, rests, totals) | P/D (spec exclusions); library preview and total were in spec and not built | low |
| presets#4 | Whole-minute progress, no visual, nothing for open-ended steps | V2 | medium |
| presets#5 | Sets three taps deep behind the tempo pill | P/D (sets spec) | low |
| presets#6 | Management screens pause playback | R/D (presets spec L135) | – |
| presets#7 | Cold-start build-up before a first set | P | low |
| presets#8 | Next disabled on last step; strip has no Finish; confirmation after target | V2 | medium |

### Settings, sounds, onboarding
| id | Finding | Mark | Sev |
|---|---|---|---|
| settings#1 | iOS Background Play switch is a no-op and shows the wrong state | V2/D (deferred in parity spec) | medium |
| settings#2 | Three sounds, pitch-shifted accent, no audition when stopped | P/D (palette on ROADMAP); audition gap real | low |
| settings#3 | Sounds differ ~10 dB peak / ~16 dB loudness; Classic lopsided | V2 (measured) | medium |
| settings#4 | Emoji tiles violate the icon rule | V2 (web rendering unverified) | low |
| settings#5 | Settings grouping muddled | P | low |
| settings#6 | Volume/sound/pan need Settings | P (hardware keys cover level) | low |
| settings#7 | Practice time credited only on pause | V2 | medium |
| settings#8 | 1.5 s splash on every cold start, hard cut | V2/D (splash deliberate; length and cut not) | medium |

### Android engine and audio (hand-verified)
| id | Finding | Mark | Sev |
|---|---|---|---|
| android#1 | Coroutine-triggered SoundPool clicks: drift-free, not jitter-free | V1 | high |
| android#2 | All focus losses stop, no resume | V1/D (phase-2 plan) | medium |
| android#3 | Bound-only service, no wake lock, no MediaSession | V1 | medium |
| android#4 | Silent gaps → output standby → late re-entry click | V1 (plausible, not device-verified) | medium |
| android#5 | Visual/haptic lead audio; no latency or route handling | V1 | medium |
| android#6 | Engine loop untested and non-injectable | V1 | medium |
| android#7 | MP3 46 ms pre-roll; mixed rates; accent resample; voice stealing | V1 (ffprobe) | low |
| android#8 | Wasm comment claims sample accuracy; `start()` without `when` | V1 | low |

### iOS engine and Live Activity (hand-verified)
| id | Finding | Mark | Sev |
|---|---|---|---|
| ios#1 | `scheduleBuffer(atTime = null)`; no lookahead, no buffer tuning | V1 | high |
| ios#2 | Lock Screen "Session ended" after 180 s of steady playback | V1 (code path certain; device run pending) | high |
| ios#3 | Visual/haptic lead audio, no `outputLatency` compensation | V1 | medium |
| ios#4 | Silent failure when the engine cannot restart; no reset observers | V1 | medium |
| ios#5 | Sound switch while stopped leaves the engine running in background | V1 | medium |
| ios#6 | Live Activity toggle after cold launch: state flips, no audio | V1 | medium |
| ios#7 | Two session owners; discarded NSErrors; duplicate wood.mp3 ref; string timerKind | V1 | low |

### Motion, components, theme (hand-verified)
| id | Finding | Mark | Sev |
|---|---|---|---|
| motion#1 | Beat pulse is a held state | V1 | high |
| motion#2 | Ring spring cannot land on-beat above ~150 BPM | V1 | high |
| motion#3 | Halo out of time; instant on/off | V1 | medium |
| motion#4 | `onSurfaceVariant` full ink in every scheme | V1 | high |
| motion#5 | `tertiary == primary` | V1 | medium |
| motion#6 | M3 lavender neutrals leak (outline roles unset) | V1 | low |
| motion#7 | No shared primary/secondary button; 40 vs 52dp | V1 | high |
| motion#8 | Stock FilterChip | V1 | medium |
| motion#9 | Session strip is the card DESIGN.md warns against | V1 | medium |
| motion#10 | Two app-bar styles, two empty states, two dialog surfaces | V1 | medium |
| motion#11 | Type scale in 67 inline overrides; `Type.kt` has bodyLarge only | V1 | medium |
| motion#12 | Duplicated one-offs, dead tokens | V1 | low |
| motion#13 | Mixed press feedback vocabulary | V1 | medium |
| motion#14 | No reduce-motion handling | V1 (two agents + grep) | medium |
| motion#15 | Stock M3 slider | V1 | medium |
| motion#16 | Readout animates every integer during drag | V1 | medium |
| motion#17 | Reorder hops per 44dp; no drag for sets | V1 | medium |
| motion#18 | Theme switch half-animated | V1 | medium |
| motion#19 | DESIGN.md motion figures stale | V1 | low |

### Accessibility, localisation, web, landing (hand-verified)
| id | Finding | Mark | Sev |
|---|---|---|---|
| a11y#1 | ~half of UI text hard-coded English | V1 | high |
| a11y#2 | Resource strings bypassed by inline duplicates | V1 | medium |
| a11y#3 | ± and gear announced by icon names | V1 | medium |
| a11y#4 | No reduce-motion | V1 | medium |
| a11y#5 | Chips and meter pill lack selection semantics | V1 | medium |
| a11y#6 | Readout, strips, timer chip lack live/purpose semantics | V1 | medium |
| a11y#7 | 8sp auto-size floors | V1 | low |
| a11y#8 | Arrows not mirrored for RTL | V1 | low |
| a11y#9 | No plurals or locale numbers | V1 | low |
| a11y#10 | Web build has no accessibility tree | V1 | medium |
| a11y#11 | Web demo 15 MB before first click; audio fetched after start | V1 | medium |
| a11y#12 | Hero proof is an example entry; no real proof | V1 | medium |
| a11y#13 | h1 names neither category nor benefit; mobile CTA is an anchor; three names | V1 | medium |
| a11y#14 | Process comment in production HTML (`index.html:506-512`); off-system support/privacy pages | V1 | low |

### Visual critiques (screenshots viewed; code facts cross-checked above)
| id | Finding | Mark | Sev |
|---|---|---|---|
| craft#1 | Leftover void between quick row and bar (11% iPhone, 36% iPad) | V1 | high |
| craft#2 | Stock M3 slider | V1 | high |
| craft#3 | M3 defaults + weight; no tabular numerals | V1 | high |
| craft#4 | Active LOW beat is a smudge; three stroke weights | V1 | medium |
| craft#5 | Andante chip reads as a passive label (5.5% fill) | V1 | medium |
| craft#6 | Unequal bottom pills, two-line Tap/Tempo, full-bleed divider | V1 | medium |
| craft#7 | Settings: ElevatedCard, gradient swatches, emoji | V1 | medium |
| craft#8 | Dark theme is #000 + DarkGray | V1 | medium |
| store#1 | First frames name no category; dark set opens on a paused, recovered session | V1 (viewed) | high |
| store#2 | Three dark frames reuse one capture with sliced chips | V1 | high |
| store#3 | Feature graphic and OG card carry no brand | V1 (viewed) | high |
| store#4 | Icon is the category cliché; PPO test not run | V1 | medium |
| store#5 | Frame 03 says "Off" under "Climb"; duplicated estimate line | V1 (viewed) | medium |
| store#6 | Frame 01 sub-line lowercase | V1 (viewed) | medium |
| store#7 | Staged 9:41 stopwatch; sliced Finish button | V1 | medium |
| store#8 | "Never drifts" escalates the approved claim | V1 (viewed) | medium |
| store#9 | "No ads" frame shows emoji Settings | V1 | medium |
| store#10 | Practice Sets frame is an almost empty list | V1 | medium |
| store#11 | Lock Screen frame dropped for a generic resume frame | V1 | medium |
| firstrun#1 | Beat pulse held, not struck | V1 | high |
| firstrun#2 | First run never shows the undimmed instrument, never reaches sound | V1 | high |
| firstrun#3 | 1.5 s splash, hard cut | V1 | medium |
| firstrun#4 | Skip collides with the app bar on iPad | V1 (viewed) | medium |
| firstrun#5 | Cards parked at screen bottom; spotlight is a full-width slab | V1 | medium |
| firstrun#6 | Identity thin; character components hidden | V1 | medium |
| firstrun#7 | Every physical channel off by default | V1 | medium |
| firstrun#8 | Light-theme glow reads as a smudge | V1 | medium |

## Appendix B. Method and limits

- **Not examined**: Android UI at phone and tablet sizes (no Android screenshots exist in `artifacts/`; no emulator was run), landscape, physical-device audio (jitter, latency, Bluetooth, standby), TalkBack/VoiceOver traversal, battery, and the web build at runtime (the local `/app/` failed to render in the automated browser session; the wasm bundle does load from the file system per the code reader).
- **Verification coverage**: the two-lens adversarial pass completed for four of eleven areas before the session's usage limit was hit; the remaining seven were verified by hand against every cited line quoted in this document. A judge panel of three prioritisation angles was planned and not run; §7 is the reviewer's synthesis of all inputs. To complete the automated pass, resume the workflow `wf_9028b2df-681` from its script; completed agents replay from cache.
- **Sources**: App Store and Play pages (fetched), iTunes Search API, `dig`, Crashlytics via the Firebase MCP, `ffprobe` on the bundled samples, `afconvert` loudness analysis, the repo at `dev` c374edd, and the roundups, forums and reviews cited in the companion landscape document.
