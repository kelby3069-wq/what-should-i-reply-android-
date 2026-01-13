package com.replysense.app.data

import com.replysense.app.model.*

object AnalysisResponseParser {

    fun parse(text: String): AnalysisResponse {

        fun section(name: String): String =
            text.substringAfter("$name:", "")
                .substringBefore("\n\n")
                .trim()

        val reassurance = section("Reassurance")

        val signalBlock = section("Signals")
        val possibleSignal =
            signalBlock.substringAfter("Possible signal:").substringBefore("\n").trim()
        val uncertainty =
            signalBlock.substringAfter("Uncertainty:").substringBefore("\n").trim()
        val whyItMatters =
            signalBlock.substringAfter("Why it matters:").trim()

        val coachingBlock = section("Coaching")

        val replyLine =
            coachingBlock.substringAfter("Suggested reply").substringAfter("\n").trim()
        val intent =
            when {
                replyLine.startsWith("clarify", true) -> "clarify"
                replyLine.startsWith("reassure", true) -> "reassure"
                replyLine.startsWith("maintain", true) -> "maintain_momentum"
                else -> "pause"
            }

        val replyText =
            replyLine.substringAfter(">").trim()

        val reflectionPrompt =
            coachingBlock.substringAfter("Reflection prompt:").substringBefore("\n").trim()

        val avoid =
            coachingBlock.substringAfter("What to avoid saying:")
                .lines()
                .map { it.trim().removePrefix("-") }
                .filter { it.isNotBlank() }

        return AnalysisResponse(
            reassurance = reassurance,
            explanation = listOf(
                Explanation(
                    possibleSignal = possibleSignal,
                    uncertainty = uncertainty,
                    whyItMatters = whyItMatters
                )
            ),
            coaching = Coaching(
                suggestedReplies = listOf(
                    SuggestedReply(
                        intent = intent,
                        text = replyText
                    )
                ),
                reflectionPrompt = reflectionPrompt,
                avoidSaying = avoid
            )
        )
    }
}
