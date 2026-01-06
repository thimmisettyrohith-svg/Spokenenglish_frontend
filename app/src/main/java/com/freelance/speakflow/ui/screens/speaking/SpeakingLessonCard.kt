package com.freelance.speakflow.ui.screens.speaking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freelance.speakflow.data.SpeakingLesson

@Composable
fun SpeakingLessonCard(
    lesson: SpeakingLesson,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !lesson.isLocked) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (lesson.isLocked)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = lesson.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = lesson.progressPercent / 100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = when {
                    lesson.isLocked -> "Locked"
                    lesson.progressPercent >= 100 -> "Completed"
                    else -> "In progress"
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (lesson.isLocked) Color.Red else Color.Green
            )
        }
    }
}
