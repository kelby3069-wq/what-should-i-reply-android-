package com.replysense.app.util

object OcrPostProcess {

    enum class Dir { THEM, ME }

    data class Msg(
        val id: Int,
        val ts: String? = null,
        val speaker: String? = null,
        val text: String,
        val dir: Dir = Dir.THEM
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
            looksLikeChatContent(line)
        }

        val merged = if (!mergeLines) filtered else mergeChatLines(filtered)

        // Default everything to THEM (safer for “generate reply” UX).
        val msgs = merged
            .mapIndexed { idx, line -> lineToMsg(idx, line) }
            .filter { it.text.isNotBlank() }

        val transcript = msgs.joinToString("\n") { m ->
            buildString {
                append(if (m.dir == Dir.THEM) "THEM: " else "ME: ")
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

    fun toggleDir(list: List<Msg>, id: Int): List<Msg> =
        list.map { m ->
            if (m.id != id) m
            else m.copy(dir = if (m.dir == Dir.THEM) Dir.ME else Dir.THEM)
        }

    fun lastIncomingText(messages: List<Msg>): String? =
        messages.lastOrNull { it.dir == Dir.THEM }?.text

    // ---------- Parsing ----------

    private fun lineToMsg(id: Int, line: String): Msg {
        val m = Regex("""^(.*\b\d{1,2}:\d{2}\s?(AM|PM)\b)\s+(.*)$""", RegexOption.IGNORE_CASE)
            .find(line)
        return if (m != null) {
            Msg(
                id = id,
                ts = m.groupValues[1].trim(),
                text = m.groupValues[3].trim(),
                dir = Dir.THEM
            )
        } else {
            Msg(id = id, text = line.trim(), dir = Dir.THEM)
        }
    }

    // ---------- Cleaning ----------

    private fun normalizeLine(s: String): String? {
        val t = s.trim()

        if (t.length <= 1) return null
        if (isKeyboardRow(t)) return null
        if (isOnlyNumbersOrSpacedNumbers(t)) return null
        if (isUiChrome(t)) return null

        val cleaned = t.replace(Regex("\\s{2,}"), " ").trim()
        if (cleaned.isBlank()) return null
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
        val stripped = t.replace(" ", "")
        if (stripped.isEmpty()) return false
        return stripped.all { it.isDigit() }
    }

    private fun isKeyboardRow(t: String): Boolean {
        val low = t.lowercase().replace(" ", "")
        val keyboardPatterns = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        if (keyboardPatterns.any { low.contains(it) }) return true

        val tokens = t.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size >= 4 && tokens.all { it.length <= 2 && it.any { c -> c.isLetter() } }) {
            val lettersOnly = tokens.joinToString("") { it.filter { c -> c.isLetter() } }.lowercase()
            if (lettersOnly.contains("qwerty") || lettersOnly.contains("asdf") || lettersOnly.contains("zxcv")) return true
        }
        return false
    }

    private fun looksLikeGibberishCaps(t: String): Boolean {
        val tokens = t.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size < 4) return false

        val capLike = tokens.count { tok ->
            tok.length in 1..3 && tok.all { it.isLetter() } && tok == tok.uppercase()
        }
        return capLike >= tokens.size - 1
    }

    // ---------- Chat detection & merging ----------

    private fun looksLikeChatContent(line: String): Boolean {
        val t = line.trim()
        if (t.length < 3) return false
        if (isKeyboardRow(t)) return false
        if (isOnlyNumbersOrSpacedNumbers(t)) return false
        if (isUiChrome(t)) return false

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
            if (looksLikeTimestamp(line) && buffer.isNotEmpty()) {
                flush()
                buffer.append(line)
                continue
            }

            if (buffer.isEmpty()) {
                buffer.append(line)
                continue
            }

            val prev = buffer.toString()
            val shouldJoin = !looksLikeNewMessageBoundary(prev, line)

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

    private fun looksLikeTimestamp(line: String): Boolean =
        Regex("""\b\d{1,2}:\d{2}\s?(AM|PM)\b""", RegexOption.IGNORE_CASE).containsMatchIn(line)

    private fun looksLikeNewMessageBoundary(prev: String, next: String): Boolean {
        if (looksLikeTimestamp(next)) return true
        if (prev.trim().lastOrNull() in listOf('.', '!', '?') && next.length >= 10) return true
        return false
    }

    // ---------- JSON ----------

    private fun msgsToJson(msgs: List<Msg>): String {
        fun esc(s: String) = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val items = msgs.joinToString(",") { m ->
            val ts = m.ts?.let { "\"ts\":\"${esc(it)}\"," } ?: ""
            val sp = m.speaker?.let { "\"speaker\":\"${esc(it)}\"," } ?: ""
            """{"id":${m.id},"dir":"${m.dir.name}",${ts}${sp}"text":"${esc(m.text)}"}"""
        }
        return "[$items]"
    }
}
