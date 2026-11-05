# Metronome — connected design exploration

3 October 2026 · Proposal grounded in the current app

## Start here

The interactive web mockups are at http://127.0.0.1:8767/. Use the screen rail on a wide browser or the Explore selector in a narrow panel. The iPhone / Pixel 10 selector changes the preview device. Each screen includes design intent outside the phone; those notes are not proposed app content.

The [screen gallery](http://127.0.0.1:8767/gallery.html) shows all seventeen views together, plus dark appearance examples. Each screen opens its interactive view. The [onboarding walkthrough](http://127.0.0.1:8767/onboarding.html) shows the distinct Hello, rhythm welcome: tap the mark, choose a pace and enter practice. The [Settings refresh](http://127.0.0.1:8767/settings.html) covers the playful hierarchy and permanent Suby studio section.

Keep the instrument identity. Make existing depth easier to find, improve Settings ownership, add a scalable sound picker, and make the first practice routine easier to create. The strongest new feature is editable starter routines built on the existing presets and sets.

These are local web mockups. Native application code, native audio resources and the adopted roadmap were not changed.

## What was explored

The source review covered every shared screen and its principal models and navigation, reusable controls, Settings persistence, preset/set validation and recovery, practice tracking, audio and platform integration, first-use guidance, What's New, product/design documents and the adopted roadmap. Six native screens captured earlier in this conversation provide the visual grounding. That installed iOS 1.3.0 build predates the checkout's Settings headings and theme selector; source and installed evidence remain distinct.

| Surface | Existing capability | Proposed improvement |
|---|---|---|
| Instrument | BPM 40–220, meter, accent/mute, slider, quick tempo, Tap Tempo, playback | Exact entry on BPM; explicit Practice and current-sound shortcuts; keep playback anchored |
| Tempo/tools sheet | Tempo markings, saved work, subdivision, tempo/gap trainers | Split Rhythm from Practice; one clear owner for each task |
| Rhythm | Six meters; quarter/eighth/triplet/sixteenth subdivisions; per-beat states | Group meter, subdivision and accents; explain pulse meaning for compound meters |
| Presets | Named full setups; favourites, recents, rename, duplicate, delete and ordering | Readable tempo/rhythm summaries and direct Save current setup; preserve migration and referenced-preset guards |
| Practice sets | Ordered preset steps with manual, duration or bar goals; editing and ordering | Editable starter, useful library summaries and a clearer first-step explanation |
| Routine editor | Name, add/remove presets, goals and reordering | Put step name, BPM, meter, subdivision and goal together; disable invalid Save; preserve edits when navigating |
| Active practice | Previous/pause/resume/next/finish, recovery and persistence warnings | One focused progress surface; clear goal reached, recovered-paused and save-failure states |
| Timer | Duration choices, custom duration, playback-linked counting and stats | Visible minute units; clear active/paused/complete states; keep extension versus restart distinct |
| Tempo trainer | Ascending/descending ramp by BPM increment and bars | Plain-language progression summary; separate edit/restart decisions during an active ramp |
| Gap trainer | Repeating audible/muted bar pattern | A visual cycle and explicit phase labels; avoid implying a silent bar stops practice |
| Sound | Wood, Click and Classic; volume; stereo pan; platform sound switching | Seven named voices with separate previews; a bold violet panel for the selected sound, volume and haptics; secondary pan controls |
| Appearance | Theme, five colour schemes, Dots/Pendulum and flash | Named selection, coherent light/dark roles, grouped display/motion preferences and system reduced motion |
| Practice data | Today, streak and total stored locally | Dedicated summary/data surface; planned export/import; reset behind confirmation |
| Help and release information | Support, rating and What's New | Move app identity/version below task preferences; make help discoverable and release information quiet |
| First use | Three coach marks over beats, tempo and Play | A distinct branded welcome with one-bar audition and Easy/Steady/Brisk pace choices; Start enters practice and Skip stays silent; teach deeper controls in context |
| Android/iOS/web integration | Audio focus, Android foreground notification, iOS Live Activity and browser demo | Platform-specific wording and controls; device checks before promises about background behaviour or timing |

The mockup contains seventeen screen views, including an optional first-use sound view and a separate view of the Settings footer. This footer is an excerpt of Settings, not a new destination in the proposed native app.

## Recommended information architecture

Instrument owns the immediate beat: BPM, meter, accents, Play/Pause, tap tempo, timer status and one active-practice strip.

Rhythm owns meter, subdivision and beat pattern. Practice owns saved presets, sets, timer, trainers and summary. Settings owns durable defaults and app information.

Use a full-screen Practice surface if the library grows. Rhythm and sound remain suitable sheets. The web exploration uses some full-screen views to make each proposal easy to inspect; that presentation does not require every native control to become a separate route.

| Settings order | Contents |
|---|---|
| Make it yours | Short opening: A little tweak. A better groove. |
| Find your click | Violet sound panel: selected voice, separate one-bar preview, volume and haptics; expand stereo pan under More audio controls |
| Set the mood | Five named palette choices and explicit System/Light/Dark selection |
| Keep your flow | Expand Practice & playback: count-in, screen awake, background playback, and iOS Live Activity |
| See the beat | Expand Beat display & motion: Dots/Pendulum, flash and reduced motion |
| A little more | Your practice/data, What's New, Help & about |
| Your beat. Any screen. | Compact web-app card with a direct Open web app link |
| More by Merkost | Quiet Suby recommendation before version |

Settings remains one scrollable surface. Sound and appearance are the expressive, directly editable areas. In-place disclosure groups keep secondary controls compact without adding another destination. Labels such as Count-in and Background playback remain explicit beneath the playful section titles. After app information, a compact semantic-surface card with a mint monitor glyph showcases Metronome on the web. The direct destination is https://metronome.merkost.dev/app/, discovered from the live landing page and checked on 3 October 2026 (HTTP 200, web-app title and Compose bootstrap). The card has one 60 px external action with shared press feedback; it does not imply that device data syncs or that browser and native capabilities match.

The user's follow-up asks for a modern, catchy, playful Metronome. This direction supersedes the first web mockup's restrained Settings treatment and its old ordering. It amplifies the existing Melrose, Periwinkle, Mint Green and Pink Lace palette, system typography, circles and Lucide icons. Native DESIGN.md and application code were not rewritten.

## Features worth adding

| Priority | Suggestion | Status and rationale |
|---|---|---|
| First | Editable starter routines | New recommendation. Turns the empty set library into a useful first session without silently creating data |
| First | Sound picker and previews | Already planned direction. Makes more sounds manageable; preview must not credit practice time or start a trainer |
| First | Exact BPM and hold-to-repeat | Already on the roadmap. Easier precise changes while holding an instrument |
| First | Settings hierarchy and quiet Suby section | Design recommendation. Improves everyday settings and gives the developer's other app a transparent home |
| Next | Export/import presets and sets | Already planned. User-owned backups and exercise sharing without accounts; needs tolerant codec migration and conflict handling |
| Next | Flexible count-in and compound pulse | Configurable count-in extends existing behaviour. Expanded meter/pulse work is planned; make BPM's pulse unit explicit |
| Later | Swing, progressive/random gaps and time-based ramps | Already planned training depth; release only with engine and device evidence |
| Later | Keyboard and pedal-style shortcuts | Hardware keys are already planned. Useful with occupied hands; preserve platform media-control behaviour |
| Research | Session history and notes | Deliberately parked in the roadmap. Validate demand before adding pressure, calendars or reminders |

Presets, sets, goals, timer, subdivisions, trainers, local stats and recovery are existing features. The proposal focuses on their discovery and presentation. Accounts, cloud sync, leaderboards, tuner/recorder expansion and subscription pressure do not fit this release.

## Sound direction

Keep the three familiar sounds. Add Soft for a rounded, gentler attack; Rim for a dry percussive reference; Clave for focused wooden resonance; Studio for a clean electronic click. The mockup includes four original synthesized sketches, each with normal and accent voices. They are auditionable design samples, not final release masters or recorded acoustic instruments.

The new sketches are mono, 48 kHz, 16-bit PCM WAV, 27–85 ms long, with normal peaks around −6.94 dBFS and accent peaks around −5.04 dBFS. These peak values do not establish equal perceived loudness. Asset details and provenance are in sound-sketches.json inside the prototype folder.

Production acceptance: deliberate accent/normal/subdivision voices, matched perceived normal levels, trimmed tails, licensing/provenance, safe loading and switching, no preview/session overlap, dismissal cleanup and no practice credit from preview. Preserve Android resources, iOS bundle resources and web mapping. Listen on phone speakers, wired headphones and Bluetooth; measure device output before claiming precision.

## Suby placement and copy

Use a permanent soft Periwinkle section, a 54 px real icon, the Suby name, short useful copy and one external action. Place it after Help & about, before version. Keep it static and below the task settings. The section has no dismissal control. No promotion on the instrument, badge, countdown, carousel or modal.

More by Merkost

**Suby**

Subscriptions, a little more sorted.

Keep renewals, costs and shared plans in one place.

Renewals · Costs · Sharing

**Explore Suby**

The feature wording is supported by [Suby's website](https://subyapp.com/), checked during this conversation. The web mockup opens that site. Native delivery should use verified store destinations and a website fallback. Do not assume an installed-app scheme exists. The user's latest direction explicitly makes this section permanent.

## Visual and accessibility improvements

Preserve the 62 px tempo display, 85 dp Play control, system font, Lucide icons, 18 dp padding and 480 dp maximum native content width. Settings gains a 34 px opening, a purposeful violet sound panel and tactile named colour choices. Keep the supporting rows and Suby quiet. Use semantic Material roles when implementing this palette natively; no gradients, decorative illustrations or custom fonts are proposed.

Use readable label/value rows, selected names beside colour swatches, checkmarks and pressed/checked states, units on timers and goals, and explicit expanded state for secondary controls. Reflow long labels and large type. Preserve 48 dp interaction targets even when a visible circle is smaller. Avoid a live announcement on every timer second.

Active trainer, routine and timer status should be understandable when paused, recovered or interrupted. Recover a routine paused, then offer Resume and Finish. A missing preset should identify the affected step and offer repair rather than a vague failed Start. A storage failure needs Retry and a truthful recovery warning. Existing source models already represent these cases; the web mockup does not reproduce every failure path.

## What the web mockup does

Navigation, tempo changes, exact-entry validation, rhythm choices, beat editing, sound choice and one-bar previews work. You can save a sample preset, preview/edit/save/start a routine, change goals, reorder steps, advance/pause/finish, configure/start a timer or trainer, switch appearance and visit the permanent Suby section. Sample export produces a clearly marked example JSON file. The prototype uses temporary in-memory state; refresh resets it. Five slider instances share a custom grip, track, value hint and motion tokens while preserving native range keyboard semantics.

Import, support/rating flows, stats reset and platform settings show their proposed entry/confirmation or explanatory state. OS integration, physical haptics, Live Activity, actual background permissions and production persistence are outside this local mockup. Practice totals are illustrative. The browser's demonstration audio loop is not the native scheduler or timing evidence.

Light and Dark are rendered; System uses the default light preview. Count-in, stereo pan, Pendulum, beat flash and reduced-motion selections demonstrate the proposed preference state. Their native playback/display effects are not reproduced. The prototype's sample presets simplify the full native preset model; production must retain saved accents and count-in.

## Suggested delivery order

1. Improve naming, entry points and Settings order; add the Suby footer using verified links.
2. Build the sound picker/preview and remaster current samples; audition and author new voices separately.
3. Add editable starter routines and remove empty-library dead ends without weakening validation/recovery.
4. Ship exact tempo and backup/import, then deeper meter and training work through existing release gates.

Review both themes and compact/large text, native Back, VoiceOver/TalkBack, offline launch, interruptions, rapid sound switching and goal boundaries. Web visual checks and a web build are separate from native builds and device acceptance.

Beat indicators use unnumbered dots throughout the current instrument and rhythm designs. The accent ring, solid normal dot and outlined mute icon distinguish states. Accessible beat controls retain their index and current state.
