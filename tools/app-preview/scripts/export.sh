#!/bin/zsh
set -euo pipefail
here=$(cd $(dirname $0)/.. && pwd)
master=${1:-$here/out/app-preview-master.mov}
dest=${2:-$here/../../artifacts/app-preview}
mkdir -p "$dest"

ffmpeg -nostdin -v error -y -i "$master" \
  -c:v libx264 -profile:v high -level:v 4.0 -pix_fmt yuv420p -r 30 \
  -b:v 11M -minrate 11M -maxrate 11M -bufsize 11M -x264-params nal-hrd=cbr:force-cfr=1 -preset slow -g 30 \
  -color_primaries bt709 -color_trc bt709 -colorspace bt709 \
  -c:a aac -b:a 256k -ar 48000 -ac 2 \
  -movflags +faststart \
  "$dest/app-preview-iphone-886x1920.mp4"

ffprobe -v error -show_entries stream=codec_name,profile,level,width,height,r_frame_rate,bit_rate,sample_rate,channels:format=duration,size \
  -of compact "$dest/app-preview-iphone-886x1920.mp4"
