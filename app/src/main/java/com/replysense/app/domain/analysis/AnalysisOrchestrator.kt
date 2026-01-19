package com.replysense.app.domain.analysis

import com.replysense.app.domain.agents.analysis.RiskAssessmentAgent
import com.replysense.app.domain.agents.analysis.SignalExtractionAgent
import com.replysense.app.domain.conversation.ConversationLineNormalizer
import com.replysense.app.domain.ocr.OcrLine

class AnalysisOrchestrator(
    private val signalAgent: SignalExtractionAgent,
    private val riskAgent: RiskAssessmentAgent,
    private val lineNormalizer: ConversationLineNormalizer
) {

    fun analyze(
        rawText: String,
        ocrLines: List<OcrLine>
    ): AnalysisResult {

        val signals = signalAgent.analyze(rawText)
        val risk = riskAgent.analyze(signals)
        val conversationLines = lineNormalizer.normalize(ocrLines)

        val bundle = buildBundle(
            rawText = rawText,
            agentTone = signals.emotionalTone,
            riskPercent = risk.percent,
            riskLabel = risk.label
        )

        // Keep the agent’s tone when it’s informative; otherwise use computed tone.
        val surfacedTone = signals.emotionalTone
            .takeIf { it.isNotBlank() && it.lowercase() !in setOf("neutral", "unknown", "unclear") }
            ?: bundle.computedTone

        return AnalysisResult(
            riskPercent = risk.percent,
            riskLabel = risk.label,
            confidenceScore = 75,
            simulatedOutcome = bundle.simulatedOutcome,
            signalSummary = surfacedTone,
            conversationLines = conversationLines,
            narrativeAnalysis = bundle.narrative,
            intentSummary = bundle.intentSummary,
            uncertaintyQuestions = bundle.uncertaintyQuestions,
            riskSummary = bundle.riskSummary,
            deEscalationTips = bundle.deEscalationTips
        )
    }

    private data class Bundle(
        val computedTone: String,
        val narrative: String,
        val intentSummary: String,
        val uncertaintyQuestions: List<String>,
        val riskSummary: String,
        val deEscalationTips: List<String>,
        val simulatedOutcome: String
    )

    /**
     * Deterministic premium surfacing bundle:
     * - narrative block (rich)
     * - intent hypothesis + clarifying questions
     * - power/risk summary + de-escalation guardrails
     * - outcome simulation (two-path)
     */
    private fun buildBundle(
        rawText: String,
        agentTone: String,
        riskPercent: Int,
        riskLabel: String
    ): Bundle {

        val t = rawText.lowercase()

        fun containsAny(list: List<String>) = list.any { it in t }
        fun countMatches(list: List<String>) = list.count { it in t }

        val profanityWords = listOf("fuck", "fucking", "shit", "bitch", "asshole", "dumb", "stupid")
        val accusationPhrases = listOf(
            "you always", "you never", "obsessed", "lying", "cheating", "sneaking",
            "i know you", "don't lie", "stop lying", "your bullshit"
        )
        val contemptMarkers = listOf("lol", "lmao", "whatever", "yeah right", "sure you did")
        val controlPhrases = listOf("be gone", "just go", "leave", "block", "don't talk to", "don't text")
        val carePhrases = listOf("drive safe", "be safe", "good night", "sweet dreams", "hug", "miss you", "i care", "love you")
        val repairPhrases = listOf("i'm sorry", "my bad", "i understand", "i hear you", "can we talk", "let's figure this out")
        val timePressurePhrases = listOf("where are you", "right now", "hurry", "minutes", "30 min", "i have to leave", "need it", "now")

        val hasProfanity = containsAny(profanityWords)
        val hasAccusation = containsAny(accusationPhrases)
        val hasContempt = containsAny(contemptMarkers)
        val hasControl = containsAny(controlPhrases)
        val hasCare = containsAny(carePhrases)
        val hasRepair = containsAny(repairPhrases)
        val hasTimePressure = containsAny(timePressurePhrases)

        val accusationCount = countMatches(accusationPhrases)
        val profanityCount = countMatches(profanityWords)

        val computedTone = when {
            hasProfanity || hasControl -> "Elevated (volatile)"
            hasAccusation || hasContempt -> "Tense (suspicious)"
            hasCare && hasTimePressure -> "Warm but strained"
            hasCare -> "Warm"
            hasRepair -> "Repair-oriented"
            else -> agentTone.takeIf { it.isNotBlank() } ?: "Unclear"
        }

        val intensity = when {
            riskPercent >= 70 || hasProfanity || hasControl -> "High"
            riskPercent >= 35 || hasAccusation || hasTimePressure || hasContempt -> "Moderate"
            else -> "Low"
        }

        val primaryEmotionalState = when {
            hasCare && (hasAccusation || hasControl) -> "Care mixed with distrust"
            hasAccusation || hasControl -> "Anger mixed with suspicion"
            hasCare -> "Care / warmth"
            hasRepair -> "Care with accountability"
            else -> "Unclear"
        }

        val trajectory = when {
            hasControl || hasProfanity -> "Escalating with emotional volatility"
            hasAccusation || hasContempt -> "Tension rising (trust is the pressure point)"
            hasCare && hasTimePressure -> "Warm but strained by logistics"
            hasCare || hasRepair -> "Stabilizing / reconnecting"
            else -> "Unclear / mixed"
        }

        // -----------------------------
        // Intent hypothesis (deterministic)
        // -----------------------------
        val intentSummary = buildString {
            val lines = mutableListOf<String>()

            when {
                hasAccusation && hasControl ->
                    lines += "The visible intent reads as pushing for a decisive outcome (leave/end/comply) under suspicion, rather than seeking mutual clarity."

                hasAccusation ->
                    lines += "The visible intent reads as seeking reassurance or accountability, but it’s being delivered through accusation rather than a direct request."

                hasTimePressure ->
                    lines += "The visible intent reads as logistical urgency (location/timing/coordination) competing with emotional tension."

                hasCare ->
                    lines += "The visible intent reads as maintaining connection and warmth, even if the exchange feels uneven."

                hasRepair ->
                    lines += "The visible intent reads as repair-oriented: understanding, resetting tone, or moving toward a calmer next step."
            }

            if (lines.isEmpty()) {
                lines += "The visible intent is unclear. The text doesn’t state a clean request, so interpretation depends heavily on missing context."
            }

            // Add a second sentence that gently labels uncertainty (voice-locked).
            lines += "Because this is only a screenshot slice, it’s possible the real intent is clearer earlier in the thread."

            append(lines.joinToString(separator = " "))
        }

        // -----------------------------
        // Clarifying questions (non-directive)
        // -----------------------------
        val uncertaintyQuestions = buildList {
            // Universal grounding
            add("What would feel most helpful right now — reassurance, a plan, or space?")
            add("What’s the one concrete thing you want from me in this moment?")

            if (hasAccusation) {
                add("When you say that, what specific thing are you referring to?")
                add("What would help you feel more certain about what’s going on?")
            }

            if (hasTimePressure) {
                add("What timing would actually work for you, and what’s the deadline?")
            }

            if (hasControl) {
                add("Are you asking for a boundary (space/no contact), or reacting out of anger in the moment?")
            }
        }.distinct().take(5)

        // -----------------------------
        // Risk / power dynamics summary
        // -----------------------------
        val riskSummary = buildString {
            val parts = mutableListOf<String>()

            when {
                hasControl ->
                    parts += "There are power-pressure signals here (directive language that pushes an outcome)."
                hasAccusation ->
                    parts += "Accusations raise escalation risk because they invite defense rather than resolution."
                hasTimePressure ->
                    parts += "Urgency compresses patience and increases misreads."
            }

            if (hasProfanity || hasContempt) {
                parts += "Tone hardening markers suggest the exchange could escalate quickly if either side argues intent."
            }

            if (parts.isEmpty()) {
                parts += "Power dynamics look mild in the visible slice; risk appears driven more by uncertainty than overt pressure."
            }

            parts += "A calm reset + one clarifying question typically reduces risk more than rebutting every detail."

            parts.joinToString(" ")
        }

        // -----------------------------
        // De-escalation guardrails (actionable, not prescriptive)
        // -----------------------------
        val deEscalationTips = buildList {
            add("Lead with one calm sentence that acknowledges emotion without admitting false claims.")
            add("Ask one clarifying question instead of arguing multiple points at once.")
            add("Keep it short; long explanations often read as defensive in tense exchanges.")

            if (hasAccusation) {
                add("Avoid counter-accusations; mirror the concern and request specifics.")
            }
            if (hasControl) {
                add("If the message is coercive, state a boundary: you’ll talk when it’s respectful and specific.")
            }
            if (hasTimePressure) {
                add("Separate logistics from emotion: confirm timing first, then address feelings.")
            }
            if (intensity == "High") {
                add("If you feel flooded, pause before responding; a delayed calm reply is usually safer than a fast reactive one.")
            }
        }.distinct().take(7)

        // -----------------------------
        // Two-path simulated outcome (still deterministic)
        // -----------------------------
        val simulatedOutcome = buildString {
            val riskTier = when {
                riskPercent >= 67 -> "higher"
                riskPercent >= 34 -> "moderate"
                else -> "lower"
            }

            appendLine("Likely paths (based on visible text):")
            appendLine()
            appendLine("• If you rebut/argue intent:")
            appendLine("  This tends to increase defensiveness and can push the exchange toward escalation ($riskTier risk).")
            appendLine()
            appendLine("• If you ground + clarify:")
            appendLine("  This tends to reduce heat and move the conversation toward specifics (often stabilizing within 1–3 messages).")
        }.trim()

        // -----------------------------
        // Narrative block (rich)
        // -----------------------------
        val narrative = buildString {
            appendLine("🧠 Emotional Read")
            appendLine()
            appendLine("Primary emotional state:")
            appendLine(primaryEmotionalState)
            appendLine()
            appendLine("Intensity:")
            appendLine(intensity)
            appendLine()
            appendLine("Overall tone:")
            appendLine("Computed tone: \"$computedTone\".")
            if (agentTone.isNotBlank()) {
                appendLine("Agent tone: \"$agentTone\". (May be coarse; missing context can change interpretation.)")
            }
            appendLine()
            appendLine("📉 Conversation Trajectory")
            appendLine()
            appendLine("Current trajectory:")
            appendLine(trajectory)
            appendLine()
            appendLine("This conversation doesn’t look “over,” but it does look sensitive to misinterpretation.")
            appendLine()
            appendLine("🧭 Summary (ReplySense Core Takeaway)")
            appendLine()
            appendLine("Risk: ${riskLabel.ifBlank { "Unlabeled" }} ($riskPercent%)")
            appendLine("Tone: $computedTone")
            appendLine("This snapshot suggests \"$primaryEmotionalState\" with $intensity intensity.")
            appendLine()
            if (accusationCount > 0 || profanityCount > 0) {
                appendLine("Evidence markers (from visible text):")
                if (accusationCount > 0) appendLine("• Accusation markers detected: $accusationCount")
                if (profanityCount > 0) appendLine("• Profanity markers detected: $profanityCount")
                appendLine()
            }
            appendLine("Note: This is based only on the visible text provided here; missing context can change interpretation.")
        }.trim()

        return Bundle(
            computedTone = computedTone,
            narrative = narrative,
            intentSummary = intentSummary,
            uncertaintyQuestions = uncertaintyQuestions,
            riskSummary = riskSummary,
            deEscalationTips = deEscalationTips,
            simulatedOutcome = simulatedOutcome
        )
    }
}
