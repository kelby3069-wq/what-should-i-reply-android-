package com.replysense.app.domain

enum class ReplyTone {
    CALM,
    SUPPORTIVE,
    DIRECT
}

data class ReplyToneEligibility(
    val allowedTones: Set<ReplyTone>,
    val recommendedTone: ReplyTone
)

object ReplyToneEvaluator {

    fun evaluate(signals: AnalysisSignals): ReplyToneEligibility {
        return when {
            signals.riskLevel == RiskLevel.HIGH -> {
                ReplyToneEligibility(
                    allowedTones = setOf(ReplyTone.CALM),
                    recommendedTone = ReplyTone.CALM
                )
            }

            signals.emotionalLoad == EmotionalLoad.HIGH -> {
                ReplyToneEligibility(
                    allowedTones = setOf(
                        ReplyTone.CALM,
                        ReplyTone.SUPPORTIVE
                    ),
                    recommendedTone = ReplyTone.SUPPORTIVE
                )
            }

            signals.momentum == Momentum.DECLINING -> {
                ReplyToneEligibility(
                    allowedTones = setOf(
                        ReplyTone.CALM,
                        ReplyTone.DIRECT
                    ),
                    recommendedTone = ReplyTone.CALM
                )
            }

            else -> {
                ReplyToneEligibility(
                    allowedTones = setOf(
                        ReplyTone.CALM,
                        ReplyTone.SUPPORTIVE,
                        ReplyTone.DIRECT
                    ),
                    recommendedTone = ReplyTone.CALM
                )
            }
        }
    }
}
