package com.replysense.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.replysense.app.model.ConversationTurn
import com.replysense.app.net.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val inputText: String = "",
    val vibeOverride: String? = null,          // null = auto vibe
    val isLoading: Boolean = false,
    val error: String? = null,
    val suggestions: List<String> = emptyList(),
    val conversation: List<ConversationTurn> = emptyList()
)

class AppViewModel : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun setInputText(value: String) {
        _state.value = _state.value.copy(inputText = value, error = null)
    }

    fun setVibeOverride(vibe: String?) {
        // null = auto vibe
        _state.value = _state.value.copy(vibeOverride = vibe, error = null)
    }

    fun addTurn(from: ConversationTurn.From, text: String) {
        val updated = _state.value.conversation.toMutableList()
        updated.add(ConversationTurn(from = from, text = text))
        _state.value = _state.value.copy(conversation = updated)
    }

    fun clearConversation() {
        _state.value = _state.value.copy(conversation = emptyList(), error = null)
    }

    fun requestReplies() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return

        _state.value = _state.value.copy(isLoading = true, error = null, suggestions = emptyList())

        viewModelScope.launch(Dispatchers.IO) {
            val req = Api.buildReplyRequest(
                message = text,
                vibe = _state.value.vibeOverride, // null = auto vibe
                context = null
            )

            val resp = runCatching { Api.postReply(req) }.getOrElse {
                return@launch postError("Network error", it.message)
            }

            if (resp.error != null) {
                postError(resp.error ?: "Error", resp.details)
                return@launch
            }

            val opts = resp.options.map { it.text }.filter { it.isNotBlank() }

            _state.value = _state.value.copy(
                isLoading = false,
                error = null,
                suggestions = opts.ifEmpty { listOf("No options returned.") }
            )
        }
    }

    private fun postError(title: String, details: String?) {
        val msg = if (details.isNullOrBlank()) title else "$title: $details"
        _state.value = _state.value.copy(isLoading = false, error = msg, suggestions = emptyList())
    }
}
