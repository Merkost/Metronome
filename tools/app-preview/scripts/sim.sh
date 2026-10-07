#!/bin/zsh
set -euo pipefail

UDID="${SIM_UDID:?set SIM_UDID}"
OUT="${SIM_OUT:-$(dirname $0)/../captures}"
mkdir -p "$OUT"

cmd="$1"; shift
case "$cmd" in
  prep)
    xcrun simctl status_bar "$UDID" override --time "9:41" --dataNetwork wifi --wifiMode active --wifiBars 3 \
      --cellularMode active --cellularBars 4 --operatorName "" --batteryState discharging --batteryLevel 100
    xcrun simctl ui "$UDID" appearance light
    ;;
  shot)
    xcrun simctl io "$UDID" screenshot --type=png "$OUT/$1.png" >/dev/null 2>&1
    echo "$OUT/$1.png"
    ;;
  rec-start)
    rm -f "$OUT/$1.mov"
    nohup xcrun simctl io "$UDID" recordVideo --codec=h264 --force "$OUT/$1.mov" > "$OUT/$1.rec.log" 2>&1 &
    echo $! > "$OUT/$1.pid"
    sleep 1.5
    ;;
  rec-stop)
    kill -INT "$(cat "$OUT/$1.pid")"
    while kill -0 "$(cat "$OUT/$1.pid")" 2>/dev/null; do sleep 0.2; done
    ls -la "$OUT/$1.mov"
    ;;
  tap)
    axe tap -x "$1" -y "$2" --udid "$UDID" >/dev/null
    ;;
  ui)
    axe describe-ui --udid "$UDID"
    ;;
  *)
    echo "unknown $cmd"; exit 1
    ;;
esac
