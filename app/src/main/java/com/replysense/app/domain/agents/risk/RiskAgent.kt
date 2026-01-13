package com.replysense.domain.agents.risk

import com.replysense.domain.agents.MicroAgent
import com.replysense.domain.memory.AgentMemory
import com.replysense.domain.model.ContextState

/**
 * RiskAgent
 *
 * Estimates escalation and misinterpretation risk using
 * conservative, explainable heuristics.
 *
 * Signals:
 * - emotional intensity
 * - power asymmetry
 * - ambiguous intent
 *
 * Never overwrites an existing risk assessment.
 */
class RiskAgent : MicroAgent<ContextState, ContextState> {

    override fun run(
        input: ContextState,
        memory: AgentMemory
    ): ContextState {

        if (input.riskAssessment != null) return input

        val emotionIntensity = input.emotionalSignals?.emotionalIntensity ?: 0.3f

        val power = input.powerDynamics
        val asymmetry = if (power != null) {
            kotlin.math.abs(power.userLeverage - power.otherPartyLeverage)
        } else {
            0.2f
        }

        val intentConfidence = input.detectedIntent?.confidence ?: 0.4f
        val ambiguity = 1f - intentConfidence

        val escalation = (
                emotionIntensity * 0.5f +
                        asymmetry * 0.3f +
                        ambiguity * 0.2f
                ).coerceIn(0f, 1f)

        val misinterpretation = (
                ambiguity * 0.6f +
                        emotionIntensity * 0.2f +
                        (1f - intentConfidence) * 0.2f
                ).coerceIn(0f, 1f)

        val confidence = (
                1f - ambiguity
                ).coerceIn(0f, 1f)

        return input.copy(
            riskAssessment = RiskAssessment(
                escalationProbability = escalation,
                misinterpretationProbability = misinterpretation,
                confidence = confidence
            )
        )
    }
}
