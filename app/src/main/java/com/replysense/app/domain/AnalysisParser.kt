package com.replysense.app.domain

object AnalysisParser {

    fun analyze(conversation: String): String {
        val messages = conversation
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (messages.isEmpty()) return emptyAnalysis()

        val signals = SignalExtractor.extract(messages)
        val toneEligibility = ReplyToneEvaluator.evaluate(signals)
        val sexualContext = SexualContextGate.evaluate(messages, signals)
        val redFlags = RedFlagDetector.detect(messages)
        val responsibility = ResponsibilityAttributor.attribute(signals, redFlags)
        val recoverability = RecoverabilityEvaluator.evaluate(signals, redFlags)

        val overallRead = when (signals.momentum) {
            Momentum.DECLINING ->
                "This conversation is losing momentum and emotional balance."
            Momentum.IMPROVING ->
                "This conversation shows strengthening engagement and responsiveness."
            Momentum.STABLE ->
                "This conversation appears emotionally steady with no major shifts yet."
        }

        val whyFeelsOff = when {
            signals.momentum == Momentum.DECLINING ->
                "Response patterns shortened and emotional reciprocity decreased, suggesting reduced availability or interest."
            signals.emotionalLoad == EmotionalLoad.HIGH ->
                "Emotionally loaded language increased, which raises sensitivity and stakes in the exchange."
            else ->
                "Message pacing and tone remain mostly consistent so far."
        }

        val coachingInsight = when (signals.riskLevel) {
            RiskLevel.HIGH ->
                "De-escalate. Avoid emotional pressure or explanations. Give space and let the other party choose engagement."
            RiskLevel.MEDIUM ->
                "Match tone carefully. Reduce message length and observe whether effort is reciprocated."
            RiskLevel.LOW ->
                "Continue naturally while staying attentive to subtle changes in response energy."
        }

        val toneGuidance =
            "Recommended Reply Tone: ${toneEligibility.recommendedTone.name.lowercase().replaceFirstChar { it.uppercase() }}\n" +
                    "Allowed Tones: ${
                        toneEligibility.allowedTones.joinToString { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                    }"

        val sexualGuidance =
            "Sexual Context: ${sexualContext.level.name.lowercase().replaceFirstChar { it.uppercase() }}\n" +
                    "Guidance: ${sexualContext.reason}"

        val redFlagText =
            if (redFlags.isEmpty()) {
                "No significant red flags detected at this time."
            } else {
                redFlags.joinToString("\n") { rf ->
                    "• ${rf.description} (Evidence: ${rf.evidence})"
                }
            }

        val responsibilityText = buildString {
            appendLine("What You Did Well:")
            responsibility.userDidWell.forEach { appendLine("• $it") }
            appendLine()
            appendLine("What You Did Not Cause:")
            responsibility.userDidNotCause.forEach { appendLine("• $it") }
            appendLine()
            appendLine("Other Party Actions:")
            responsibility.otherPartyActions.forEach { appendLine("• $it") }
        }

        val decisionText = buildString {
            appendLine("Recoverability: ${recoverability.level.name.lowercase().replaceFirstChar { it.uppercase() }}")
            appendLine("Recommended Action: ${recoverability.decision.name.lowercase().replaceFirstChar { it.uppercase() }}")
            appendLine()
            appendLine(recoverability.rationale)
        }

        return buildString {
            appendLine("Overall Read:")
            appendLine(overallRead)
            appendLine()
            appendLine("Why This Feels Off:")
            appendLine(whyFeelsOff)
            appendLine()
            appendLine("Coaching Insight:")
            appendLine(coachingInsight)
            appendLine()
            appendLine("Reply Tone Guidance:")
            appendLine(toneGuidance)
            appendLine()
            appendLine("Sexual Context Guidance:")
            appendLine(sexualGuidance)
            appendLine()
            appendLine("Red Flags:")
            appendLine(redFlagText)
            appendLine()
            appendLine("Responsibility Check:")
            appendLine(responsibilityText)
            appendLine()
            appendLine("Decision Guidance:")
            appendLine(decisionText)
        }
    }

    private fun emptyAnalysis(): String {
        return """
            Overall Read:
            There is not enough conversational data to form a reliable emotional assessment.

            Why This Feels Off:
            The conversation is too brief or incomplete to identify patterns.

            Coaching Insight:
            Allow the interaction to develop before making adjustments.
        """.trimIndent()
    }
}
