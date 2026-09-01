package com.merkost.metronome.presets

import com.merkost.metronome.model.Beat
import com.merkost.metronome.model.Subdivision
import com.merkost.metronome.model.TimeSignature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PracticePresetCodecTest {
    private val preset = PracticePreset(
        id = "preset-1",
        name = "Warmup % line\nA\tB",
        createdAtEpochMillis = 100L,
        lastUsedAtEpochMillis = 200L,
        isFavourite = true,
        sortPosition = 2,
        bpm = 96,
        timeSignature = TimeSignature.FOUR_FOUR,
        subdivision = Subdivision.TRIPLET,
        beats = listOf(Beat.HIGH, Beat.LOW, Beat.MUTE, Beat.LOW),
        countInEnabled = true,
    )

    @Test
    fun roundTripsEveryField() {
        val encoded = PracticePresetCodec.encode(listOf(preset))

        assertEquals(listOf(preset), PracticePresetCodec.decode(encoded))
    }

    @Test
    fun ignoresUnknownTrailingFields() {
        val encoded = PracticePresetCodec.encode(listOf(preset)).trimEnd() + "\tignored\tfuture"

        assertEquals(listOf(preset), PracticePresetCodec.decode(encoded))
    }

    @Test
    fun skipsInvalidRecordsWithoutDroppingValidRecords() {
        val valid = PracticePresetCodec.encode(listOf(preset)).trimEnd()
        val invalidVersion = valid.replaceFirst("v1", "v9")
        val invalidBpm = valid.replace("\t96\t", "\t999\t")

        assertEquals(listOf(preset), PracticePresetCodec.decode("$invalidVersion\n$valid\n$invalidBpm"))
    }

    @Test
    fun draftValidationTrimsNamesAndRejectsInvalidBeatCounts() {
        val draft = PracticePresetDraft(
            name = "  Warmup  ",
            bpm = 96,
            timeSignature = TimeSignature.FOUR_FOUR,
            subdivision = Subdivision.QUARTER,
            beats = listOf(Beat.HIGH),
            countInEnabled = false,
        )

        assertEquals("Warmup", draft.normalizedName)
        assertEquals(PresetValidationError.INVALID_BEATS, draft.validationError)
    }

    @Test
    fun emptyInputDecodesToEmptyCollection() {
        assertTrue(PracticePresetCodec.decode(null).isEmpty())
        assertTrue(PracticePresetCodec.decode("").isEmpty())
    }
}
