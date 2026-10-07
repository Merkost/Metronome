# Rhythm and home layout

Rhythm groups time signature and beat editing into a clear primary panel, follows with subdivisions, and finishes with a quieter count-in panel. Concise state keys preserve accent/normal/muted semantics; the actual playback ring remains independent. Meter and subdivision controls expose radio selection, headings are explicit, and count-in reuses the shared switch row.

The home content owns 32dp of padding at its scroll end. Practice and Sound use one shared sizing calculation with reserved title/caption slots for every sound. Both controls therefore remain equal in height while captions transition. The layout keeps side-by-side cards while words fit, moves icons above text when needed, then uses full-width rows for narrow large-text content. Fonts, semantic colors, rounded shapes and motion tokens stay branded; RTL arrows mirror.

## Evidence

- [Before shortcuts](baseline-shortcuts.png), [matching shortcuts](fixed-shortcuts.png), [dark RTL at 2x text](shortcuts-dark-rtl-2x.png).
- [Rhythm hierarchy](rhythm-light.png), [large RTL rhythm top](rhythm-dark-rtl-2x-top.png), [reachable count-in at scroll end](rhythm-dark-rtl-2x-end.png).
- [Review](review.md), [source hashes/results](validation.json), [gate output](build-result.txt), [native cases](rendering-result.xml).

Five native cases reproduce the prior height/padding defects, check equal geometry across every sound and transition, measure whole-word text fit, exercise separate Practice/Sound callbacks, verify painted dark-title ink, check Rhythm reading order and individual meter/beat/subdivision/count-in actions, and scroll large RTL content to the final switch. The baseline controls measured 86px and 70px; end padding measured 0. Final shared controls match and end padding is 32dp.

These are native Compose fixtures rendering production extracted components, not screenshots of the complete MainScreen, UIKit modal or a physical device. Existing sheet safe-area evidence remains in the earlier bottom-sheet artifact. Dark ink-channel difference detects the inherited-black regression; it does not certify WCAG contrast. No full-app dynamic-type or VoiceOver/TalkBack traversal is claimed.

Temporary export code is preserved in `capture-fixture.kt.txt` outside compiled source sets and was removed before final gates. PPM pixels were converted losslessly to PNG. The native suite uses the project's packaged real simulator SwiftPM SDK. Playback, rhythm settings and native picker callbacks retain their existing behavior.
