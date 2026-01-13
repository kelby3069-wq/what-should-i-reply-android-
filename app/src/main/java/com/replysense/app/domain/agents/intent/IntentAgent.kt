package com.replysense.domain.agents.intent

import com.replysense.domain.agents.MicroAgent
import com.replysense.domain.memory.AgentMemory
import com.replysense.domain.model.ContextState

/**
 * IntentAgent
 *
 * Determines what the other party is ACTUALLY doing,
 * not what they explicitly say.
 *
 * Deterministic v1:
 * - keyword + structural cues
 * - conservative confidence assignment
 * - never overwrites existing intent
 */
class IntentAgent : MicroAgent<ContextState, ContextState> {

    override fun run(
        input: ContextState,
        memory: AgentMemory
    ): ContextState {

        // Never override an existing intent
        if (input.detectedIntent != null) return input

        val text = input.text.lowercase()

        val (intent, confidence) = when {
            containsAny(text, listOf("why", "how", "what do you mean", "?")) ->
                IntentType.INFORMATION_SEEKING to 0.75f

            containsAny(text, listOf("are you sure", "do you still", "just checking")) ->
                IntentType.REASSURANCE_SEEKING to 0.70f

            containsAny(text, listOf("you didn't", "you never", "you always")) ->
                IntentType.BOUNDARY_TESTING to 0.65f

            containsAny(text, listOf("i need you to", "you should", "you have to")) ->
                IntentType.CONTROL_ATTEMPT to 0.70f

            containsAny(text, listOf("miss you", "thinking of you", "just wanted to say")) ->
                IntentType.AFFILIATION to 0.80f

            containsAny(text, listOf("fine", "whatever", "ok.")) ->
                IntentType.DISENGAGEMENT to 0.60f

            else ->
                IntentType.AMBIGUOUS to 0.40f
        }

        return input.copy(
            detectedIntent = IntentResult(
                primaryIntent = intent,
                confidence = confidence
            )
        )
    }

    private fun containsAny(text: String, needles: List<String>): Boolean {
        return needles.any { text.contains(it) }
    }
}
