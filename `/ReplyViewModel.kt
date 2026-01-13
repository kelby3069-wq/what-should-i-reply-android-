package com.replysense.app.viewmodel

import androidx.lifecycle.ViewModel
import com.replysense.app.analytics.AnalyticsEvent
import com.replysense.app.analytics.AnalyticsTracker
import com.replysense.app.domain.analysis.SexualContext
import com.replysense.app.domain.reply.ReplyStyleRenderer
import com.replysense.app.domain.reply.SexualReplyStyle
import com.replysense.app.domain.voice.UserVoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReplyViewModel(
    private val analytics: AnalyticsTracker = AnalyticsTracker(),
    private val renderer: ReplyStyleRenderer = ReplyStyleRenderer()
) : ViewModel() {

    private val _draft = MutableStateFlow<String?>(null)
    val draft: StateFlow<String?> = _draft

    fun onHelpMeReply(context: SexualContext) {
        analytics.track(AnalyticsEvent.SexualReplyHelpOpened(context))
    }

    fun onStyleSelected(
        style: SexualReplyStyle,
        voice: UserVoice
    ) {
        val reply = renderer.render(style, voice)
        _draft.value = reply
        analytics.track(AnalyticsEvent.ToneSelected(renderer
            .run { ReplyToneMapper().fromSexualStyle(style) }))
    }
}
