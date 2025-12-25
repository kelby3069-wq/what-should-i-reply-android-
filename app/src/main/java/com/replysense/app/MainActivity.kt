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
import androidx.compose.foundation.clickable
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
import com.replysense.app.net.Api
import com.replysense.app.ui.CropperDialog
import com.replysense.app.util.OcrPostProcess
import com.replysense.app.util.ReplyGenerator
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
        Surface(Modifier.fillMaxSize()) { OcrScreen() }
    }
}

private enum class OutputMode { Transcript, Json, Messages }
private enum class Tone { Chill, Flirty, Firm, Savage }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrScreen() {
    val context = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var isWorking by remember { mutableStateOf(false) }
    var rawText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Options
    var useCrop by remember { mutableStateOf(true) }
    var useClean by remember { mutableStateOf(true) }
    var useMerge by remember { mutableStateOf(true) }
    var useThread by remember { mutableStateOf(true) }

    var outputMode by remember { mutableStateOf(OutputMode.Messages) }

    // Crop UI state
    var showCropper by remember { mutableStateOf(false) }
    var cropBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // ReplySense upgrades
    var tone by remember { mutableStateOf(Tone.Chill) }
    var selectedMsgIndex by remember { mutableStateOf<Int?>(null) }
    var generatedReply by remember { mutableStateOf("") }
    var genError by remember { mutableStateOf<String?>(null) }
    var useAiLater by remember { mutableStateOf(false) } // toggle hook; API currently stubbed

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
        rawText = ""
        errorText = null
        cropBitmap = null
        selectedMsgIndex = null
        generatedReply = ""
        genError = null
        selectedBitmap = uri?.let { decodeBitmap(context, it) }
        if (useCrop && selectedBitmap != null) showCropper = true
    }

    fun runOcr(bitmap: Bitmap) {
        isWorking = true
        errorText = null
        rawText = ""
        selectedMsgIndex = null
        generatedReply = ""
        genError = null

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

    val processed = remember(rawText, useClean, useMerge, useThread) {
        val t = rawText.trim()
        if (t.isEmpty()) return@remember OcrPostProcess.Processed(
            transcript = "",
            json = "[]",
            messageCount = 0,
            messages = emptyList()
        )
        OcrPostProcess.processThreadAware(
            input = t,
            clean = useClean,
            mergeLines = useMerge,
            threadOnly = useThread
        )
    }

    fun generateReplyForSelected() {
        genError = null
        generatedReply = ""
        val idx = selectedMsgIndex
        if (idx == null) {
            genError = "Tap a message first."
            return
        }
        val msg = processed.messages.getOrNull(idx)
        if (msg == null) {
            genError = "Selected message not found."
            return
        }

        val toneStr = tone.name.lowercase()
        val contextText = processed.transcript.take(2000)

        if (useAiLater && Api.isEnabled()) {
            // AI hook (stubbed right now)
            val prompt = ReplyGenerator.buildPrompt(
                tone = toneStr,
                selectedMessage = msg.text,
                transcriptContext = contextText
            )
            val res = Api.sendPrompt(prompt)
            if (res.isSuccess) {
                generatedReply = res.getOrNull().orEmpty().trim()
            } else {
                genError = res.exceptionOrNull()?.message ?: "AI failed."
            }
        } else {
            // Local baseline generator
            generatedReply = ReplyGenerator.generate(
                tone = toneStr,
                incoming = msg.text,
                transcriptContext = contextText
            )
        }
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
                "Pick → Crop → OCR → Clean/Merge → Thread → tap message → generate reply.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { pickImage.launch("image/*") }, enabled = !isWorking) { Text("Pick image") }
                Button(
                    onClick = {
                        val bmp = (cropBitmap ?: selectedBitmap)
                        if (bmp != null) runOcr(bmp) else errorText = "Pick an image first."
                    },
                    enabled = !isWorking
                ) { Text(if (isWorking) "Working…" else "Run OCR") }
                OutlinedButton(
                    onClick = { if (selectedBitmap != null) showCropper = true },
                    enabled = selectedBitmap != null && !isWorking
                ) { Text("Crop") }
            }

            // OCR pipeline toggles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = useCrop, onClick = { useCrop = !useCrop }, label = { Text("Crop") })
                FilterChip(selected = useClean, onClick = { useClean = !useClean }, label = { Text("Clean") })
                FilterChip(selected = useMerge, onClick = { useMerge = !useMerge }, label = { Text("Merge") })
                FilterChip(selected = useThread, onClick = { useThread = !useThread }, label = { Text("Thread") })
            }

            // Output mode
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AssistChip(onClick = { outputMode = OutputMode.Messages }, label = { Text("Messages") })
                AssistChip(onClick = { outputMode = OutputMode.Transcript }, label = { Text("Transcript") })
                AssistChip(onClick = { outputMode = OutputMode.Json }, label = { Text("JSON") })
            }

            // Tone + AI hook
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Tone:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 10.dp))
                FilterChip(selected = tone == Tone.Chill, onClick = { tone = Tone.Chill }, label = { Text("Chill") })
                FilterChip(selected = tone == Tone.Flirty, onClick = { tone = Tone.Flirty }, label = { Text("Flirty") })
                FilterChip(selected = tone == Tone.Firm, onClick = { tone = Tone.Firm }, label = { Text("Firm") })
                FilterChip(selected = tone == Tone.Savage, onClick = { tone = Tone.Savage }, label = { Text("Savage") })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = useAiLater,
                    onClick = { useAiLater = !useAiLater },
                    label = { Text(if (useAiLater) "AI (hook)" else "Local") }
                )
                Button(
                    onClick = { generateReplyForSelected() },
                    enabled = processed.messages.isNotEmpty() && !isWorking
                ) { Text("Generate reply") }
            }

            if (errorText != null) {
                Text(errorText!!, color = MaterialTheme.colorScheme.error)
            }

            when (outputMode) {
                OutputMode.Messages -> {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Messages (tap one)", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            if (processed.messages.isEmpty()) {
                                Text("(No messages detected)", style = MaterialTheme.typography.bodySmall)
                            } else {
                                processed.messages.forEachIndexed { i, m ->
                                    val selected = selectedMsgIndex == i
                                    val label = buildString {
                                        if (m.ts != null) append("[${m.ts}] ")
                                        if (m.speaker != null) append("${m.speaker}: ")
                                        append(m.text)
                                    }
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                            .clickable {
                                                selectedMsgIndex = i
                                                generatedReply = ""
                                                genError = null
                                            },
                                        colors = if (selected)
                                            CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                        else CardDefaults.elevatedCardColors()
                                    ) {
                                        Column(Modifier.padding(10.dp)) {
                                            Text("Message ${i + 1}", style = MaterialTheme.typography.labelMedium)
                                            Text(label, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutputMode.Transcript -> {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Transcript", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                processed.transcript.ifBlank { "(empty)" },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                OutputMode.Json -> {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("JSON", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(processed.json.ifBlank { "[]" }, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (genError != null) {
                Text(genError!!, color = MaterialTheme.colorScheme.error)
            }

            if (generatedReply.isNotBlank()) {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Generated reply", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(generatedReply, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (rawText.isNotBlank()) {
                Divider()
                Text("Debug", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Raw: ${rawText.length} | Messages: ${processed.messageCount}",
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
