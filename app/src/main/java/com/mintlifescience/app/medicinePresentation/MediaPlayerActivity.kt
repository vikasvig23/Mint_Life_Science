package com.mintlifescience.app.medicinePresentation

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mintlifescience.app.R

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        playerView = findViewById(R.id.player_view)

        val mediaUrl = intent.getStringExtra("MEDIA_URL")
        if (mediaUrl != null) {
            initializePlayer(mediaUrl)
        } else {
            Toast.makeText(this, "Media URL is missing!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializePlayer(mediaUrl: String) {
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
        exoPlayer?.setMediaItem(MediaItem.fromUri(Uri.parse(mediaUrl)))
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
        exoPlayer = null
    }
}
