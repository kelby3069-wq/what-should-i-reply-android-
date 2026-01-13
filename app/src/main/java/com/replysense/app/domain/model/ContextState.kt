package com.replysense.domain.model

import com.replysense.domain.agents.emotion.EmotionalResult
import com.replysense.domain.agents.intent.IntentResult
import com.replysense.domain.agents.outcome.OutcomeSimulation
import com.replysense.domain.agents.power.PowerResult
import com.replysense.domain.agents.response.ProposedReply
import com.replysense.domain.agents.risk.RiskAssessment

/**
 * Canonical conversation state.
 *
 * This is the SINGLE source of truth for:
 * - meaning
 * - inferred intent
 * - emotional signals
 * - power dynamics
 * - risk
 * - simulated outcomes
 *
 * UI must adapt FROM this, never redefine it.
 */
data class ContextState(
    val text: String,

    val detectedIntent: IntentResult? = null,
    val emotionalSignals: EmotionalResult? = null,
    val powerDynamics: PowerResult? = null,
    val riskAssessment: RiskAssessment? = null,
    val simulatedOutcome: OutcomeSimulation? = null,
    val proposedReply: ProposedReply? = null
)
