package com.replysense.app.domain.emotion

object UserVoiceProfileExtractor {

    fun extract(userMessages: List<String>): UserVoiceProfile {
        if (userMessages.isEmpty()) {
            return defaultProfile()
        }

        val allText = userMessages.joinToString(" ")
        val wordCount = allText.split("\\s+".toRegex()).size.coerceAtLeast(1)

        val emojiCount = Regex("[\\p{So}\\p{Cn}]").findAll(allText).count()
        val slangCount = listOf("lol", "idk", "tbh", "btw", "nah", "yeah")
            .sumOf { slang ->
                Regex("\\b$slang\\b", RegexOption.IGNORE_CASE)
                    .findAll(allText).count()
            }

        val softeners = listOf("maybe", "i think", "i feel", "kind of", "sort of")
        val usesSoftening = softeners.any { allText.contains(it, true) }

        val usesLowercase =
            userMessages.any { it == it.lowercase() }

        val sentences =
            allText.split(Regex("[.!?]")).filter { it.isNotBlank() }

        val avgSentenceLength =
            sentences.map { it.split("\\s+".toRegex()).size }
                .average().toFloat()

        return UserVoiceProfile(
            formality = (1f - slangCount.toFloat() / wordCount).coerceIn(0f, 1f),
            emojiFrequency = (emojiCount.toFloat() / wordCount).coerceIn(0f, 1f),
            slangFrequency = (slangCount.toFloat() / wordCount).coerceIn(0f, 1f),
            avgSentenceLength = avgSentenceLength,
            usesLowercase = usesLowercase,
            usesSoftening = usesSoftening,
            usesDirectStatements = avgSentenceLength < 8
        )
    }

    private fun defaultProfile() =
        UserVoiceProfile(
            formality = 0.5f,
            emojiFrequency = 0f,
            slangFrequency = 0f,
            avgSentenceLength = 10f,
            usesLowercase = false,
            usesSoftening = false,
            usesDirectStatements = true
        )
}
