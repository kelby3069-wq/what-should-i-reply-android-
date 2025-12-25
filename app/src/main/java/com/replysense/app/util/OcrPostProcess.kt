package com.replysense.app.util

import java.util.Locale

object OcrPostProcess {

    data class Processed(
        val transcript: String,
        val json: String,
        val messageCount: Int
    )

    data class Msg(
        val idx: Int,
        val ts: String?,     // if we detect a timestamp
        val speaker: String?,// if we detect a name/header
        val text: String
    )

    fun processThreadAware(
        input: String,
        clean: Boolean,
        mergeLines: Boolean,
        threadOnly: Boolean
    ): Processed {
        var lines = normalize(input)
        if (clean) lines = cleanLines(lines)
        if (mergeLines) lines = mergeBrokenLines(lines)

        val msgs = if (threadOnly) extractThread(lines) else listOf(
            Msg(0, null, null, lines.joinToString("\n"))
        )

        val transcript = if (threadOnly) {
            msgs.joinToString("\n\n") { m ->
                buildString {
                    if (m.ts != null) append("[${m.ts}] ")
                    if (m.speaker != null) append("${m.speaker}: ")
                    append(m.text)
                }
            }
        } else {
            msgs.firstOrNull()?.text.orEmpty()
        }

        val json = msgsToJson(msgs)

        return Processed(transcript = transcript.trim(), json = json, messageCount = msgs.size)
    }

    // ---------- Normalize ----------
    private fun normalize(input: String): List<String> =
        input.replace("\r\n", "\n")
            .split('\n')
            .map { it.trim() }
            .filter { it.isNotBlank() }

    // ---------- Clean ----------
    private fun cleanLines(lines: List<String>): List<String> {
        val blacklistExact = setOf(
            "active now",
            "message",
            "english (us)",
            "search",
            "home",
            "back",
            "typing…",
            "typing..."
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

    // ---------- Merge broken lines ----------
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
            val t = s.trim()
            if (t.length in 2..26 && t.count { it == ' ' } <= 2) {
                val words = t.split(" ").filter { it.isNotBlank() }
                if (words.size in 1..3 && words.all { it.firstOrNull()?.isUpperCase() == true }) return true
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

    // ---------- Thread extraction ----------
    private fun extractThread(lines: List<String>): List<Msg> {
        // Detect lines that "look like timestamps", plus optional day separators
        val tsRegex = Regex("""\b\d{1,2}:\d{2}\s?(AM|PM)?\b""", RegexOption.IGNORE_CASE)
        val daySepRegex = Regex("""^(today|yesterday|mon|tue|wed|thu|fri|sat|sun)(day)?\b""", RegexOption.IGNORE_CASE)

        fun looksLikeNameHeader(s: String): Boolean {
            val t = s.trim()
            if (t.length !in 2..26) return false
            val words = t.split(" ").filter { it.isNotBlank() }
            if (words.isEmpty() || words.size > 3) return false
            // all words capitalized -> likely a contact name
            return words.all { it.firstOrNull()?.isUpperCase() == true }
        }

        // Heuristic: build message blocks. A new block starts at:
        // - a timestamp line OR
        // - a name header line OR
        // - a large gap marker we already cleaned out (rare)
        val msgs = mutableListOf<Msg>()
        var currentTs: String? = null
        var currentSpeaker: String? = null
        val buf = mutableListOf<String>()

        fun flush() {
            val text = buf.joinToString(" ").replace(Regex("\\s+"), " ").trim()
            if (text.isNotBlank()) {
                msgs += Msg(
                    idx = msgs.size,
                    ts = currentTs,
                    speaker = currentSpeaker,
                    text = text
                )
            }
            buf.clear()
            currentTs = null
            // keep speaker until a new one is detected? we’ll reset (safer)
            currentSpeaker = null
        }

        for (line in lines) {
            val s = line.trim()
            if (s.isBlank()) continue

            // Skip separators
            if (daySepRegex.containsMatchIn(s)) continue

            val hasTs = tsRegex.containsMatchIn(s)
            val isName = looksLikeNameHeader(s)

            if (isName && buf.isNotEmpty()) {
                flush()
            }

            if (hasTs) {
                // If timestamp appears alone or at end, treat as boundary.
                if (buf.isNotEmpty()) flush()
                // capture the first timestamp string
                currentTs = tsRegex.find(s)?.value
                // If there's more than just the timestamp, keep remaining text
                val remaining = s.replace(tsRegex, "").trim()
                if (remaining.isNotBlank()) buf += remaining
                continue
            }

            if (isName) {
                currentSpeaker = s
                continue
            }

            // Normal content line
            buf += s
        }

        flush()

        // Final pass: remove tiny garbage "messages"
        return msgs.filter { it.text.length >= 2 }
    }

    // ---------- JSON ----------
    private fun msgsToJson(msgs: List<Msg>): String {
        fun esc(s: String): String =
            s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

        val items = msgs.joinToString(",") { m ->
            buildString {
                append("{")
                append("\"idx\":${m.idx},")
                append("\"ts\":${m.ts?.let { "\"${esc(it)}\"" } ?: "null"},")
                append("\"speaker\":${m.speaker?.let { "\"${esc(it)}\"" } ?: "null"},")
                append("\"text\":\"${esc(m.text)}\"")
                append("}")
            }
        }
        return "[$items]"
    }
                       }
