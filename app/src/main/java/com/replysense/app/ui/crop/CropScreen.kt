package com.replysense.app.ui.crop

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.components.RSScaffold
import com.replysense.app.ui.components.RSSecondaryButton
import com.replysense.app.ui.components.RSPrimaryButton
import com.replysense.app.ui.theme.AppSpacing
import kotlin.math.roundToInt

/**
 * Premium crop screen shell:
 * - Clean top bar
 * - Bottom sticky action bar
 * - Cropper UI fills remaining space
 *
 * Returns crop rect in ORIGINAL bitmap pixels.
 */
@Composable
fun CropScreen(
    screenshot: Bitmap,
    onCancel: () -> Unit,
    onDone: (cropPx: android.graphics.Rect) -> Unit
) {
    var rect by remember { mutableStateOf(Rect(0.10f, 0.12f, 0.90f, 0.88f)) }

    RSScaffold(
        title = "Crop",
        navigationIcon = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Cropper area
            Box(modifier = Modifier.weight(1f)) {
                CropperUi(
                    bitmap = screenshot,
                    rect = rect,
                    onRectChange = { rect = it },
                    modifier = Modifier.fillMaxSize(),
                    showGrid = true
                )
            }

            // Bottom bar
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    RSSecondaryButton(
                        text = "Reset",
                        onClick = { rect = Rect(0.10f, 0.12f, 0.90f, 0.88f) },
                        modifier = Modifier.weight(1f)
                    )
                    RSPrimaryButton(
                        text = "Done",
                        onClick = { onDone(rectNormToBitmapPx(screenshot, rect)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun rectNormToBitmapPx(bitmap: Bitmap, n: Rect): android.graphics.Rect {
    val nn = Rect(
        n.left.coerceIn(0f, 1f),
        n.top.coerceIn(0f, 1f),
        n.right.coerceIn(0f, 1f),
        n.bottom.coerceIn(0f, 1f)
    )

    val left = (nn.left * bitmap.width).roundToInt().coerceIn(0, bitmap.width - 1)
    val top = (nn.top * bitmap.height).roundToInt().coerceIn(0, bitmap.height - 1)
    val right = (nn.right * bitmap.width).roundToInt().coerceIn(left + 1, bitmap.width)
    val bottom = (nn.bottom * bitmap.height).roundToInt().coerceIn(top + 1, bitmap.height)

    return android.graphics.Rect(left, top, right, bottom)
}
