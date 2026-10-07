import argparse
import hashlib
import json
import math
import re
import shutil
import struct
import subprocess
import sys
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "shared/src/commonMain/kotlin/com/merkost/metronome/model/ClickSoundSpectrum.kt"
ENUM_SOURCE = ROOT / "shared/src/commonMain/kotlin/com/merkost/metronome/model/ClickSound.kt"
RESOURCE_FOLDERS = (
    "shared/src/androidMain/res/raw",
    "iosApp/iosApp/Resources",
    "shared/src/wasmJsMain/resources/sounds",
)
RESOURCES = {
    "WOOD": "wood.mp3",
    "CLICK": "click.mp3",
    "CLASSIC": "metronome.wav",
    "SOFT": "soft.wav",
    "RIM": "rim.wav",
    "CLAVE": "clave.wav",
    "STUDIO": "studio.wav",
}
BAR_COUNT = 32
MIN_FREQUENCY_HZ = 80
MAX_FREQUENCY_HZ = 12000
MIN_FFT_SIZE = 8192
BAND_EDGES = [
    MIN_FREQUENCY_HZ * (MAX_FREQUENCY_HZ / MIN_FREQUENCY_HZ) ** (index / BAR_COUNT)
    for index in range(BAR_COUNT + 1)
]
BAND_EDGES[0] = MIN_FREQUENCY_HZ
BAND_EDGES[-1] = MAX_FREQUENCY_HZ


def decode(path):
    if path.suffix == ".wav":
        with wave.open(str(path), "rb") as source:
            channels = source.getnchannels()
            sample_rate = source.getframerate()
            width = source.getsampwidth()
            if width not in (2, 3, 4) or source.getcomptype() != "NONE":
                raise ValueError(f"Expected uncompressed 16-, 24- or 32-bit PCM: {path}")
            data = source.readframes(source.getnframes())
        scale = 1 << (width * 8 - 1)
        samples = tuple(
            int.from_bytes(data[index:index + width], "little", signed=True) / scale
            for index in range(0, len(data), width)
        )
    else:
        if not shutil.which("ffmpeg") or not shutil.which("ffprobe"):
            raise ValueError("Legacy MP3 extraction requires installed ffmpeg and ffprobe")
        metadata = json.loads(subprocess.check_output([
            "ffprobe", "-v", "error", "-select_streams", "a:0", "-show_entries",
            "stream=sample_rate,channels", "-of", "json", str(path),
        ]))["streams"]
        if len(metadata) != 1:
            raise ValueError(f"Expected one audio stream: {path}")
        sample_rate = int(metadata[0]["sample_rate"])
        channels = int(metadata[0]["channels"])
        data = subprocess.check_output([
            "ffmpeg", "-v", "error", "-nostdin", "-threads", "1", "-i", str(path),
            "-map", "0:a:0", "-f", "f32le", "-acodec", "pcm_f32le", "-threads", "1", "pipe:1",
        ])
        samples = struct.unpack(f"<{len(data) // 4}f", data)
    if not samples or len(samples) % channels or not all(math.isfinite(value) for value in samples):
        raise ValueError(f"Invalid PCM samples: {path}")
    return [samples[channel::channels] for channel in range(channels)], sample_rate, max(abs(value) for value in samples)



def fft(samples, size):
    values = [complex(value, 0) for value in samples] + [0j] * (size - len(samples))
    reversed_index = 0
    for index in range(1, size):
        bit = size >> 1
        while reversed_index & bit:
            reversed_index ^= bit
            bit >>= 1
        reversed_index ^= bit
        if index < reversed_index:
            values[index], values[reversed_index] = values[reversed_index], values[index]
    width = 2
    while width <= size:
        half = width // 2
        angle = -2 * math.pi / width
        step = complex(math.cos(angle), math.sin(angle))
        for start in range(0, size, width):
            rotation = 1 + 0j
            for index in range(start, start + half):
                even = values[index]
                odd = rotation * values[index + half]
                values[index] = even + odd
                values[index + half] = even - odd
                rotation *= step
        width <<= 1
    return values


