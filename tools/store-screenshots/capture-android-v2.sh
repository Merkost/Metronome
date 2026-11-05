#!/usr/bin/env bash
set -euo pipefail

PKG=com.merkost.metronome
SERIAL="${ANDROID_SERIAL:-emulator-5554}"
export ANDROID_SERIAL="$SERIAL"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="${1:-$HERE/artifacts/store-screenshots-2026-11/raw/android}"
mkdir -p "$OUT"

dump() { adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb shell cat /sdcard/ui.xml; }

tap_text() {
  local bounds a b c d
  bounds=$(dump | tr '<' '\n<' | grep -F "text=\"$1\"" | grep -oE 'bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"' | head -1)
  if [ -z "$bounds" ]; then echo "  ! not found: $1" >&2; return 1; fi
  read -r a b c d <<<"$(echo "$bounds" | grep -oE '[0-9]+' | tr '\n' ' ')"
  adb shell input tap $(( (a + c) / 2 )) $(( (b + d) / 2 ))
  sleep 2.5
}

shot() { adb shell screencap -p /sdcard/s.png; adb pull /sdcard/s.png "$OUT/$1.png" >/dev/null; echo "  captured $1"; }

demo() {
  adb shell settings put global sysui_demo_allowed 1
  adb shell am broadcast -a com.android.systemui.demo -e command enter >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0941 >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile hide >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false >/dev/null
  adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false >/dev/null
}

demo_off() { adb shell am broadcast -a com.android.systemui.demo -e command exit >/dev/null || true; }
trap demo_off EXIT

demo
adb shell am force-stop "$PKG"
adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
until dump | grep -q 'text="Tap"'; do sleep 2; done
sleep 2

shot main

W=$(adb shell wm size | grep -oE '[0-9]+x[0-9]+' | tail -1 | cut -dx -f1)
adb shell input tap $(( W - 72 )) 146
sleep 4
shot settings
adb shell input swipe 540 1800 540 700 400
sleep 1.5
shot settings-2
adb shell input keyevent KEYCODE_BACK
sleep 3

tap_text "Practice"
shot practice-sheet
tap_text "Timer"
shot timer-sheet
adb shell input keyevent KEYCODE_BACK
sleep 2

tap_text "Andante"
shot tempo-sheet
tap_text "Tempo trainer"
shot tempo-trainer
tap_text "Tempo trainer"
tap_text "Gap trainer"
shot gap-trainer
tap_text "Gap trainer"
tap_text "Subdivision"
shot subdivisions
adb shell input keyevent KEYCODE_BACK
