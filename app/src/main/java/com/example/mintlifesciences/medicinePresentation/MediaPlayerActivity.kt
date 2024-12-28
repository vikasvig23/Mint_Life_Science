package com.example.mintlifesciences.medicinePresentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import androidx.appcompat.app.AppCompatActivity
import com.example.mintlifesciences.R

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        playerView = findViewById(R.id.player_view)

        val mediaUrl = intent.getStringExtra("MEDIA_URL")
        Log.d("MediaPlayerActivity", "Received media URL: $mediaUrl")

        if (!mediaUrl.isNullOrEmpty()) {
            initializePlayer(mediaUrl)
        } else {
            Toast.makeText(this, "Media URL is missing!", Toast.LENGTH_SHORT).show()
            Log.e("MediaPlayerActivity", "Media URL is null or empty!")
        }
    }

    private fun initializePlayer(mediaUrl: String) {
        try {
            if (mediaUrl.contains("youtube.com") || mediaUrl.contains("youtu.be")) {

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setPackage("com.google.android.youtube")

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "YouTube app is not installed", Toast.LENGTH_SHORT).show()
                }
            } else {
                exoPlayer = ExoPlayer.Builder(this).build()

                playerView.player = exoPlayer

                Log.d("MediaPlayerActivity", "Initializing ExoPlayer with URL: $mediaUrl")
                val mediaItem = MediaItem.fromUri(Uri.parse(mediaUrl))
                exoPlayer?.setMediaItem(mediaItem)

                exoPlayer?.prepare()
                exoPlayer?.playWhenReady = true
            }
        } catch (e: Exception) {
            Log.e("MediaPlayerActivity", "Error initializing ExoPlayer", e)
            Toast.makeText(this, "Error initializing media player", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
        exoPlayer = null
        Log.d("MediaPlayerActivity", "ExoPlayer released")
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
