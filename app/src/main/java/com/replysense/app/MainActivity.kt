package com.replysense.app

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.replysense.app.ui.CropperDialog
import com.replysense.app.util.OcrPostProcess
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            OcrScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrScreen() {
    val context = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var isWorking by remember { mutableStateOf(false) }
    var rawText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // ✅ The “3 options”
    var useCrop by remember { mutableStateOf(true) }
    var useClean by remember { mutableStateOf(true) }
    var useMerge by remember { mutableStateOf(true) }

    // Crop UI state
    var showCropper by remember { mutableStateOf(false) }
    var cropBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
        rawText = ""
        errorText = null
        cropBitmap = null
        selectedBitmap = uri?.let { decodeBitmap(context, it) }
        if (useCrop && selectedBitmap != null) {
            showCropper = true
        }
    }

    fun runOcr(bitmap: Bitmap) {
        isWorking = true
        errorText = null
        rawText = ""

        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                rawText = visionText.text.trim()
                isWorking = false
            }
            .addOnFailureListener { e ->
                errorText = "OCR failed: ${e.message ?: e.javaClass.simpleName}"
                isWorking = false
            }
    }

    val processedText = remember(rawText, useClean, useMerge) {
        val t = rawText.trim()
        if (t.isEmpty()) "" else OcrPostProcess.process(t, clean = useClean, mergeLines = useMerge)
    }

    val displayText = when {
        errorText != null -> errorText!!
        rawText.isBlank() -> "(empty)"
        processedText.isBlank() -> "(Nothing useful after processing)"
        else -> processedText
    }

    if (showCropper && selectedBitmap != null) {
        CropperDialog(
            title = "Crop to chat area",
            bitmap = selectedBitmap!!,
            onCancel = { showCropper = false },
            onConfirm = { cropped ->
                cropBitmap = cropped
                showCropper = false
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ReplySense OCR") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Pick a screenshot → optionally crop → OCR → optionally clean + merge lines.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { pickImage.launch("image/*") }, enabled = !isWorking) {
                    Text("Pick image")
                }

                Button(
                    onClick = {
                        val bmp = (cropBitmap ?: selectedBitmap)
                        if (bmp != null) runOcr(bmp)
                        else errorText = "Pick an image first."
                    },
                    enabled = !isWorking
                ) {
                    Text(if (isWorking) "Working…" else "Run OCR")
                }

                OutlinedButton(
                    onClick = { if (selectedBitmap != null) showCropper = true },
                    enabled = selectedBitmap != null && !isWorking
                ) {
                    Text("Crop")
                }
            }

            // Toggles (the “3 options”)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = useCrop,
                    onClick = { useCrop = !useCrop },
                    label = { Text("Crop") }
                )
                FilterChip(
                    selected = useClean,
                    onClick = { useClean = !useClean },
                    label = { Text("Clean") }
                )
                FilterChip(
                    selected = useMerge,
                    onClick = { useMerge = !useMerge },
                    label = { Text("Merge lines") }
                )
            }

            if (useCrop && selectedBitmap != null && cropBitmap == null) {
                Text(
                    "Tip: Tap “Crop” and box the chat area for way cleaner extraction.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Result", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(displayText, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (rawText.isNotBlank()) {
                Divider()
                Text("Debug", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Raw length: ${rawText.length}, Processed length: ${processedText.length}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun decodeBitmap(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = false
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}

/**
 * Crop a bitmap using normalized [0..1] rect values.
 */
internal fun cropBitmapNormalized(
    src: Bitmap,
    leftN: Float,
    topN: Float,
    rightN: Float,
    bottomN: Float
): Bitmap {
    val left = (leftN.coerceIn(0f, 1f) * src.width).roundToInt()
    val top = (topN.coerceIn(0f, 1f) * src.height).roundToInt()
    val right = (rightN.coerceIn(0f, 1f) * src.width).roundToInt()
    val bottom = (bottomN.coerceIn(0f, 1f) * src.height).roundToInt()

    val x = left.coerceIn(0, src.width - 1)
    val y = top.coerceIn(0, src.height - 1)
    val w = (right - left).coerceAtLeast(1).coerceAtMost(src.width - x)
    val h = (bottom - top).coerceAtLeast(1).coerceAtMost(src.height - y)

    return Bitmap.createBitmap(src, x, y, w, h)
}
