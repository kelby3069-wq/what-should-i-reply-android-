package com.replysense.app.domain.paywall

object UsageLimiter {

    private const val FREE_ANALYSES_PER_DAY = 5
    private var countToday = 0

    fun canAnalyze(): Boolean = countToday < FREE_ANALYSES_PER_DAY

    fun recordAnalysis() {
        countToday++
    }

    fun resetDaily() {
        countToday = 0
    }
}
