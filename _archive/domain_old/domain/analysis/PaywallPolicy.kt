package com.replysense.app.domain.analysis

class PaywallPolicy {

    fun requiresUnlock(result: AnalysisResult): Boolean {
        return result.requiresDeepAnalysis
    }
}
