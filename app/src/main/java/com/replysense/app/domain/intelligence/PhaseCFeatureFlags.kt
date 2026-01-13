package com.replysense.app.domain.intelligence

/**
 * Centralized feature flags for Phase C.
 *
 * Defaults:
 * - Phase C OFF
 * - Phase B remains canonical
 *
 * This file is the ONLY place Phase C is enabled.
 */
object PhaseCFeatureFlags {

    /**
     * Master kill switch for Phase C intelligence.
     *
     * true  → Phase C allowed
     * false → Always use Phase B
     */
    const val ENABLE_PHASE_C = false
}
