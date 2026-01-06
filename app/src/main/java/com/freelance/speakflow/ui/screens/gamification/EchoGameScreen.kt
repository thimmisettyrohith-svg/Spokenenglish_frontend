package com.freelance.speakflow.ui.screens.gamification

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.freelance.speakflow.data.EchoLevel
import com.freelance.speakflow.data.ProgressUpdateRequest
import com.freelance.speakflow.data.ProgressUpdateResponse
import com.freelance.speakflow.data.RetrofitInstance
import kotlinx.coroutines.delay
import java.util.Locale

// ==========================================
// DATA MODELS
// ==========================================
data class ScoringResult(
    val accuracy: Int,
    val fluency: Int,
    val totalScore: Int
)

// ==========================================
// MAIN SCREEN CONTAINER
// ==========================================

@Composable
fun EchoGameScreen(
    userId: Int, // ✅ Added userId
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var levelData by remember { mutableStateOf<EchoLevel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // FETCH DATA
    LaunchedEffect(Unit) {
        try {
            // Fetch Level 1
            val response = RetrofitInstance.api.getEchoLevel(1)
            levelData = response.payload
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading || levelData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFF57C00))
        }
    } else {
        EchoGameLogic(userId, levelData!!, onBack)
    }
}

// ==========================================
// GAME LOGIC & STATES
// ==========================================

enum class EchoGameState { INTRO, PLAYING, RESULT }

