package com.freelance.speakflow.ui.screens.speaking

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.freelance.speakflow.data.ProgressUpdateRequest
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.data.SpeakingAnalysisData
import com.freelance.speakflow.data.SpeakingProgressRequest
import kotlinx.coroutines.launch

@Composable
fun SpeakingAnalysisScreen(
    userId: Int,
    lessonId: Int,
    dialogueId: Int,
    result: SpeakingAnalysisData,
    onBackToLessons: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    // Store progress response to show in UI
    var progressData by remember { mutableStateOf<com.freelance.speakflow.data.ProgressUpdateResponse?>(null) }
    var hasSaved by remember { mutableStateOf(false) }

    // Automatically Save when screen opens (Best practice for results)
    LaunchedEffect(Unit) {
        if (!hasSaved) {
            try {
                // 1. Save Lesson Progress (Legacy)
                RetrofitInstance.api.saveSpeakingProgress(
                    SpeakingProgressRequest(
                        userId = userId,
                        lessonId = lessonId,
                        dialogueId = dialogueId,
                        score = result.overall_score
                    )
                )

                // 2. Save XP & Streak (New System)
                // XP = Score / 2 (e.g., 90 score -> 45 XP)
                val xpEarned = (result.overall_score / 2).coerceAtLeast(10)

                val progressResponse = RetrofitInstance.api.updateProgress(
                    ProgressUpdateRequest(
                        userId = userId,
                        gameType = "speaking",
                        xpEarned = xpEarned,
                        score = result.overall_score
                    )
                )
                progressData = progressResponse
                hasSaved = true

            } catch (e: Exception) {
                e.printStackTrace()
                // Silent fail: User can still see their score
            }
        }
    }

    SpeakingAnalysisLayout(
        overallScore = result.overall_score,
        fluency = result.metrics.fluency,
        clarity = result.metrics.clarity,
        accent = result.metrics.accent,
        transcript = result.debug_transcript,
        tips = result.feedback.tips,
        wordFeedback = result.word_analysis,
        progress = progressData, // ✅ Pass progress data to UI
        onBack = {
            onBackToLessons()
        }
    )
}