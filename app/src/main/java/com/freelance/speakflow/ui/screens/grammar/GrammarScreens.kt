package com.freelance.speakflow.ui.screens.grammar

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.freelance.speakflow.data.GrammarLevelData
import com.freelance.speakflow.data.GrammarQuestion
import com.freelance.speakflow.data.ProgressUpdateRequest
import com.freelance.speakflow.data.ProgressUpdateResponse
import com.freelance.speakflow.data.RetrofitInstance
import kotlinx.coroutines.launch

// ==========================================
// 1. GRAMMAR INTRO SCREEN
// ==========================================
@Composable
fun GrammarIntroScreen(
    levelId: Int,
    onStartGame: (GrammarLevelData) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mascot or Icon
        Text("🧩", fontSize = 60.sp)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Sentence Builder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Arrange the words to form complete sentences.",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(Modifier.height(48.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitInstance.api.getGrammarLevel(levelId)
                            onStartGame(response.payload)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Start Level $levelId", fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}

// ==========================================
// 2. GRAMMAR GAME SCREEN (THE LOGIC)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrammarGameScreen(
    userId: Int, // ✅ Added userId
    levelData: GrammarLevelData,
    onLevelComplete: (Int, ProgressUpdateResponse?) -> Unit // ✅ Updated Callback
) {
    // Game State
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    // API State
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Get Current Question
    val question = levelData.questions.getOrNull(currentIndex)

    if (question == null) {
        // Game Over - Save Progress
        if (!isSaving) {
            isSaving = true
            LaunchedEffect(Unit) {
                try {
                    val request = ProgressUpdateRequest(
                        userId = userId,
                        gameType = "grammar",
                        xpEarned = score, // 10 XP per question
                        score = score
                    )
                    val response = RetrofitInstance.api.updateProgress(request)
                    onLevelComplete(score, response)
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save progress", Toast.LENGTH_SHORT).show()
                    onLevelComplete(score, null)
                }
            }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Question State (Reset when question changes)
    val userSentence = remember(question) { mutableStateListOf<String>() }
    val availablePool = remember(question) { mutableStateListOf(*question.wordsPool.toTypedArray()) }

    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${currentIndex + 1}/${levelData.questions.size}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Score: $score",
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = (currentIndex + 1) / levelData.questions.size.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.height(16.dp))

        // --- Image Prompt ---
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = question.image,
                contentDescription = "Question Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Your Sentence:", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        // --- THE SENTENCE BOX (Target) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .border(
                    width = 2.dp,
                    color = if (isError) Color.Red else if (isSuccess) Color.Green else Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (userSentence.isEmpty()) {
                Text("Tap words below to build...", color = Color.Gray)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userSentence.forEach { word ->
                        SuggestionChip(
                            onClick = {
                                userSentence.remove(word)
                                availablePool.add(word)
                                isError = false
                            },
                            label = { Text(word) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- THE WORD POOL (Source) ---
        Text("Available Words:", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availablePool.forEach { word ->
                AssistChip(
                    onClick = {
                        availablePool.remove(word)
                        userSentence.add(word)
                        isError = false
                    },
                    label = { Text(word) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // --- CHECK BUTTON ---
        Button(
            enabled = userSentence.isNotEmpty(),
            onClick = {
                val formedSentence = userSentence.joinToString(" ")

                if (formedSentence.equals(question.correctSentence, ignoreCase = true)) {
                    isSuccess = true
                    Toast.makeText(context, "Correct! 🎉", Toast.LENGTH_SHORT).show()
                    score += 10
                    currentIndex++
                } else {
                    isError = true
                    Toast.makeText(context, "Try again!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isError) Color.Red else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Check Sentence")
        }
    }
}

// ==========================================
// 3. GRAMMAR RESULT SCREEN
// ==========================================
@Composable
fun GrammarResultScreen(
    score: Int,
    progress: ProgressUpdateResponse?, // ✅ Added Progress Data
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆", fontSize = 80.sp)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Level Completed!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(32.dp))

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Score Earned", color = Color.Gray)
                Text("+$score XP", fontSize = 32.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total XP")
                        Text("${progress?.totalXp ?: "-"}", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Streak")
                        Text("${progress?.currentStreak ?: "-"} 🔥", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Back to Home")
        }
    }
}