package com.replysense.app.domain.analysis

import com.replysense.app.data.sms.SmsMessage
import com.replysense.app.model.ConversationDynamics
import com.replysense.app.model.SpeakerMetrics
import kotlin.math.max

class SpeakerAnalyzer {

    fun analyze(messages: List<SmsMessage>): ConversationDynamics {
        if (messages.size < 2) {
            return emptyDynamics(messages)
        }

        var meCount = 0
        var themCount = 0
        var meInitiations = 0
        var themInitiations = 0

        var meResponseTotal = 0L
        var themResponseTotal = 0L
        var meResponses = 0
        var themResponses = 0

        var longestSilence = 0L
        val momentumDrops = mutableListOf<Int>()

        for (i in 1 until messages.size) {
            val prev = messages[i - 1]
            val current = messages[i]

            val gapSeconds = (current.timestamp - prev.timestamp) / 1000
            longestSilence = max(longestSilence, gapSeconds)

            if (gapSeconds > 6 * 60 * 60) { // 6 hours
                momentumDrops.add(i)
            }

            if (current.isFromUser != prev.isFromUser) {
                if (current.isFromUser) {
                    meResponses++
                    meResponseTotal += gapSeconds
                } else {
                    themResponses++
                    themResponseTotal += gapSeconds
                }
            } else {
                if (current.isFromUser) meInitiations++
                else themInitiations++
            }

            if (current.isFromUser) meCount++ else themCount++
        }

        val meMetrics = SpeakerMetrics(
            averageResponseSeconds = safeAvg(meResponseTotal, meResponses),
            longestSilenceSeconds = longestSilence,
            messageCount = meCount,
            initiations = meInitiations
        )

        val themMetrics = SpeakerMetrics(
            averageResponseSeconds = safeAvg(themResponseTotal, themResponses),
            longestSilenceSeconds = longestSilence,
            messageCount = themCount,
            initiations = themInitiations
        )

        val totalMessages = meCount + themCount
        val dominance = if (totalMessages == 0) 0f else meCount.toFloat() / totalMessages

        return ConversationDynamics(
            me = meMetrics,
            them = themMetrics,
            dominanceRatio = dominance,
            momentumDropPoints = momentumDrops
        )
    }

    private fun safeAvg(total: Long, count: Int): Long =
        if (count == 0) 0 else total / count

    private fun emptyDynamics(messages: List<SmsMessage>): ConversationDynamics {
        return ConversationDynamics(
            me = SpeakerMetrics(0, 0, messages.count { it.isFromUser }, 0),
            them = SpeakerMetrics(0, 0, messages.count { !it.isFromUser }, 0),
            dominanceRatio = 0.5f,
            momentumDropPoints = emptyList()
        )
    }
}
