package com.replysense.app.model

data class SpeakerMetrics(
    val averageResponseSeconds: Long,
    val longestSilenceSeconds: Long,
    val messageCount: Int,
    val initiations: Int
)
