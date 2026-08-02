package com.rlvault.ui.player

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rlvault.databinding.ActivityPlayerBinding

/**
 * Plays a clip's stored content:// URI directly in-app via VideoView, instead of handing off to
 * whatever external video app the phone has. RL Vault still never copies/moves the file — this
 * only reads the same URI ImportClip already took a persistable permission for.
 */
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        if (filePath == null) {
            finish()
            return
        }

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.setVideoURI(Uri.parse(filePath))

        binding.videoView.setOnPreparedListener { it.start() }
        binding.videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Couldn't play this clip.", Toast.LENGTH_LONG).show()
            finish()
            true
        }
    }

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"
    }
}
