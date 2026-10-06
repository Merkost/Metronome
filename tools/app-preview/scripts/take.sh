#!/bin/zsh
set -euo pipefail
here=$(dirname $0)
name=$1; secs=$2; shift 2
$here/sim.sh rec-start $name
for t in "$@"; do
  case $t in
    wait:*) sleep ${t#wait:} ;;
    *) x=${t%,*}; y=${t#*,}; $here/sim.sh tap $x $y ;;
  esac
done
sleep $secs
$here/sim.sh rec-stop $name
