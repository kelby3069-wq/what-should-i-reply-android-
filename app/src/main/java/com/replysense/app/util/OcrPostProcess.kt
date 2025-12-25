package com.replysense.app.util

import java.util.Locale

object OcrPostProcess {

    fun process(input: String, clean: Boolean, mergeLines: Boolean): String {
        var lines = normalize(input)

        if (clean) lines = cleanLines(lines)
        if (mergeLines) lines = mergeBrokenLines(lines)

        return lines.joinToString("\n").trim()
    }

    private fun normalize(input: String): List<String> {
        return input
            .replace("\r\n", "\n")
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun cleanLines(lines: List<String>): List<String> {
        val blacklistExact = setOf(
            "active now",
            "message",
            "english (us)",
            "search",
            "home",
            "back"
        )

        fun isKeyboardRow(s: String): Boolean {
            val tokens = s.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (tokens.size < 6) return false
            val singleLetter = tokens.count { it.length == 1 && it[0].isLetter() }
            return singleLetter >= 6
        }

        fun isLoneDigitLine(s: String): Boolean = s.length <= 2 && s.all { it.isDigit() }

        val timeRegex = Regex("""^\d{1,2}:\d{2}\s?(AM|PM)?$""", RegexOption.IGNORE_CASE)

        fun isSymbolNoise(s: String): Boolean =
            s.length <= 2 && s.any { !it.isLetterOrDigit() } && s.all { !it.isLetterOrDigit() || it == '+' }

        return lines.filter { raw ->
            val s = raw.trim()
            val lower = s.lowercase(Locale.US)

            if (blacklistExact.contains(lower)) return@filter false
            if (isKeyboardRow(s)) return@filter false
            if (isLoneDigitLine(s)) return@filter false
            if (timeRegex.matches(s)) return@filter false
            if (isSymbolNoise(s)) return@filter false
            if (lower == "||" || lower == "|" || lower == "ll") return@filter false

            true
        }
    }

    /**
     * Merge OCR-wrapped lines into more natural message blocks.
     * Heuristics:
     * - If next starts lowercase, or current lacks ending punctuation, merge.
     * - If current line is short and next is a continuation, merge.
     */
    private fun mergeBrokenLines(lines: List<String>): List<String> {
        if (lines.isEmpty()) return lines

        val out = mutableListOf<String>()
        var buf = lines.first()

        fun endsHard(s: String): Boolean {
            val t = s.trim()
            if (t.isEmpty()) return true
            val last = t.last()
            return last in listOf('.', '!', '?', ':')
        }

        fun startsLower(s: String): Boolean {
            val t = s.trim()
            if (t.isEmpty()) return false
            val c = t.first()
            return c.isLowerCase()
        }

        fun looksLikeHeaderOrName(s: String): Boolean {
            // crude: single/short name-ish lines often separate messages (not perfect)
            val t = s.trim()
            if (t.length in 2..24 && t.count { it == ' ' } <= 2) {
                // Avoid merging if it's “Ashlynn Raya” etc.
                val words = t.split(" ")
                if (words.all { it.isNotBlank() && it[0].isUpperCase() }) return true
            }
            return false
        }

        for (i in 1 until lines.size) {
            val next = lines[i].trim()

            val merge =
                !looksLikeHeaderOrName(next) &&
                    (
                        startsLower(next) ||
                            (!endsHard(buf) && buf.length >= 12) ||
                            (buf.length < 22 && next.length >= 10)
                        )

            if (merge) {
                buf = "$buf $next".replace(Regex("\\s+"), " ").trim()
            } else {
                out += buf
                buf = next
            }
        }

        out += buf
        return out
    }
}
