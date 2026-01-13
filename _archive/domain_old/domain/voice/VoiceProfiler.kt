package com.replysense.app.domain.voice

class VoiceProfiler {

    fun infer(messages: List<String>): UserVoice {
        val metrics = calculateMetrics(messages)

        return when {
            metrics.averageWordCount <= 5 && metrics.emojiRate < 0.1f ->
                UserVoice.MINIMAL

            metrics.hedgeRate > 0.3f ->
                UserVoice.SHY

            metrics.emojiRate > 0.4f && metrics.averageWordCount > 12 ->
                UserVoice.EXPRESSIVE

            metrics.lowercaseRate > 0.6f ->
                UserVoice.CASUAL

            else ->
                UserVoice.DIRECT
        }
    }

    private fun calculateMetrics(messages: List<String>): VoiceMetrics {
        if (messages.isEmpty()) {
            return VoiceMetrics(0, 0f, 0f, 0f)
        }

        val total = messages.size
        val avgWords = messages.sumOf { it.split(" ").size } / total

        val emojiRate =
            messages.count { it.contains(Regex("[\\p{So}]")) }.toFloat() / total

        val lowercaseRate =
            messages.count { it == it.lowercase() }.toFloat() / total

        val hedgeRate =
            messages.count {
                it.contains(Regex("\\b(kinda|maybe|idk|lol)\\b", RegexOption.IGNORE_CASE))
            }.toFloat() / total

        return VoiceMetrics(avgWords, emojiRate, lowercaseRate, hedgeRate)
    }
}
