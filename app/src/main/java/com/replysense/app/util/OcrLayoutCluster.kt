package com.replysense.app.util

import android.graphics.Rect
import com.google.mlkit.vision.text.Text
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object OcrLayoutCluster {

    enum class MySide { RIGHT, LEFT }

    data class LineBox(
        val text: String,
        val box: Rect
    )

    /**
     * Extract messages by clustering OCR lines using bounding boxes.
     *
     * Improvements:
     * - Drops bottom keyboard zone by position
     * - Clusters into bubbles using vertical gap + alignment + dir changes
     * - Assigns ME/THEM using bubble geometry + user “my side” preference
     */
    fun extractMessages(
        result: Text,
        imageWidthPx: Int,
        imageHeightPx: Int,
        mySide: MySide
    ): List<OcrPostProcess.Msg> {
        val w = imageWidthPx.coerceAtLeast(1)
        val h = imageHeightPx.coerceAtLeast(1)

        val lines = mutableListOf<LineBox>()

        for (block in result.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                val t = line.text.trim()
                if (t.isBlank()) continue

                // Drop bottom area (keyboard / input bar)
                if (box.top >= (h * 0.72f).toInt()) continue

                // Drop obvious junk
                if (t.length <= 1) continue
                if (isUiChrome(t)) continue
                if (isOnlyNumbersOrSpacedNumbers(t)) continue
                if (isKeyboardRow(t)) continue
                if (looksLikeGibberishCaps(t)) continue

                lines += LineBox(text = normalizeSpaces(t), box = box)
            }
        }

        if (lines.isEmpty()) return emptyList()

        // Sort: top then left
        lines.sortWith(compareBy<LineBox> { it.box.top }.thenBy { it.box.left })

        // Thresholds
        val gapPx = (h * 0.030f).toInt().coerceAtLeast(18)
        val minorGapPx = (h * 0.015f).toInt().coerceAtLeast(10)
        val alignJumpPx = (w * 0.26f).toInt().coerceAtLeast(120)

        // Build bubble clusters
        data class Bubble(var rect: Rect, val parts: MutableList<LineBox>)

        val bubbles = mutableListOf<Bubble>()
        var cur = Bubble(Rect(lines.first().box), mutableListOf(lines.first()))

        fun flushBubble() {
            if (cur.parts.isNotEmpty()) bubbles += cur
        }

        for (i in 1 until lines.size) {
            val prevRect = cur.rect
            val next = lines[i]
            val nextRect = next.box

            val vGap = nextRect.top - prevRect.bottom
            val leftShift = abs(nextRect.left - prevRect.left)

            // provisional dir changes can suggest boundary, but we do it after bubble is built;
            // we approximate boundary here using alignment and vertical gap.
            val newBubble =
                vGap >= gapPx ||
                (leftShift >= alignJumpPx && vGap >= minorGapPx)

            if (newBubble) {
                flushBubble()
                cur = Bubble(Rect(nextRect), mutableListOf(next))
            } else {
                cur.parts += next
                cur.rect.union(nextRect)
            }
        }
        flushBubble()

        // Assign direction per bubble using geometry
        val msgs = bubbles.mapIndexed { idx, b ->
            val text = b.parts.joinToString(" ") { it.text }.trim()
            val dir = bubbleDir(b.rect, w, mySide)

            OcrPostProcess.Msg(
                id = idx,
                text = text,
                dir = dir
            )
        }.filter { it.text.isNotBlank() }

        return msgs
    }

    /**
     * Better ME/THEM classifier:
     * - Uses bubble center relative to screen
     * - Uses bubble margins: who is closer to edge?
     * - Uses bubble width: narrow right-aligned bubbles are often ME on messaging apps
     */
    private fun bubbleDir(rect: Rect, w: Int, mySide: MySide): OcrPostProcess.Dir {
        val left = rect.left.toFloat()
        val right = rect.right.toFloat()
        val width = (right - left).coerceAtLeast(1f)
        val cx = rect.exactCenterX()

        val leftMargin = left
        val rightMargin = (w - right)

        // Normalize
        val cxNorm = cx / w.toFloat()
        val widthNorm = width / w.toFloat()

        val closerToRightEdge = rightMargin < leftMargin
        val stronglyRight = cxNorm > 0.60f
        val stronglyLeft = cxNorm < 0.40f

        // Heuristic score: positive means RIGHT-side bubble
        var score = 0.0f
        if (closerToRightEdge) score += 1.0f else score -= 1.0f
        if (stronglyRight) score += 1.0f
        if (stronglyLeft) score -= 1.0f

        // Narrow bubbles tend to be “typed” bubbles; give slight bias to edge closeness
        if (widthNorm < 0.55f && closerToRightEdge) score += 0.5f
        if (widthNorm < 0.55f && !closerToRightEdge) score -= 0.5f

        val bubbleSideRight = score > 0f

        val meIsRight = (mySide == MySide.RIGHT)
        val isMe = if (meIsRight) bubbleSideRight else !bubbleSideRight
        return if (isMe) OcrPostProcess.Dir.ME else OcrPostProcess.Dir.THEM
    }

    // ---------- Cleanup helpers ----------

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
