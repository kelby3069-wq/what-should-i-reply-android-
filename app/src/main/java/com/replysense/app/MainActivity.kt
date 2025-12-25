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
import java.util.Locale

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
    var isWorking by remember { mutableStateOf(false) }
    var rawText by remember { mutableStateOf("") }
    var showCleaned by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
        rawText = ""
        errorText = null
    }

    fun decodeBitmap(uri: Uri): Bitmap {
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

    fun runOcr(uri: Uri) {
        isWorking = true
        errorText = null
        rawText = ""

        val bitmap = try {
            decodeBitmap(uri)
        } catch (t: Throwable) {
            isWorking = false
            errorText = "Couldn’t decode image: ${t.message ?: t.javaClass.simpleName}"
            return
        }

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

    val displayText = remember(rawText, showCleaned) {
        val t = rawText.trim()
        if (t.isEmpty()) "(empty)"
        else if (!showCleaned) t
        else cleanOcrText(t).ifBlank { "(Nothing useful after cleanup)" }
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
                "Pick a screenshot. We OCR it and (optionally) clean the junk UI text.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { pickImage.launch("image/*") }) { Text("Pick image") }

                Button(
                    onClick = { selectedUri?.let { runOcr(it) } },
                    enabled = selectedUri != null && !isWorking
                ) {
                    Text(if (isWorking) "Working…" else "Run OCR")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = showCleaned,
                    onClick = { showCleaned = !showCleaned },
                    label = { Text(if (showCleaned) "Cleaned" else "Raw") }
                )
            }

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Result", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(displayText, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * Simple, brutal OCR cleanup tuned for chat/screenshot OCR:
 * - drops keyboard rows
 * - drops obvious UI chrome (Active Now, Message, language)
 * - drops lone digits (often keyboard index labels)
 * - removes repeated short garbage lines
 */
private fun cleanOcrText(input: String): String {
    val lines = input
        .replace("\r\n", "\n")
        .split('\n')
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val blacklistExact = setOf(
        "active now",
        "message",
        "english (us)",
        "search",
        "home",
        "back"
    )

    // Keyboard-ish lines: lots of single letters separated by spaces, e.g. "Q W E R T Y"
    fun isKeyboardRow(s: String): Boolean {
        val noDots = s.replace(".", "")
        val tokens = noDots.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size < 6) return false
        val singleLetter = tokens.count { it.length == 1 && it[0].isLetter() }
        return singleLetter >= 6
    }

    // Lone digits are often keyboard row labels in OCR output from screenshots
    fun isLoneDigitLine(s: String): Boolean = s.length <= 2 && s.all { it.isDigit() }

    // Time lines like "12:38 AM" / "2:18" etc.
    val timeRegex = Regex("""^\d{1,2}:\d{2}\s?(AM|PM)?$""", RegexOption.IGNORE_CASE)

    // Remove super-short noise lines that are just symbols
    fun isSymbolNoise(s: String): Boolean =
        s.length <= 2 && s.any { !it.isLetterOrDigit() } && s.all { !it.isLetterOrDigit() || it == '+' }

    val cleaned = mutableListOf<String>()
    for (raw in lines) {
        val s = raw.trim()
        val lower = s.lowercase(Locale.US)

        if (blacklistExact.contains(lower)) continue
        if (isKeyboardRow(s)) continue
        if (isLoneDigitLine(s)) continue
        if (timeRegex.matches(s)) continue
        if (isSymbolNoise(s)) continue

        // Drop lines that are basically “divider” OCR like "||" etc.
        if (lower == "||" || lower == "|" || lower == "ll") continue

        cleaned += s
    }

    // Collapse multiple blank-ish / repeated garbage
    return cleaned
        .joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}
