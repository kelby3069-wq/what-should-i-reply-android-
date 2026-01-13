package com.replysense.app.domain.intelligence.openai

import com.replysense.app.domain.intelligence.coaching.CoachingProfile

/**
 * Read-only context passed to the enhancer.
 * NEVER contains authoritative conclusions.
 */
data class EnhancerContext(
    val coachingProfile: CoachingProfile,
    val userGoalHint: String? = null
)
