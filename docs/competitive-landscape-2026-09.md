# Metronome — Competitive Landscape, September 2026

> Status: research complete on 2026-09-05. Companion to `docs/product-review-2026-09.md`, which draws its market conclusions from this document.
> Scope: the standalone metronome apps a musician finds on the App Store and Google Play, the design-led and specialist indies that roundups recommend, and the "default" alternatives (Google's search widget, GarageBand, YouTube, hardware, lesson apps) that capture the job without a store search.
> Method: three research agents with web search and page fetches against primary sources (store listings and their review pages, developer sites) and the 2025–26 roundups (Practis, Musician Wave, BeatIt, Rhythm Notes, Bulletproof Musician, Lindeblad, Ted's List), plus a fourth agent on musician needs by segment (Reddit r/drums, r/Guitar, r/piano; Piano World; TalkBass; Gearspace; teacher blogs). Prices and ratings are as displayed on the US storefronts on the fetch date; "not found" is recorded rather than guessed. Every entry lists its sources.
> Reading guide: §1 is the one-page summary; §2 the established apps; §3 the premium and indie apps; §4 the defaults; §5 category norms and differentiators; §6 the gap matrix against this app; §7 what musicians want; §8 monetisation; §9 sources.

---

## 1. Summary

The category is mature, crowded and split into three tiers:

1. **Ecosystem apps** with 20K–180K ratings: Soundbrenner (tracker + tuner + hardware funnel, subscription), Pro Metronome (feature-maximal, à-la-carte unlocks, ±20 µs marketing claim), TonalEnergy (band-room tuner suite, $6.99 up front), Metronome Beats (mass-market Android, ads, Pro $9.99), Smart Metronome & Tuner (free with pop-up ads, 60K ratings, "1/44,100 s" claim).
2. **Paid specialists** at $2.99–$3.99 one-time: Tempo and Tempo Advance (setlists, gig modes, pedals), Time Guru (random mute), Gap Click (Benny Greb), Metronomics (independence training), True Metronome (mechanical authenticity), ONYX (accuracy claim, minimal face).
3. **Free and clean**: Tack (open source, Material You, best Wear OS story on Android), Takt (new, Drift Mode), Pulse (the design reference of the category, abandoned since 2018), BackBeat (routine builder, new).

Every 2025–26 roundup rewards the same things: no ads, no nags, no account, one-time or free pricing, a click that "disappears into the background", and a training feature that makes the click go silent (gap, random mute, drift). They punish subscriptions (Soundbrenner, Practice+), features moved behind paywalls (Pro Metronome), bloat (TonalEnergy, PolyNome), and pop-up ads (Smart Metronome, Metronome ∞).

**This app's position in that map:** free with everything unlocked (tier 3 economics) with a practice toolkit that competes with tier 1 and 2 (presets, sets with goals and recovery, both trainers, stats, Live Activity), on both platforms from one codebase, and a calm monochrome design closer to Pulse and Tack than to anyone in tier 1. Its measurable gaps are all table-stakes breadth: BPM range, meters, sounds, subdivisions, watch, and a published timing figure. The vacuum it can fill is the one Pulse left: a genuinely minimal, beautiful, free metronome that is still updated, but with practice depth Pulse never had.

---

## 2. Established apps

### Pro Metronome (EUMLab / Polybeat Limited; Android developer shown as Yuan Zhou)
- **Platforms**: iOS 16+, iPad, Apple Watch, Apple TV, Vision; Android (own description: "far from perfect" versus iOS).
- **Pricing**: free; Pro $3.99 one-time, or $0.99/month, $3.99/year; à-la-carte $0.99 unlocks (Subdivisions, Practice Mode, Rhythm Trainer, Polyrhythm, Vibrate/Flash/AirPlay, tone generator); Playlists $1.99. Ad-free.
- **Ratings / scale**: App Store 4.7 (20K); Play 3.59 (22K); Play 5M+ downloads. Marketing claims range from "3 million" to "150 million users".
- **Standout**: 13 sounds including a counting voice; dynamic and additive meters (4+3/4) with four accent levels per beat (f, mf, p, mute); subdivisions incl. dotted (Pro); polyrhythm (Pro); Rhythm Trainer (mute bars, growing mute time; Pro); Practice Mode (programmed tempo change; Pro); Stage Mode (auto-stop after N bars); visual, pendulum, screen flash, camera-LED flash and vibrate modes; playlists with iCloud sync; MIDI, Bluetooth pedals, Ableton Link, AirPlay, Audiobus; tuner; Watch app.
- **Timing claim**: ±20 µs via proprietary "RTP (Real-Time Playback)"; the single most repeated line in roundups. Android reviews still report drift ("drags and rushes").
- **Design**: dark, dense control-panel; "elegantly simple" (Tape Op), "clean" (Melodics) but a steep learning curve, no tutorial.
- **Weaknesses**: the move to subscription invalidated earlier Pro purchases ("every feature unavailable without a subscription"); trainers, subdivisions and visual modes all paywalled; upgrade pop-ups; Android build rated 3.6 and admitted weaker; complex for beginners.
- **Positioning**: the "studio-grade precision" pro metronome for practice and stage.

### Tempo – Metronome with Setlist (Frozen Ape, Singapore; Android "Metronome: Tempo"; free "Tempo Lite")
- **Platforms**: iOS 16.6+, iPad, Apple Watch, Mac; Android, Amazon. Sibling Tempo Advance (iOS, $3.99) adds polyrhythms and accelerando.
- **Pricing**: iOS $2.99 one-time (+ $0.99 theme pack, $0.99 theme editor); Android $1.49; Lite free with ads.
- **Ratings / scale**: App Store 4.8 (4.9K); Play 3.68 (1.8K; Lite 3.76 / 7.4K); Play 100K+ (Lite 1M+). Apple "Staff Favorite" in the past; Practis' iPhone pick for 2026.
- **Standout**: 35 time signatures (1–13/2, 1–13/4, 3/8, 6/8, 9/8, 12/8) with 6 simple and 3 compound rhythm patterns; per-beat accent/off editing on an LED row; 10–800 BPM; 14 sound sets incl. one tuned for live drum click tracks; voice counting; five morphing modes (Basic, Preset, Setlist, Practice, Gig); setlists with search, copy, multi-delete, email/AirDrop sharing, iOS↔Android files; Tracker and Automator (bar/time counters, auto-stop, tempo change every N bars or seconds); Coach Mode (alternating muted bars); AirTurn/iRig/PageFlip pedals; Watch app; custom theme editor.
- **Timing claim**: "engine written from the ground up for high accuracy and stability"; no figure.
- **Design**: skeuomorphic-lite LED row; "mature rather than modern" (Practis), "attractive and comprehensive" (developer).
- **Weaknesses**: dated look; Android behind iOS (fewer signatures and sounds, no count-in, volume dropouts on some devices); press-and-hold tempo "runs away"; volume inconsistent across tempos (worship-team review); no practice history; no cloud sync; no polyrhythm in the base app.
- **Positioning**: the workflow-first, paid, privacy-respecting gigging metronome.

### The Metronome by Soundbrenner (Soundbrenner; maker of Pulse and Core wearables)
- **Platforms**: iOS 17+, iPad, Mac, Vision; Android; web. No Apple Watch app (the Watch motor is "too weak"; a remote is an open request). Home and lock-screen widgets.
- **Pricing**: free core; Soundbrenner+ $4.99 / $7.99 / $9.99 per month, $39.99 / $59.99 / $79.99 per year. Free tier capped at 10 songs / 2 setlists (was unlimited).
- **Ratings / scale**: App Store 4.7 (48K); Play 4.59 (90K); Play 5M+; claims "10 million musicians"; NAMM Best in Show, WIRED "best metronome app". 313 MB on iOS.
- **Standout**: custom meters, subdivisions, 4-state accents; 20+ sounds; count-in; library with cloud sync (Plus); practice tracker with per-instrument hours, goals, streaks, "10,000-hour challenge", reminders, widgets; Incremental Tempo Change and Muted Beats Trainer (Plus); tuner with 100+ profiles; USB and Bluetooth MIDI, Ableton Link; instructor videos (Plus); wearable integration.
- **Timing claim**: "ultra-precise", "rock-solid"; no figure. Reviews report stutter with some sounds above ~120 BPM.
- **Design**: slick, dark, consumer-app look with a big circular dial and bold accent; praised as "sleek and easily understood in seconds", widely called "bloated" with nag screens and hardware marketing; BeatIt rates the UI 5/5.
- **Weaknesses**: subscription prompts on launch; onboarding upsell; "bait-and-switch" as customisation moved behind the paywall; the two independence-building trainers are Plus-only; account-based ecosystem; heavy install; library corruption and backup failures after updates; uncontrollable flashing during tap tempo raised as a photosensitivity risk.
- **Positioning**: the "world's leading metronome app and practice tracker": an ecosystem hub monetised through Plus and hardware.

### TonalEnergy Tuner & Metronome (TonalEnergy Inc., formerly Sonosaurus)
- **Platforms**: iOS 15+, iPad, Apple Watch (remote), Vision; Android; Mac and Windows; institutional licensing.
- **Pricing**: $6.99 one-time (iOS), $5.99 (Android), no IAP, no ads; desktop key $9.99.
- **Ratings / scale**: App Store 4.8 (58K); Play 4.28 (2.8K); Play 100K+; "band-room standard", endorsed by Canadian Brass and Conn-Selmer educators.
- **Standout**: tempo to 1000 BPM; 32 sounds, randomised sounds; any meter with beat-unit choice, subdivision patterns, nine visualisations; preset groups and programmable click tracks with meter and tempo changes, accelerando/ritardando; "Metronome Assistant" random beat/bar silencing; polyrhythm; voice count-ins; drones attached to presets; full tuner suite; recorder with looping and time-stretch; practice-log export; VoiceOver.
- **Timing claim**: none; Ableton Link.
- **Design**: dense, colourful, tab-heavy; "not quite as elegant-looking as some", "overengineered for click-only users".
- **Weaknesses**: cluttered and dated; mixed-meter programming "really clunky"; metronome is one page of a tuner-first suite; slow tempo adjustment; Android lower rated with settings-persistence history; single-speaker output; paid up front.
- **Positioning**: all-in-one suite for wind, brass, string and vocal players where tuning matters as much as tempo.

### Metronome Beats / Metronome Beats Pro (Stonekick, London)
- **Platforms**: Android (free app with ads + separate Pro app); iOS 15+ with Pro IAP; Apple Watch; Mac; web.
- **Pricing**: Android free with ads, Pro $9.99 separate app; iOS free, Pro unlock $8.99.
- **Ratings / scale**: Play 4.76 (182K; Pro 4.84 / 5K); App Store 4.7 (5.4K); Play 10M+; claims 25M+ downloads, "more than any other metronome".
- **Standout**: 1–900 BPM; one-handed ±1/±5; subdivisions up to 16 per beat, polyrhythms, swing, unusual meters, custom accent patterns; adjustable pitch and custom sounds; Speed Trainer by bars or seconds with selective muting; drum machine with kits and programmable patterns; timer with auto-stop; count-in; presets; Pro adds setlists, songs built from sections, Live mode, audio export; background play with lock-screen and Bluetooth media controls; `.mbeats` export/import.
- **Timing claim**: "free, accurate"; no figure; occasional unexplained tempo jumps in iOS reviews.
- **Design**: utilitarian, grid-based Android-native; "familiar", "elegant enough", "less elegant than specialised paid alternatives".
- **Weaknesses**: ads distract during practice (including inappropriate ad content reported by teachers); Pro seen as poor value at $9.99 and split across a second app; pattern editor auto-scrolls; preset editing requires save-as-then-delete.
- **Positioning**: the mass-market free Android metronome; the benchmark for reach rather than design.

### Smart Metronome & Tuner (Tomohiro Ihara / IHARA PRODUCTS)
- **Platforms**: iOS 15.6+, iPad, Mac, Vision; Android.
- **Pricing**: free, ad-supported (pop-up ads); no IAP listed.
- **Ratings / scale**: App Store 4.7 (60K); Play 4.6 (8.5K); Play 500K+.
- **Standout**: timing "controlled via hardware, not software", 1/44,100 s (±20 µs) independent of CPU load; Normal (pendulum), Repeat (auto tempo increase) and Program (tempo and meter changes with rehearsal marks) modes; setlists by song; continuous timer and practice log; drum machine, tuner, recorder; MusicXML import; large BPM numeral with Italian markings; VoiceOver.
- **Timing claim**: the strongest in the category ("the same as listening to a recorded CD").
- **Design**: "Simple, Stylish and Accurate" Japanese utility aesthetic; advanced modes hidden behind a learning curve.
- **Weaknesses**: intrusive pop-up ads that sometimes fail to load; no quintuplets/sextuplets; discoverability of Repeat/Program; kitchen-sink extras.
- **Positioning**: free precision metronome with a program mode for classical and ensemble players; the most-rated indie in the set.

### Metronome Ϟ / MetroTimer (ONYX Apps)
- **Platforms**: iPhone, iPad (iOS 17+).
- **Pricing**: free; Pro $9.99 (custom time signatures, accents, subdivisions, beat editor, presets).
- **Ratings / scale**: App Store 4.6 (37K); claims 1M+ users.
- **Standout**: "measured to be more accurate than any other metronome app for iOS", "laboratory standards" (the developer specialises in clock regulation and argues competitors rely on flawed Apple sample code); 8 sounds incl. voice; LED flash; practice timer; 30–250 BPM; beat editor and presets (Pro).
- **Design**: dark, digital, deliberately simple; "beautifully designed and really easy to use" (Musician Wave); featured on Behance.
- **Weaknesses**: time signatures and subdivisions behind a $9.99 unlock; AirPods audio bug and setlist reordering bug in reviews; no Watch or widgets.
- **Positioning**: "the most accurate metronome for iPhone" with a minimalist face.

---

## 3. Premium, indie and design-led apps

### Time Guru Metronome (Avi Bortnick with Adam Bellard)
- iPhone $2.99 (separate $2.99 iPad/Mac app); Android paid. App Store 4.5 (450), iPad 4.6 (43); Play 4.6 (~700); 10K+ installs; 6.4 MB.
- **Signature**: random muting, patterned muting, or both; unlimited chains of shifting meters up to 16/x with drag reordering; gradual tempo change; practice timer with auto-stop; 35 sound sets with drum-machine patterns; voice counting in five languages; 5–300 BPM; iCloud backup and preset export/import; lock-screen player; mixes with other audio; full VoiceOver.
- **Design**: function-first and plain ("Time Guru isn't pretty but it does have random beat muting"); users ask for a more intuitive layout.
- **Weaknesses**: iPad/Mac a second purchase; no Watch, widgets, Link or pedals; no 32nds or custom accent sounds.
- **Positioning**: the specialist for internal-pulse training; recommended by Practis, Rhythm Notes, Bulletproof Musician.

### Dr.Betotte Metronome (Seishu Murakami)
- iOS only; free since 2019 with a premium unlock (a full unlock around $19.99 per Coda); last updated Nov 2023; the US listing was unreachable on every mirror tried.
- **Signature**: six independent note divisions each with volume, mute and sound; per-division note value, polyrhythm, tuplet or custom sequence; swing; loop timer; MIDI out per division; custom sound import; coach functions (gradual tempo, "quiet count" muted bars); playlists with iCloud sync; Bluetooth pedal.
- **Design**: a software Boss Dr. Beat; dense skeuomorphic panel.
- **Weaknesses**: overwhelming for non-drummers; stale; features behind IAP; no Android.

### Metronome ∞ / Metronome Infinity (Panoramic Software)
- iPhone, iPad, Mac, Vision, Apple Watch start/stop; free with ads; Full $1.99, ad removal $0.99, Guitar Suite $0.99, Tuner $0.99. App Store 4.5 (800); claims 12M+ downloads.
- Orbit and Pendulum visualisers; bar-muting and tempo ramp; presets and setlists; count-in; custom sounds; bundled tuner and chord library.
- **Weaknesses**: ads; diluted by guitar tools; vague engine claims; a generic name that cannot be recommended by name.

### Tempo Advance (Frozen Ape)
- iOS only, $3.99 one-time, no IAP; App Store 4.8 (706).
- Up to 20 beats per bar with up to 20 subdivisions per beat; four beat states (Accent 1, Accent 2, Regular, Silent); polyrhythm mode with two channels; 10–800 BPM; four ways to edit tempo (gestures, tap, buttons, direct input); Automator (accelerando/ritardando by BPM or time) and Tracker; unlimited presets; setlists with auto-advance; 15 sound sets incl. voice; audio panning; five themes plus editor; linear or rotary display; AirTurn/iRig pedals; proximity-sensor control; Mike Mangini endorsement.
- **Weaknesses**: polyrhythm mode confuses users; reports of slight tempo fluctuation; iOS only; no Watch, widgets or Link.

### Metronomics (John Nastos)
- iOS $4.99 plus Pro $4.99/year; Android free and stale (2022, Play 3.8); Mac, Watch, Vision. App Store 5.0 (25).
- Probability slider per subdivision (beat randomisation); up to 10 subdivisions with custom ratios (5/7, 23/4); any and mixed meters; 40+ samples per subdivision incl. speech; preset grooves; rhythmic displacement offset; precise swing; independence mode; AUv3, Ableton Link, MIDI in/out; presets as files.
- **Design**: mixer-desk UI; "not the most attractive" but "easier to use than Time Guru" for some.
- **Weaknesses**: tiny rating base; paid plus subscription; sound quality criticised; niche jazz focus.

### Pulse – Metronome & Tap Tempo (MuseScore BVBA; Android by Crescendo Technologies)
- Free, no IAP, no ads. App Store 4.6 (4.5–5.1K); Play 4.3 (431); ~750K iOS downloads estimated. **Last updated Nov 2018 (iOS) and Dec 2017 (Android).**
- One large tap circle with a live readout; custom colours for downbeats/upbeats; playlists; Bluetooth tempo sync across devices; haptics; Watch app.
- **Design**: still the reference the category is compared to (Lindeblad, Musician Wave, Omari MC); praised specifically for looks.
- **Weaknesses**: abandoned; Watch app halts when the wrist lowers; no accent editor, trainers or count-in; split publishers.
- **Lesson**: a beautiful free metronome earned a large rating base on design alone, then stopped.

### Metronome Tuner | Practice+ and Practice Pro (Dynamic App Design)
- Practice+: free with ads; Unlock Everything $19.99, $2.99/month, $9.99/year, and $5.99 each for six unlocks; App Store 4.4 (2.8K); last updated Jul 2024. Full-screen ads interrupt practice.
- Practice Pro: iOS 18+, free (launched at $5); App Store 4.7 (162); 20 practice widgets on a home-screen-style grid (metronome, tuner, drone, recorder, tracker, tally, timer, mirror); "an elegant, all-in-one practice tool" (Music Ed Tech Talk); iPad glitches; the metronome is one widget among twenty.

### Tack: Metronome (Patrick Zedler; open source, GPLv3)
- Android and Wear OS only; free, no ads, no analytics; optional paid "key" donation. Play 4.9 (1.6K); 100K+ installs; 480 GitHub stars; v6.3.1 Mar 2026.
- Material You, animated icons, tiny download; per-beat emphasis and subdivisions; polyrhythm; swing; muted beats; count-in; duration limit; incremental tempo; song library; bookmarks; app shortcuts; best-in-class Wear OS app (rotary input, side button, wrist gestures); screen flash; volume boost; **audio latency correction**.
- **Design**: the best-looking Android metronome; "the cleanest Android-only pick" (Practis).
- **Weaknesses**: Android only; no practice log.

### Takt: Precision Metronome (Pialon)
- Android only; free; v1.2.0 Aug 2026; Play 4.0 (1 review), 100+ installs.
- Large BPM wheel with 0.1 BPM steps; **Drift Mode** (a deliberately unstable click to train internal pulse); **Accuracy Test** that scores timing; polyrhythm, tempo trainer, swing, accents, presets, haptics. Practis made it the Android practice pick despite no track record.

### True Metronome (Mikhail Motyzhenkov)
- iPhone, iPad, Mac, Watch, Android, web; free core; Pro $9.99 (site says $4.99), explicitly anti-subscription; App Store 4.6 (196); developed since 2012; 22 languages.
- Physics-based pendulum; recorded mechanical ticks that vary yet stay "frame-accurate"; 20–400 BPM; 1–13 beats; triplets, sixteenths, swing, dotted, tuplets; tempo trainer; live tempo detection via mic; presets; Watch, widgets, Siri; stats with global rankings.
- **Weaknesses**: thin free tier (subdivisions and trainer are Pro); price inconsistency.

### BackBeat Drums & Metronome Pro (Timothy James Poulos)
- iOS 18+, Mac; free, Pro $4.99; App Store 4.8 (5); v2.2.2 Aug 2026.
- Sequence Builder chaining rhythms, tempos, ramps, meter changes into routines; visual grid editor; drum machine with custom WAV kits; drones; WAV/MIDI export; swing; polyrhythm visualisation; progressive-speed loop; 37 themes.
- **Weaknesses**: almost no review base; iOS 18+ only; scope creep.

### Gap Click by Benny Greb (Kick Snare Hat Apps)
- iPhone and Android; $2.99 one-time, "no IAP, all future features included"; App Store 4.6 (74); last updated Oct 2023.
- Configurable gap sequences; off-beat click patterns (binary and ternary syncopations); 2/4–7/4; 10–300 BPM; VoiceOver.
- **Weaknesses**: limited meters, no presets, stale, stops when the phone locks per reviews.
- **Note**: owns the term "gap trainer" in web search.

### Metronome+ (Dynamic App Design)
- iOS, Mac, Vision; free; Unlock Everything $9.99 or $3.99 per feature (even the options menu); App Store 4.5 (1.2K); last updated May 2024.
- Metronome and tuner simultaneously; recorder; auto-loop with tempo increase; setlists; pitch player; Siri Shortcuts; "futuristic", "fabulous design" in roundups.

---

## 4. The defaults: what captures the job without a store search

| Alternative | Cost | What it does | What it lacks | Job share |
|---|---|---|---|---|
| **Google Search "metronome"** | free, online | 40–218 BPM, default 120, instant | no meter, accents, subdivisions, tap, sounds, presets; needs internet; suspends when the tab backgrounds; drift reported on mobile | the largest slice of one-off "what does 92 feel like" uses; ~none of sustained practice (metronomeonline.com alone draws ~70K US visits/month on the keyword) |
| **GarageBand / Logic** | preinstalled / paid | count-in, sound and level, per-song tempo and meter; Logic adds bar/beat/division levels, polyphonic click, MIDI out | only runs while a song plays; no trainer, timer, tracking; heavy for a one-second job | modest; people already recording |
| **YouTube tempo videos** | free with ads; Premium for screen-off | zero setup on any device, long-form | one tempo per video; ads interrupt; screen-off paywalled; no accents or subdivisions | small; beginners, school and church rooms |
| **Soundbrenner Pulse / Core 2** | $119 / $229 / $329 | wrist haptics, 20–400 BPM, app ecosystem | subscription for songs and trainers; 3–6 h battery; vibration dulled by playing | dominant among app users who want "full-featured free", and the only wrist option that works |
| **Korg MA-2** | ~$30 | 30–252 BPM, 0–9 beats, 8 rhythm types, 1–120 min timer, tones, ~400 h battery | one tone, tiny buttons, no presets or trainer | still large among school and orchestral players, shrinking |
| **Boss DB-90 Dr. Beat** | ~$155 | 30–250 BPM, 4 clicks incl. voice, 30 drum patterns, note mixing, 50 memories, Rhythm Coach with mic input, footswitch, MIDI | expensive, "harder to use", heavy on batteries | small and sticky (studios, drum teachers, marching) |
| **Moises** | $5.99/mo | click synced to any uploaded song | no standalone click; paywalled after 60 s | niche "play along with the record" |
| **BandLab** | free | tap, meters, subdivisions, patterns, sounds inside a DAW | account sign-in; no trainer, presets or stats | Gen-Z producers and singers already in BandLab |
| **Yousician / Fender Play / Drumeo** | $120–$279/yr | a metronome inside lesson content (Drumeo adds auto tempo increase and repeat measure) | nothing standalone; expensive for a click | own the click for their subscribers during lessons, then lose them |
| **Simply Guitar** | $120/yr | – | has no metronome at all, sending its users to the store | none |
| **Drum Beats+** | free + IAP | drum loops instead of clicks; drop-out practice; swing; count-in | 5 BPM steps to 200; subscription for loops | a loyal slice who hate the tick |

What a standalone free app must offer to be chosen over these: instant start offline (beats Google and YouTube), accents and subdivisions (beats Google, YouTube, GarageBand), a locked-screen story (beats all three), presets and trainers (beats hardware and lesson apps), and no account or subscription (beats Soundbrenner, Moises, BandLab). This app already clears every one of those bars except loudness and range.

---

## 5. Category norms and differentiators

### 5.1 What every credible app in the tier has

- Tap tempo, ±1 and ±5 nudges, a slider or dial, Italian markings.
- A BPM range of at least 20–300 (Tempo 10–800, Beats 1–900, TonalEnergy to 1000, Soundbrenner 20–400, Time Guru 5–300, True 20–400, Gap Click 10–300, ONYX 30–250, Korg 30–252, Boss 30–250, Yousician 30–240, Google 40–218). **40–220 is the narrowest range in this document.**
- Arbitrary or near-arbitrary time signatures (numerator ≥ 1–13, /8 and /16 denominators), compound meters, often additive groupings.
- Per-beat accent editing with at least three states; several apps have two accent levels.
- Subdivisions beyond eighth/triplet/sixteenth: swing and dotted are near-universal in the paid tier.
- A library of 8–35 sounds, usually including a counting voice.
- Visual beat (flash, pendulum, LEDs, dots), vibration, screen flash.
- Count-in; a timer or auto-stop after N bars or seconds; bar and beat counters.
- Tempo trainer (step every N bars or seconds) and a muted-bar or gap trainer.
- Named presets and some form of setlist; export/backup or cloud sync of them.
- Background playback that mixes with other audio, with lock-screen or media controls.
- Light and dark themes.
- A free core with a one-time upgrade, or a low one-time price; ads are a stated negative in every roundup.
- Apple Watch companion (Pro Metronome, Tempo, TonalEnergy, Beats, True, Pulse, Metronomics, Metronome ∞) or Wear OS (Tack).
- Bluetooth pedal / MIDI / keyboard control in the pro apps; Ableton Link in three.

### 5.2 What makes an app stand out in reviews and roundups

- A **numeric timing claim**: Pro Metronome ±20 µs, Smart Metronome 1/44,100 s, ONYX "laboratory standards", True "zero drift", Korg ±0.02%, Boss ±0.1%. Practis' thesis: "the training feature that improves timing is often the one that makes the click disappear".
- **Silence training** in many flavours: random and pattern mute (Time Guru), Coach Mode (Tempo), Muted Beats (Soundbrenner, paid), Metronome Assistant (TonalEnergy), probability sliders (Metronomics), gap sequences and off-beat clicks (Gap Click), Drift Mode and Accuracy Test (Takt), drop-out practice (Drum Beats+).
- **Routine and program builders**: Time Guru chains, Smart Metronome Program Mode with rehearsal marks, BackBeat Sequence Builder, Tempo Advance Automator, PolyNome song maps, Metronome Beats Pro songs with sections.
- **Polyrhythms and custom ratios** (Tempo Advance, Tack, Beats, Pro Metronome, Dr. Betotte, Metronomics).
- **Sound character**: voice counting in multiple languages, drum-machine patterns, recorded mechanical ticks with natural variation (True).
- **Visualisers as identity**: Pulse's tap circle, Metronome ∞'s Orbit, Tempo Advance's ring, True's physics pendulum, Metronomics' wheel, Tack's beat bars.
- **Wearables and glanceable surfaces**: Tack's Wear OS app, the Apple Watch apps, home and lock-screen widgets (Soundbrenner, Pro Metronome, True).
- **Practice logging** with goals, streaks and stats (Soundbrenner, Smart Metronome LOG, True rankings, Practice Pro, Metronomics).
- **Honest pricing as a feature**: "no IAP, all future features included" (Gap Click), "no ads or analytics" (Tack), one-time $2.99–$3.99 (Time Guru, Tempo Advance).
- **Hands-free control**: pedals (Tempo, Tempo Advance, Dr. Betotte, Pro Metronome), keyboard shortcuts (Soundbrenner iPad/Mac), proximity sensor (Tempo Advance), media buttons (Beats), Siri Shortcuts (Metronome+, True).
- **Accessibility as a listed feature** (Time Guru, Smart Metronome, Gap Click: full VoiceOver) and **tiny download size** (Time Guru 6.4 MB, Tempo 16 MB) versus Soundbrenner's 313 MB.

---

## 6. Gap matrix against this app

| Capability | This app (1.3.0) | Who has it | Importance |
|---|---|---|---|
| BPM range | 40–220 | everyone wider; Google 40–218 | **high** |
| Time signatures | 2/4, 3/4, 4/4, 5/4, 6/8, 7/8 fixed | Tempo 35, Time Guru to 16/x, Tempo Advance 20 beats, Tack/Metronomics any, Pro Metronome additive, True 1–13 | **high** |
| Click sounds | 3, no voice, no pitch | 8–35 with voice in most paid apps | **high** |
| Subdivisions | 8th, triplet, 16th; no swing/dotted/quintuplet; no per-subdivision accent | Tack, True, Beats, Pro Metronome, Metronomics, Dr. Betotte, BackBeat, Takt, Tempo Advance | **high** |
| Watch companion | none | Tack (Wear OS); Tempo, Pro Metronome, TonalEnergy, Beats, True, Pulse, Metronomics, Metronome ∞ (Apple Watch) | **high** (most-requested platform feature in reviews) |
| Gig setlists (song names, notes, big display, auto-advance, sharing) | Practice Sets are goal-oriented, not gig-oriented; no export | Tempo, Tempo Advance, Beats Pro, Soundbrenner, Time Guru, Pro Metronome | medium |
| Published timing figure | none; drift-free scheduling only | Pro Metronome, Smart Metronome, ONYX, True, Korg, Boss | medium |
| Gap trainer modes | fixed N-on/N-off bars | Time Guru random/pattern, Takt drift + scoring, Metronomics probability, Gap Click off-beat, TonalEnergy Assistant | medium |
| Home/lock-screen widget | Live Activity only (iOS) | Soundbrenner, Pro Metronome, True | medium |
| Pedal / keyboard / media-button / Siri control | none; Android notification has Stop only | Tempo, Tempo Advance, Soundbrenner, Pro Metronome, Dr. Betotte, Beats, Metronome+, True | medium |
| Program mode (meter and tempo changes within a piece) | none (sets change presets, not bars) | Time Guru, Smart Metronome, BackBeat, Tempo Advance, PolyNome | medium |
| Two accent levels | accent / normal / mute | Tempo Advance, Pro Metronome (f/mf/p), Tack | low |
| Polyrhythm | none (ROADMAP: after unified scheduler) | Tempo Advance, Tack, Beats, Pro Metronome, Dr. Betotte, Metronomics, Takt, BackBeat | low–medium |
| Export/backup of presets and sets | none | Pro Metronome (iCloud), Soundbrenner (Plus), Tempo (files), Beats (.mbeats), Time Guru | low–medium |
| Tuner / drone | none (deliberate non-goal) | Soundbrenner, TonalEnergy, Pro Metronome, Metronome ∞, Metronome+ | low |
| Ableton Link / MIDI / AUv3 | none | Pro Metronome, Soundbrenner, TonalEnergy, Metronomics, Dr. Betotte | low |
| Mic tempo detection | none | True, Metronomics Pro | low |
| Accessibility statement in listing | none | Time Guru, Smart Metronome, Gap Click | low |

**Where this app already leads**: free with no ads, account or paywall (only Tack and Pulse match, and neither has practice tools); both trainers free (paid in Pro Metronome, Soundbrenner, Practice+, True); Practice Sets with goals and session recovery (no equal); local stats without an account (only Soundbrenner tracks, with an account and upsell); Live Activity and Dynamic Island (none list it); real Android parity from one codebase (every indie is single-platform or lags on Android); Material You plus five schemes (only Tack has dynamic colour); stereo pan (only Tempo Advance); active maintenance in a field of dormant apps; a browser build.

---

## 7. What musicians want, by segment

Condensed from reviews, forums and teacher blogs. The full per-segment lists are in the review's §3.1 table; the segment-specific needs that shape roadmap choices:

- **Drummers**: several sounds that sit differently in a mix (woodblock through a kit, soft click for late-night headphones, never a hi-hat sample); per-beat accent and mute; triplets to septuplets; sample-accurate timing ("if the click is the one drifting, you'll spend months fixing a problem that doesn't exist"); gap and random mute; tempo ramp "start at N, +N every N bars"; setlists for several bands; 20–300 BPM (some ask for 400); loud through headphones; keeps playing when locked; pedal control on stage.
- **Guitarists and bassists**: a speed trainer ("revolutionized my practicing"); gap mode as a time test; a countdown timer inside the metronome; tap tempo; click on 2 and 4 and half-time for jazz; free and clean tools; drum grooves as an alternative to a bare click; setlists that can be edited and reordered; tap-to-type a BPM.
- **Pianists**: lock-screen control without unlocking (the exact Piano World complaint the Live Activity answers); settings that save; a distinct beat-1 sound ("classic bell") and realistic wood; loud enough over an acoustic piano; simple UI ("one or two extra steps and I won't bother"); runs behind a sheet-music app.
- **Classical and orchestral**: the dotted-quarter pulse in 6/8, not the eighth; up to septuplets; accent patterns and programmable meter and tempo changes; 5-BPM-increment building automated; rock-steady tempo and a clean design (Metronome+ won 79% of a Bulletproof Musician vote for exactly this); wide meter set incl. cut time; loud enough for brass.
- **Singers**: slow tempos with the click on the beat only; count-in and counting rests; body-pulse exercises (a gap trainer by another name); progressive subdivision; voice counting; a click that is not fatiguing and switches off quickly.
- **Teachers and students**: something recommendable without a payment conversation; a plain default click; tap tempo to check a student; practice tracking that motivates; no data collection and no ad content (a band director deleted an app after an inappropriate ad); students "will barely download a metronome app", so any friction kills adoption.
- **Beginners**: start slower than you think; count along; visual feedback and a clear beat 1; count-in; a progression that weans them off the click; encouragement; basics free with no account.

---

## 8. Monetisation landscape

Models seen: free + ads + one-time unlock (Beats, MetroTimer, Smart Metronome); free core + one-time Pro, no ads (True, Very Good Metronome, Tack donation key, BackBeat themes); free + à-la-carte $0.99 unlocks + $3.99 Pro + subscription (Pro Metronome, with backlash); free + $7.99/month subscription with account and song cap (Soundbrenner); paid up front $1.99–$3.99 (Tempo, Tempo Advance, Metronome+, Metronomics, Time Guru, Gap Click); paid + large Pro + subscription (PolyNome $9.99 / $49.99 / $7.99 per month); separate desktop licence (TonalEnergy $9.99); metronome as a lead magnet for lessons or hardware; open source / donation-ware; tip jar as consumable IAP ($1.99 / $4.99 / $9.99).

What free users tolerate: a small static banner, grudgingly; nothing else. Interstitials, pop-ups on return, ads mid-gig and any inappropriate ad content produce 1-star reviews and deletions, especially from teachers. Subscription nags on launch and moving once-free basics behind a wall are the most-hated moves in the category. Users readily pay $2–$10 once for things they classify as "extra": polyrhythms, setlists, custom meters, sound packs, Watch, desktop.

Options consistent with "no ads, no account, no subscription pressure", should revenue ever be a goal: a tip jar in About; a one-time Supporter/Pro unlock ($2.99–$4.99) covering only genuinely new advanced work, never anything already free; paid sound and voice packs; a separately sold desktop build; a teacher pack (shareable preset/set links, exportable logs, projector mode). Avoid banners, subscriptions for basics, account gates, promo interstitials and hardware-upsell home screens.

---

## 9. Sources

Store listings and review pages (fetched 2026-09-05): Pro Metronome `apps.apple.com/us/app/id477960671`, `play.google.com/store/apps/details?id=com.eumlab.android.prometronome`, `eumlab.com/pro-metronome`; Tempo `id304731501`, `com.frozenape.tempo`, `com.frozenape.tempolite`, `frozenape.com`; Soundbrenner `id1048954353`, `com.soundbrenner.pulse`, `soundbrenner.com`; TonalEnergy `id497716362`, `com.sonosaurus.tonalenergytuner`, `tonalenergy.com`; Metronome Beats `id1486779670`, `com.andymstone.metronome`, `com.andymstone.metronomepro`; Time Guru `id421929034`, `id1065287026`; Dr. Betotte via `appstor.io`, `updatestar`, `codamusictech.com`; Metronome ∞ `id540427956`, `panoramicsoft.com`; Smart Metronome `id889571826`; Tempo Advance `id368169363`; Metronomics `id435393098`, `metronomicsapp.com`; Pulse `id1097323003`; Practice+ `id858142974`; Practice Pro `id1615430454`; Tack `github.com/patzly/tack-android`, `patrickzedler.com/tack`; Takt via apkcombo; ONYX `id416443133`, `metronomeapp.onyx3.com`; True Metronome `id574204413`, `truemetronome.app`; BackBeat `id6748804205`; Gap Click `id1443682940`; Metronome+ `id434136233`.

Roundups and press: Practis "Best Metronome Apps in 2026", "Best Metronome Apps for iPhone 2026", "for Android 2026", "Best Free Metronome Apps in 2026"; Musician Wave "10 Best Metronome Apps"; BeatIt "Top 5 Metronome Apps"; Rhythm Notes "Best Metronome Apps"; Bulletproof Musician "Five Best Metronome Apps"; Lindeblad Piano; Omari MC; Ted's List; Music Ed Tech Talk (Practice Pro); Tape Op and Melodics (Pro Metronome); WIRED and MusicRadar (Soundbrenner); Search Engine Land and 9to5Google (Google metronome); Korg, Boss/Thomann, Drum Helper, No Treble (hardware); Moises, BandLab, Yousician, Fender, Drumeo, Simply Guitar (Guitar.com review) product pages.

Musician needs: r/drums, r/Guitar, r/guitarlessons, r/Bass, r/piano, r/MusicTeachers threads; Piano World thread 3267469 (lock-screen control); TalkBass thread titles; Gearspace; Violin Lounge; Seagraves Violin Studio; Jazzadvice; Six String Journal; ArtistWorks; Willan Academy; PerformanceUp; Chris Turnbaugh; JustinGuitar Time Trainer reviews; metrognome.co.za; App Store and Play reviews of the apps above.
