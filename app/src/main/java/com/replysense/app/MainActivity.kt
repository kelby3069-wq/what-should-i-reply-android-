package com.replysense.app

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

private const val PREFS = "replysense_prefs"
private const val KEY_MY_SIDE = "my_side" // "RIGHT" | "LEFT"

private enum class Tone(val label: String, val emoji: String) {
    CHILL("Chill", "😌"),
    FLIRTY("Flirty", "😏"),
    FIRM("Firm", "🧊")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReplySenseApp() {
    val ctx = LocalContext.current

    // Core state
    var pickedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCrop by remember { mutableStateOf(false) }

    var rawText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<OcrPostProcess.Msg>>(emptyList()) }
    var selectedMsgId by remember { mutableStateOf<Int?>(null) }
    var lastThemId by remember { mutableStateOf<Int?>(null) }

    var tone by remember { mutableStateOf(Tone.FLIRTY) }
    var generatedReply by remember { mutableStateOf("") }
    var transcript by remember { mutableStateOf("") }
    var json by remember { mutableStateOf("") }

    var mySide by remember { mutableStateOf(loadMySide(ctx)) }

    // UI state
    var showDebug by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }

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
            val tag = if (m.id == lastThemId) "  ← target" else ""
            "$who: ${m.text}$tag"
        }
        json = messagesToJson(messages)
    }

    fun recomputeTargets() {
        lastThemId = messages.lastOrNull { it.dir == OcrPostProcess.Dir.THEM }?.id
        selectedMsgId = lastThemId ?: messages.lastOrNull()?.id
    }

    fun runOcr(bitmap: Bitmap) {
        isBusy = true
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                rawText = result.text ?: ""

                val extracted = OcrLayoutCluster.extractMessages(
                    result = result,
                    imageWidthPx = bitmap.width,
                    imageHeightPx = bitmap.height,
                    mySide = mySide
                )

                messages = if (extracted.isNotEmpty()) extracted else {
                    OcrPostProcess.processThreadAware(
                        input = rawText,
                        clean = true,
                        mergeLines = true,
                        threadOnly = true
                    ).messages
                }

                generatedReply = ""
                recomputeTargets()
                rebuildDebug()
                isBusy = false
            }
            .addOnFailureListener { e ->
                rawText = "OCR error: ${e.message ?: e.javaClass.simpleName}"
                messages = emptyList()
                selectedMsgId = null
                lastThemId = null
                transcript = ""
                json = ""
                generatedReply = ""
                isBusy = false
            }
    }

    fun toggleDir(id: Int) {
        messages = OcrPostProcess.toggleDir(messages, id)
        recomputeTargets()
        rebuildDebug()
    }

    fun targetTextPreferThem(): String? {
        val them = lastThemId?.let { id -> messages.firstOrNull { it.id == id }?.text }
        if (!them.isNullOrBlank()) return them

        val sel = selectedMsgId?.let { id -> messages.firstOrNull { it.id == id }?.text }
        if (!sel.isNullOrBlank()) return sel

        return messages.lastOrNull()?.text
    }

    fun generateReply() {
        val src = targetTextPreferThem()?.trim().orEmpty()
        if (src.isBlank()) {
            generatedReply = "No message found. Crop tighter to the chat bubbles."
            return
        }
        generatedReply = LocalReplyEngine.generate(src, tone)
    }

    MaterialTheme(
        colorScheme = lightColorScheme(), // keep simple; we’ll theme next pass
        typography = Typography()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ReplySense", fontWeight = FontWeight.SemiBold)
                            Text(
                                "OCR → Clean thread → Smart reply",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        AssistChip(
                            onClick = {
                                mySide = if (mySide == OcrLayoutCluster.MySide.RIGHT)
                                    OcrLayoutCluster.MySide.LEFT else OcrLayoutCluster.MySide.RIGHT
                                saveMySide(ctx, mySide)
                            },
                            label = { Text("My side: ${mySide.name}") }
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { showDebug = !showDebug }) {
                            Text(if (showDebug) "Hide" else "Debug", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
            }
        ) { pad ->
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // HERO CARD
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Start here", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Pick a screenshot, crop to the chat area, run OCR.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isBusy) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { pickImageLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Pick")
                            }

                            FilledTonalButton(
                                onClick = { pickedBitmap?.let { runOcr(it) } },
                                enabled = pickedBitmap != null && !isBusy,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Run OCR")
                            }
                        }
                    }
                }

                // TONE SELECTOR
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Tone", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        SegmentedTone(
                            tone = tone,
                            onTone = { tone = it }
                        )
                    }
                }

                // TARGET + ACTIONS
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Reply target", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        val targetPreview = targetTextPreferThem()?.trim().orEmpty()
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (targetPreview.isBlank()) "No target yet — run OCR first." else targetPreview,
                                modifier = Modifier.padding(12.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = { selectedMsgId = lastThemId ?: selectedMsgId },
                                enabled = lastThemId != null,
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Select last THEM") }

                            Button(
                                onClick = { generateReply() },
                                enabled = messages.isNotEmpty(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Generate")
                            }
                        }
                    }
                }

                // MESSAGES LIST
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Messages", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tap to select. Tap the chip to flip ME/THEM if OCR guessed wrong.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (messages.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text("No messages yet.", modifier = Modifier.padding(12.dp))
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                messages.forEach { m ->
                                    PremiumMessageCard(
                                        msg = m,
                                        selected = (m.id == selectedMsgId),
                                        isTarget = (m.id == lastThemId),
                                        onSelect = { selectedMsgId = m.id },
                                        onToggleDir = { toggleDir(m.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // GENERATED REPLY
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Generated reply", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (generatedReply.isBlank()) "—" else generatedReply,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = { copyToClipboard(ctx, "ReplySense Reply", generatedReply) },
                                enabled = generatedReply.isNotBlank(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Copy")
                            }

                            OutlinedButton(
                                onClick = { shareText(ctx, "ReplySense Reply", generatedReply) },
                                enabled = generatedReply.isNotBlank(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Share")
                            }
                        }
                    }
                }

                // DEBUG (collapsible)
                AnimatedVisibility(visible = showDebug) {
                    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Debug", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Raw: ${rawText.length} | Messages: ${messages.size} | Target: ${lastThemId ?: "—"}")

                            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("Transcript", fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    Text(if (transcript.isBlank()) "—" else transcript)
                                }
                            }

                            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("JSON", fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    Text(if (json.isBlank()) "—" else json)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
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
private fun SegmentedTone(tone: Tone, onTone: (Tone) -> Unit) {
    val options = Tone.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, t ->
            SegmentedButton(
                selected = tone == t,
                onClick = { onTone(t) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text("${t.emoji} ${t.label}")
            }
        }
    }
}

@Composable
private fun PremiumMessageCard(
    msg: OcrPostProcess.Msg,
    selected: Boolean,
    isTarget: Boolean,
    onSelect: () -> Unit,
    onToggleDir: () -> Unit
) {
    val container = when {
        isTarget -> MaterialTheme.colorScheme.primaryContainer
        selected -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val label = if (msg.dir == OcrPostProcess.Dir.THEM) "THEM" else "ME"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        color = container
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onToggleDir, label = { Text(label) })
                if (selected) AssistChip(onClick = {}, label = { Text("Selected") })
                if (isTarget) AssistChip(onClick = {}, label = { Text("Target") })
            }
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected || isTarget) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

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

private fun loadBitmapFromUri(ctx: Context, uri: Uri): Bitmap? {
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

private fun loadMySide(ctx: Context): OcrLayoutCluster.MySide {
    val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val v = p.getString(KEY_MY_SIDE, OcrLayoutCluster.MySide.RIGHT.name) ?: OcrLayoutCluster.MySide.RIGHT.name
    return runCatching { OcrLayoutCluster.MySide.valueOf(v) }.getOrDefault(OcrLayoutCluster.MySide.RIGHT)
}

private fun saveMySide(ctx: Context, side: OcrLayoutCluster.MySide) {
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_MY_SIDE, side.name)
        .apply()
}

/** Clipboard + Share helpers (no extra deps) */
private fun copyToClipboard(ctx: Context, label: String, text: String) {
    if (text.isBlank()) return
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    cm.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
}

private fun shareText(ctx: Context, subject: String, text: String) {
    if (text.isBlank()) return
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
        putExtra(android.content.Intent.EXTRA_TEXT, text)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    ctx.startActivity(android.content.Intent.createChooser(send, "Share via").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
}
