package com.merkost.metronome.practiceSets

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticeSetCodecTest {
    private val practiceSet = PracticeSet(
        id = "set-1",
        name = "Daily % foundations\nA\tB",
        createdAtEpochMillis = 10L,
        updatedAtEpochMillis = 20L,
        lastStartedAtEpochMillis = 30L,
        lastCompletedAtEpochMillis = null,
        sortPosition = 2,
        steps = listOf(
            PracticeSetStep("step-1", "preset-a", PracticeStepTarget.None),
            PracticeSetStep("step-2", "preset-b", PracticeStepTarget.Duration(12)),
            PracticeSetStep("step-3", "preset-c", PracticeStepTarget.Bars(24)),
        ),
    )

    @Test
    fun roundTripsSetStepsAndEveryTargetType() {
        assertEquals(
            listOf(practiceSet),
            PracticeSetCodec.decode(PracticeSetCodec.encode(listOf(practiceSet))),
        )
    }

    @Test
    fun roundTripsCompletionTimestampAndReadsOlderRecordsAsIncomplete() {
        val completed = practiceSet.copy(lastCompletedAtEpochMillis = 40L)
        assertEquals(
            listOf(completed),
            PracticeSetCodec.decode(PracticeSetCodec.encode(listOf(completed))),
        )

        val oldRecord = PracticeSetCodec.encode(listOf(practiceSet)).substringBeforeLast('\t')

        assertNull(PracticeSetCodec.decode(oldRecord).single().lastCompletedAtEpochMillis)
    }

    @Test
    fun selectsTheMostRecentlyCompletedExistingSet() {
        val sets = listOf(
            practiceSet.copy(id = "older", lastCompletedAtEpochMillis = 10L),
            practiceSet.copy(id = "recent", lastCompletedAtEpochMillis = 20L),
            practiceSet.copy(id = "never", lastCompletedAtEpochMillis = null),
        )

        assertEquals("recent", sets.mostRecentlyCompleted()!!.id)
    }

    @Test
    fun ignoresUnknownTrailingFields() {
        val encoded = PracticeSetCodec.encode(listOf(practiceSet)) + "\tignored\tfuture"

        assertEquals(listOf(practiceSet), PracticeSetCodec.decode(encoded))
    }

    @Test
    fun skipsInvalidRecordsWithoutDroppingValidRecords() {
        val valid = PracticeSetCodec.encode(listOf(practiceSet)).trimEnd()
        val invalidVersion = valid.replaceFirst("v1", "v9")
        val invalidStepTarget = valid.replace("duration%3A12", "duration%3A121")

        assertEquals(
            listOf(practiceSet),
            PracticeSetCodec.decode("$invalidVersion\n$valid\n$invalidStepTarget"),
        )
    }

    @Test
    fun rejectsBlankEmptyDuplicateAndOutOfRangeDrafts() {
        assertEquals(PracticeSetValidationError.EMPTY_NAME, draft(name = " ").validationError)
        assertEquals(PracticeSetValidationError.EMPTY_STEPS, draft(steps = emptyList()).validationError)
        assertEquals(
            PracticeSetValidationError.DUPLICATE_STEP_ID,
            draft(
                steps = listOf(
                    PracticeSetStep("same", "preset-a", PracticeStepTarget.None),
                    PracticeSetStep("same", "preset-b", PracticeStepTarget.None),
                ),
            ).validationError,
        )
        assertEquals(
            PracticeSetValidationError.INVALID_TARGET,
            draft(target = PracticeStepTarget.Duration(121)).validationError,
        )
        assertEquals(
            PracticeSetValidationError.INVALID_TARGET,
            draft(target = PracticeStepTarget.Bars(1000)).validationError,
        )
    }

    @Test
    fun normalizesNamesAndCopiesSteps() {
        val steps = mutableListOf(PracticeSetStep("step-1", "preset-a", PracticeStepTarget.None))
        val normalized = draft(name = "  Daily  ", steps = steps).normalized()
        steps.clear()

        assertEquals("Daily", normalized.name)
        assertEquals(1, normalized.steps.size)
        assertNull(normalized.validationError)
    }

    @Test
    fun emptyInputDecodesToEmptyCollection() {
        assertTrue(PracticeSetCodec.decode(null).isEmpty())
        assertTrue(PracticeSetCodec.decode("").isEmpty())
    }

    private fun draft(
        name: String = "Daily",
        target: PracticeStepTarget = PracticeStepTarget.None,
        steps: List<PracticeSetStep> = listOf(PracticeSetStep("step-1", "preset-a", target)),
    ) = PracticeSetDraft(name = name, steps = steps)
}
