package com.replysense.app.analysis

import com.replysense.app.domain.analysis.*
import com.replysense.app.domain.reply.ReplyTone
import org.junit.Assert.*
import org.junit.Test

class PaywallAndSafetyGoldenTests {

    private val analyzer = ConversationAnalyzer()

    @Test
    fun analysis_requires_deep_unlock() {
        val result = analyzer.analyze(
            listOf("hey", "what’s going on?")
        )

        assertTrue(result.requiresDeepAnalysis)
    }

    @Test
    fun pressure_context_forces_protective_tone() {
        val result = analyzer.analyze(
            listOf(
                "come on just say yes",
                "why won’t you answer",
                "after everything we did"
            )
        )

        assertEquals(SexualContext.PRESSURE_OR_COERCION, result.sexualContext)
        assertFalse(result.allowSexualReplyHelp)
        assertEquals(ReplyTone.PROTECTIVE, result.enforcedReplyTone)
    }
}
