package com.replysense.app.ui.reply

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GenerateReplyViewModel : ViewModel() {

    private val _replies = MutableStateFlow<List<String>>(emptyList())
    val replies: StateFlow<List<String>> = _replies.asStateFlow()

    fun setReplies(newReplies: List<String>) {
        _replies.value = newReplies
    }

    fun clear() {
        _replies.value = emptyList()
    }
}
