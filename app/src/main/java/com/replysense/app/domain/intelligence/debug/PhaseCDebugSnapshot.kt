package com.replysense.app.domain.intelligence.debug

import com.replysense.app.domain.intelligence.explainability.ExplainabilityTrace
import com.replysense.app.domain.intelligence.confidence.ConfidenceScore
import com.replysense.app.domain.intelligence.validation.ConflictSignal
import com.replysense.app.domain.intelligence.validation.AmbiguitySignal
import com.replysense.app.domain.intelligence.coaching.CoachingProfile

/**
 * Complete internal snapshot of a Phase C/D analysis run.
 * DEBUG / INTERNAL USE ONLY.
 */
data class PhaseCDebugSnapshot(
    val explainabilityTraces: List<ExplainabilityTrace>,
    val confidenceScores: List<ConfidenceScore>,
    val conflicts: List<ConflictSignal>,
    val ambiguities: List<AmbiguitySignal>,
    val coachingProfile: CoachingProfile
)
