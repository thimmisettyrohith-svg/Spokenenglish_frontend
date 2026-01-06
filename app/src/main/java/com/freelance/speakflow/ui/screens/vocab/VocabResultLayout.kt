package com.freelance.speakflow.ui.screens.vocab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.data.VocabAnswerResult
import com.freelance.speakflow.ui.theme.PurplePrimary

@Composable
fun VocabResultLayout(
    total: Int,
    correct: Int,
    earnedXp: Int,
    totalXp: Int?, // ✅ Nullable (in case offline)
    streak: Int?,  // ✅ Nullable
    results: List<VocabAnswerResult>,
    onReview: () -> Unit,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // HEADER
        Text(
            text = "Quiz Results",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // STATS ROW (Updated)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Score
            ResultStat("Score", "$correct/$total")

            // 2. Streak (Show only if available)
            if (streak != null) {
                ResultStat("Streak", "$streak 🔥")
            }

            // 3. XP (Earned + Total)
            val xpText = if (totalXp != null) "+$earnedXp (Tot: $totalXp)" else "+$earnedXp"
            ResultStat("XP", xpText)
        }

        Spacer(Modifier.height(24.dp))

        // REVIEW LIST
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(results) { result ->
                ReviewRow(result)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ACTIONS
        Button(
            onClick = onReview,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            Text("Review Answers")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold) // Slightly smaller to fit text
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun ReviewRow(result: VocabAnswerResult) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        // IMAGE PLACEHOLDER (You can replace with AsyncImage later)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.LightGray, CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = result.word,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )

        Icon(
            imageVector = if (result.isCorrect) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (result.isCorrect) Color(0xFF2ECC71) else Color(0xFFE74C3C)
        )
    }
}