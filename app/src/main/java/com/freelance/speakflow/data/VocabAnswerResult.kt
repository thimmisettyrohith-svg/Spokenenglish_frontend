package com.freelance.speakflow.data

data class VocabAnswerResult(
    val word: String,        // Correct word (e.g., "Lion")
    val image: String,       // Image reference (emoji or image name/url)
    val isCorrect: Boolean   // User answered correctly or not
)
