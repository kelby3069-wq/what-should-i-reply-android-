package com.replysense.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.replysense.app.ui.theme.Spacing

@Composable
fun VerticalSpace(size: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.height(size))
}

object ComponentSpacing {
    val xs = Spacing.inline
    val sm = Spacing.paragraph
    val md = Spacing.card
    val lg = Spacing.section
}
