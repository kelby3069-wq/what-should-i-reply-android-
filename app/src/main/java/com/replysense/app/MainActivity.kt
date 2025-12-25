package com.replysense.app

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.replysense.app.ui.CropperDialog
import com.replysense.app.util.OcrLayoutCluster
import com.replysense.app.util.OcrPostProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReplySenseApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReplySenseApp() {
    val ctx = LocalContext.current

    var pickedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    var rawText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<OcrPostProcess.Msg>>(emptyList()) }
    var selectedMsgId by remember { mutableStateOf<Int?>(null) }

    var replyTarget by remember { mutableStateOf(ReplyTarget.LAST_INCOMING) }
    var tone by remember { mutableStateOf(Tone.FLIRTY) }

    var generatedReply by remember { mutableStateOf("") }
    var transcript by remember { mutableStateOf("") }
    var json by remember { mutableStateOf("") }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedBitmap = loadBitmapFromUri(ctx, uri)
            showCrop = pickedBitmap != null
        }
    }

    fun rebuildDebug() {
        transcript = messages.joinToString("\n") { m ->
            val who = if (m.dir == OcrPostProcess.Dir.THEM) "THEM" else "ME"
            "$who: ${m.text}"
        }
        json = messagesToJson(messages)
    }

    fun runOcr(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                rawText = result.text ?: ""

                // ✅ NEW: bubble-aware splitter
                val extracted = OcrLayoutCluster.extractMessages(
                    result = result,
                    imageWidthPx = bitmap.width,
                    imageHeightPx = bitmap.height
                )

                messages = if (extracted.isNotEmpty()) extracted else {
                    // Fallback to old pipeline if clustering returns nothing
                    OcrPostProcess.processThreadAware(
                        input = rawText,
                        clean = true,
                        mergeLines = true,
                        threadOnly = true
                    ).messages
                }

                selectedMsgId = messages.firstOrNull()?.id
                generatedReply = ""
                rebuildDebug()
            }
            .addOnFailureListener { e ->
                rawText = "OCR error: ${e.message ?: e.javaClass.simpleName}"
                messages = emptyList()
                selectedMsgId = null
                transcript = ""
                json = ""
                generatedReply = ""
            }
    }

    fun currentSelectedText(): String? =
        messages.firstOrNull { it.id == selectedMsgId }?.text

    fun replySourceText(): String? = when (replyTarget) {
        ReplyTarget.SELECTED -> currentSelectedText()
        ReplyTarget.LAST_INCOMING -> OcrPostProcess.lastIncomingText(messages)
    }

    fun generateReply() {
        val src = replySourceText()?.trim().orEmpty()
        if (src.isBlank()) {
            generatedReply = "Pick a message (and mark THEM/ME if needed)."
            return
        }
        generatedReply = LocalReplyEngine.generate(src, tone)
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ReplySense OCR") },
                    actions = {
                        AssistChip(
                            onClick = {
                                replyTarget =
                                    if (replyTarget == ReplyTarget.LAST_INCOMING) ReplyTarget.SELECTED
                                    else ReplyTarget.LAST_INCOMING
                            },
                            label = {
                                Text(
                                    if (replyTarget == ReplyTarget.LAST_INCOMING) "Reply: Last THEM"
                                    else "Reply: Selected"
                                )
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                )
            }
        ) { pad ->
            Column(
                Modifier
                    .padding(pad)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { pickImageLauncher.launch("image/*") }) { Text("Pick image") }
                    Button(
                        onClick = { pickedBitmap?.let { runOcr(it) } },
                        enabled = pickedBitmap != null
                    ) { Text("Run OCR") }
                }

                Spacer(Modifier.height(12.dp))

                ToneRow(tone = tone, onTone = { tone = it })

                Spacer(Modifier.height(16.dp))

                Text("Messages (tap one)", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                if (messages.isEmpty()) {
                    Text("No messages yet. Pick an image → crop → Run OCR.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        messages.forEach { m ->
                            MessageCard(
                                msg = m,
                                selected = (m.id == selectedMsgId),
                                onSelect = { selectedMsgId = m.id },
                                onToggleDir = {
                                    messages = OcrPostProcess.toggleDir(messages, m.id)
                                    rebuildDebug()
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { generateReply() },
                    enabled = messages.isNotEmpty()
                ) { Text("Generate reply") }

                Spacer(Modifier.height(16.dp))

                Text("Generated reply", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Text(
                        text = if (generatedReply.isBlank()) "—" else generatedReply,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Debug", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Raw: ${rawText.length} | Messages: ${messages.size}")

                Spacer(Modifier.height(8.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Transcript", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(if (transcript.isBlank()) "—" else transcript)
                    }
                }

                Spacer(Modifier.height(8.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("JSON", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(if (json.isBlank()) "—" else json)
                    }
                }
            }
        }
    }

    if (showCrop && pickedBitmap != null) {
        CropperDialog(
            title = "Crop to chat area",
            bitmap = pickedBitmap!!,
            onCancel = { showCrop = false },
            onConfirm = { cropped ->
                pickedBitmap = cropped
                showCrop = false
                runOcr(cropped)
            }
        )
    }
}

@Composable
private fun ToneRow(tone: Tone, onTone: (Tone) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(selected = tone == Tone.CHILL, onClick = { onTone(Tone.CHILL) }, label = { Text("Chill") })
        FilterChip(selected = tone == Tone.FLIRTY, onClick = { onTone(Tone.FLIRTY) }, label = { Text("Flirty") })
        FilterChip(selected = tone == Tone.FIRM, onClick = { onTone(Tone.FIRM) }, label = { Text("Firm") })
    }
}

@Composable
private fun MessageCard(
    msg: OcrPostProcess.Msg,
    selected: Boolean,
    onSelect: () -> Unit,
    onToggleDir: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onToggleDir,
                    label = { Text(if (msg.dir == OcrPostProcess.Dir.THEM) "THEM" else "ME") }
                )
                if (selected) AssistChip(onClick = {}, label = { Text("Selected") })
            }
            Spacer(Modifier.height(8.dp))
            Text(text = msg.text, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

private enum class ReplyTarget { LAST_INCOMING, SELECTED }
private enum class Tone { CHILL, FLIRTY, FIRM }

private object LocalReplyEngine {
    fun generate(input: String, tone: Tone): String {
        val s = input.trim()
        val isQuestion = s.contains('?') || s.lowercase().startsWith("wyd") || s.lowercase().startsWith("wya")
        return when (tone) {
            Tone.CHILL -> if (isQuestion) "Lowkey yeah — what’s the move?" else "Bet 😌 what were you thinking?"
            Tone.FLIRTY -> if (isQuestion) "Maybe 😏 convince me." else "Okayyy 👀 you trying to tempt me or what?"
            Tone.FIRM -> if (isQuestion) "What exactly are you asking me to do?" else "Say it straight — what do you want?"
        }
    }
}

private fun loadBitmapFromUri(ctx: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val resolver = ctx.contentResolver
        resolver.openInputStream(uri)?.use { input ->
            android.graphics.BitmapFactory.decodeStream(input)
        }
    } catch (_: Throwable) {
        null
    }
}

private fun messagesToJson(msgs: List<OcrPostProcess.Msg>): String {
    fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    val items = msgs.joinToString(",") { m ->
        """{"id":${m.id},"dir":"${m.dir.name}","text":"${esc(m.text)}"}"""
    }
    return "[$items]"
}
