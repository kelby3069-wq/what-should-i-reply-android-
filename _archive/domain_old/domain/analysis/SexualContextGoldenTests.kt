package com.replysense.app.analysis

import com.replysense.app.domain.analysis.SexualContext
import com.replysense.app.domain.analysis.SexualContextDetector
import org.junit.Assert.assertEquals
import org.junit.Test

class SexualContextGoldenTests {

    private val detector = SexualContextDetector()

    @Test
    fun pressure_disables_sexual_context() {
        val messages = listOf(
            "come on you know you want to",
            "haha idk",
            "stop playing just say yes",
            "why are you being like this"
        )

        val context = detector.detect(messages)

        assertEquals(SexualContext.PRESSURE_OR_COERCION, context)
    }

    @Test
    fun light_flirt_is_detected() {
        val messages = listOf(
            "you’re trouble 😏",
            "lol what",
            "just saying"
        )

        val context = detector.detect(messages)

        assertEquals(SexualContext.FLIRTATIOUS, context)
    }
}
