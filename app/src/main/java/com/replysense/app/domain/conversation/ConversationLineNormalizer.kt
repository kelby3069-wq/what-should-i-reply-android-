package com.replysense.app.domain.conversation

import com.replysense.app.domain.ocr.OcrLine

/**
 * Converts raw OCR lines into stable, analyzable conversation lines.
 *
 * Rules:
 * - Preserve order
 * - Preserve geometry
 * - Trim whitespace
 * - Drop empty lines
 * - Deterministic IDs
 */
class ConversationLineNormalizer {

    fun normalize(ocrLines: List<OcrLine>): List<ConversationLine> {
        val result = mutableListOf<ConversationLine>()

        var index = 0
        for (line in ocrLines) {
            val cleaned = cleanText(line.text)
            if (cleaned.isNotEmpty()) {
                result.add(
                    ConversationLine(
                        id = index,
                        text = cleaned,
                        boundingBox = line.boundingBox
                    )
                )
                index++
            }
        }

        return result
    }

    private fun cleanText(raw: String): String {
        return raw
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
