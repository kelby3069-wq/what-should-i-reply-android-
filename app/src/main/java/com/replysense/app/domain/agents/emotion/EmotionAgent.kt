package com.replysense.domain.agents.emotion

import com.replysense.domain.agents.MicroAgent
import com.replysense.domain.memory.AgentMemory
import com.replysense.domain.model.ContextState

/**
 * EmotionAgent
 *
 * Extracts emotional tone + volatility using
 * conservative lexical and structural cues.
 *
 * v1 rules:
 * - punctuation density
 * - emotionally loaded words
 * - message abruptness
 *
 * This agent NEVER overwrites existing emotion signals.
 */
class EmotionAgent : MicroAgent<ContextState, ContextState> {

    override fun run(
        input: ContextState,
        memory: AgentMemory
    ): ContextState {

        if (input.emotionalSignals != null) return input

        val text = input.text.lowercase()

        val emotions = mutableMapOf<String, Float>()

        if (containsAny(text, listOf("angry", "upset", "annoyed", "frustrated"))) {
            emotions["anger"] = 0.7f
        }

        if (containsAny(text, listOf("sad", "hurt", "disappointed"))) {
            emotions["sadness"] = 0.6f
        }

        if (containsAny(text, listOf("happy", "excited", "glad"))) {
            emotions["positive"] = 0.6f
        }

        if (containsAny(text, listOf("lol", "haha", "😂", "😅"))) {
            emotions["playful"] = 0.5f
        }

        val intensity = calculateIntensity(text)

        return input.copy(
            emotionalSignals = EmotionalResult(
                emotions = emotions,
                emotionalIntensity = intensity
            )
        )
    }

    private fun calculateIntensity(text: String): Float {
        val exclamations = text.count { it == '!' }
        val questionMarks = text.count { it == '?' }
        val capsRatio = text.count { it.isUpperCase() }.toFloat() / text.length.coerceAtLeast(1)

        return (
                (exclamations * 0.1f) +
                        (questionMarks * 0.05f) +
                        (capsRatio * 0.5f)
                ).coerceIn(0f, 1f)
    }

    private fun containsAny(text: String, needles: List<String>): Boolean {
        return needles.any { text.contains(it) }
    }
}
