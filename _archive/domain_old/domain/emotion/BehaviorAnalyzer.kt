package com.replysense.app.domain.emotion

object BehaviorAnalyzer {

    fun detectFlags(text: String): Set<RedFlag> {
        val lower = text.lowercase()
        val flags = mutableSetOf<RedFlag>()

        if (lower.contains("where are you") || lower.contains("send a pic")) {
            flags += RedFlag.CONTROL
        }

        if (lower.contains("why didn't you reply")) {
            flags += RedFlag.ONE_SIDED
        }

        if (lower.contains("nsfw") || lower.contains("pic")) {
            flags += RedFlag.BOUNDARY_VIOLATION
        }

        return flags
    }
}
