package com.freelance.speakflow.ui.screens.vocab

import androidx.annotation.DrawableRes

data class VocabTopic(
    val id: String,
    val title: String,
    @DrawableRes val imageRes: Int
)