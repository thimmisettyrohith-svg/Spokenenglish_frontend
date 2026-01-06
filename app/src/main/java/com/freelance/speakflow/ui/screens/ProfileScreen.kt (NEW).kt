package com.freelance.speakflow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.freelance.speakflow.data.WeeklyActivityItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(userId: Int) {
    val context = LocalContext.current

    // Data States
    var weeklyData by remember { mutableStateOf<List<WeeklyActivityItem>>(emptyList()) }
    var userProfile by remember { mutableStateOf<DashboardResponse?>(null) } // ✅ Added to store user info

    var isLoading by remember { mutableStateOf(true) }

    // Fetch BOTH Weekly Stats AND User Profile
    LaunchedEffect(userId) {
        // Use async to fetch both in parallel
        val scope = this
        try {
            // 1. Fetch Graph Data
            val weeklyDeferred = scope.async { RetrofitInstance.api.getWeeklyStats(userId) }

            // 2. Fetch User Details (Name, Level, etc.)
            val profileDeferred = scope.async { RetrofitInstance.api.getDashboard(userId) }

            val weeklyResponse = weeklyDeferred.await()
            val profileResponse = profileDeferred.await()

            weeklyData = weeklyResponse.activities
            userProfile = profileResponse // ✅ Store user details

        } catch (e: Exception) {
            e.printStackTrace()
            // Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. PROFILE HEADER ---
        item {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .padding(16.dp),
                tint = Color.White
            )
            Spacer(Modifier.height(16.dp))

            // ✅ DISPLAY REAL USERNAME
            Text(
                text = userProfile?.userName ?: "User #$userId",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // ✅ DISPLAY REAL LEVEL
            Text(
                text = "Level ${userProfile?.currentLevel ?: 1} Learner",
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))
        }

        // --- 2. ACTIVITY GRAPH ---
        item {
            Text(
                "Weekly Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().height(250.dp)
            ) {
                // Determine max XP for scaling
                val maxXp = weeklyData.maxOfOrNull { it.xp } ?: 100
                val safeMax = if (maxXp == 0) 100 else maxXp

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Ensure we display something even if data is empty (placeholder days)
                    val displayData = if (weeklyData.isEmpty()) {
                        listOf(
                            WeeklyActivityItem("Mon", 0, "Mon"),
                            WeeklyActivityItem("Tue", 0, "Tue"),
                            WeeklyActivityItem("Wed", 0, "Wed"),
                            WeeklyActivityItem("Thu", 0, "Thu"),
                            WeeklyActivityItem("Fri", 0, "Fri"),
                            WeeklyActivityItem("Sat", 0, "Sat"),
                            WeeklyActivityItem("Sun", 0, "Sun")
                        )
                    } else weeklyData

                    displayData.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            // The Bar
                            val heightFraction = (day.xp.toFloat() / safeMax.toFloat()).coerceIn(0.1f, 1f)

                            // XP Label above bar (Optional, if space permits)
                            if(day.xp > 0) {
                                Text(day.xp.toString(), fontSize = 10.sp, color = Color.DarkGray)
                            }

                            Box(
                                modifier = Modifier
                                    .width(16.dp) // Slightly thinner bars
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (day.xp > 0) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
                                    )
                            )

                            Spacer(Modifier.height(8.dp))

                            // Day Label
                            Text(day.dayName, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}