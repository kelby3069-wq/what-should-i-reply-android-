package com.replysense.app.domain

object SignalExtractor {

    fun extract(messages: List<String>): AnalysisSignals {
        if (messages.isEmpty()) {
            return AnalysisSignals(
                momentum = Momentum.STABLE,
                emotionalLoad = EmotionalLoad.LOW,
                riskLevel = RiskLevel.LOW
            )
        }

        val lengths = messages.map { it.length }
        val avg = lengths.average()

        val shortCount = lengths.count { it < avg * 0.6 }
        val emotionalWords = listOf(
            "feel", "felt", "hurt", "upset", "confused",
            "miss", "care", "love", "frustrated", "angry"
        )

        val emotionalHits =
            messages.count { msg ->
                emotionalWords.any { w -> msg.contains(w, ignoreCase = true) }
            }

        val momentum = when {
            shortCount >= messages.size / 2 -> Momentum.DECLINING
            shortCount <= messages.size / 4 -> Momentum.IMPROVING
            else -> Momentum.STABLE
        }

        val emotionalLoad = when {
            emotionalHits >= messages.size / 2 -> EmotionalLoad.HIGH
            emotionalHits > 0 -> EmotionalLoad.MEDIUM
            else -> EmotionalLoad.LOW
        }

        val riskLevel = when {
            momentum == Momentum.DECLINING && emotionalLoad == EmotionalLoad.HIGH ->
                RiskLevel.HIGH
            momentum == Momentum.DECLINING ->
                RiskLevel.MEDIUM
            else ->
                RiskLevel.LOW
        }

        return AnalysisSignals(
            momentum = momentum,
            emotionalLoad = emotionalLoad,
            riskLevel = riskLevel
        )
    }
}
