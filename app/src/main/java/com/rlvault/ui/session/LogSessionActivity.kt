package com.rlvault.ui.session

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rlvault.databinding.ActivityLogSessionBinding
import com.rlvault.di.ServiceLocator

/**
 * Manual session entry — the "manually entered" half of Session.isAutomatic. The auto-tracked
 * Start/End Session flow (startAutomaticSession/endAutomaticSession) is a separate, later screen;
 * this one just logs wins/losses/rank/notes for a session after the fact.
 */
class LogSessionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogSessionBinding

    private val viewModel: LogSessionViewModel by viewModels {
        LogSessionViewModel.Factory(ServiceLocator.sessionRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            val wins = binding.winsInput.text.toString().toIntOrNull() ?: 0
            val losses = binding.lossesInput.text.toString().toIntOrNull() ?: 0
            val rank = binding.rankInput.text.toString().ifBlank { null }
            val notes = binding.notesInput.text.toString().ifBlank { null }
            viewModel.logSession(wins, losses, rank, notes)
        }

        viewModel.statusText.observe(this) { status ->
            binding.statusText.text = status
            if (status != null && status.startsWith("Saved")) finish()
        }
    }
}
