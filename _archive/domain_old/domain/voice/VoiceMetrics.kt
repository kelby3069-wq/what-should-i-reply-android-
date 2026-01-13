package com.replysense.app.domain.voice

data class VoiceMetrics(
    val averageWordCount: Int,
    val emojiRate: Float,
    val lowercaseRate: Float,
    val hedgeRate: Float
)
