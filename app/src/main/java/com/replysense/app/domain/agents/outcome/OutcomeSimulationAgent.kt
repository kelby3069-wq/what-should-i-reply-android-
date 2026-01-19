package com.replysense.app.domain.agents.outcome

import com.replysense.app.domain.agents.MicroAgent
import com.replysense.app.domain.memory.AgentMemory
import com.replysense.app.domain.model.ContextState
import com.replysense.app.domain.model.SimulatedOutcome

class OutcomeSimulationAgent : MicroAgent<ContextState, ContextState> {

    override fun run(input: ContextState, memory: AgentMemory): ContextState {
        val summary =
            if (input.emotionalSignals.isNotEmpty())
                "Emotional tension may affect how the message is received."
            else
                "The exchange appears relatively stable."

        return input.copy(
            simulatedOutcome = SimulatedOutcome(summary)
        )
    }
}
