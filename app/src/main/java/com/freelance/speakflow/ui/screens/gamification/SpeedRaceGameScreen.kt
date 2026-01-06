package com.freelance.speakflow.ui.screens.gamification

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.data.ProgressUpdateRequest
import com.freelance.speakflow.data.ProgressUpdateResponse
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.data.SpeedRaceLevel
import kotlinx.coroutines.delay

// ==========================================
// MAIN SCREEN
// ==========================================
@Composable
fun SpeedRaceGameScreen(
    userId: Int, // ✅ Added userId
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var level by remember { mutableStateOf<SpeedRaceLevel?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            level = RetrofitInstance.api.getSpeedRaceLevel(1).payload
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load level", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
        }
    }

    if (loading || level == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Red)
        }
    } else {
        SpeedRaceLogic(userId, level!!, onBack)
    }
}

// ==========================================
// GAME LOGIC
// ==========================================
@Composable
fun SpeedRaceLogic(
    userId: Int,
    level: SpeedRaceLevel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Game state
    var index by remember { mutableIntStateOf(0) }
    var phrasesCompleted by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableLongStateOf(level.timeLimit) }
    var gameActive by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    // API State
    var isSaving by remember { mutableStateOf(false) }
    var progressData by remember { mutableStateOf<ProgressUpdateResponse?>(null) }

    // Speech state
    var listening by remember { mutableStateOf(false) }
    var phraseLocked by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }

    // Visual feedback
    var flash by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        if (flash) Color(0xFFE8F5E9) else Color.White,
        label = "flash"
    )

    // ==========================================
    // SAVE LOGIC
    // ==========================================
    if (isSaving) {
        LaunchedEffect(Unit) {
            try {
                // 5 XP per phrase in Speed Race
                val xpEarned = phrasesCompleted * 5

                val request = ProgressUpdateRequest(
                    userId = userId,
                    gameType = "speed_race",
                    xpEarned = xpEarned,
                    score = phrasesCompleted
                )
                progressData = RetrofitInstance.api.updateProgress(request)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save progress", Toast.LENGTH_SHORT).show()
            } finally {
                isSaving = false
                finished = true
            }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Red)
        }
        return
    }

    // ==========================================
    // TIMER
    // ==========================================
    val timer = remember {
        object : CountDownTimer(level.timeLimit * 1000, 1000) {
            override fun onTick(ms: Long) {
                timeLeft = ms / 1000
            }

            override fun onFinish() {
                gameActive = false
                listening = false
                isSaving = true // ✅ Trigger Save when time is up
            }
        }
    }

    // ==========================================
    // SPEECH RECOGNIZER
    // ==========================================
    val recognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    fun startListening() {
        if (gameActive && !listening) {
            listening = true
            try {
                recognizer.startListening(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // FAST MATCH LOGIC
    // ==========================================
    fun checkMatch(spoken: String) {
        if (!gameActive || phraseLocked) return

        val currentPhrase = level.questions.getOrNull(index)?.text ?: ""
        if (currentPhrase.isEmpty()) return

        val spokenWords = spoken.lowercase()
            .replace(Regex("[^a-z ]"), "")
            .split(" ")
            .filter { it.length > 2 }

        val targetWords = currentPhrase.lowercase()
            .replace(Regex("[^a-z ]"), "")
            .split(" ")
            .filter { it.length > 2 }

        if (targetWords.isEmpty()) return

        val matchedCount = targetWords.count { targetWord ->
            spokenWords.contains(targetWord)
        }

        val ratio = matchedCount.toFloat() / targetWords.size

        if (ratio >= 0.4f) {
            phraseLocked = true
            phrasesCompleted++
            flash = true

            if (index < level.questions.lastIndex) {
                index++
                partialText = ""
                phraseLocked = false
            } else {
                // Finished all questions before time runs out!
                timer.cancel()
                timer.onFinish()
            }
        }
    }

    // Reset Flash Animation
    LaunchedEffect(flash) {
        if (flash) {
            delay(150)
            flash = false
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onPartialResults(b: Bundle?) {
                val matches = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: return

                partialText = text
                checkMatch(text)
            }

            override fun onResults(b: Bundle?) {
                val matches = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: return

                checkMatch(text)
            }

            override fun onEndOfSpeech() {
                listening = false
                if (gameActive) {
                    startListening()
                }
            }

            override fun onError(error: Int) {
                listening = false
                if (gameActive) {
                    startListening()
                }
            }

            override fun onReadyForSpeech(p0: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(p0: Float) {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onEvent(p0: Int, p1: Bundle?) {}
        }

        recognizer.setRecognitionListener(listener)

        onDispose {
            recognizer.destroy()
            timer.cancel()
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                gameActive = true
                timer.start()
                startListening()
            } else {
                Toast.makeText(context, "Mic permission required", Toast.LENGTH_SHORT).show()
            }
        }

    // ==========================================
    // UI LAYOUT
    // ==========================================

    // 1. RESULT SCREEN
    if (finished) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF3E0))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("⏱️", fontSize = 60.sp)
            Text("Time’s Up!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))

            // Stats Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Phrases Completed", color = Color.Gray)
                    Text(
                        phrasesCompleted.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315)
                    )

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("XP Earned", color = Color.Gray, fontSize = 12.sp)
                            Text("+${phrasesCompleted * 5}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total XP", color = Color.Gray, fontSize = 12.sp)
                            Text("${progressData?.totalXp ?: "-"}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Streak", color = Color.Gray, fontSize = 12.sp)
                            Text("${progressData?.currentStreak ?: "-"} 🔥", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Finish") }
        }
        return
    }

    // 2. INTRO SCREEN
    if (!gameActive) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("⚡", fontSize = 72.sp)
            Text(
                "Speed Speak Race",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Read as many phrases as you can in 60 seconds!",
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("START RACE")
            }
        }
        return
    }

    // 3. GAMEPLAY SCREEN
    val currentDisplayPhrase = level.questions.getOrNull(index)?.text ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text(
                "$timeLeft s",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Score: $phrasesCompleted",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = timeLeft / level.timeLimit.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color.Red
        )

        Spacer(Modifier.weight(1f))

        // Target Phrase
        Text(
            currentDisplayPhrase,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(Modifier.height(24.dp))

        // Feedback
        Text(
            if (partialText.isEmpty()) "Speak fast!" else "...$partialText",
            color = Color.Gray,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        // Skip Button
        Button(
            onClick = {
                if (index < level.questions.lastIndex) {
                    index++
                    partialText = ""
                    phraseLocked = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Skip", color = Color.Black)
        }
    }
}