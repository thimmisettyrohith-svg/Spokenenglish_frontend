package com.freelance.speakflow.data

// This matches the JSON object inside "items": []
data class VocabPreviewItem(
    val word: String,

    // AI Image URL (e.g., "https://image.pollinations.ai/...")
    val image: String,

    // AI Audio URL (e.g., "http://.../static/audio/cat.mp3")
    val audio: String
)