@Composable
fun EchoGameLogic(
    userId: Int,
    level: EchoLevel,
    onBack: () -> Unit
) {
    var gameState by remember { mutableStateOf(EchoGameState.INTRO) }
    val context = LocalContext.current

    // Game Stats
    var totalScore by remember { mutableIntStateOf(0) }
    var questionsPlayed by remember { mutableIntStateOf(0) }

    // API State
    var isSaving by remember { mutableStateOf(false) }
    var progressData by remember { mutableStateOf<ProgressUpdateResponse?>(null) }

    // Logic to Save Progress
    if (isSaving) {
        LaunchedEffect(Unit) {
            try {
                val avgScore = if (questionsPlayed > 0) totalScore / questionsPlayed else 0
                val xpEarned = (avgScore * 1.5).toInt()

                val request = ProgressUpdateRequest(
                    userId = userId,
                    gameType = "echo_game",
                    xpEarned = xpEarned,
                    score = avgScore
                )
                progressData = RetrofitInstance.api.updateProgress(request)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save progress", Toast.LENGTH_SHORT).show()
            } finally {
                isSaving = false
                gameState = EchoGameState.RESULT
            }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    when (gameState) {
        EchoGameState.INTRO -> {
            EchoIntroView(
                onStart = { gameState = EchoGameState.PLAYING }
            )
        }
        EchoGameState.PLAYING -> {
            EchoPlayingView(
                level = level,
                onGameFinished = { score, count ->
                    totalScore = score
                    questionsPlayed = count
                    isSaving = true // ✅ Trigger Save
                },
                onBack = onBack
            )
        }
        EchoGameState.RESULT -> {
            val avg = if (questionsPlayed > 0) totalScore / questionsPlayed else 0
            EchoResultView(
                accuracy = avg,
                xpEarned = (avg * 1.5).toInt(),
                progress = progressData, // ✅ Pass backend data
                onContinue = onBack
            )
        }
    }
}

// ==========================================
// 1. INTRO VIEW
// ==========================================
@Composable
fun EchoIntroView(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = "https://img.freepik.com/free-vector/cute-monster-cartoon-character_1308-135706.jpg",
            contentDescription = "Monster",
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(Modifier.height(32.dp))

        Text("Repeat Phrases", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Listen & Speak", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text(
            "Listen to the phrase and repeat it back. Earn points for accuracy and fluency.",
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Start Game", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 2. PLAYING VIEW
// ==========================================
@Composable
fun EchoPlayingView(
    level: EchoLevel,
    onGameFinished: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var sessionTotalScore by remember { mutableIntStateOf(0) }

    // UI States
    var isListening by remember { mutableStateOf(false) }
    var userSpokenText by remember { mutableStateOf("") }
    var currentScoreDisplay by remember { mutableIntStateOf(0) }
    var showNextButton by remember { mutableStateOf(false) }

    var speechStartTime by remember { mutableLongStateOf(0L) }
    val currentQuestion = level.questions[currentIndex]
    var tts: TextToSpeech? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.US
            }
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            isListening = true
            userSpokenText = "Listening..."
            speechRecognizer.startListening(intent)
        }
    }

    fun playAudio() {
        tts?.speak(currentQuestion.text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    LaunchedEffect(currentIndex) {
        delay(500)
        playAudio()
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {
                speechStartTime = System.currentTimeMillis()
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
                userSpokenText = "Try again"
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val endTime = System.currentTimeMillis()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spoken = matches[0]
                    userSpokenText = spoken
                    val duration = endTime - speechStartTime
                    val result = calculateSmartScore(currentQuestion.text, spoken, duration)

                    currentScoreDisplay = result.totalScore
                    sessionTotalScore += result.totalScore
                    showNextButton = true
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose {
            speechRecognizer.destroy()
            tts?.shutdown()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Echo Game", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Text("Question ${currentIndex + 1}/${level.questions.size}", fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = (currentIndex + 1) / level.questions.size.toFloat(),
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFF57C00),
            trackColor = Color(0xFFFFE0B2)
        )

        Spacer(Modifier.height(32.dp))

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = "https://img.freepik.com/free-vector/monster-character-collection_23-2147633633.jpg",
                contentDescription = null,
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(Modifier.height(32.dp))

        Text("Repeat the phrase", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().clickable { playAudio() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color(0xFFF57C00))
            Spacer(Modifier.width(8.dp))
            Text(currentQuestion.text, fontSize = 20.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.weight(1f))

        if (showNextButton) {
            Text(
                "Score: $currentScoreDisplay%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if(currentScoreDisplay > 80) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (currentIndex < level.questions.size - 1) {
                        currentIndex++
                        showNextButton = false
                        userSpokenText = ""
                        currentScoreDisplay = 0
                    } else {
                        onGameFinished(sessionTotalScore, level.questions.size)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
            ) {
                Text("Next")
            }
        } else {
            val scale by animateFloatAsState(if (isListening) 1.2f else 1f)
            Button(
                onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth().height(56.dp).scale(scale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color.Red else Color(0xFFF57C00)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(if(isListening) Icons.Default.MicOff else Icons.Default.Mic, null)
                Spacer(Modifier.width(8.dp))
                Text(if(isListening) "Listening..." else "Record")
            }
        }
    }
}

// ==========================================
// 3. RESULT VIEW
// ==========================================
@Composable
fun EchoResultView(
    accuracy: Int,
    xpEarned: Int,
    progress: ProgressUpdateResponse?,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Great job!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("You're doing amazing! Keep up the fantastic work.", textAlign = TextAlign.Center)

        Spacer(Modifier.height(32.dp))

        AsyncImage(
            model = "https://img.freepik.com/free-vector/happy-monster-waving-hand_23-2147633633.jpg",
            contentDescription = null,
            modifier = Modifier.size(250.dp).clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(32.dp))

        // STATS ROW 1
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard("Avg Score", "$accuracy%")
            StatCard("XP Earned", "+$xpEarned")
        }

        Spacer(Modifier.height(16.dp))

        // STATS ROW 2 (Backend Data)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard("Total XP", "${progress?.totalXp ?: "-"}")
            StatCard("Streak", "${progress?.currentStreak ?: "-"} 🔥")
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(140.dp).height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// UTILS
// ==========================================
fun calculateSmartScore(target: String, spoken: String, durationMs: Long): ScoringResult {
    val cleanTarget = normalizeAndConvertNumbers(target)
    val cleanSpoken = normalizeAndConvertNumbers(spoken)

    val targetWords = cleanTarget.split(" ").filter { it.isNotEmpty() }
    val spokenWords = cleanSpoken.split(" ").filter { it.isNotEmpty() }

    val matches = targetWords.count { spokenWords.contains(it) }
    val accuracy = if (targetWords.isNotEmpty()) {
        ((matches.toDouble() / targetWords.size) * 100).toInt().coerceIn(0, 100)
    } else 0

    val seconds = if (durationMs < 100) 1.0 else durationMs / 1000.0
    val wpm = (spokenWords.size / seconds) * 60

    val fluency = when {
        wpm < 50 -> 40
        wpm < 90 -> 70
        else -> 100
    }

    val total = (accuracy * 0.7 + fluency * 0.3).toInt()

    return ScoringResult(accuracy, fluency, total)
}

fun normalizeAndConvertNumbers(input: String): String {
    var text = input.lowercase()
    val numMap = mapOf(
        "0" to "zero", "1" to "one", "2" to "two", "3" to "three", "4" to "four",
        "5" to "five", "6" to "six", "7" to "seven", "8" to "eight", "9" to "nine", "10" to "ten"
    )
    numMap.forEach { (digit, word) -> text = text.replace(digit, word) }
    return text.replace(Regex("[^a-z ]"), "").replace(Regex("\\s+"), " ").trim()
}