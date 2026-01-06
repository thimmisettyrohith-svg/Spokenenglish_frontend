package com.freelance.speakflow.ui.screens.vocab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.freelance.speakflow.R

@Composable
fun VocabTopicSelectionScreen(
    onTopicChosen: (String) -> Unit,
    onBack: () -> Unit
) {
    // ONLY topics that actually exist in backend + database
    val topics = remember {
        listOf(
            VocabTopic(
                id = "animals",
                title = "Animals",
                imageRes = R.drawable.animals
            ),
            VocabTopic(
                id = "food",
                title = "Food",
                imageRes = R.drawable.food
            ),
            VocabTopic(
                id = "colors",
                title = "Colors",
                imageRes = R.drawable.colors
            ),
            VocabTopic(
                id = "clothing",
                title = "Clothing",
                imageRes = R.drawable.clothing
            )
        )
    }

    VocabTopicSelectionLayout(
        topics = topics,
        onTopicSelected = onTopicChosen,
        onBack = onBack
    )
}
