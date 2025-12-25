package com.replysense.app.ui.crop

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onCropped: (Bitmap) -> Unit,
) {
    var cropRequestKey by remember { mutableIntStateOf(0) }
    var latestCropped by remember { mutableStateOf<Bitmap?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop") },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Trigger crop generation via CropperUi callback state
                            cropRequestKey++
                        }
                    ) { Text("Done") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
        ) {
            CropperUi(
                bitmap = bitmap,
                requestCropKey = cropRequestKey,
                onCropped = { out ->
                    latestCropped = out
                    onCropped(out)
                }
            )

            // Optional: subtle helper text
            AssistChip(
                onClick = {},
                label = { Text("Drag corners to resize • Drag inside to move") },
                enabled = false,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}
