package com.replysense.app.viewmodel.ui

import androidx.lifecycle.ViewModel
import com.replysense.app.model.EmotionalRead
import com.replysense.app.model.EmotionalStateResult

/**
 * Phase E — UI-only ViewModel
 * No logic, no interpretation.
 */
class EmotionalDynamicsViewModel(
    emotionalState: EmotionalStateResult
) : ViewModel() {

    val userEmotion: EmotionalRead = emotionalState.user
    val otherEmotion: EmotionalRead = emotionalState.other
}
