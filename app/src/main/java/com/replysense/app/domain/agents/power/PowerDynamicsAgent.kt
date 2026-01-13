package com.replysense.domain.agents.power

import com.replysense.domain.agents.MicroAgent
import com.replysense.domain.memory.AgentMemory
import com.replysense.domain.model.ContextState

/**
 * PowerDynamicsAgent
 *
 * Infers leverage and asymmetry in the interaction.
 *
 * v1 signals:
 * - directives vs questions
 * - dependency language
 * - withdrawal / disengagement cues
 *
 * Never overwrites existing power dynamics.
 */
class PowerDynamicsAgent : MicroAgent<ContextState, ContextState> {

    override fun run(
        input: ContextState,
        memory: AgentMemory
    ): ContextState {

        if (input.powerDynamics != null) return input

        val text = input.text.lowercase()

        var userLeverage = 0.5f
        var otherLeverage = 0.5f
        var type = "balanced"

        if (containsAny(text, listOf("you need to", "you have to", "i expect"))) {
            otherLeverage += 0.2f
            type = "directive_pressure"
        }

        if (containsAny(text, listOf("can you", "would you", "is it okay"))) {
            userLeverage += 0.15f
            type = "requesting"
        }

        if (containsAny(text, listOf("fine", "whatever", "do what you want"))) {
            otherLeverage += 0.25f
            type = "withdrawal_control"
        }

        if (containsAny(text, listOf("i just wanted", "no worries", "up to you"))) {
            userLeverage += 0.2f
            type = "deference"
        }

        return input.copy(
            powerDynamics = PowerResult(
                userLeverage = userLeverage.coerceIn(0f, 1f),
                otherPartyLeverage = otherLeverage.coerceIn(0f, 1f),
                dynamicType = type
            )
        )
    }

    private fun containsAny(text: String, needles: List<String>): Boolean {
        return needles.any { text.contains(it) }
    }
}
