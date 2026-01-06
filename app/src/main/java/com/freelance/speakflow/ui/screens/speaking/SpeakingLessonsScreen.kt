package com.freelance.speakflow.ui.screens.speaking

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelance.speakflow.data.RetrofitInstance
import com.freelance.speakflow.data.SpeakingLesson

@Composable
fun SpeakingLessonsScreen(
    userId: Int,
    onLessonClick: (SpeakingLesson) -> Unit,
    onBack: () -> Unit // ✅ Added Back Callback
) {
    val context = LocalContext.current

    var lessons by remember { mutableStateOf<List<SpeakingLesson>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            lessons = RetrofitInstance.api
                .getSpeakingLessons(userId)
                .payload
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load speaking lessons", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background
    ) {
        // ================= HEADER =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() } // ✅ Handle Click
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Speaking Practice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ================= CONTENT =================
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lessons) { lesson ->
                    SpeakingLessonCard(
                        lesson = lesson,
                        onClick = { onLessonClick(lesson) }
                    )
                }
            }
        }
    }
}