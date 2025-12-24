package com.replysense.app.util

import com.replysense.app.net.ConversationTurn

/**
 * Smart extract for pasted conversation blocks.
 *
 * Supports:
 * - "Me: text" / "Them: text"
 * - "You: text" / "Her: text" / "Him: text"
 * - "Name: text" (unknown names default to "them")
 * - Multi-line messages: lines without a new speaker tag append to previous message
 *
 * Heuristics are intentionally simple and robust (MVP > perfect).
 */
object ConversationParser {

    private val speakerRegex = Regex("""^\s*([A-Za-z][A-Za-z0-9 _-]{0,24})\s*:\s*(.+?)\s*$""")

    fun parse(raw: String): List<ConversationTurn> {
        val lines = raw
            .replace("\r\n", "\n")
            .split("\n")
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return emptyList()

        val out = mutableListOf<ConversationTurn>()
        var currentFrom: String? = null
        var currentText = StringBuilder()

        fun flush() {
            val from = currentFrom
            val text = currentText.toString().trim()
            if (!from.isNullOrBlank() && text.isNotBlank()) {
                out += ConversationTurn(from = from, text = text)
            }
            currentFrom = null
            currentText = StringBuilder()
        }

        for (line in lines) {
            val m = speakerRegex.find(line)
            if (m != null) {
                val speaker = m.groupValues[1].trim()
                val msg = m.groupValues[2].trim()
                val mapped = mapSpeakerToFrom(speaker)

                // new message starts
                flush()
                currentFrom = mapped
                currentText.append(msg)
            } else {
                // continuation line: append to previous message if exists,
                // else treat as "them" (fallback)
                if (currentFrom == null) {
                    currentFrom = "them"
                    currentText.append(line.trim())
                } else {
                    currentText.append("\n").append(line.trim())
                }
            }
        }
        flush()

        // If parser found nothing structured, fallback to single "them" message
        return if (out.isEmpty()) listOf(ConversationTurn("them", raw.trim())) else out
    }

    private fun mapSpeakerToFrom(speaker: String): String {
        val s = speaker.lowercase()
        return when {
            s in setOf("me", "i", "my", "mine") -> "me"
            s in setOf("you", "them", "they", "other") -> "them"
            s in setOf("her", "him") -> "them"
            // If user literally writes "myself:" or "user:"
            s in setOf("user") -> "me"
            // Unknown names: assume "them" (safer than mislabeling as "me")
            else -> "them"
        }
    }
}
