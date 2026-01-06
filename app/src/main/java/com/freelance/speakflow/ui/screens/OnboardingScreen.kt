package com.freelance.speakflow.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Data model for each page
data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val mascotType: MascotType
)

enum class MascotType { BLUE, PURPLE, GREEN }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    // Define the pages
    val pages = listOf(
        OnboardingPage(
            title = "Speak",
            subtitle = "Practice pronunciation with AI feedback",
            icon = Icons.Default.RecordVoiceOver,
            color = Color(0xFF4285F4), // Blue
            mascotType = MascotType.BLUE
        ),
        OnboardingPage(
            title = "Play",
            subtitle = "Learn through fun games with monsters",
            icon = Icons.Default.Gamepad,
            color = Color(0xFFA855F7), // Purple
            mascotType = MascotType.PURPLE
        ),
        OnboardingPage(
            title = "Level Up",
            subtitle = "Track progress and unlock achievements",
            icon = Icons.Default.EmojiEvents, // Trophy
            color = Color(0xFF22C55E), // Green
            mascotType = MascotType.GREEN
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE)) // Very light gray/blue background
            .padding(24.dp)
    ) {
        // --- SKIP BUTTON (Optional, typically top right) ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(onClick = onFinished) {
                    Text("Skip", color = Color.Gray)
                }
            } else {
                Spacer(Modifier.height(48.dp)) // Maintain spacing
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- PAGER SECTION ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            OnboardingPageContent(page = pages[index])
        }

        Spacer(Modifier.height(32.dp))

        // --- BOTTOM NAVIGATION SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Back Button (Hide on first page)
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp)) // Light gray square
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            } else {
                Spacer(Modifier.size(50.dp)) // Invisible spacer to keep layout balanced
            }

            // 2. Pagination Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        Color(0xFFA855F7) // Active Purple
                    else
                        Color.LightGray

                    val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .background(color, CircleShape)
                    )
                }
            }

            // 3. Next / Get Started Button
            TextButton(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                }
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // --- MASCOT ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp) // Large Mascot
        ) {
            DynamicMascot(page.mascotType, page.color)
        }

        Spacer(Modifier.height(48.dp))

        // --- ICON ---
        // Small icon floating above text (Head, Controller, Trophy)
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFF374151) // Dark Gray
        )

        Spacer(Modifier.height(16.dp))

        // --- TEXT ---
        Text(
            text = page.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = page.subtitle,
            fontSize = 18.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
            lineHeight = 26.sp
        )
    }
}

// --- CUSTOM MASCOT DRAWING ---
// Reuses the logic from splash but adapts based on Type
@Composable
fun DynamicMascot(type: MascotType, color: Color) {
    Box(
        modifier = Modifier.size(200.dp)
    ) {
        // Antenna (Common)
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-15).dp)
                .background(color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .background(color, CircleShape)
        )

        // Ears (Common)
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-15).dp)
                .background(color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 15.dp)
                .background(color, CircleShape)
        )

        // Head (Gradient)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(color.copy(alpha = 0.8f), color)
                    ),
                    shape = CircleShape
                )
        )

        // Eyes (Common)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            MascotEye()
            MascotEye()
        }

        // Mouth / Expression (Dynamic)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-50).dp)
        ) {
            when (type) {
                MascotType.BLUE -> {
                    // Smile
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(30.dp) // Half circle
                            .background(Color(0xFF1F2937), RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    )
                }
                MascotType.PURPLE -> {
                    // Open Mouth (Surprised/Excited)
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(50.dp)
                            .background(Color(0xFF1F2937), RoundedCornerShape(20.dp))
                    )
                }
                MascotType.GREEN -> {
                    // Neutral / Small Smile
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(10.dp)
                            .background(Color(0xFF1F2937), CircleShape)
                    )
                }
            }
        }
    }
}

// Reusing the Eye from Splash
@Composable
fun MascotEye() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color(0xFF1F2937), CircleShape)
        )
    }
}