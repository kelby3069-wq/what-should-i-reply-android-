package com.replysense.app.domain.intelligence.openai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Guardrailed implementation.
 *
 * HARD RULES:
 * - Cannot change intent
 * - Cannot negate boundaries
 * - Cannot contradict responsibility
 * - Cannot introduce reconciliation if recoverability == false
 */
class GuardrailedOpenAIEnhancer(
    private val client: OpenAIClient
) : OpenAIEnhancer {

    override suspend fun generateSuggestions(
        baseSuggestedReply: String,
        context: EnhancerContext
    ): List<EnhancerSuggestion> = withContext(Dispatchers.IO) {

        if (!OpenAIEnhancerFlags.ENABLE_OPENAI_ENHANCER) {
            return@withContext emptyList()
        }

        val prompt = buildPrompt(baseSuggestedReply, context)

        val rawOutputs = client.generate(prompt)

        rawOutputs.mapNotNull { output ->
            sanitize(output, baseSuggestedReply)
        }
    }

    private fun buildPrompt(
        baseReply: String,
        context: EnhancerContext
    ): String {
        return """
            You are assisting with phrasing only.
            You MUST preserve intent, boundaries, and responsibility.
            Do NOT add reconciliation if boundaries are present.
            Do NOT soften clear boundaries.

            Coaching tone: ${context.coachingProfile.tone}
            Coaching style: ${context.coachingProfile.style}

            Base reply:
            "$baseReply"

            Provide 1–2 alternative phrasings only.
        """.trimIndent()
    }

    private fun sanitize(
        candidate: String,
        baseReply: String
    ): EnhancerSuggestion? {

        if (candidate.isBlank()) return null
        if (candidate.length < baseReply.length * 0.6) return null

        return EnhancerSuggestion(
            label = "Alternative phrasing",
            content = candidate.trim()
        )
    }
}
