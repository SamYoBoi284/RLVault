package com.rlvault.ui.clip

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.rlvault.databinding.ActivityImportClipBinding
import com.rlvault.di.ServiceLocator
import com.rlvault.util.ClipFolderPrefs

/**
 * Folder-based clip detection (spec: "user selects ONE folder on first launch"). First launch (or
 * after "Change Folder") shows a folder picker; once a folder's chosen, its URI is persisted via
 * [ClipFolderPrefs] and every subsequent visit just re-scans it, diffing against what's already
 * indexed and reporting only the delta ("N new clips found."). A manual single-file picker stays
 * available underneath for one-off adds outside the selected folder.
 */
class ImportClipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportClipBinding

    private val viewModel: ImportClipViewModel by viewModels {
        ImportClipViewModel.Factory(ServiceLocator.clipRepository)
    }

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) handleFolderPicked(uri)
    }

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) handleSingleFilePicked(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportClipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectFolderButton.setOnClickListener {
            pickFolder.launch(null)
        }
        binding.rescanButton.setOnClickListener { rescanSavedFolder() }
        binding.changeFolderButton.setOnClickListener { pickFolder.launch(null) }
        binding.pickFileButton.setOnClickListener {
            pickFile.launch(arrayOf("video/*"))
        }

        viewModel.statusText.observe(this) { binding.statusText.text = it }

        refreshFolderUi()
    }

    override fun onResume() {
        super.onResume()
        // Covers the case where the user added files to the folder while the app was backgrounded
        // and comes back to this screen — rescan so the count is current without an extra tap.
        if (ClipFolderPrefs.getFolderUri(this) != null) rescanSavedFolder()
    }

    private fun handleFolderPicked(treeUri: Uri) {
        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        ClipFolderPrefs.setFolderUri(this, treeUri.toString())
        refreshFolderUi()
        scanUri(treeUri)
    }

    private fun rescanSavedFolder() {
        val saved = ClipFolderPrefs.getFolderUri(this) ?: return
        scanUri(Uri.parse(saved))
    }

    private fun scanUri(treeUri: Uri) {
        val root = DocumentFile.fromTreeUri(this, treeUri)
        if (root == null || !root.isDirectory) {
            binding.statusText.text = "Couldn't open that folder — try selecting it again."
            return
        }
        viewModel.scanFolder(root)
    }

    private fun refreshFolderUi() {
        val saved = ClipFolderPrefs.getFolderUri(this)
        val hasFolder = saved != null
        binding.selectFolderButton.visibility = if (hasFolder) android.view.View.GONE else android.view.View.VISIBLE
        binding.rescanButton.visibility = if (hasFolder) android.view.View.VISIBLE else android.view.View.GONE
        binding.changeFolderButton.visibility = if (hasFolder) android.view.View.VISIBLE else android.view.View.GONE

        binding.folderPathText.text = if (hasFolder) {
            DocumentFile.fromTreeUri(this, Uri.parse(saved))?.name?.let { "Folder: $it" }
                ?: "Folder selected"
        } else {
            "No folder selected yet."
        }
    }

    private fun handleSingleFilePicked(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val displayName = queryDisplayName(uri)
        viewModel.importSingleFile(uri.toString(), displayName)
    }

    private fun queryDisplayName(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }
}
