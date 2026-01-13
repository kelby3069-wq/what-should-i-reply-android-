package com.replysense.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.replysense.app.domain.analysis.AnalysisRepository
import kotlinx.coroutines.launch

class ScreenshotAnalysisViewModel(
    private val repository: AnalysisRepository
) : ViewModel() {

    fun analyze(lines: List<String>, onResult: (Any) -> Unit) {
        viewModelScope.launch {
            val result = repository.analyze(lines)
            onResult(result)
        }
    }
}
