package com.replysense.app.domain.emotion

object HistoryPatternAnalyzer {

    fun analyze(history: List<String>): Set<RedFlag> {
        val flags = mutableSetOf<RedFlag>()

        if (history.size < 3) return flags

        val shortReplies = history.count { it.length <= 3 }
        if (shortReplies >= history.size / 2) {
            flags += RedFlag.ONE_SIDED
        }

        val unanswered = history.takeLast(3).all { it.isBlank() }
        if (unanswered) {
            flags += RedFlag.EMOTIONAL_UNAVAILABILITY
        }

        return flags
    }
}
