package com.merkost.metronome.practiceSets

object PracticeSetCodec {
    private const val VERSION = "v1"
    private const val FIELD_COUNT = 8

    fun encode(sets: List<PracticeSet>): String = sets.joinToString("\n") { practiceSet ->
        listOf(
            VERSION,
            escape(practiceSet.id),
            escape(practiceSet.name),
            practiceSet.createdAtEpochMillis.toString(),
            practiceSet.updatedAtEpochMillis.toString(),
            practiceSet.lastStartedAtEpochMillis?.toString().orEmpty(),
            practiceSet.sortPosition.toString(),
            practiceSet.steps.joinToString(",") { step ->
                listOf(
                    escape(step.id),
                    escape(step.presetId),
                    escape(step.target.token),
                ).joinToString(":")
            },
            practiceSet.lastCompletedAtEpochMillis?.toString().orEmpty(),
        ).joinToString("\t")
    }

    fun decode(raw: String?): List<PracticeSet> = raw
        .orEmpty()
        .lineSequence()
        .mapNotNull(::decodeRecord)
        .toList()

    private fun decodeRecord(record: String): PracticeSet? {
        val fields = record.split('\t')
        if (fields.size < FIELD_COUNT || fields[0] != VERSION) return null
        return runCatching {
            val id = unescape(fields[1])
            val steps = decodeSteps(fields[7]) ?: return null
            val draft = PracticeSetDraft(
                name = unescape(fields[2]),
                steps = steps,
            ).normalized()
            if (id.isBlank() || draft.validationError != null) return null
            PracticeSet(
                id = id,
                name = draft.name,
                createdAtEpochMillis = fields[3].toLong().coerceAtLeast(0L),
                updatedAtEpochMillis = fields[4].toLong().coerceAtLeast(0L),
                lastStartedAtEpochMillis = fields[5].takeIf(String::isNotEmpty)?.toLong()?.coerceAtLeast(0L),
                lastCompletedAtEpochMillis = fields.getOrNull(8)
                    ?.takeIf(String::isNotEmpty)
                    ?.toLongOrNull()
                    ?.coerceAtLeast(0L),
                sortPosition = fields[6].toInt().coerceAtLeast(0),
                steps = draft.steps,
            )
        }.getOrNull()
    }

    private fun decodeSteps(raw: String): List<PracticeSetStep>? = raw
        .split(',')
        .map { encodedStep ->
            val fields = encodedStep.split(':')
            if (fields.size != 3) return null
            PracticeSetStep(
                id = unescape(fields[0]),
                presetId = unescape(fields[1]),
                target = decodeTarget(unescape(fields[2])) ?: return null,
            )
        }

    private val PracticeStepTarget.token: String
        get() = when (this) {
            PracticeStepTarget.None -> "none"
            is PracticeStepTarget.Duration -> "duration:$minutes"
            is PracticeStepTarget.Bars -> "bars:$count"
        }

    private fun decodeTarget(raw: String): PracticeStepTarget? = when {
        raw == "none" -> PracticeStepTarget.None
        raw.startsWith("duration:") -> raw.substringAfter(':').toIntOrNull()?.let(PracticeStepTarget::Duration)
        raw.startsWith("bars:") -> raw.substringAfter(':').toIntOrNull()?.let(PracticeStepTarget::Bars)
        else -> null
    }?.takeIf(PracticeStepTarget::isValid)

    private fun escape(value: String): String = value
        .replace("%", "%25")
        .replace("\t", "%09")
        .replace("\r", "%0D")
        .replace("\n", "%0A")
        .replace(":", "%3A")
        .replace(",", "%2C")

    private fun unescape(value: String): String = value
        .replace("%2C", ",")
        .replace("%3A", ":")
        .replace("%0A", "\n")
        .replace("%0D", "\r")
        .replace("%09", "\t")
        .replace("%25", "%")
}
