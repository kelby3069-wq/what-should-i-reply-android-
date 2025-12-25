package com.replysense.app.ui.crop

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min

@Composable
fun CropperUi(
    bitmap: Bitmap,
    onCropRectChanged: (Rect) -> Unit
) {
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    var cropRect by remember {
        mutableStateOf(
            Rect(200f, 200f, 800f, 800f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        val newRect = Rect(
                            left = cropRect.left + dragAmount.x,
                            top = cropRect.top + dragAmount.y,
                            right = cropRect.right + dragAmount.x,
                            bottom = cropRect.bottom + dragAmount.y
                        )

                        cropRect = newRect
                        onCropRectChanged(newRect)
                    }
                }
        ) {
            imageSize = IntSize(size.width.toInt(), size.height.toInt())

            // Draw screenshot bitmap
            drawImage(
                image = bitmap.asImageBitmap(),
                dstSize = imageSize
            )

            // Dim outside crop
            drawRect(
                color = Color.Black.copy(alpha = 0.55f)
            )

            // Clear crop window
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = cropRect.size,
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            // Crop border
            drawRect(
                color = Color.White,
                topLeft = Offset(cropRect.left, cropRect.top),
                size = cropRect.size,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
    }
}
