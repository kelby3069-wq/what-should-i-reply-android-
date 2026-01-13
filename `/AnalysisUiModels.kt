package com.replysense.app._disabled_phase_c

import com.replysense.app.domain.emotion.ConfidenceLevel
import com.replysense.app.domain.emotion.GuidanceType
import com.replysense.app.ui.reply.ReplyOptionUi

data class AnalysisUiState(
    val isLoading: Boolean = false,
    val reassurance: String = "",
    val explanation: String = "",
    val intentUser: String = "",
    val intentOther: String = "",
    val guidance: String = "",
    val guidanceType: GuidanceType = GuidanceType.RESPOND,
    val confidence: ConfidenceLevel = ConfidenceLevel.MEDIUM,
    val severity: EmotionSeverity = EmotionSeverity.LOW,
    val replyOptions: List<ReplyOptionUi> = emptyList(),
    val showIntentMismatchWarning: Boolean = false,
    val errorMessage: String? = null
)
