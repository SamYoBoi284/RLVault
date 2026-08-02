package com.rlvault.ui.clip

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rlvault.databinding.ActivityImportClipBinding
import com.rlvault.di.ServiceLocator

/**
 * Milestone 2 first cut: pick a single video file via the system file picker (SAF) and index it
 * as a Clip row. RL Vault never copies/moves the file — [ClipRepository] only ever stores the
 * content:// URI string, per Clip.filePath's contract.
 */
class ImportClipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportClipBinding

    private val viewModel: ImportClipViewModel by viewModels {
        ImportClipViewModel.Factory(ServiceLocator.clipRepository)
    }

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) handlePickedFile(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportClipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pickFileButton.setOnClickListener {
            pickFile.launch(arrayOf("video/*"))
        }

        viewModel.statusText.observe(this) { binding.statusText.text = it }
    }

    private fun handlePickedFile(uri: Uri) {
        // Without this, the URI permission only lasts for this process — we need to read the
        // file's path back out on every future launch (Home stats, clip list, etc.), not just now.
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val displayName = queryDisplayName(uri)
        viewModel.importClip(uri.toString(), displayName)
    }

    private fun queryDisplayName(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }
}
