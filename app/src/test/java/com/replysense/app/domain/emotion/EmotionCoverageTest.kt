package com.replysense.app.domain.emotion

class EmotionCoverageTest {

    @Test
    fun anger_blocks_playful_even_with_flirt() {
        val result = EmotionScorer.score("I’m pissed off 😏")

        assertEquals(Emotion.ANGER, result.primaryEmotion)
        assertFalse(result.allowedReplyTones.contains(ReplyTone.PLAYFUL))
    }

    @Test
    fun anxiety_prioritizes_reassurance() {
        val result = EmotionScorer.score("I’m not sure about this…")

        assertEquals(Emotion.ANXIETY, result.primaryEmotion)
        assertTrue(result.allowedReplyTones.contains(ReplyTone.WARM))
        assertFalse(result.allowedReplyTones.contains(ReplyTone.PLAYFUL))
    }

    @Test
    fun sadness_blocks_playful() {
        val result = EmotionScorer.score("That really hurt")

        assertEquals(Emotion.SADNESS, result.primaryEmotion)
        assertFalse(result.allowedReplyTones.contains(ReplyTone.PLAYFUL))
    }

    @Test
    fun neutral_allows_safe_neutral_only() {
        val result = EmotionScorer.score("Okay")

        assertEquals(Emotion.NEUTRAL, result.primaryEmotion)
        assertTrue(result.allowedReplyTones.contains(ReplyTone.NEUTRAL))
    }
}