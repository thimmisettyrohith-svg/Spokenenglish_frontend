package com.freelance.speakflow.ui.screens.vocab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.freelance.speakflow.data.QuizQuestion
import com.freelance.speakflow.data.VocabOption
import com.freelance.speakflow.ui.theme.PurplePrimary

@Composable
fun VocabGameLayout(
    currentQuestion: QuizQuestion,
    questionIndex: Int,
    totalQuestions: Int,
    onPlayAudio: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Progress Bar
            LinearProgressIndicator(
                progress = { (questionIndex + 1) / totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = PurplePrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Question ${questionIndex + 1} / $totalQuestions",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Listen and click the correct image",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // 2. Audio Button
            Button(
                onClick = onPlayAudio,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                modifier = Modifier.size(80.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Play Audio",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(32.dp))

            // 3. Grid of Images
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentQuestion.options) { option ->
                    VocabOptionCard(
                        option = option,
                        onClick = { onOptionSelected(option.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VocabOptionCard(
    option: VocabOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Slightly taller for images
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // ✅ FIXED: Using Coil to load image from URL
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(option.image)
                    .crossfade(true)
                    .build(),
                contentDescription = "Option Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                },
                error = {
                    // Fallback if image fails (show the word or an icon)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("❌ No Image", color = Color.Red)
                    }
                }
            )
        }
    }
}