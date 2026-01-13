package com.replysense.app.ui.analysis

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.replysense.app.domain.core.AnalysisResult

    private val _analysisResult = MutableStateFlow(
        AnalysisResult(
            primaryEmotion = "Unknown",
            severity = "Low",
            attractionSignal = "None",
            flags = emptyList(),
            confidenceScore = 0
        )
    )

    val analysisResult: StateFlow<AnalysisResult> =
        _analysisResult.asStateFlow()

    fun setResult(result: AnalysisResult) {
        _analysisResult.value = result
    }
}
