package com.replysense.app.domain.analysis

import android.graphics.Bitmap

/**
 * Canonical input object for Phase C analysis.
 * This is the ONLY request type the AnalysisEngine accepts.
 */
data class AnalysisRequest(
    val rawText: String,
    val screenshot: Bitmap? = null
)
