package com.replysense.app.ui.crop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.theme.AppSpacing

/**
 * Premium Crop Screen (visual-first).
 *
 * You provide:
 * - imageBitmap (the screenshot)
 * - callbacks for Cancel / Reset / Done
 *
 * Crop rect is normalized (0..1) relative to the displayed image bounds.
 * This keeps it easy to map later into real pixel crop math.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    imageBitmap: ImageBitmap,
    onCancel: () -> Unit,
    onDone: (rect: androidx.compose.ui.geometry.Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    // Default: a nice centered crop that feels “smart”
    var rect by remember {
        mutableStateOf(
            androidx.compose.ui.geometry.Rect(
                left = 0.08f,
                top = 0.18f,
                right = 0.92f,
                bottom = 0.82f
            )
        )
    }

    // Reset target: preserves aspect-ish feel, avoids tiny crops
    fun resetRect() {
        rect = androidx.compose.ui.geometry.Rect(
            left = 0.08f,
            top = 0.18f,
            right = 0.92f,
            bottom = 0.82f
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crop screenshot", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { resetRect() }) { Text("Reset") }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screen, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onDone(rect) },
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            // Big “premium” crop stage
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 2.dp,
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RectangleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Base layer: the screenshot itself
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap,
                        contentDescription = "Screenshot to crop",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Overlay layer: premium visual crop UI
                    PremiumCropOverlay(
                        modifier = Modifier.fillMaxSize(),
                        rect = rect,
                        onRectChange = { rect = it },
                        showGrid = true
                    )
                }
            }

            // Subtext / instruction line (premium calm, not tutorial-y)
            Text(
                text = "Drag to reposition. Reset if it gets weird.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
