package com.freelance.speakflow.ui.screens.situations

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.freelance.speakflow.data.ProgressUpdateRequest
import com.freelance.speakflow.data.ProgressUpdateResponse
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.data.SituationDetail
import com.freelance.speakflow.data.TurnOption

// 1. STATE MODEL
sealed class TurnUIState {
    data class Staff(val text: String) : TurnUIState()
    data class UserChoice(val options: List<TurnOption>) : TurnUIState()
    object Completed : TurnUIState()
}

@Composable
fun SituationGameScreen(
    userId: Int, // ✅ Added userId
    situationId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var situationDetail by remember { mutableStateOf<SituationDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch Data
    LaunchedEffect(situationId) {
        try {
            val response = RetrofitInstance.api.getSituationDetail(situationId)
            situationDetail = response.payload
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading: ${e.message}", Toast.LENGTH_SHORT).show()
            onBack()
        } finally {
            isLoading = false
        }
    }

    if (isLoading || situationDetail == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // 2. GAME LOGIC
    val detail = situationDetail!!
    var currentTurnIndex by remember { mutableIntStateOf(0) }
    var totalPoints by remember { mutableIntStateOf(0) }

    // Initialize state with first turn
    var uiState by remember {
        mutableStateOf<TurnUIState>(TurnUIState.Staff(detail.turns[0].aiText))
    }

    // ✅ API SAVING STATE
    var isSaving by remember { mutableStateOf(false) }
    var progressData by remember { mutableStateOf<ProgressUpdateResponse?>(null) }

    // 3. SAVE PROGRESS HANDLER
    if (isSaving) {
        LaunchedEffect(Unit) {
            try {
                val request = ProgressUpdateRequest(
                    userId = userId,
                    gameType = "situations",
                    xpEarned = totalPoints, // Points directly convert to XP here
                    score = totalPoints
                )
                progressData = RetrofitInstance.api.updateProgress(request)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save progress", Toast.LENGTH_SHORT).show()
            } finally {
                isSaving = false
                uiState = TurnUIState.Completed // Move to result screen
            }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Saving Progress...")
            }
        }
        return
    }

    val currentTurn = if (currentTurnIndex < detail.turns.size) detail.turns[currentTurnIndex] else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FF))
            .padding(16.dp)
    ) {

        // 🔹 TOP HEADER (Chips)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val turnDisplay = if(currentTurnIndex < detail.totalTurns) currentTurnIndex + 1 else detail.totalTurns

            GameChip("Turn $turnDisplay/${detail.totalTurns}", Color(0xFFE3EDFF), Color(0xFF304FFE))
            GameChip("⭐ $totalPoints Points", Color(0xFFFFF3C4), Color(0xFFB08C00))
        }

        Spacer(Modifier.height(24.dp))

        // 🔹 DYNAMIC CONTENT
        if (uiState is TurnUIState.Completed) {
            // ✅ COMPLETION SCREEN WITH STATS
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎉", fontSize = 60.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Situation Completed!", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(32.dp))

                    // Stats Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Points Earned", color = Color.Gray)
                            Text("+$totalPoints XP", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))

                            Spacer(Modifier.height(16.dp))
                            Divider()
                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total XP", fontSize = 12.sp, color = Color.Gray)
                                    Text("${progressData?.totalXp ?: "-"}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Streak", fontSize = 12.sp, color = Color.Gray)
                                    Text("${progressData?.currentStreak ?: "-"} 🔥", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("Finish")
                    }
                }
            }
        } else if (currentTurn != null) {
            // Active Game
            when (uiState) {
                is TurnUIState.Staff -> {
                    StaffBubble(
                        text = (uiState as TurnUIState.Staff).text,
                        avatarUrl = currentTurn.aiAvatar
                    )
                }

                is TurnUIState.UserChoice -> {
                    UserBubble()
                    Spacer(Modifier.height(16.dp))

                    // Options List
                    val options = (uiState as TurnUIState.UserChoice).options
                    options.forEach { option ->
                        OptionCard(option) {
                            // 1. Handle Score
                            if (option.isCorrect) {
                                totalPoints += option.points
                                Toast.makeText(context, "Correct! +${option.points}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Incorrect", Toast.LENGTH_SHORT).show()
                            }

                            // 2. Advance Turn
                            currentTurnIndex++
                            if (currentTurnIndex < detail.turns.size) {
                                uiState = TurnUIState.Staff(detail.turns[currentTurnIndex].aiText)
                            } else {
                                // ✅ TRIGGER SAVE INSTEAD OF DIRECT COMPLETION
                                isSaving = true
                            }
                        }
                    }
                }
                else -> {}
            }

            Spacer(Modifier.weight(1f))

            // 🔹 CONTINUE BUTTON (Only shown during Staff Speaking phase)
            if (uiState is TurnUIState.Staff) {
                Button(
                    onClick = {
                        // Switch to User Choice Mode
                        currentTurn.options?.let {
                            uiState = TurnUIState.UserChoice(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5DA9FF))
                ) {
                    Text("Continue", fontSize = 18.sp)
                }
            }
        }
    }
}

// ------------------------------------
// UI COMPONENTS
// ------------------------------------

@Composable
fun StaffBubble(text: String, avatarUrl: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Avatar
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFE0E0E0), CircleShape)
                    .padding(4.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                ChatLabel("Staff", Color(0xFFEDE7FF), Color(0xFF6200EA))
                Spacer(Modifier.height(4.dp))
                Text(text, fontSize = 16.sp, lineHeight = 24.sp)
            }
        }
    }
}

@Composable
fun UserBubble() {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFF5DA9FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("You", color = Color.White, fontSize = 12.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                ChatLabel("You", Color(0xFFE3F2FD), Color(0xFF1565C0))
                Spacer(Modifier.height(4.dp))
                Text("Choose your response:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun OptionCard(option: TurnOption, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            option.text,
            modifier = Modifier.padding(20.dp),
            fontSize = 16.sp
        )
    }
}

@Composable
fun GameChip(text: String, bg: Color, textColor: Color) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(50),
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
        }
    }
}

@Composable
fun ChatLabel(text: String, bg: Color, textColor: Color) {
    Surface(color = bg, shape = RoundedCornerShape(4.dp)) {
        Text(
            text,
            fontSize = 11.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}