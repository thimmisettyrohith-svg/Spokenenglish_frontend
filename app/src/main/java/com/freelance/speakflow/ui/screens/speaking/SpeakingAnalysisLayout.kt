package com.freelance.speakflow.ui.screens.speaking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.data.ProgressUpdateResponse
import com.freelance.speakflow.data.WordAnalysis
import com.freelance.speakflow.ui.theme.PurplePrimary

@Composable
fun SpeakingAnalysisLayout(
    overallScore: Int,
    fluency: Int,
    clarity: Int,
    accent: Int,
    transcript: String,
    tips: List<String>,
    wordFeedback: List<WordAnalysis>,
    progress: ProgressUpdateResponse?, // ✅ Added Progress Data
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        item {
            Text(
                "Your Speaking Analysis",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // OVERALL SCORE & XP CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Overall Score", fontWeight = FontWeight.Bold)
                    Text(
                        "$overallScore%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )

                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color.LightGray)
                    Spacer(Modifier.height(16.dp))

                    // XP & Streak Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total XP", fontSize = 12.sp)
                            Text(
                                "${progress?.totalXp ?: "..."}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Streak", fontSize = 12.sp)
                            Text(
                                "${progress?.currentStreak ?: "..."} 🔥",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // METRICS
        item {
            MetricRow("Fluency", fluency)
            MetricRow("Clarity", clarity)
            MetricRow("Accent", accent)
        }

        // TRANSCRIPT
        item {
            Text("What you said", fontWeight = FontWeight.Bold)
            Text(transcript, color = Color.DarkGray)
        }

        // TIPS
        item {
            Text("Tips to Improve", fontWeight = FontWeight.Bold)
            tips.forEach { tip ->
                Text("• $tip")
            }
        }

        // WORD FEEDBACK
        item {
            Text("Word Feedback", fontWeight = FontWeight.Bold)
        }

        items(wordFeedback) { wf ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(wf.word)
                Text(
                    wf.status.uppercase(),
                    color = when (wf.status) {
                        "perfect" -> Color(0xFF2ECC71)
                        "good" -> Color(0xFFF1C40F)
                        else -> Color(0xFFE74C3C)
                    }
                )
            }
        }

        // BACK BUTTON
        item {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Back to Lessons")
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text("$value%")
    }
}