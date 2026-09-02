package com.merkost.metronome.presets

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature

object PracticePresetCodec {
    private const val VERSION = "v1"
    private const val FIELD_COUNT = 12

    fun encode(presets: List<PracticePreset>): String = presets.joinToString("\n") { preset ->
        listOf(
            VERSION,
            escape(preset.id),
            escape(preset.name),
            preset.createdAtEpochMillis.toString(),
            preset.lastUsedAtEpochMillis?.toString().orEmpty(),
            if (preset.isFavourite) "1" else "0",
            preset.sortPosition.toString(),
            preset.bpm.toString(),
            preset.timeSignature.name,
            preset.subdivision.name,
            preset.beats.joinToString(",") { it.name },
            if (preset.countInEnabled) "1" else "0",
        ).joinToString("\t")
    }

    fun decode(raw: String?): List<PracticePreset> = raw
        .orEmpty()
        .lineSequence()
        .mapNotNull(::decodeRecord)
        .toList()

    private fun decodeRecord(record: String): PracticePreset? {
        val fields = record.split('\t')
        if (fields.size < FIELD_COUNT || fields[0] != VERSION) return null
        return runCatching {
            val id = unescape(fields[1])
            val name = unescape(fields[2])
            val timeSignature = TimeSignature.valueOf(fields[8])
            val beats = fields[10].split(',').map(Beat::valueOf)
            val draft = PracticePresetDraft(
                name = name,
                bpm = fields[7].toInt(),
                timeSignature = timeSignature,
                subdivision = Subdivision.valueOf(fields[9]),
                beats = beats,
                countInEnabled = fields[11] == "1",
            )
            if (id.isBlank() || draft.validationError != null) return null
            PracticePreset(
                id = id,
                name = draft.normalizedName,
                createdAtEpochMillis = fields[3].toLong().coerceAtLeast(0L),
                lastUsedAtEpochMillis = fields[4].takeIf(String::isNotEmpty)?.toLong()?.coerceAtLeast(0L),
                isFavourite = fields[5] == "1",
                sortPosition = fields[6].toInt().coerceAtLeast(0),
                bpm = draft.bpm,
                timeSignature = draft.timeSignature,
                subdivision = draft.subdivision,
                beats = draft.beats,
                countInEnabled = draft.countInEnabled,
            )
        }.getOrNull()
    }

    private fun escape(value: String): String = value
        .replace("%", "%25")
        .replace("\t", "%09")
        .replace("\r", "%0D")
        .replace("\n", "%0A")

    private fun unescape(value: String): String = value
        .replace("%0A", "\n")
        .replace("%0D", "\r")
        .replace("%09", "\t")
        .replace("%25", "%")
}
