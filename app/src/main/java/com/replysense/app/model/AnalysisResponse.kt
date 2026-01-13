package com.replysense.app.model

data class AnalysisResponse(
    val reassurance: String,
    val explanation: List<Explanation>,
    val coaching: Coaching
) {
    companion object {
        fun mock(): AnalysisResponse {
            return AnalysisResponse(
                reassurance = "It’s understandable to want clarity here. Nothing in this exchange suggests you did something wrong.",
                explanation = listOf(
                    Explanation(
                        possibleSignal = "There are moments where reassurance seems to be sought more than information.",
                        uncertainty = "It’s unclear whether this comes from anxiety or a desire to stay connected.",
                        whyItMatters = "Repeated reassurance-seeking can sometimes add pressure, even when closeness is the goal."
                    )
                ),
                coaching = Coaching(
                    suggestedReplies = listOf(
                        SuggestedReply(
                            intent = "clarify",
                            text = "I want to make sure I’m understanding you correctly."
                        )
                    ),
                    reflectionPrompt = "What were you hoping they would understand here?",
                    avoidSaying = listOf(
                        "Did I do something wrong?",
                        "Are you upset with me?"
                    )
                )
            )
        }
    }
}

data class Explanation(
    val possibleSignal: String,
    val uncertainty: String,
    val whyItMatters: String
)

data class Coaching(
    val suggestedReplies: List<SuggestedReply>,
    val reflectionPrompt: String?,
    val avoidSaying: List<String>?
)

data class SuggestedReply(
    val intent: String,
    val text: String
)
