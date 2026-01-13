package com.replysense.app.domain.intelligence.inference

/**
 * Calibrated thresholds for SMS-style conversations.
 * Values tuned to reduce false positives on short chats.
 */
object InferenceThresholds {

    // Emotional volatility (SMS-calibrated)
    const val HIGH_EXCLAMATION_DENSITY = 0.025f
    const val HIGH_CAPS_RATE = 0.18f
    const val HIGH_PUNCTUATION_BURSTS = 2

    // Escalation dynamics
    const val ESCALATION_POSITIVE = 0.6f
    const val ESCALATION_SEVERE = 1.4f

    // Rupture (composite score)
    const val RUPTURE_SCORE_WARNING = 2.0f
    const val RUPTURE_SCORE_SEVERE = 3.5f

    // Dominance
    const val MONOLOGUE_MULTIPLIER = 3
}
