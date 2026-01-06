package com.freelance.speakflow.ui.screens.vocab

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freelance.speakflow.data.ProgressUpdateRequest
import com.freelance.speakflow.data.ProgressUpdateResponse
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.data.VocabAnswerResult
import com.freelance.speakflow.data.VocabListenClickResponse
import java.util.Locale

@Composable
fun VocabGameScreen(
    userId: Int, // ✅ Added userId to save progress
    category: String,
    // ✅ Updated callback to include Backend Response (XP & Streak)
    onGameComplete: (List<VocabAnswerResult>, ProgressUpdateResponse?) -> Unit
) {
    val context = LocalContext.current

    var response by remember { mutableStateOf<VocabListenClickResponse?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // State for saving progress
    var isSaving by remember { mutableStateOf(false) }

    val results = remember { mutableStateListOf<VocabAnswerResult>() }

    // -------- TTS --------
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        tts = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
        onDispose {
            tts?.shutdown()
        }
    }

    fun speak(word: String) {
        if (ttsReady) {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // -------- LOAD GAME DATA --------
    LaunchedEffect(category) {
        try {
            response = RetrofitInstance.api.getVocabListenClick(category, 1)
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading game", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // -------- SAVE PROGRESS LOGIC --------
    if (isSaving) {
        LaunchedEffect(Unit) {
            try {
                // 1. Calculate Score & XP
                val score = results.count { it.isCorrect }
                val total = results.size
                val xpEarned = score * 5 // Rule: 5 XP per correct word

                // 2. Send to Backend
                val request = ProgressUpdateRequest(
                    userId = userId,
                    gameType = "vocab",
                    xpEarned = xpEarned,
                    score = score
                )
                val apiResponse = RetrofitInstance.api.updateProgress(request)

                // 3. Finish
                onGameComplete(results, apiResponse)

            } catch (e: Exception) {
                e.printStackTrace()
                // If API fails, still show results but without new XP info
                Toast.makeText(context, "Could not save progress", Toast.LENGTH_SHORT).show()
                onGameComplete(results, null)
            }
        }

        // Saving UI
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Saving Progress...")
            }
        }
        return
    }

    // -------- LOADING UI --------
    if (isLoading || response == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // -------- GAME UI --------
    val questions = response!!.payload.questions.take(5) // Limit to 5 for now
    val question = questions[index]

    LaunchedEffect(index) {
        speak(question.targetWord)
    }

    VocabGameLayout(
        currentQuestion = question,
        questionIndex = index,
        totalQuestions = questions.size,
        onPlayAudio = { speak(question.targetWord) },
        onOptionSelected = { selectedId ->

            results.add(
                VocabAnswerResult(
                    word = question.targetWord,
                    image = question.options.first { it.id == selectedId }.image,
                    isCorrect = selectedId == question.correctOptionId
                )
            )

            if (index < questions.lastIndex) {
                index++
            } else {
                // Trigger Save
                isSaving = true
            }
        }
    )
}