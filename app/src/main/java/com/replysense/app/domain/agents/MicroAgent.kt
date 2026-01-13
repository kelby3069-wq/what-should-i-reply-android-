package com.replysense.domain.agents

import com.replysense.domain.memory.AgentMemory

interface MicroAgent<I, O> {
    fun run(input: I, memory: AgentMemory): O
}
