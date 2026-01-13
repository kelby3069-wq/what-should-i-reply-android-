package com.replysense.app.domain.intelligence.openai

/**
 * Contract for optional OpenAI-based enhancement.
 */
interface OpenAIEnhancer {

    suspend fun generateSuggestions(
        baseSuggestedReply: String,
        context: EnhancerContext
    ): List<EnhancerSuggestion>
}
