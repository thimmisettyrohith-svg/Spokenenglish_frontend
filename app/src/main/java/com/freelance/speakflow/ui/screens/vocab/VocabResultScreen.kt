package com.freelance.speakflow.ui.screens.vocab

import androidx.compose.runtime.Composable
import com.freelance.speakflow.data.ProgressUpdateResponse
import com.freelance.speakflow.data.VocabAnswerResult

@Composable
fun VocabResultScreen(
    results: List<VocabAnswerResult>,
    progress: ProgressUpdateResponse?, // ✅ Receive Backend Data
    onBackHome: () -> Unit
) {
    val total = results.size
    val correct = results.count { it.isCorrect }
    val earnedXp = correct * 5 // ✅ Match Game Logic (5 XP per word)

    VocabResultLayout(
        total = total,
        correct = correct,
        earnedXp = earnedXp,
        totalXp = progress?.totalXp,       // ✅ Pass to Layout
        streak = progress?.currentStreak,  // ✅ Pass to Layout
        results = results,
        onReview = { /* Already on review screen */ },
        onBackHome = onBackHome
    )
}