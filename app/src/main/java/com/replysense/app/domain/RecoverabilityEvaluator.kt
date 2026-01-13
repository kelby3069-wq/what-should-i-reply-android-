package com.replysense.app.domain

object RecoverabilityEvaluator {

    fun evaluate(
        signals: AnalysisSignals,
        redFlags: List<RedFlag>
    ): RecoverabilityResult {

        // Hard stop: high risk
        if (signals.riskLevel == RiskLevel.HIGH) {
            return RecoverabilityResult(
                level = RecoverabilityLevel.LOW,
                decision = DecisionGuidance.PAUSE,
                rationale = "Emotional risk is elevated. Continuing now may increase harm or pressure."
            )
        }

        // Multiple red flags + declining momentum
        if (
            signals.momentum == Momentum.DECLINING &&
            redFlags.size >= 2
        ) {
            return RecoverabilityResult(
                level = RecoverabilityLevel.LOW,
                decision = DecisionGuidance.PAUSE,
                rationale = "Multiple warning signals and declining engagement suggest low short-term recoverability."
            )
        }

        // Declining but not critical
        if (signals.momentum == Momentum.DECLINING) {
            return RecoverabilityResult(
                level = RecoverabilityLevel.MEDIUM,
                decision = DecisionGuidance.RESET_TONE,
                rationale = "Momentum has softened, but a calmer or lighter tone may stabilize the interaction."
            )
        }

        // Stable or improving
        if (signals.momentum == Momentum.STABLE || signals.momentum == Momentum.IMPROVING) {
            return RecoverabilityResult(
                level = RecoverabilityLevel.HIGH,
                decision = DecisionGuidance.CONTINUE,
                rationale = "Engagement remains stable or improving with no critical risk indicators."
            )
        }

        // Fallback (should never hit)
        return RecoverabilityResult(
            level = RecoverabilityLevel.MEDIUM,
            decision = DecisionGuidance.RESET_TONE,
            rationale = "Mixed signals detected. Proceed with caution."
        )
    }
}
