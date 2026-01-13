package com.replysense.app.domain.intelligence.openai

/**
 * Minimal abstraction for OpenAI calls.
 * Allows mocking, testing, or replacement.
 */
interface OpenAIClient {
    suspend fun generate(prompt: String): List<String>
}
