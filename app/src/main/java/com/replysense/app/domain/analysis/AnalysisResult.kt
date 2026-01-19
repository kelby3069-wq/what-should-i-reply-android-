package com.replysense.app.domain.analysis

import com.replysense.app.domain.conversation.ConversationLine

data class AnalysisResult(
    val riskPercent: Int,
    val riskLabel: String,
    val confidenceScore: Int,
    val simulatedOutcome: String,
    val signalSummary: String,
    val conversationLines: List<ConversationLine>,
    val narrativeAnalysis: String = "",

    // NEW: richer structured surfacing for Phase N premium UI
    val intentSummary: String = "",
    val uncertaintyQuestions: List<String> = emptyList(),
    val riskSummary: String = "",
    val deEscalationTips: List<String> = emptyList()
)
