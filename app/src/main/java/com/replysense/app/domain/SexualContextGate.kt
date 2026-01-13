package com.replysense.app.domain

object SexualContextGate {

    private val flirtTerms = listOf(
        "cute", "hot", "attractive", "handsome", "pretty",
        "flirting", "flirty", "kiss", "kissing", "date"
    )

    private val explicitTerms = listOf(
        "sex", "nude", "naked", "hook up", "hookup",
        "bed", "sleep with", "oral", "touch me"
    )

    fun evaluate(
        messages: List<String>,
        signals: AnalysisSignals
    ): SexualContextResult {

        val flirtHits = messages.count { m ->
            flirtTerms.any { t -> m.contains(t, ignoreCase = true) }
        }

        val explicitHits = messages.count { m ->
            explicitTerms.any { t -> m.contains(t, ignoreCase = true) }
        }

        val level = when {
            explicitHits > 0 -> SexualContextLevel.EXPLICIT
            flirtHits > 0 -> SexualContextLevel.FLIRTATIOUS
            else -> SexualContextLevel.NONE
        }

        // Hard safety rails
        if (signals.riskLevel == RiskLevel.HIGH) {
            return SexualContextResult(
                level = level,
                decision = SexualGateDecision.BLOCK,
                reason = "High emotional risk detected. Sexual escalation is not appropriate."
            )
        }

        if (signals.momentum == Momentum.DECLINING && level != SexualContextLevel.NONE) {
            return SexualContextResult(
                level = level,
                decision = SexualGateDecision.RESTRICT,
                reason = "Conversation momentum is declining. Escalation could increase pressure."
            )
        }

        if (level == SexualContextLevel.EXPLICIT && signals.emotionalLoad != EmotionalLoad.LOW) {
            return SexualContextResult(
                level = level,
                decision = SexualGateDecision.BLOCK,
                reason = "Explicit sexual content combined with emotional load requires restraint."
            )
        }

        return SexualContextResult(
            level = level,
            decision = SexualGateDecision.ALLOW,
            reason = "No safety conflicts detected."
        )
    }
}
