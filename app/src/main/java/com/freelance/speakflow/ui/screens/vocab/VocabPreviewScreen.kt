package com.freelance.speakflow.ui.screens.vocab

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.data.VocabPreviewItem
import com.freelance.speakflow.ui.theme.PurplePrimary
import java.util.Locale

@Composable
fun VocabPreviewScreen(
    category: String,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var previewItems by remember { mutableStateOf<List<VocabPreviewItem>>(emptyList()) }

    // ✅ TTS STATE
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // ✅ PROPER TTS INIT
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
            }
        }

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun speak(word: String) {
        if (!isTtsReady) {
            Toast.makeText(context, "Audio loading…", Toast.LENGTH_SHORT).show()
            return
        }
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // API
    LaunchedEffect(category) {
        try {
            val response = RetrofitInstance.api.getVocabPreview(category, level = 1)
            previewItems = response.payload.items
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load content", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PurplePrimary)
        }
    } else {
        VocabPreviewLayout(
            items = previewItems,
            onListenClick = { word ->
                speak(word)   // ✅ NOW WORKS
            },
            onStartGame = onStartGame,
            onBack = onBack
        )
    }
}
