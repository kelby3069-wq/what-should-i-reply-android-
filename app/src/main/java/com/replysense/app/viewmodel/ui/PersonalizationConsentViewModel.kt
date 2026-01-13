package com.replysense.app.viewmodel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.replysense.app.data.prefs.UserPreferencesDataStore
import com.replysense.app.domain.prefs.*

class PersonalizationConsentViewModel(
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    private val _selectedTone = MutableStateFlow(CoachingTone.BALANCED)
    val selectedTone: StateFlow<CoachingTone> = _selectedTone

    private val _selectedDensity = MutableStateFlow(ExplanationDensity.STANDARD)
    val selectedDensity: StateFlow<ExplanationDensity> = _selectedDensity

    fun setTone(tone: CoachingTone) {
        _selectedTone.value = tone
    }

    fun setDensity(density: ExplanationDensity) {
        _selectedDensity.value = density
    }

    fun confirmOptIn() {
        viewModelScope.launch {
            prefsStore.optInAndSave(
                tone = _selectedTone.value,
                density = _selectedDensity.value
            )
        }
    }

    fun decline() {
        viewModelScope.launch {
            prefsStore.revokeConsent()
        }
    }
}
