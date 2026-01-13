package com.replysense.app.ui.settings

import androidx.lifecycle.ViewModel
import com.replysense.app.domain.billing.BillingClientManager

class PaywallViewModel(
    private val billingManager: BillingClientManager = BillingClientManager()
) : ViewModel() {

    fun isPro(): Boolean = billingManager.isProUnlocked()
}
