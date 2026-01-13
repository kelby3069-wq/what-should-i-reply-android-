package com.replysense.app.data.history

import com.replysense.app.model.AnalysisResult

data class HistoryItem(
    val result: AnalysisResult,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val title: String? = null,
    val tags: List<String> = emptyList()
)
