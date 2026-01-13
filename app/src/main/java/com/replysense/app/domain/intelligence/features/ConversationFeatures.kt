package com.replysense.app.domain.intelligence.features

data class ConversationFeatures(
    val emotionalVolatility: Float,
    val reassuranceFailureCount: Int,
    val boundaryPressureScore: Float,
    val ruptureDetected: Boolean
)
