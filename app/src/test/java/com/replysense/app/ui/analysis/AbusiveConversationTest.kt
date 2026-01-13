package com.replysense.app.ui.analysis

import com.replysense.app.domain.emotion.BehaviorAnalyzer
import com.replysense.app.domain.emotion.EmotionScorer
import com.replysense.app.model.ConversationTurn

class AbusiveConversationTest {

    @Test
    fun abusiveConversation_isClassifiedAsAbusive() {

        val convo = listOf(
            ConversationTurn(false, "I hate who you are."),
            ConversationTurn(false, "You’ll NEVER be different."),
            ConversationTurn(false, "You should go sit in prison.")
        )

        val score = EmotionScorer.score(convo)
        val flags = BehaviorAnalyzer.detect(convo)
        val severity =
            ConversationSafetyClassifier.classify(score, flags)

        assertEquals(EmotionSeverity.ABUSIVE, severity)
    }
}