package com.mintlifescience.app.medicinePresentation

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import androidx.appcompat.app.AppCompatActivity
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
        // Create ExoPlayer instance
        exoPlayer = ExoPlayer.Builder(this).build()

        // Bind the ExoPlayer instance to the PlayerView
        playerView.player = exoPlayer

        // Create media source from the URL
        val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))
        exoPlayer?.setMediaItem(mediaItem)

        // Prepare and start the player
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }


    override fun onStop() {
        super.onStop()
        // Release the player when the activity is stopped
        exoPlayer?.release()
    }
}
