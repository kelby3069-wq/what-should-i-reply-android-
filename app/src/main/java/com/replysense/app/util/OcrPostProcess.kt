package com.replysense.app.util

object OcrPostProcess {

    data class Msg(
        val ts: String? = null,
        val speaker: String? = null,
        val text: String
    )

    data class Processed(
        val transcript: String,
        val json: String,
        val messageCount: Int,
        val messages: List<Msg>
    )

    fun processThreadAware(
        input: String,
        clean: Boolean,
        mergeLines: Boolean,
        threadOnly: Boolean
    ): Processed {
        val rawLines = input
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val lines = if (clean) rawLines.mapNotNull { normalizeLine(it) } else rawLines

        val filtered = lines.filter { line ->
            if (!threadOnly) return@filter true
            // Thread-only mode: keep only stuff that looks like chat text
            looksLikeChatContent(line)
        }

        val merged = if (!mergeLines) filtered else mergeChatLines(filtered)

        val msgs = splitIntoMessages(merged)

        val transcript = msgs.joinToString("\n") { m ->
            buildString {
                if (m.ts != null) append("[${m.ts}] ")
                if (m.speaker != null) append("${m.speaker}: ")
                append(m.text)
            }
        }

        val json = msgsToJson(msgs)

        return Processed(
            transcript = transcript,
            json = json,
            messageCount = msgs.size,
            messages = msgs
        )
    }

    // ---------- Cleaning ----------

    private fun normalizeLine(s: String): String? {
        val t = s.trim()

        // Hard drop common UI / OCR junk
        if (t.length <= 1) return null
        if (isKeyboardRow(t)) return null
        if (isOnlyNumbersOrSpacedNumbers(t)) return null
        if (isUiChrome(t)) return null

        // Compress repeated spaces
        val cleaned = t.replace(Regex("\\s{2,}"), " ").trim()
        if (cleaned.isBlank()) return null

        // Drop lines that are basically random caps fragments (but keep real acronyms inside sentences)
        if (looksLikeGibberishCaps(cleaned)) return null

        return cleaned
    }

    private fun isUiChrome(t: String): Boolean {
        val low = t.lowercase()
        val exact = setOf(
            "active now",
            "message",
            "english (us)",
            "search",
            "watch",
            "today",
            "yesterday"
        )
        if (low in exact) return true
        if (low.startsWith("watch ")) return true
        if (low.contains("active now")) return true
        if (low.contains("english (us)")) return true
        if (low == "replysense ocr") return true
        return false
    }

    private fun isOnlyNumbersOrSpacedNumbers(t: String): Boolean {
        // "1 2 3 4 5" or "12345"
        val stripped = t.replace(" ", "")
        if (stripped.isEmpty()) return false
        return stripped.all { it.isDigit() }
    }

    private fun isKeyboardRow(t: String): Boolean {
        // Typical OCR of on-screen keyboard rows
        val low = t.lowercase().replace(" ", "")
        val keyboardPatterns = listOf(
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm"
        )
        if (keyboardPatterns.any { low.contains(it) }) return true

        // Also reject mostly single-letter tokens like: "Q WE RTY U" or "A S D"
        val tokens = t.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size >= 4 && tokens.all { it.length <= 2 && it.any { c -> c.isLetter() } }) {
            val lettersOnly = tokens.joinToString("") { it.filter { c -> c.isLetter() } }.lowercase()
            if (lettersOnly.contains("qwerty") || lettersOnly.contains("asdf") || lettersOnly.contains("zxcv")) return true
        }
        return false
    }

    private fun looksLikeGibberishCaps(t: String): Boolean {
        // If line is mostly caps tokens with little vowel/space structure, it's likely UI/keyboard OCR.
        // Keep normal sentences even if they contain acronyms.
        val tokens = t.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size < 4) return false

        val capLike = tokens.count { tok ->
            tok.length in 1..3 && tok.all { it.isLetter() } && tok == tok.uppercase()
        }
        if (capLike >= tokens.size - 1) return true

        return false
    }

    // ---------- Chat detection & merging ----------

    private fun looksLikeChatContent(line: String): Boolean {
        val t = line.trim()
        if (t.length < 3) return false
        if (isKeyboardRow(t)) return false
        if (isOnlyNumbersOrSpacedNumbers(t)) return false
        if (isUiChrome(t)) return false

        // looks like a sentence-ish line
        val hasSpace = t.contains(' ')
        val hasLetters = t.any { it.isLetter() }
        val hasPunct = t.any { it in ".?!," }
        val longEnough = t.length >= 12

        return hasLetters && (longEnough || (hasSpace && (hasPunct || t.length >= 8)))
    }

    private fun mergeChatLines(lines: List<String>): List<String> {
        if (lines.isEmpty()) return emptyList()
        val out = mutableListOf<String>()
        val buffer = StringBuilder()

        fun flush() {
            val s = buffer.toString().trim()
            if (s.isNotBlank()) out.add(s)
            buffer.clear()
        }

        for (line in lines) {
            // If line looks like a timestamp header, split
            if (looksLikeTimestamp(line) && buffer.isNotEmpty()) {
                flush()
                buffer.append(line)
                continue
            }

            // If buffer empty, start
            if (buffer.isEmpty()) {
                buffer.append(line)
                continue
            }

            // Join “continuation lines” into same message
            val prev = buffer.toString()
            val shouldJoin =
                !looksLikeNewMessageBoundary(prev, line)

            if (shouldJoin) {
                buffer.append(' ')
                buffer.append(line)
            } else {
                flush()
                buffer.append(line)
            }
        }
        flush()
        return out
    }

    private fun looksLikeTimestamp(line: String): Boolean {
        // crude: "12:38 AM", "7:05 PM"
        return Regex("""\b\d{1,2}:\d{2}\s?(AM|PM)\b""", RegexOption.IGNORE_CASE).containsMatchIn(line)
    }

    private fun looksLikeNewMessageBoundary(prev: String, next: String): Boolean {
        // boundary if next starts like a new thought or a timestamp
        if (looksLikeTimestamp(next)) return true
        // boundary if next is very long and prev ends with punctuation (new msg often follows)
        if (prev.trim().lastOrNull() in listOf('.', '!', '?') && next.length >= 10) return true
        return false
    }

    // ---------- Split into messages ----------

    private fun splitIntoMessages(lines: List<String>): List<Msg> {
        if (lines.isEmpty()) return emptyList()

        // Basic approach: each merged line becomes a message,
        // but if it contains a timestamp + text, we keep it as one message anyway.
        return lines.map { line ->
            // Try extract timestamp if present
            val m = Regex("""^(.*\b\d{1,2}:\d{2}\s?(AM|PM)\b)\s+(.*)$""", RegexOption.IGNORE_CASE)
                .find(line)
            if (m != null) {
                val ts = m.groupValues[1].trim()
                val txt = m.groupValues[3].trim()
                Msg(ts = ts, text = txt)
            } else {
                Msg(text = line.trim())
            }
        }.filter { it.text.isNotBlank() }
    }

    private fun msgsToJson(msgs: List<Msg>): String {
        fun esc(s: String) = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val items = msgs.joinToString(",") { m ->
            val ts = m.ts?.let { "\"ts\":\"${esc(it)}\"," } ?: ""
            val sp = m.speaker?.let { "\"speaker\":\"${esc(it)}\"," } ?: ""
            """{${ts}${sp}"text":"${esc(m.text)}"}"""
        }
        return "[$items]"
    }
}
