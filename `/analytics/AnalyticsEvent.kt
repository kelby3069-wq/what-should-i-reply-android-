package com.replysense.app.analytics

import com.replysense.app.domain.analysis.SexualContext
import com.replysense.app.domain.reply.ReplyTone

sealed class AnalyticsEvent {

    object AnalysisStarted : AnalyticsEvent()
    object AnalysisCompleted : AnalyticsEvent()
    object DeepAnalysisUnlocked : AnalyticsEvent()

    data class SexualReplyHelpOpened(
        val context: SexualContext
    ) : AnalyticsEvent()

    data class ToneSelected(
        val tone: ReplyTone
    ) : AnalyticsEvent()
}
