package com.replysense.app.domain.intelligence.parsing

/**
 * Minimal deterministic parser.
 * Trims whitespace, removes empty lines, preserves order.
 */
class SimpleSemanticParser : SemanticParser {

    override fun parse(lines: List<String>): List<String> {
        return lines
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
