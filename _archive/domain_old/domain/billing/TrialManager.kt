package com.replysense.app.domain.billing

import java.util.concurrent.TimeUnit

object TrialManager {

    private const val TRIAL_DAYS = 7
    private var trialStartTime: Long? = null

    fun startTrial() {
        if (trialStartTime == null) {
            trialStartTime = System.currentTimeMillis()
        }
    }

    fun isTrialActive(): Boolean {
        val start = trialStartTime ?: return false
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - start
        )
        return elapsedDays < TRIAL_DAYS
    }

    fun hasAccess(): Boolean {
        return SubscriptionManager.isPro() || isTrialActive()
    }
}
