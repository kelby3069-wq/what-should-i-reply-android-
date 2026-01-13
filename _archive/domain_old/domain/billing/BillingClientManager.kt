package com.replysense.app.domain.billing

class BillingClientManager {

    fun isProUnlocked(): Boolean {
        // v1 stub – replaced when Play Billing is enabled
        return false
    }

    fun launchPurchaseFlow(onResult: (Boolean) -> Unit) {
        // v1 stub – always fail safely
        onResult(false)
    }
}
