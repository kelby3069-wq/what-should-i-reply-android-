package com.replysense.app.domain

object ReplySuggestionGenerator {

    fun generate(
        messages: List<String>,
        signals: AnalysisSignals,
        toneEligibility: ReplyToneEligibility,
        sexualContext: SexualContextResult,
        recoverability: RecoverabilityResult
    ): List<ReplySuggestion> {

        if (recoverability.decision == DecisionGuidance.PAUSE) return emptyList()

        val voice = UserVoiceProfiler.profile(messages)
        val suggestions = mutableListOf<ReplySuggestion>()

        toneEligibility.allowedTones.forEach { tone ->
            if (sexualContext.decision != SexualGateDecision.ALLOW && tone == ReplyTone.DIRECT) return@forEach

            val text = buildReply(tone, voice, recoverability.decision)
            suggestions += ReplySuggestion(
                tone = tone,
                text = text,
                rationale = "Suggested based on allowed tone, safety gates, and conversation momentum."
            )
        }

        return suggestions
    }

    private fun buildReply(
        tone: ReplyTone,
        voice: UserVoiceProfile,
        decision: DecisionGuidance
    ): String {
        val base = when (tone) {
            ReplyTone.CALM -> "that makes sense — no rush on this."
            ReplyTone.SUPPORTIVE -> "i get where you’re coming from. take your time."
            ReplyTone.DIRECT -> "let me know if you want to continue — totally fine either way."
        }

        val adjusted = when (decision) {
            DecisionGuidance.RESET_TONE -> base.replace("—", ".")
            else -> base
        }

        val styled = if (voice.usesLowercase) adjusted.lowercase() else adjusted.replaceFirstChar { it.uppercase() }
        return styled.take(voice.avgLength)
    }
}
