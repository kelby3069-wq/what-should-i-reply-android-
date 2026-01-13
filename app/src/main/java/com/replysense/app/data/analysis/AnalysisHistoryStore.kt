package com.replysense.app.data.analysis

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.replysense.app.model.AnalysisResult

object AnalysisHistoryStore {

    private val _history = MutableStateFlow<List<AnalysisResult>>(emptyList())
    val history: StateFlow<List<AnalysisResult>> = _history

    fun add(result: AnalysisResult) {
        _history.value = listOf(result) + _history.value.take(49)
    }
}
