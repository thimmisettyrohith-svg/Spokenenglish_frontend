package com.freelance.speakflow.ui.screens.speaking

import android.Manifest
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.R
import com.freelance.speakflow.data.*
import com.freelance.speakflow.ui.theme.PurplePrimary
import com.freelance.speakflow.ui.utils.AudioRecorder
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Locale

@Composable
fun SpeakingDialogueScreen(
    lessonId: Int,
    dialogueIndex: Int,
    onFinishDialogue: (SpeakingAnalysisData) -> Unit


) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---------------- STATE ----------------
    val lessonDetailState = remember {
        mutableStateOf<SpeakingLessonDetail?>(null)
    }
    var isLoading by remember { mutableStateOf(true) }

    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // ---------------- PERMISSION ----------------
    val micPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // ---------------- FETCH LESSON ----------------
    LaunchedEffect(lessonId) {
        try {
            lessonDetailState.value =
                RetrofitInstance.api
                    .getSpeakingLessonDetail(lessonId)
                    .payload
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load lesson", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading || lessonDetailState.value == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val lessonDetail = lessonDetailState.value!!
    val dialogue = lessonDetail.dialogues
        .sortedBy { it.order }
        .getOrNull(dialogueIndex)
        ?: return

    val aiSentence = dialogue.aiPrompt
    val suggestedResponse = dialogue.targetResponse

    // ---------------- TTS ----------------
    val tts = remember {
        TextToSpeech(context) { }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // ---------------- UI ----------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.speaking),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("AI says", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text(aiSentence, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        tts.language = Locale.US
                        tts.speak(aiSentence, TextToSpeech.QUEUE_FLUSH, null, null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Listen")
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isRecording) {
                    recordedFile = recorder.startRecording()
                    isRecording = true
                } else {
                    recordedFile = recorder.stopRecording()
                    isRecording = false
                }
            },
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) Color.Red else PurplePrimary
            )
        ) {
            Icon(
                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            enabled = recordedFile != null && !isAnalyzing,
            onClick = {
                val file = recordedFile ?: return@Button
                isAnalyzing = true

                scope.launch {
                    try {
                        val part = MultipartBody.Part.createFormData(
                            "file",
                            file.name,
                            file.asRequestBody("audio/m4a".toMediaType())
                        )

                        val response = RetrofitInstance.api.analyzeSpeaking(
                            part,
                            aiSentence.toRequestBody("text/plain".toMediaType()),
                            lessonId.toString().toRequestBody("text/plain".toMediaType())
                        )

                        onFinishDialogue(response.data)


                    } finally {
                        isAnalyzing = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            Text("Finish", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
