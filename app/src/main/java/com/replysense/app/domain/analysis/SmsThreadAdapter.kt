package com.replysense.app.domain.analysis

class SmsThreadAdapter {

    fun normalize(rawMessages: List<String>): List<String> {
        return rawMessages
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
