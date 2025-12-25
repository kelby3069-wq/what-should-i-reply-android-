package com.replysense.app.ui.crop

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

data class CropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@Composable
fun CropperUi(
    bitmap: Bitmap,
    cropRect: CropRect,
    onCropRectChange: (CropRect) -> Unit
) {
    var dragStart by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragStart = it },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        val dx = dragAmount.x / size.width
                        val dy = dragAmount.y / size.height

                        onCropRectChange(
                            cropRect.copy(
                                left = (cropRect.left + dx).coerceIn(0f, 0.8f),
                                top = (cropRect.top + dy).coerceIn(0f, 0.8f),
                                right = (cropRect.right + dx).coerceIn(0.2f, 1f),
                                bottom = (cropRect.bottom + dy).coerceIn(0.2f, 1f)
                            )
                        )
                    }
                )
            }
    ) {
        // Draw image
        drawImage(
            image = bitmap.asImageBitmap(),
            topLeft = Offset.Zero
        )

        val rect = Rect(
            left = cropRect.left * size.width,
            top = cropRect.top * size.height,
            right = cropRect.right * size.width,
            bottom = cropRect.bottom * size.height
        )

        // Dark overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )

        // Clear crop window
        drawRect(
            color = Color.Transparent,
            topLeft = rect.topLeft,
            size = rect.size,
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Crop border
        drawRect(
            color = Color.White,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
