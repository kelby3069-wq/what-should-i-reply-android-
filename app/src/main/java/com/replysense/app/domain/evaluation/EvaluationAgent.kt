package com.replysense.domain.evaluation

import com.replysense.domain.model.ContextState

interface EvaluationAgent {
    fun evaluate(state: ContextState): EvaluationScores
}
