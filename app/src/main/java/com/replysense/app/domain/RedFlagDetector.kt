package com.replysense.app.domain

object RedFlagDetector {

    fun detect(messages: List<String>): List<RedFlag> {
        if (messages.size < 3) return emptyList()

        val flags = mutableListOf<RedFlag>()

        // Ghosting / disengagement (very conservative)
        val lastThird = messages.takeLast(messages.size / 3)
        val avgLen = messages.map { it.length }.average()
        val shortReplies = lastThird.count { it.length < avgLen * 0.5 }

        if (shortReplies >= lastThird.size && lastThird.size >= 2) {
            flags += RedFlag(
                type = RedFlagType.GHOSTING,
                description = "Engagement appears to drop sharply near the end of the conversation.",
                evidence = "Recent replies became significantly shorter or non-responsive."
            )
        }

        // Hot–cold pattern (length volatility)
        val volatility =
            messages.zipWithNext { a, b -> kotlin.math.abs(a.length - b.length) }
        if (volatility.any { it > avgLen * 0.9 }) {
            flags += RedFlag(
                type = RedFlagType.HOT_COLD,
                description = "Inconsistent engagement intensity across messages.",
                evidence = "Message length fluctuates sharply between turns."
            )
        }

        // Pressure language (light heuristic)
        val pressureTerms = listOf(
            "why won't you", "you need to", "answer me",
            "please respond", "are you ignoring me"
        )
        if (messages.any { m -> pressureTerms.any { t -> m.contains(t, true) } }) {
            flags += RedFlag(
                type = RedFlagType.PRESSURE,
                description = "Language that may increase emotional pressure.",
                evidence = "Messages contain urgency or demand phrasing."
            )
        }

        return flags
    }
}
