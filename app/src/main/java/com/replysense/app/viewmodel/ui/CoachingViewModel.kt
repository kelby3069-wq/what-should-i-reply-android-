package com.replysense.app.viewmodel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.replysense.app.data.prefs.UserPreferencesDataStore
import com.replysense.app.domain.presentation.CoachingPresentationAdapter
import com.replysense.app.model.AnalysisResult
import kotlinx.coroutines.flow.*

class CoachingViewModel(
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    // ─────────────────────────────────────────────
    // Phase E input (already existing)
    // ─────────────────────────────────────────────

    private val _analysisResult = MutableStateFlow<AnalysisResult?>(null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult.asStateFlow()

    fun setAnalysisResult(result: AnalysisResult) {
        _analysisResult.value = result
    }

    // ─────────────────────────────────────────────
    // Phase F: presentation-aware coaching summary
    // ─────────────────────────────────────────────

    val coachingSummary: StateFlow<String> =
        combine(
            analysisResult,
            prefsStore.prefsFlow
        ) { analysis, prefs ->
            if (analysis == null) return@combine ""

            CoachingPresentationAdapter.coachingSummary(
                analysis = analysis,
                prefs = prefs
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )
}
