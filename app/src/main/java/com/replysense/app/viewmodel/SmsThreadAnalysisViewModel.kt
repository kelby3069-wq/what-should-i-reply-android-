package com.replysense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.replysense.app.data.sms.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SmsThreadState {
    object Idle : SmsThreadState()
    object Loading : SmsThreadState()
    data class Ready(val conversationText: String) : SmsThreadState()
    data class Error(val message: String) : SmsThreadState()
}

class SmsThreadAnalysisViewModel(
    private val smsRepository: SmsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SmsThreadState>(SmsThreadState.Idle)
    val state: StateFlow<SmsThreadState> = _state.asStateFlow()

    fun loadThread(threadId: Long) {
        viewModelScope.launch {
            _state.value = SmsThreadState.Loading

            try {
                val messages = smsRepository.getThreadMessages(threadId)

                if (messages.isEmpty()) {
                    _state.value = SmsThreadState.Error("Conversation is empty.")
                    return@launch
                }

                val formatted = buildConversation(messages)
                _state.value = SmsThreadState.Ready(formatted)

            } catch (e: Exception) {
                _state.value = SmsThreadState.Error(
                    e.message ?: "Unable to load messages."
                )
            }
        }
    }

    private fun buildConversation(messages: List<com.replysense.app.data.sms.SmsMessage>): String {
        return messages.joinToString("\n") { msg ->
            val speaker = if (msg.isFromUser) "Me" else "Them"
            "$speaker: ${msg.body.trim()}"
        }
    }
}
