package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.data.DashboardResponse
import com.freelance.speakflow.data.RetrofitInstance

@Composable
fun GamesScreen(
    userId: Int, // ✅ Added userId to fetch stats
    onGameClick: (String) -> Unit
) {
    val context = LocalContext.current
    var dashboardData by remember { mutableStateOf<DashboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ FETCH REAL STATS
    LaunchedEffect(userId) {
        try {
            dashboardData = RetrofitInstance.api.getDashboard(userId)
        } catch (e: Exception) {
            // Silent fail or toast
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFFA726))
        }
        return
    }

    val stats = dashboardData // Nullable check handled by logic or defaults below
    val currentLevel = stats?.currentLevel ?: 1
    val totalXp = stats?.totalXp ?: 0
    val progress = stats?.levelProgress ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp)
    ) {
        // --- Header ---
        Text(
            text = "Game Zone",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        // --- Level Progress (Dynamic) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Level $currentLevel", fontWeight = FontWeight.SemiBold)
                    Text("$totalXp XP", fontWeight = FontWeight.Bold, color = Color(0xFFFFA726))
                }

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFFFFA726),
                    trackColor = Color(0xFFFFF3E0)
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    "Keep playing to level up!",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Available Games", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))

        // --- Game List ---

        // 1. Echo Game
        GameItemCard(
            title = "Echo Game",
            status = "Play Now",
            iconBg = Color(0xFFFFF3E0),
            iconColor = Color(0xFFFFA726),
            isLocked = false,
            onClick = { onGameClick("echo_game") }
        )

        Spacer(Modifier.height(16.dp))

        // 2. Speed Speak
        GameItemCard(
            title = "Speed Speak",
            status = "Play Now",
            iconBg = Color(0xFFFFEBEE),
            iconColor = Color(0xFFEF5350), // Red
            isLocked = false,
            onClick = { onGameClick("speed_race") }
        )

        Spacer(Modifier.height(16.dp))

        // 3. Voice Match
        GameItemCard(
            title = "Voice Match",
            status = "Play Now",
            iconBg = Color(0xFFE8F5E9),
            iconColor = Color(0xFF66BB6A), // Green
            isLocked = false,
            onClick = { onGameClick("voice_match") }
        )
    }
}

@Composable
fun GameItemCard(
    title: String,
    status: String,
    iconBg: Color,
    iconColor: Color,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(iconBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    status,
                    fontSize = 12.sp,
                    color = if (isLocked) Color.Gray else iconColor
                )
            }
        }
    }
}