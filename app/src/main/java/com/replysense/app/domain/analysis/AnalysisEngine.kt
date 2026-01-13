package com.replysense.app.domain.analysis

import com.replysense.app.model.*
import java.time.Duration
import java.time.Instant

/**
 * Phase B AnalysisEngine
 * Deterministic, offline-first
 * Strictly maps to Phase-A AnalysisResult (LOCKED)
 */
class AnalysisEngine {

    data class NormalizedMessage(
        val index: Int,
        val author: Author,
        val text: String,
        val timestamp: Instant? = null
    )

    enum class Author { USER, OTHER }

    fun analyze(messages: List<NormalizedMessage>): AnalysisResult {
        require(messages.isNotEmpty())

        val metrics = computeMetrics(messages)

        return AnalysisResult(
            overallRead = overallRead(metrics),
            emotionalState = emotionalState(metrics),
            trajectory = trajectory(metrics),
            keyMoments = keyMoments(messages),
            redFlags = redFlags(metrics),
            responsibilityCheck = responsibilityCheck(metrics),
            recoverability = recoverability(metrics),
            coaching = coaching(metrics)
        )
    }

    // ───────────────────────── Metrics ─────────────────────────

    private data class Metrics(
        val userCount: Int,
        val otherCount: Int,
        val avgGapMin: Double,
        val sentiment: Int
    )

    private fun computeMetrics(m: List<NormalizedMessage>): Metrics {
        val user = m.count { it.author == Author.USER }
        val other = m.size - user
        val gaps = gaps(m)

        return Metrics(
            userCount = user,
            otherCount = other,
            avgGapMin = if (gaps.isEmpty()) 0.0 else gaps.map { it.toMinutes() }.average(),
            sentiment = sentiment(m)
        )
    }

    private fun gaps(m: List<NormalizedMessage>): List<Duration> =
        m.zipWithNextNotNull { a, b ->
            if (a.timestamp != null && b.timestamp != null)
                Duration.between(a.timestamp, b.timestamp)
            else null
        }

    private fun sentiment(m: List<NormalizedMessage>): Int {
        var score = 0
        for (msg in m) {
            val t = msg.text.lowercase()
            if ("!" in t) score++
            if ("?" in t) score++
            if (t == "ok" || t == "k" || "fine" in t) score--
        }
        return score.coerceIn(-3, 3)
    }

    // ───────────────────── Phase-A Mapping ─────────────────────

    private fun overallRead(m: Metrics): String =
        when {
            m.sentiment >= 1 -> "Overall tone is open and emotionally positive."
            m.sentiment <= -1 -> "Overall tone feels guarded and uncertain."
            else -> "Overall tone is neutral with mixed signals."
        }

    private fun emotionalState(m: Metrics): EmotionalStateResult =
        EmotionalStateResult(
            user = EmotionalRead(
                primary = if (m.userCount >= m.otherCount) "Engaged" else "Measured",
                secondary = listOf("Curious"),
                intensity = "Moderate"
            ),
            other = EmotionalRead(
                primary = if (m.sentiment >= 0) "Responsive" else "Reserved",
                secondary = listOf("Cautious"),
                intensity = "Low"
            )
        )

    private fun trajectory(m: Metrics): String =
        if (m.avgGapMin > 120)
            "Momentum has slowed over time."
        else
            "Momentum has remained relatively stable."

    private fun keyMoments(m: List<NormalizedMessage>): List<KeyMoment> =
        listOf(
            KeyMoment(
                messageIndex = m.first().index,
                excerpt = m.first().text,
                explanation = "This sets the initial emotional tone of the exchange."
            ),
            KeyMoment(
                messageIndex = m.last().index,
                excerpt = m.last().text,
                explanation = "This reflects the current state of engagement."
            )
        )

    private fun redFlags(m: Metrics): List<RedFlagResult> =
        if (m.avgGapMin > 240)
            listOf(
                RedFlagResult(
                    title = "Extended response gap",
                    explanation = "Long delays suggest a drop in priority or emotional availability.",
                    severity = "Medium",
                    evidenceMessageIndices = listOf(m.userCount + m.otherCount - 1)
                )
            )
        else emptyList()

    private fun responsibilityCheck(m: Metrics): ResponsibilityCheck =
        ResponsibilityCheck(
            userDidWell = listOf("Maintained respectful tone"),
            userDidNotCause = listOf("Did not create the response delay"),
            otherPartyActions = listOf("Reduced response frequency")
        )

    private fun recoverability(m: Metrics): String =
        if (m.avgGapMin < 180)
            "The conversation is likely recoverable with adjusted pacing."
        else
            "Recoverability is uncertain without renewed engagement."

    private fun coaching(m: Metrics): CoachingResult =
        CoachingResult(
            summary = "Slow down and allow space for reciprocal effort.",
            suggestedReply = null,
            boundaryGuidance = "Avoid chasing or over-explaining."
        )

    // ───────────────────────── Helpers ─────────────────────────

    private fun <T> List<T>.zipWithNextNotNull(
        block: (T, T) -> Duration?
    ): List<Duration> {
        val out = mutableListOf<Duration>()
        for (i in 0 until size - 1) {
            block(this[i], this[i + 1])?.let(out::add)
        }
        return out
    }
}
