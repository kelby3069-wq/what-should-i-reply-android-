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
import com.replysense.app.ui.components.*
import com.replysense.app.ui.crop.CropScreen
import com.replysense.app.ui.theme.AppSpacing
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
   Flow (no nav lib)
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
        ErrorDialog(message = error!!, onDismiss = { error = null })
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

        is UiState.Cropped -> CroppedSuccessScreen(
            onBack = { state = UiState.Home },
            onContinue = {
                error = "Next: OCR + reply generation screen (we wire this after theme)."
            }
        )
    }
}

/* ---------------------------
   Screens (premium components)
   --------------------------- */

@Composable
private fun HomeScreen(onPick: () -> Unit) {
    RSScaffold(title = "ReplySense") { padding ->
        RSScreen(modifier = Modifier.padding(padding)) {

            RSSectionHeader(
                title = "Turn screenshots into replies",
                subtitle = "Pick a chat screenshot, crop the conversation, then generate a reply."
            )

            RSCard {
                Text(
                    "Pipeline",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    RSBadge("1) Pick screenshot")
                    RSBadge("2) Crop chat area")
                    RSBadge("3) OCR + parse ME/THEM")
                    RSBadge("4) Generate reply")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                RSPrimaryButton(
                    text = "Pick screenshot",
                    onClick = onPick,
                    modifier = Modifier.fillMaxWidth()
                )
                RSSecondaryButton(
                    text = "How it works",
                    onClick = { /* optional later */ },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            RSCard {
                Text(
                    "Design sprint mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "We’re polishing visuals first. No new features until this feels App-Store-ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CroppedSuccessScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    RSScaffold(title = "Cropped") { padding ->
        RSScreen(modifier = Modifier.padding(padding)) {

            RSSectionHeader(
                title = "Crop complete",
                subtitle = "Next is OCR → parse → reply (we’ll wire it after visuals)."
            )

            RSCard {
                Text(
                    "Nice.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Your crop is ready. This confirms the new crop UI is working and stable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                RSSecondaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                RSPrimaryButton(
                    text = "Continue",
                    onClick = onContinue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/* ---------------------------
   Crash UI (keep it, but premium)
   --------------------------- */

@Composable
private fun CrashReportScreen(
    crash: String,
    onClear: () -> Unit
) {
    RSScaffold(title = "Crash Report") { padding ->
        RSScreen(modifier = Modifier.padding(padding)) {

            RSSectionHeader(
                title = "It crashed on launch",
                subtitle = "Copy the stacktrace and paste it here. We’ll fix the real root cause."
            )

            RSCard(modifier = Modifier.weight(1f, fill = true)) {
                Text(
                    text = crash,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            RSPrimaryButton(
                text = "Clear & Relaunch",
                onClick = onClear,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Heads up") },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

/* ---------------------------
   Crop helper
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
