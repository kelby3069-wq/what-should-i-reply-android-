package com.replysense.app.data.history

import com.replysense.app.model.AnalysisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AnalysisHistoryRepository {

    private val _history =
        MutableStateFlow<List<HistoryItem>>(emptyList())

    val history: StateFlow<List<HistoryItem>> = _history

    fun save(result: AnalysisResult) {
        _history.value =
            listOf(
                HistoryItem(
                    result = result,
                    title = autoTitle(result)
                )
            ) + _history.value
    }

    fun togglePin(item: HistoryItem) {
        _history.value =
            _history.value
                .map {
                    if (it === item) it.copy(pinned = !it.pinned) else it
                }
                .sortedWith(
                    compareByDescending<HistoryItem> { it.pinned }
                        .thenByDescending { it.createdAt }
                )
    }

    fun rename(item: HistoryItem, newTitle: String) {
        _history.value =
            _history.value.map {
                if (it === item) it.copy(title = newTitle.trim()) else it
            }
    }

    fun setTags(item: HistoryItem, tagsCsv: String) {
        val tags = tagsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        _history.value =
            _history.value.map {
                if (it === item) it.copy(tags = tags) else it
            }
    }

    private fun autoTitle(result: AnalysisResult): String =
        result.overallRead
            .take(48)
            .let { if (it.length == 48) "$it…" else it }
}
