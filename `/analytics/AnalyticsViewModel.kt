package com.replysense.app.ui.analytics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AnalyticsState(
    val paywallShown: Int = 0,
    val upgradeStarted: Int = 0,
    val upgradeCompleted: Int = 0,
    val trialStarted: Int = 0,
    val trialConverted: Int = 0,
    val retainedDay1: Int = 0,
    val retainedDay7: Int = 0
)

class AnalyticsViewModel : ViewModel() {

    private val _analyticsState =
        MutableStateFlow(AnalyticsState())
    val analyticsState: StateFlow<AnalyticsState> =
        _analyticsState.asStateFlow()

    fun log(event: AnalyticsEvent) {
        _analyticsState.value = when (event) {

            is AnalyticsEvent.PaywallShown ->
                _analyticsState.value.copy(
                    paywallShown = _analyticsState.value.paywallShown + 1
                )

            is AnalyticsEvent.UpgradeStarted ->
                _analyticsState.value.copy(
                    upgradeStarted = _analyticsState.value.upgradeStarted + 1
                )

            is AnalyticsEvent.UpgradeCompleted ->
                _analyticsState.value.copy(
                    upgradeCompleted = _analyticsState.value.upgradeCompleted + 1
                )

            is AnalyticsEvent.TrialStarted ->
                _analyticsState.value.copy(
                    trialStarted = _analyticsState.value.trialStarted + 1
                )

            is AnalyticsEvent.TrialConverted ->
                _analyticsState.value.copy(
                    trialConverted = _analyticsState.value.trialConverted + 1
                )

            is AnalyticsEvent.RetentionDay1 ->
                _analyticsState.value.copy(
                    retainedDay1 = _analyticsState.value.retainedDay1 + 1
                )

            is AnalyticsEvent.RetentionDay7 ->
                _analyticsState.value.copy(
                    retainedDay7 = _analyticsState.value.retainedDay7 + 1
                )
        }
    }
}
