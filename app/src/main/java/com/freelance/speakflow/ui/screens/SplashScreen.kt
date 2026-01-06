package com.freelance.speakflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF60A5FA), // Blue
                        Color(0xFFA855F7), // Purple
                        Color(0xFFEC4899)  // Pink
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // --- 1. THE MONSTER MASCOT ICON ---
            MonsterMascot()

            Spacer(Modifier.height(40.dp))

            // --- 2. HEADING & SUBTITLE ---
            Text(
                text = "SpeakMonster",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Learn English by Playing",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // --- 3. FEATURE CARDS ---
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    icon = Icons.Default.Mic,
                    title = "Speak & Practice",
                    subtitle = "Real-time pronunciation feedback"
                )
                FeatureCard(
                    icon = Icons.Default.Gamepad,
                    title = "Play Games",
                    subtitle = "Learn with cute monsters"
                )
                FeatureCard(
                    icon = Icons.Default.Star,
                    title = "Level Up",
                    subtitle = "Track progress & earn rewards"
                )
            }

            Spacer(Modifier.weight(1f))

            // --- 4. START BUTTON ---
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Start Learning",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun FeatureCard(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // Text
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun MonsterMascot() {
    // This recreates the CSS geometric monster logic
    Box(
        modifier = Modifier
            .size(150.dp) // Scaled slightly for fit
    ) {
        // Main Face (Orange/Yellow Gradient Circle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFACC15), Color(0xFFF97316))
                    ),
                    shape = CircleShape
                )
                .shadow(elevation = 10.dp, shape = CircleShape)
        )

        // Eyes Container
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-10).dp), // Move eyes up slightly
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            MonsterEye()
            MonsterEye()
        }

        // Mouth (Semi-circle)
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(30.dp)
                .align(Alignment.Center)
                .offset(y = 40.dp) // Position mouth down
                .background(Color(0xFF1F2937), RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            // Tongue (Red bit)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFFEF4444), CircleShape)
            )
        }

        // Little Horns/Ears (Decorations)
        Box(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 10.dp)
                .background(Color(0xFFFACC15), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 10.dp)
                .background(Color(0xFFF97316), CircleShape)
        )
    }
}

@Composable
fun MonsterEye() {
    // White Sclera
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color.White, CircleShape)
            .shadow(2.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Dark Pupil
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color(0xFF1F2937), CircleShape)
        )
        // Reflection
        Box(
            modifier = Modifier
                .size(6.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
                .background(Color.White, CircleShape)
        )
    }
}