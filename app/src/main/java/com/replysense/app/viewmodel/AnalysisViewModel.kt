package com.replysense.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.replysense.app.viewmodel.ui.AnalysisUiState
import com.replysense.domain.model.MessageSource
import com.replysense.domain.model.UserMessage
import com.replysense.domain.orchestrator.ReplySenseOrchestrator
import com.replysense.domain.evaluation.EvaluationScores
import com.replysense.domain.model.ContextState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalysisViewModel(
    private val orchestrator: ReplySenseOrchestrator = ReplySenseOrchestrator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AnalysisUiState(
            confidencePercent = 0,
            showRiskWarning = false,
            riskLabel = "",
            explanation = ""
        )
    )
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    fun analyzeText(rawText: String) {
        viewModelScope.launch {
            val result = orchestrator.analyze(
                UserMessage(
                    rawText = rawText,
                    source = MessageSource.TEXT_INPUT
                )
            )

            _uiState.value = result.state.toUiState(result.evaluation)
        }
    }
}

/**
 * Domain → UI projection
 * Lives with the ViewModel intentionally.
 */
private fun ContextState.toUiState(
    evaluation: EvaluationScores
): AnalysisUiState {

    val riskScore = riskAssessment?.escalationProbability ?: 0f

    return AnalysisUiState(
        confidencePercent = (evaluation.intentConfidence * 100).toInt(),
        showRiskWarning = riskScore >= 0.6f,
        riskLabel = if (riskScore >= 0.75f) "High risk" else "Moderate risk",
        explanation = when {
            riskScore >= 0.75f ->
                "Emotional intensity and power imbalance increase escalation risk."
            riskScore >= 0.6f ->
                "Some ambiguity or emotional charge could be misread."
            else ->
                "Signals indicate low volatility and balanced dynamics."
        }
    )
}
