package com.replysense.domain.evaluation

import com.replysense.domain.model.ContextState

/**
 * BasicEvaluationAgent
 *
 * Temporary deterministic evaluator.
 * Provides conservative defaults until
 * real scoring logic is added.
 */
class BasicEvaluationAgent : EvaluationAgent {

    override fun evaluate(state: ContextState): EvaluationScores {
        return EvaluationScores(
            emotionalAccuracy = 0.5f,
            intentConfidence = state.detectedIntent?.confidence ?: 0.4f,
            escalationRisk = 0.3f,
            alignmentWithUserGoal = 0.5f,
            ambiguity = if (state.detectedIntent == null) 0.8f else 0.4f
        )
    }
}
