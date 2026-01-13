package com.replysense.domain.orchestrator

import com.replysense.domain.agents.MicroAgent
import com.replysense.domain.agents.emotion.EmotionAgent
import com.replysense.domain.agents.intent.IntentAgent
import com.replysense.domain.agents.power.PowerDynamicsAgent
import com.replysense.domain.agents.risk.RiskAgent
import com.replysense.domain.evaluation.BasicEvaluationAgent
import com.replysense.domain.evaluation.EvaluationAgent
import com.replysense.domain.evaluation.EvaluationScores
import com.replysense.domain.memory.AgentMemory
import com.replysense.domain.model.ContextState
import com.replysense.domain.model.UserMessage

/**
 * ReplySenseOrchestrator
 *
 * Ordered, deterministic micro-agent pipeline.
 *
 * Current pipeline:
 * 1. IntentAgent
 * 2. EmotionAgent
 * 3. PowerDynamicsAgent
 * 4. RiskAgent
 */
class ReplySenseOrchestrator(
    private val memory: AgentMemory = AgentMemory(),
    private val evaluator: EvaluationAgent = BasicEvaluationAgent()
) {

    private val agents: List<MicroAgent<ContextState, ContextState>> =
        listOf(
            IntentAgent(),
            EmotionAgent(),
            PowerDynamicsAgent(),
            RiskAgent()
        )

    fun analyze(input: UserMessage): AnalysisResult {
        var state = ContextState(
            text = input.rawText
        )

        agents.forEach { agent ->
            state = agent.run(state, memory)
        }

        val evaluation: EvaluationScores =
            evaluator.evaluate(state)

        return AnalysisResult(
            state = state,
            evaluation = evaluation
        )
    }
}
