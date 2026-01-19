package com.replysense.app.viewmodel.mapper

import com.replysense.app.domain.analysis.AnalysisResult
import com.replysense.app.viewmodel.ui.AnalysisUiState
import com.replysense.app.viewmodel.ui.CoachingUi
import com.replysense.app.viewmodel.ui.ExplainabilityUi

object AnalysisUiMapper {

    fun map(result: AnalysisResult): AnalysisUiState {
        val riskPercent = result.riskPercent.coerceIn(0, 100)
        val confidenceScore = result.confidenceScore.coerceIn(0, 100)

        val riskLabel = result.riskLabel.ifBlank {
            when {
                riskPercent < 34 -> "Low"
                riskPercent < 67 -> "Moderate"
                else -> "High"
            }
        }

        val narrative = result.narrativeAnalysis

        val riskDrivers = buildList {
            add("Escalation risk")
            if (narrative.contains("directive", ignoreCase = true) || narrative.contains("control", ignoreCase = true)) {
                add("Control pressure")
            }
            if (narrative.contains("accusation", ignoreCase = true) || narrative.contains("suspicion", ignoreCase = true)) {
                add("Pressure / accusations")
            }
            if (narrative.contains("profanity", ignoreCase = true) || narrative.contains("volatile", ignoreCase = true)) {
                add("Hostile language / volatility")
            }
            add("Context sensitivity")
        }.distinct()

        val confidenceDrivers = buildList {
            add("OCR quality")
            if (result.conversationLines.size >= 6) add("Sufficient visible text")
            add("Signal consistency")
        }.distinct()

        val uncertaintyDrivers = buildList {
            add("Missing context")
            if (result.conversationLines.size < 5) add("Limited visible text")
            add("Assumptions about intent")
        }.distinct()

        val coaching = deriveCoaching(
            escalationRisk = riskPercent / 100f,
            ambiguity = if (result.conversationLines.size < 5) 0.7f else 0.5f,
            alignmentWithUserGoal = 0.5f
        )

        return AnalysisUiState(
            isLoading = false,
            riskPercent = riskPercent,
            riskLabel = riskLabel,
            confidenceScore = confidenceScore,
            simulatedOutcome = result.simulatedOutcome,
            signalSummary = result.signalSummary,
            conversationLines = result.conversationLines,
            narrativeAnalysis = result.narrativeAnalysis,

            intentSummary = result.intentSummary,
            uncertaintyQuestions = result.uncertaintyQuestions,
            riskSummary = result.riskSummary,
            deEscalationTips = result.deEscalationTips,

            explainability = ExplainabilityUi(
                riskDrivers = riskDrivers,
                confidenceDrivers = confidenceDrivers,
                uncertaintyDrivers = uncertaintyDrivers
            ),
            coaching = coaching
        )
    }

    private fun deriveCoaching(
        escalationRisk: Float,
        ambiguity: Float,
        alignmentWithUserGoal: Float
    ): CoachingUi {

        val questions = mutableListOf<String>()

        if (ambiguity > 0.6f) {
            questions += "What feels unclear or open-ended in this exchange right now?"
            questions += "What information do you notice yourself filling in or assuming?"
        }

        if (escalationRisk > 0.6f) {
            questions += "What outcome would feel least disruptive if the situation intensified?"
            questions += "What signals suggest this could escalate, and which suggest it might not?"
        }

        if (alignmentWithUserGoal < 0.4f) {
            questions += "How does this interaction relate to what you ultimately want here?"
            questions += "What feels aligned — or misaligned — with your original intention?"
        }

        if (questions.isEmpty()) return CoachingUi.Empty

        return CoachingUi(
            reflectionPrompt = "You might find it useful to reflect on a few questions before responding:",
            neutralQuestions = questions
        )
    }
}
