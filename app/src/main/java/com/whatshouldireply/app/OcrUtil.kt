package com.whatshouldireply.app

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

data class OcrItem(val text: String, val box: Rect)
data class OcrChunk(val text: String, val box: Rect)
data class OcrConversation(val chunks: List<OcrChunk>, val target: OcrChunk?)

object OcrUtil {

    suspend fun extractConversation(context: Context, uri: Uri): OcrConversation = withContext(Dispatchers.IO) {
        val bmp = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return@withContext OcrConversation(emptyList(), null)

        val image = InputImage.fromBitmap(bmp, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).await()

        val items = flatten(result)
            .map { it.copy(text = sanitize(it.text)) }
            .filter { it.text.isNotBlank() }
            .sortedWith(compareBy({ it.box.top }, { it.box.left }))

        val chunks = groupByVerticalGaps(items, bmp.height)
            .filter { it.text.isNotBlank() }

        val target = chunks.maxByOrNull { it.box.bottom }
        OcrConversation(chunks, target)
    }

    fun buildPrompt(convo: OcrConversation): String {
        val target = convo.target ?: return ""
        return buildString {
            appendLine("FULL_CONTEXT (top → bottom):")
            convo.chunks.forEach { c ->
                if (c == target) appendLine("TARGET (reply to this last message): ${c.text}")
                else appendLine(c.text)
            }
        }.trim()
    }

    private fun flatten(result: Text): List<OcrItem> {
        val out = mutableListOf<OcrItem>()
        for (block in result.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                val txt = line.text?.trim().orEmpty()
                if (txt.isNotBlank()) out.add(OcrItem(txt, box))
            }
        }
        return out
    }

    private fun groupByVerticalGaps(items: List<OcrItem>, height: Int): List<OcrChunk> {
        if (items.isEmpty()) return emptyList()

        val gap = (height * 0.02).toInt().coerceIn(16, 48)

        fun union(a: Rect, b: Rect): Rect =
            Rect(min(a.left, b.left), min(a.top, b.top), max(a.right, b.right), max(a.bottom, b.bottom))

        val groups = mutableListOf<MutableList<OcrItem>>()
        var current = mutableListOf(items.first())
        var rect = Rect(items.first().box)

        for (i in 1 until items.size) {
            val it = items[i]
            val vGap = it.box.top - rect.bottom
            if (vGap <= gap) {
                current.add(it)
                rect = union(rect, it.box)
            } else {
                groups.add(current)
                current = mutableListOf(it)
                rect = Rect(it.box)
            }
        }
        groups.add(current)

        return groups.map { g ->
            val ordered = g.sortedWith(compareBy({ it.box.top }, { it.box.left }))
            val r = ordered.map { it.box }.reduce { acc, b -> union(acc, b) }
            val text = ordered.joinToString("\n") { it.text }.trim()
            OcrChunk(text, r)
        }
    }

    private fun sanitize(s: String): String =
        s.replace("\u00A0", " ").replace(Regex("\\s+"), " ").trim()
}
