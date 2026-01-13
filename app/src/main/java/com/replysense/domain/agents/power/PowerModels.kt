package com.replysense.app.model

data class ConversationDynamics(
    val me: SpeakerMetrics,
    val them: SpeakerMetrics,
    val dominanceRatio: Float, // me / total
    val momentumDropPoints: List<Int> // message indices
)
