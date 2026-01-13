package com.replysense.app.ui.analysis

import com.replysense.app.domain.emotion.BehaviorAnalyzer
import com.replysense.app.domain.emotion.EmotionScorer
import com.replysense.app.model.ConversationTurn

class FriendlyConversationTest {

    @Test
    fun friendlyConversation_isSafeAndWarm() {

        val convo = listOf(
            ConversationTurn(true, "You guys like doing that kind of stuff?"),
            ConversationTurn(false, "Yes for sure. I want to hike but haven’t done it"),
            ConversationTurn(false, "I need to text you earlier lol"),
            ConversationTurn(true, "yeah that’s the best time 😁")
        )

        val score = EmotionScorer.score(convo)
        val flags = BehaviorAnalyzer.detect(convo)
        val severity =
            ConversationSafetyClassifier.classify(score, flags)

        assertEquals(EmotionSeverity.SAFE, severity)
    }
}