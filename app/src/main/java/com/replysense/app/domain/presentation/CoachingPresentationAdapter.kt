package com.replysense.app.domain.presentation

import com.replysense.app.domain.prefs.*
import com.replysense.app.model.AnalysisResult

object CoachingPresentationAdapter {

    fun coachingSummary(
        analysis: AnalysisResult,
        prefs: UserPresentationPrefs
    ): String {
        // Phase A contract: coaching is nullable
        val baseSummary = analysis.coaching?.summary
            ?: return "" // UI already conditionally renders; never invent content

        if (!prefs.optedIn) return baseSummary

        return when (prefs.tone) {
            CoachingTone.GENTLE ->
                "Take this at your pace.\n\n$baseSummary"

            CoachingTone.BALANCED ->
                baseSummary

            CoachingTone.DIRECT ->
                "Bottom line:\n$baseSummary"
        }
    }

    fun explanationDensity(
        explanation: String,
        prefs: UserPresentationPrefs
    ): String {
        if (!prefs.optedIn) return explanation

        return when (prefs.density) {
            ExplanationDensity.BRIEF ->
                explanation.lines()
                    .take(3)
                    .joinToString("\n")

            ExplanationDensity.STANDARD ->
                explanation

            ExplanationDensity.DEEP ->
                explanation
        }
    }
}
