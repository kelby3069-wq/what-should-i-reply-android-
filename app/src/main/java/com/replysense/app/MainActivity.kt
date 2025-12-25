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
    var resultText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
        resultText = ""
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
        resultText = ""

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
                resultText = visionText.text.trim().ifEmpty { "(No text found)" }
                isWorking = false
            }
            .addOnFailureListener { e ->
                errorText = "OCR failed: ${e.message ?: e.javaClass.simpleName}"
                isWorking = false
            }
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
                "Pick an image. We run OCR and dump the result below.",
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
                    Text(
                        text = if (resultText.isBlank()) "(empty)" else resultText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
