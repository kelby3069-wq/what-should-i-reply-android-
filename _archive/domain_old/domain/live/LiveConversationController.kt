package com.replysense.app.domain.live

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

object LiveConversationController {

    private var lastAnalysisTime = 0L
    private const val COOLDOWN_MS = 15_000L

    fun canAnalyze(): Boolean {
        val now = System.currentTimeMillis()
        return now - lastAnalysisTime > COOLDOWN_MS
    }

    suspend fun markAnalyzed() {
        lastAnalysisTime = System.currentTimeMillis()
        delay(1.seconds) // intentional friction
    }
}
