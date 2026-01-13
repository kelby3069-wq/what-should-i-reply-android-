package com.replysense.app.domain.emotion

enum class ConfidenceLevel {
    HIGH,       // ≥ 0.7
    MEDIUM,     // 0.4 – 0.69
    LOW         // < 0.4
}
