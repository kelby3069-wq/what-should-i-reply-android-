package com.replysense.app.viewmodel.ui

data class AnalysisUiState(
    val confidencePercent: Int,
    val showRiskWarning: Boolean,
    val riskLabel: String,
    val explanation: String
)
