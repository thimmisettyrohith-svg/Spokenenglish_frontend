package com.freelance.speakflow.ui.screens.speaking

data class SpeakingDialogueResult(
    val targetSentence: String,
    val userSpokenText: String,
    val fluency: Int,
    val clarity: Int,
    val accent: Int
) {
    val overallScore: Int
        get() = (fluency + clarity + accent) / 3
}
