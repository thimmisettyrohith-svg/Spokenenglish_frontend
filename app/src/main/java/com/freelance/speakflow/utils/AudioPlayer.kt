package com.freelance.speakflow.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayer(context: Context) {
    private var player: ExoPlayer? = null

    init {
        player = ExoPlayer.Builder(context).build()
    }

    fun playUrl(url: String) {
        if (url.isBlank()) return

        try {
            player?.stop()
            player?.clearMediaItems()

            // Auto-play the URL
            val mediaItem = MediaItem.fromUri(url)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}