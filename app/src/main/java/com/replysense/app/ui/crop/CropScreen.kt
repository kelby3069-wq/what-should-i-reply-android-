package com.replysense.app.ui.crop

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    screenshot: Bitmap,
    onConfirmCrop: (Rect) -> Unit,
    onCancel: () -> Unit
) {
    var cropRect by remember { mutableStateOf<Rect?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crop Screenshot") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(
                        enabled = cropRect != null,
                        onClick = {
                            cropRect?.let(onConfirmCrop)
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CropperUi(
                bitmap = screenshot,
                onCropRectChanged = { rect ->
                    cropRect = rect
                }
            )
        }
    }
}
