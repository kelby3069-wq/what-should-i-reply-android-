package com.replysense.app.domain.emotion

class EmotionScorerTest {

    @Test
    fun explicitContent_forcesFirmTone() {
        val result = EmotionScorer.score("send nudes")

        assertEquals(AttractionSignal.EXPLICIT_CONTENT, result.attractionSignal)
        assertTrue(result.allowedReplyTones.contains(ReplyTone.FIRM))
        assertEquals(1, result.allowedReplyTones.size)
    }

    @Test
    fun playfulFlirt_allowsPlayfulWhenSafe() {
        val result = EmotionScorer.score("you’re cute 😏")

        assertEquals(AttractionSignal.PLAYFUL_FLIRTATION, result.attractionSignal)
        assertTrue(result.allowedReplyTones.contains(ReplyTone.PLAYFUL))
    }

    @Test
    fun anxiety_blocksPlayfulEvenWithFlirt() {
        val result = EmotionScorer.score("i’m worried but you’re cute 😏")

        assertEquals(Emotion.ANXIETY, result.primaryEmotion)
        assertFalse(result.allowedReplyTones.contains(ReplyTone.PLAYFUL))
    }
}