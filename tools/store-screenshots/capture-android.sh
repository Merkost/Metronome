#!/usr/bin/env bash
# Captures source screenshots from a running Android emulator into raw/.
# Taps are resolved from the accessibility tree by visible text, not fixed
# coordinates, so this survives layout changes.
set -euo pipefail

PKG=com.merkost.metronome
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RAW="$HERE/raw"
mkdir -p "$RAW"

dump() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb shell cat /sdcard/ui.xml; }

# tap_text "Some label" — taps the centre of the first node containing that text
tap_text() {
  local bounds
  bounds=$(dump | tr '<' '\n<' | grep -F "text=\"$1\"" | grep -oE 'bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"' | head -1)
  if [ -z "$bounds" ]; then echo "  ! not found: $1" >&2; return 1; fi
  read -r a b c d <<<"$(echo "$bounds" | grep -oE '[0-9]+' | tr '\n' ' ')"
  adb shell input tap $(( (a + c) / 2 )) $(( (b + d) / 2 ))
  sleep 3
}

shot() { adb shell screencap -p /sdcard/s.png; adb pull /sdcard/s.png "$RAW/$1.png" >/dev/null; echo "  captured $1.png"; }

wait_for_app() {
  until dump | grep -q "Tap"; do sleep 3; done
}

echo "Launching $PKG"
adb shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
wait_for_app
tap_text "Skip" 2>/dev/null || true   # dismiss onboarding if this is a fresh install

echo "main"
shot main

echo "settings"
adb shell input tap "$(adb shell wm size | grep -oE '[0-9]+x[0-9]+' | cut -dx -f1 | awk '{print $1-80}')" 149
sleep 5
shot settings
adb shell input keyevent KEYCODE_BACK; sleep 4

echo "tempo sheet states"
tap_text "Andante"
tap_text "Tempo Trainer" && shot tempo-trainer
tap_text "Tempo Trainer"          # collapse
tap_text "Gap Trainer" && shot gap-trainer
tap_text "Gap Trainer"
tap_text "Subdivision" && shot subdivisions
adb shell input keyevent KEYCODE_BACK; sleep 3

echo
echo "Captured what can be reached deterministically."
echo "Still to capture by hand (each needs app state this script cannot seed):"
echo "  accents.png        main screen with beats accented and muted"
echo "  practice-sets.png  a Practice Set with two or three steps"
echo "  background.png     Lock Screen Live Activity (iOS) or the notification (Android)"
