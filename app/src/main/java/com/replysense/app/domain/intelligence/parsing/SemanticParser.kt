package com.replysense.app.domain.intelligence.parsing

/**
 * Phase C parsing contract.
 *
 * Normalizes raw text into inference-ready strings.
 * No semantic objects. No interpretation.
 */
interface SemanticParser {
    fun parse(lines: List<String>): List<String>
}
