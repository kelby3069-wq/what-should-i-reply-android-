package com.replysense.app.domain.emotion

data class UserVoiceProfile(
    val formality: Float,            // 0.0 casual → 1.0 formal
    val emojiFrequency: Float,        // 0.0 none → 1.0 frequent
    val slangFrequency: Float,        // 0.0 none → 1.0 heavy
    val avgSentenceLength: Float,     // word count
    val usesLowercase: Boolean,
    val usesSoftening: Boolean,       // "maybe", "I think", etc
    val usesDirectStatements: Boolean // short, declarative
)
