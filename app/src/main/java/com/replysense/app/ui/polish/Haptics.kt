package com.replysense.app.ui.polish

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

object AppHaptics {
    @Composable fun tap() = LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    @Composable fun confirm() = LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)
}
