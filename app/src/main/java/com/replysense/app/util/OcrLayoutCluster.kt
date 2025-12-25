package com.replysense.app.util

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

object OcrLayoutCluster {

    data class LineBox(
        val text: String,
        val box: Rect
    )

    /**
     * Extract chat-ish messages by clustering OCR lines using bounding boxes.
     *
     * - Filters bottom keyboard area + obvious junk lines
     * - Sorts by top then left
     * - Groups lines into message "bubbles" using vertical gaps and alignment changes
     * - Auto-tags THEM vs ME by horizontal position (right side = ME)
     */
    fun extractMessages(
        result: Text,
        imageWidthPx: Int,
        imageHeightPx: Int
    ): List<OcrPostProcess.Msg> {
        val w = imageWidthPx.coerceAtLeast(1)
        val h = imageHeightPx.coerceAtLeast(1)

        val lines = mutableListOf<LineBox>()

        for (block in result.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                val t = line.text.trim()
                if (t.isBlank()) continue

                // 1) Drop bottom area (keyboard / input bar)
                if (box.top >= (h * 0.72f).toInt()) continue

                // 2) Drop obvious garbage
                if (t.length <= 1) continue
                if (isUiChrome(t)) continue
                if (isOnlyNumbersOrSpacedNumbers(t)) continue
                if (isKeyboardRow(t)) continue
                if (looksLikeGibberishCaps(t)) continue

                lines += LineBox(text = normalizeSpaces(t), box = box)
            }
        }

        if (lines.isEmpty()) return emptyList()

        // Sort reading order-ish: top first, then left
        lines.sortWith(compareBy<LineBox> { it.box.top }.thenBy { it.box.left })

        // Thresholds (tuned for screenshots)
        val gapPx = (h * 0.030f).toInt().coerceAtLeast(18)        // vertical gap to start new bubble
        val alignJumpPx = (w * 0.30f).toInt().coerceAtLeast(140)  // left edge shift indicating other side
        val minorGapPx = (h * 0.015f).toInt().coerceAtLeast(10)

        val msgs = mutableListOf<OcrPostProcess.Msg>()
        var cur = StringBuilder()
        var curBox = Rect(lines.first().box)
        var curDir = dirFromBox(lines.first().box, w)

        fun flush() {
            val text = cur.toString().trim()
            if (text.isNotBlank()) {
                msgs += OcrPostProcess.Msg(
                    id = msgs.size,
                    text = text,
                    dir = curDir
                )
            }
            cur = StringBuilder()
        }

        fun startNew(lb: LineBox) {
            flush()
            cur.append(lb.text)
            curBox = Rect(lb.box)
            curDir = dirFromBox(lb.box, w)
        }

        // Seed
        cur.append(lines.first().text)

        for (i in 1 until lines.size) {
            val prev = curBox
            val next = lines[i].box
            val nextText = lines[i].text

            val vGap = next.top - prev.bottom
            val leftShift = kotlin.math.abs(next.left - prev.left)
            val nextDir = dirFromBox(next, w)

            val newBubble =
                vGap >= gapPx ||
                (leftShift >= alignJumpPx && vGap >= minorGapPx) ||
                (nextDir != curDir && vGap >= minorGapPx)

            if (newBubble) {
                startNew(lines[i])
            } else {
                // Same bubble → append as continuation
                cur.append(' ')
                cur.append(nextText)
                curBox.union(next)
            }
        }

        flush()
        return msgs
    }

    private fun dirFromBox(box: Rect, w: Int): OcrPostProcess.Dir {
        val cx = box.exactCenterX()
        return if (cx > w * 0.56f) OcrPostProcess.Dir.ME else OcrPostProcess.Dir.THEM
    }

    // ---------- Light cleanup helpers (duplicated on purpose: fewer dependencies) ----------

    private fun normalizeSpaces(s: String): String =
        s.replace(Regex("\\s{2,}"), " ").trim()

    private fun isUiChrome(t: String): Boolean {
        val low = t.lowercase()
        if (low == "replysense ocr") return true
        if (low == "message") return true
        if (low.contains("active now")) return true
        if (low.contains("english (us)")) return true
        if (low.startsWith("watch ")) return true
        if (low == "today" || low == "yesterday") return true
        return false
    }

    private fun isOnlyNumbersOrSpacedNumbers(t: String): Boolean {
        val stripped = t.replace(" ", "")
        if (stripped.isEmpty()) return false
        return stripped.all { it.isDigit() }
    }

    private fun isKeyboardRow(t: String): Boolean {
        val low = t.lowercase().replace(" ", "")
        val patterns = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        if (patterns.any { low.contains(it) }) return true

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
}
