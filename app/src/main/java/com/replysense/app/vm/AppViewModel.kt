package com.replysense.app.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ReplySense baseline ViewModel.
 *
 * Baseline goal: compile + run OCR (ML Kit) with minimal UI.
 * No networking, no repository, no DB, no tabs.
 *
 * Later we can reintroduce the real model (API requests, history, options, etc.)
 * once the build is stable.
 */
class AppViewModel : ViewModel() {

    data class UiState(
        val inputText: String = "",
        val outputText: String = "",
        val isBusy: Boolean = false,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun setInput(text: String) {
        _state.value = _state.value.copy(inputText = text, errorMessage = null)
    }

    fun setOutput(text: String) {
        _state.value = _state.value.copy(outputText = text, errorMessage = null)
    }

    fun clear() {
        _state.value = UiState()
    }

    /**
     * Placeholder for the future "generate reply" behavior.
     * Right now it just echoes input to output so UI can wire up without deps.
     */
    fun generateReply() {
        val input = _state.value.inputText.trim()
        if (input.isEmpty()) {
            _state.value = _state.value.copy(errorMessage = "Enter text first.")
            return
        }
        _state.value = _state.value.copy(
            isBusy = false,
            outputText = input,
            errorMessage = null
        )
    }
}
