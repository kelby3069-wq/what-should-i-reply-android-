package com.replysense.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.replysense.app.repo.ReplyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val theirMessage: String = "",
    val variants: Int = 3,

    // Auto vibe defaults
    val vibe: String = "auto",
    val tone: String = "auto",
    val writingStyle: String = "auto",
    val textQuality: String = "auto",
    val emojiLevel: String = "auto",
    val spiceLevel: String = "auto",
    val age: String = "auto",
    val punctuationPreference: String = "auto",

    val isLoading: Boolean = false,
    val error: String? = null,

    val detectedNotes: String? = null,
    val replies: List<String> = emptyList()
)

class ComposerViewModel : ViewModel() {

    private val repo = ReplyRepository()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var incomingApplied = false

    fun applyIncomingTextOnce(text: String) {
        if (incomingApplied) return
        incomingApplied = true
        _state.update { it.copy(theirMessage = text.trim()) }
    }

    fun setTheirMessage(v: String) = _state.update { it.copy(theirMessage = v, error = null) }
    fun setVariants(v: Int) = _state.update { it.copy(variants = v.coerceIn(1, 6)) }

    fun setVibe(v: String) = _state.update { it.copy(vibe = v) }
    fun setTone(v: String) = _state.update { it.copy(tone = v) }
    fun setWritingStyle(v: String) = _state.update { it.copy(writingStyle = v) }
    fun setTextQuality(v: String) = _state.update { it.copy(textQuality = v) }
    fun setEmojiLevel(v: String) = _state.update { it.copy(emojiLevel = v) }
    fun setSpiceLevel(v: String) = _state.update { it.copy(spiceLevel = v) }
    fun setAge(v: String) = _state.update { it.copy(age = v) }
    fun setPunctuationPreference(v: String) = _state.update { it.copy(punctuationPreference = v) }

    fun clearAll() {
        _state.value = UiState()
        incomingApplied = false
    }

    fun generate() {
        val s = _state.value
        if (s.theirMessage.isBlank()) {
            _state.update { it.copy(error = "Paste what they said first.") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null, replies = emptyList(), detectedNotes = null) }

        viewModelScope.launch {
            val res = repo.generate(
                theirMessage = s.theirMessage,
                variants = s.variants,
                vibe = s.vibe,
                tone = s.tone,
                writingStyle = s.writingStyle,
                textQuality = s.textQuality,
                emojiLevel = s.emojiLevel,
                spiceLevel = s.spiceLevel,
                age = s.age,
                punctuationPreference = s.punctuationPreference,
            )

            res.fold(
                onSuccess = { r ->
                    val replies = r.result?.replies.orEmpty()
                    val notes = r.result?.vibe?.notes
                    _state.update { it.copy(isLoading = false, replies = replies, detectedNotes = notes) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Something broke.") }
                }
            )
        }
    }
}
