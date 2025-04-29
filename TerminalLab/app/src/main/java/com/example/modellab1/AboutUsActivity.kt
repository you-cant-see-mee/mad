package com.example.modellab1  // replace with your actual package

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class AboutUsActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var seekBar: SeekBar
    private val handler = Handler(Looper.getMainLooper())
    private var isSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        videoView = findViewById(R.id.localVideoView)
        seekBar = findViewById(R.id.seekBar)

        val playButton: Button = findViewById(R.id.btnPlay)
        val pauseButton: Button = findViewById(R.id.btnPause)
        val stopButton: Button = findViewById(R.id.btnStop)

        val uri = Uri.parse("android.resource://$packageName/${R.raw.intro}")
        videoView.setVideoURI(uri)

        playButton.setOnClickListener {
            videoView.start()
        }

        pauseButton.setOnClickListener {
            videoView.pause()
        }

        stopButton.setOnClickListener {
            videoView.stopPlayback()
            videoView.setVideoURI(uri)
        }

        // SeekBar sync
        videoView.setOnPreparedListener {
            seekBar.max = videoView.duration
            updateSeekBar()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isSeeking = false
            }
        })

        // YouTube Video
        val youTubePlayerView = findViewById<YouTubePlayerView>(R.id.youtubePlayerView)
        lifecycle.addObserver(youTubePlayerView)

        var youTubePlayerInstance: YouTubePlayer? = null

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayerInstance = player
                player.loadVideo("dQw4w9WgXcQ", 0f) // Replace with your video ID
            }
        })
        youTubePlayerView.setOnClickListener {
            youTubePlayerInstance?.play()
        }

        // Social Media Buttons
        findViewById<Button>(R.id.btnInstagram).setOnClickListener {
            openUrl("https://www.instagram.com/your_profile")
        }

        findViewById<Button>(R.id.btnFacebook).setOnClickListener {
            openUrl("https://www.facebook.com/your_profile")
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isSeeking && videoView.isPlaying) {
                    seekBar.progress = videoView.currentPosition
                }
                handler.postDelayed(this, 500)
            }
        }, 0)
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
