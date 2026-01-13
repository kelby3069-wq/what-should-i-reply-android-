package com.replysense.app.model

/* ---------------- Supporting Models ---------------- */

/**
 * Phase A / UI-safe analysis result.
 * No enums. No logic. Pure data only.
 * Enums will be reintroduced in Phase B.
 */
data class AnalysisResult(
    val overallRead: String,

    val emotionalState: EmotionalStateResult,

    val trajectory: String,

    val keyMoments: List<KeyMoment>,

    val redFlags: List<RedFlagResult>,

    val responsibilityCheck: ResponsibilityCheck,

    val recoverability: String,

    val coaching: CoachingResult?
)

data class EmotionalStateResult(
    val user: EmotionalRead,
    val other: EmotionalRead
)

data class EmotionalRead(
    val primary: String,
    val secondary: List<String>,
    val intensity: String
)

data class KeyMoment(
    val messageIndex: Int,
    val excerpt: String,
    val explanation: String
)

data class RedFlagResult(
    val title: String,
    val explanation: String,
    val severity: String,
    val evidenceMessageIndices: List<Int>
)

data class ResponsibilityCheck(
    val userDidWell: List<String>,
    val userDidNotCause: List<String>,
    val otherPartyActions: List<String>
)

data class CoachingResult(
    val summary: String,
    val suggestedReply: String?,
    val boundaryGuidance: String?
)