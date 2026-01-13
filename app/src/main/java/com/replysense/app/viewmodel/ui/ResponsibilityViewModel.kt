package com.replysense.app.viewmodel.ui

import androidx.lifecycle.ViewModel
import com.replysense.app.model.ResponsibilityCheck

/**
 * Phase E — UI-only ViewModel
 * Pure presentation of responsibility conclusions.
 */
class ResponsibilityViewModel(
    responsibilityCheck: ResponsibilityCheck
) : ViewModel() {

    val userDidWell: List<String> = responsibilityCheck.userDidWell
    val userDidNotCause: List<String> = responsibilityCheck.userDidNotCause
    val otherPartyActions: List<String> = responsibilityCheck.otherPartyActions
}
