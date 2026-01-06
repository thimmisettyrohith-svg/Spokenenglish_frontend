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
import com.freelance.speakflow.data.VocabAnswerResult
import com.freelance.speakflow.data.VocabListenClickResponse
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun VocabListenClickScreen(
    category: String,
    level: Int, // ✅ REQUIRED
    onFinish: (List<VocabAnswerResult>) -> Unit
) {
    val context = LocalContext.current

    // ---------------- STATE ----------------
    var response by remember { mutableStateOf<VocabListenClickResponse?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val answerResults = remember { mutableStateListOf<VocabAnswerResult>() }

    // ---------------- TEXT TO SPEECH ----------------
    val tts = remember(context) {
        TextToSpeech(context) { /* init callback */ }
    }

    // Set language ONCE
    LaunchedEffect(Unit) {
        tts.language = Locale.US
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speak(word: String) {
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // ---------------- API CALL ----------------
    LaunchedEffect(category, level) {
        try {
            response = RetrofitInstance.api.getVocabListenClick(
                category = category,
                level = level
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to load vocabulary",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            isLoading = false
        }
    }

    // ---------------- LOADING ----------------
    if (isLoading || response == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val questions = response!!.payload.questions.take(5)

    if (questions.isEmpty()) return

    val currentQuestion = questions[currentIndex]

    // ---------------- AUTO PLAY AUDIO ----------------
    LaunchedEffect(currentIndex) {
        delay(400)
        speak(currentQuestion.targetWord)
    }

    // ---------------- UI ----------------
    VocabGameLayout(
        currentQuestion = currentQuestion,
        questionIndex = currentIndex,
        totalQuestions = questions.size,
        onPlayAudio = { speak(currentQuestion.targetWord) },
        onOptionSelected = { selectedOptionId ->

            val isCorrect = selectedOptionId == currentQuestion.correctOptionId
            val selectedImage =
                currentQuestion.options.firstOrNull { it.id == selectedOptionId }?.image ?: ""

            answerResults.add(
                VocabAnswerResult(
                    word = currentQuestion.targetWord,
                    image = selectedImage,
                    isCorrect = isCorrect
                )
            )

            if (currentIndex < questions.lastIndex) {
                currentIndex++
            } else {
                onFinish(answerResults.toList())
            }
        }
    )
}
