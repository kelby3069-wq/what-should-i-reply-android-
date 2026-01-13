package com.replysense.app.model

object AnalysisComposer {

    fun composeExplanation(
        signals: Set<EmotionalSignal>,
        trajectory: ConversationTrajectory
    ): String {
        return """
This conversation feels off because it carries emotional intimacy without a clear container.

There is warmth, care, and continued engagement, but the signals suggest hesitation rather than forward momentum. Emotional presence exists, yet the interaction has stalled instead of progressing.

This pattern usually reflects caution or uncertainty rather than rejection. Nothing appears broken — the connection is hovering without grounding, which creates low-level tension for both people.
        """.trimIndent()
    }
}
