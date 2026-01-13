package com.replysense.app.domain.intelligence.features

/**
 * INTERNAL ONLY.
 * Deterministic, derived signals from raw message lines.
 */
data class PhaseCFeatures(
    // Structure
    val messageCount: Int,
    val averageMessageLength: Int,
    val longestMessageLength: Int,
    val backAndForthRatio: Float,
    val monologueFlag: Boolean,

    // Emotional volatility
    val exclamationDensity: Float,
    val questionDensity: Float,
    val capitalizationRate: Float,
    val abruptLengthChangeIndex: Float,
    val emotionalPunctuationBursts: Int,

    // Rupture indicators
    val withdrawalLanguageHits: Int,
    val blameLanguageHits: Int,
    val reassuranceSeekingHits: Int,
    val escalationSlope: Float,
    val ruptureScore: Float,                // NEW

    // Power / responsibility
    val commandLanguageHits: Int,
    val apologyPresent: Boolean,
    val repairAttemptPresent: Boolean,
    val deflectionMarkers: Int,

    // Temporal polarity
    val lateStageNegativityBias: Float       // NEW
)
