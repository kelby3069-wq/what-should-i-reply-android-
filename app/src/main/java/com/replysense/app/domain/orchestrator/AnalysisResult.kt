package com.replysense.domain.orchestrator

import com.replysense.domain.evaluation.EvaluationScores
import com.replysense.domain.model.ContextState

/**
 * Final output of the ReplySense pipeline.
 *
 * Contains:
 * - fully populated ContextState (meaning)
 * - evaluation scores (confidence + risk)
 *
 * UI should NEVER recompute meaning —
 * only project and explain this result.
 */
data class AnalysisResult(
    val state: ContextState,
    val evaluation: EvaluationScores
)
