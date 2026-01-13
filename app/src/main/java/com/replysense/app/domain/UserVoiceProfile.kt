package com.replysense.app.domain

data class UserVoiceProfile(
    val avgLength: Int,
    val usesEmoji: Boolean,
    val usesLowercase: Boolean,
    val directness: Directness
)

enum class Directness {
    SOFT,
    NEUTRAL,
    DIRECT
}
