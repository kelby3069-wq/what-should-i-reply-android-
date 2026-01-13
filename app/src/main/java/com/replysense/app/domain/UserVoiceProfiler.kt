package com.replysense.app.domain

object UserVoiceProfiler {

    fun profile(messages: List<String>): UserVoiceProfile {
        val avgLen = messages.map { it.length }.average().toInt().coerceAtLeast(12)
        val emoji = messages.any { it.any { ch -> ch.code > 0x1F300 } }
        val lowercase = messages.count { it == it.lowercase() } > messages.size / 2

        val directness = when {
            messages.any { it.contains("?", true) } -> Directness.SOFT
            messages.any { it.contains("!") } -> Directness.DIRECT
            else -> Directness.NEUTRAL
        }

        return UserVoiceProfile(
            avgLength = avgLen,
            usesEmoji = emoji,
            usesLowercase = lowercase,
            directness = directness
        )
    }
}
