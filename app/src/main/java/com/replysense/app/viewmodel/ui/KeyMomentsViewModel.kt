package com.replysense.app.viewmodel.ui

import androidx.lifecycle.ViewModel
import com.replysense.app.model.KeyMoment

/**
 * Phase E — UI-only ViewModel
 * Holds immutable key moments.
 */
class KeyMomentsViewModel(
    val keyMoments: List<KeyMoment>
) : ViewModel()
