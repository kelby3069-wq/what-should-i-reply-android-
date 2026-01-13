package com.replysense.app.domain.emotion

object LexicalAnalyzer {

    fun analyze(text: String): Triple<Emotion, Float, Float> {
        val lower = text.lowercase()

        return when {
            lower.contains("hate") || lower.contains("angry") ->
                Triple(Emotion.ANGER, 0.6f, 0.7f)

            lower.contains("sad") || lower.contains("hurt") ->
                Triple(Emotion.SADNESS, 0.5f, 0.6f)

            lower.contains("worried") || lower.contains("anxious") ->
                Triple(Emotion.ANXIETY, 0.5f, 0.6f)

            lower.contains("miss you") || lower.contains("love") ->
                Triple(Emotion.AFFECTION, 0.6f, 0.7f)

            lower.contains("thank") ->
                Triple(Emotion.GRATITUDE, 0.4f, 0.6f)

            else ->
                Triple(Emotion.NEUTRAL, 0.1f, 0.3f)
        }
    }
}
