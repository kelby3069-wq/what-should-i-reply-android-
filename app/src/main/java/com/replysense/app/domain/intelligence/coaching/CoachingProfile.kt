package com.replysense.app.domain.intelligence.coaching

/**
 * Resulting coaching delivery profile.
 * INTERNAL ONLY — not persisted.
 */
data class CoachingProfile(
    val tone: CoachingTone,
    val style: CoachingStyle,
    val rationale: List<String>
)
