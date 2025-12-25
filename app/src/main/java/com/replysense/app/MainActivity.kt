package com.replysense.app

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.crop.CropScreen
import com.replysense.app.ui.theme.ReplySenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lastCrash = CrashStore.read(this)

        setContent {
            ReplySenseTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    if (lastCrash != null) {
                        CrashReportScreen(
                            crash = lastCrash,
                            onClear = {
                                CrashStore.clear(this@MainActivity)
                                recreate()
                            }
                        )
                        return@Surface
                    }

                    ReplySenseFlow(
                        decodeBitmap = { uri -> decodeUriToBitmap(uri) }
                    )
                }
            }
        }
    }

    private fun decodeUriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }
}

/* ---------------------------
   App Flow (no nav library)
   --------------------------- */

private sealed class UiState {
    data object Home : UiState()
    data class Cropping(val bitmap: Bitmap) : UiState()
    data class Cropped(val original: Bitmap, val cropped: Bitmap) : UiState()
}

@Composable
private fun ReplySenseFlow(
    decodeBitmap: (Uri) -> Bitmap
) {
    var state by remember { mutableStateOf<UiState>(UiState.Home) }
    var error by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val bmp = decodeBitmap(uri)
            state = UiState.Cropping(bmp)
        } catch (t: Throwable) {
            error = "Failed to load image: ${t.message}"
        }
    }

    if (error != null) {
        ErrorDialog(
            message = error!!,
            onDismiss = { error = null }
        )
    }

    when (val s = state) {
        UiState.Home -> HomeScreen(
            onPick = { pickImage.launch("image/*") }
        )

        is UiState.Cropping -> {
            CropScreen(
                screenshot = s.bitmap,
                onCancel = { state = UiState.Home },
                onDone = { cropPx ->
                    try {
                        val cropped = cropBitmapSafe(s.bitmap, cropPx)
                        state = UiState.Cropped(original = s.bitmap, cropped = cropped)
                    } catch (t: Throwable) {
                        error = "Crop failed: ${t.message}"
                        state = UiState.Home
                    }
                }
            )
        }

        is UiState.Cropped -> CroppedPreviewScreen(
            onBack = { state = UiState.Home },
            onContinue = {
                // ✅ Next step will be: OCR → parse ME/THEM → generate reply
                // For now: placeholder to prove pipeline is back.
                error = "Next: OCR + reply generation screen (we wire this after theme)."
            }
        )
    }
}

/* ---------------------------
   Screens
   --------------------------- */

@Composable
private fun HomeScreen(onPick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ReplySense",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Pick a screenshot, crop the chat, then we generate a reply.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Pick screenshot")
        }

        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Theme polish coming next")
        }
    }
}

@Composable
private fun CroppedPreviewScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Cropped", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Crop complete. Next step will be OCR → parse → reply.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = onContinue) { Text("Continue") }
        }
    }
}

/* ---------------------------
   Crash UI (keep it)
   --------------------------- */

@Composable
private fun CrashReportScreen(
    crash: String,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crash Report", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This is why the app instantly closed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = crash,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onClear) { Text("Clear & Relaunch") }
        }
    }
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Heads up") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

/* ---------------------------
   Crop helper (pixel-rect)
   --------------------------- */

private fun cropBitmapSafe(src: Bitmap, r: android.graphics.Rect): Bitmap {
    val left = r.left.coerceIn(0, src.width - 1)
    val top = r.top.coerceIn(0, src.height - 1)
    val right = r.right.coerceIn(left + 1, src.width)
    val bottom = r.bottom.coerceIn(top + 1, src.height)

    val w = (right - left).coerceAtLeast(1)
    val h = (bottom - top).coerceAtLeast(1)

    return Bitmap.createBitmap(src, left, top, w, h)
}
