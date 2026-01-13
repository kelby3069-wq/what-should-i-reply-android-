package com.replysense.app.domain.analysis

import com.replysense.app.model.AnalysisResult
import com.squareup.moshi.Moshi

class AnalysisResponseParser(
    moshi: Moshi
) {

    private val adapter = moshi.adapter(AnalysisResult::class.java)

    fun parse(rawText: String): AnalysisResult {
        return adapter.fromJson(rawText)
            ?: error("Analysis response did not conform to AnalysisResult contract")
    }
}