def spectrum(channels, sample_rate):
    if not channels:
        raise ValueError("Expected at least one channel")
    frames = len(channels[0])
    if not frames or any(len(channel) != frames for channel in channels):
        raise ValueError("Expected nonempty channels of equal length")
    size = 1 << (max(frames, MIN_FFT_SIZE) - 1).bit_length()
    power = [0.0] * (size // 2 + 1)
    for channel in channels:
        transformed = fft(channel, size)
        for index in range(1, len(power)):
            value = transformed[index]
            power[index] += value.real ** 2 + value.imag ** 2
    scale = len(channels) * size ** 2
    amplitudes = []
    band_bins = []
    for index in range(BAR_COUNT):
        start = max(1, math.ceil(BAND_EDGES[index] * size / sample_rate))
        end = math.ceil(BAND_EDGES[index + 1] * size / sample_rate)
        if index == BAR_COUNT - 1:
            end = math.floor(MAX_FREQUENCY_HZ * size / sample_rate) + 1
        end = min(end, len(power))
        if end <= start:
            raise ValueError(f"No FFT bins in band {index}: increase MIN_FFT_SIZE")
        amplitudes.append(math.sqrt(sum(power[start:end]) / ((end - start) * scale)))
        band_bins.append(end - start)
    peak = max(amplitudes)
    if peak <= 0:
        raise ValueError("No energy within the shared tone bands")
    return [value / peak for value in amplitudes], size, band_bins


def analytic_validation():
    impulse, _, _ = spectrum([[1.0]], 48000)
    if any(abs(value - 1.0) > 1e-10 for value in impulse):
        raise ValueError("Impulse should have equal normalized power in every band")
    peaks = []
    for frequency in (375, 6000):
        samples = [math.sin(2 * math.pi * frequency * index / 48000) for index in range(MIN_FFT_SIZE)]
        bars, _, _ = spectrum([samples], 48000)
        expected = next(index for index in range(BAR_COUNT) if BAND_EDGES[index] <= frequency < BAND_EDGES[index + 1])
        actual = max(range(BAR_COUNT), key=lambda index: bars[index])
        if actual != expected or max(bars) != 1.0:
            raise ValueError(f"Tone {frequency} Hz should peak in normalized band {expected}, got {actual}")
        peaks.append(actual)
        stereo, _, _ = spectrum([samples, [-value for value in samples]], 48000)
        if any(abs(left - right) > 1e-10 for left, right in zip(bars, stereo)):
            raise ValueError("Opposite stereo channels must preserve tone power")
    if peaks[0] >= peaks[1]:
        raise ValueError("Tone bands must run from low to high frequency")
    for channels in ([[0.0]], [[1.0] * MIN_FFT_SIZE]):
        try:
            spectrum(channels, 48000)
        except ValueError:
            pass
        else:
            raise ValueError("Silence and FFT-sized constant signals must be rejected")
    return {
        "impulseFlatResponse": True,
        "tone375HzPeakBand": peaks[0],
        "tone6000HzPeakBand": peaks[1],
        "normalizedPeaks": True,
        "oppositeStereoChannelsPreservePower": True,
        "silenceRejected": True,
        "fftSizedConstantSignalRejected": True,
    }


def extract(sound, resource):
    sources = [ROOT / folder / resource for folder in RESOURCE_FOLDERS]
    data = sources[0].read_bytes()
    if any(path.read_bytes() != data for path in sources[1:]):
        raise ValueError(f"Normal sound differs across platforms: {resource}")
    channels, sample_rate, sample_peak = decode(sources[0])
    bars, fft_size, band_bins = spectrum(channels, sample_rate)
    return {
        "sound": sound,
        "resourceName": resource,
        "sourceSha256": hashlib.sha256(data).hexdigest(),
        "durationMs": round(len(channels[0]) / sample_rate * 1000, 3),
        "sampleRateHz": sample_rate,
        "channels": len(channels),
        "samplePeakDbfs": round(20 * math.log10(sample_peak), 3),
        "fftSize": fft_size,
        "binsPerBand": band_bins,
        "bars": [round(value, 4) for value in bars],
        "identicalOnAllPlatforms": True,
    }


def render(records):
    lines = [
        "package com.merkost.metronome.model",
        "",
        "internal data class ClickSoundSpectrum(",
        "    val bars: List<Float>,",
        "    val resourceName: String,",
        "    val sourceSha256: String,",
        ") {",
        "    companion object {",
        f"        const val barCount = {BAR_COUNT}",
        f"        const val minFrequencyHz = {MIN_FREQUENCY_HZ}",
        f"        const val maxFrequencyHz = {MAX_FREQUENCY_HZ}",
        "    }",
        "}",
        "",
        "internal val ClickSound.spectrum: ClickSoundSpectrum",
        "    get() = when (this) {",
    ]
    for record in records:
        lines.append(f"        ClickSound.{record['sound']} -> {record['sound'].lower()}Spectrum")
    lines.extend(["    }", ""])
    for record in records:
        lines.extend([
            f"private val {record['sound'].lower()}Spectrum = ClickSoundSpectrum(",
            "    bars = listOf(",
        ])
        for start in range(0, BAR_COUNT, 8):
            formatted = ", ".join(f"{value:.4f}f" for value in record["bars"][start:start + 8])
            lines.append(f"        {formatted},")
        lines.extend([
            "    ),",
            f"    resourceName = \"{record['resourceName']}\",",
            f"    sourceSha256 = \"{record['sourceSha256']}\",",
            ")",
            "",
        ])
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Generate normalized tone spectra from bundled normal click audio")
    parser.add_argument("--check", action="store_true", help="Fail if generated Kotlin data is missing or stale; do not write")
    parser.add_argument("--output", type=Path, default=OUTPUT)
    args = parser.parse_args()
    if sys.version_info < (3, 12):
        raise ValueError("Python 3.12+ is required for the bundled Classic WAVE_FORMAT_EXTENSIBLE PCM")
    enum_names = re.findall(r"^    ([A-Z]+)\(", ENUM_SOURCE.read_text(), flags=re.MULTILINE)
    if enum_names != list(RESOURCES):
        raise ValueError("Update RESOURCES to match every ClickSound entry in declaration order")
    validation = analytic_validation()
    records = [extract(sound, resource) for sound, resource in RESOURCES.items()]
    generated = render(records)
    if args.check:
        if not args.output.exists() or args.output.read_text() != generated:
            print(f"Stale click spectra: run python3 tools/generate-click-spectra.py --output {args.output}", file=sys.stderr)
            return 1
    else:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(generated)
    print(json.dumps({
        "status": "current" if args.check else "generated",
        "output": str(args.output),
        "barCount": BAR_COUNT,
        "frequencyRangeHz": [MIN_FREQUENCY_HZ, MAX_FREQUENCY_HZ],
        "bandEdgesHz": [round(value, 6) for value in BAND_EDGES],
        "method": "Full-clip rectangular FFT, zero-padded to next power of two >=8192; DC bin excluded; averaged channel power; mean power per logarithmic band; square-root then per-sound peak normalization",
        "amplitude": "Relative tone profile; not a loudness comparison",
        "rectangularWindowLimit": "A shorter constant clip acquires nonzero low-band energy when zero-padded because of its rectangular truncation; excluding FFT bin 0 does not subtract the source mean or remove this boundary energy",
        "normalResourcesOnly": True,
        "analyticValidation": validation,
        "resources": records,
    }, indent=2))
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, wave.Error, subprocess.CalledProcessError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
