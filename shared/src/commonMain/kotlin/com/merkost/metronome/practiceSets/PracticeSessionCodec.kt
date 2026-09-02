package com.merkost.metronome.practiceSets

import com.merkost.metronome.presets.PracticePresetCodec

object PracticeSessionCodec {
    private const val VERSION = "v1"
    private const val FIELD_COUNT = 14

    fun encode(session: ActivePracticeSession): String = listOf(
        VERSION,
        escape(session.id),
        escape(session.sourceSetId),
        escape(session.setName),
        session.steps.joinToString(",") { step ->
            listOf(
                escape(step.stepId),
                escape(step.target.token),
                escape(PracticePresetCodec.encode(listOf(step.preset))),
            ).joinToString(":")
        },
        session.currentStepIndex.toString(),
        session.pendingStepIndex?.toString().orEmpty(),
        session.elapsedMillis.toString(),
        session.completedBars.toString(),
        session.playbackIntent.name,
        if (session.targetReached) "1" else "0",
        if (session.currentStepEdited) "1" else "0",
        session.startedAtEpochMillis.toString(),
        session.lastCheckpointAtEpochMillis.toString(),
    ).joinToString("\t")

    fun decode(raw: String?): ActivePracticeSession? {
        val fields = raw.orEmpty().split('\t')
        if (fields.size < FIELD_COUNT || fields[0] != VERSION) return null
        return runCatching {
            val session = ActivePracticeSession(
                id = unescape(fields[1]),
                sourceSetId = unescape(fields[2]),
                setName = unescape(fields[3]),
                steps = decodeSteps(fields[4]) ?: return null,
                currentStepIndex = fields[5].toInt(),
                pendingStepIndex = null,
                elapsedMillis = fields[7].toLong(),
                completedBars = fields[8].toInt(),
                playbackIntent = PracticePlaybackIntent.Paused,
                targetReached = fields[10] == "1",
                currentStepEdited = fields[11] == "1",
                startedAtEpochMillis = fields[12].toLong(),
                lastCheckpointAtEpochMillis = fields[13].toLong(),
            )
            session.takeIf(ActivePracticeSession::isValid)
        }.getOrNull()
    }

    private fun decodeSteps(raw: String): List<ResolvedPracticeStep>? = raw
        .split(',')
        .map { encodedStep ->
            val fields = encodedStep.split(':')
            if (fields.size != 3) return null
            val presets = PracticePresetCodec.decode(unescape(fields[2]))
            ResolvedPracticeStep(
                stepId = unescape(fields[0]),
                target = decodeTarget(unescape(fields[1])) ?: return null,
                preset = presets.singleOrNull() ?: return null,
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
