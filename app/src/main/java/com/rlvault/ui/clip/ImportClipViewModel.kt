package com.rlvault.ui.clip

import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Clip
import com.rlvault.data.repository.ClipRepository
import kotlinx.coroutines.launch

class ImportClipViewModel(private val clipRepository: ClipRepository) : ViewModel() {

    private val _statusText = MutableLiveData("Select a clip folder to get started.")
    val statusText: LiveData<String> = _statusText

    /** Recursively walks [root], finds video files, diffs them against what's already indexed
     *  by file_path (the content:// document URI), and inserts only the new ones. Spec: "12 new
     *  clips found." — so we report exactly the delta, not the folder's total file count. */
    fun scanFolder(root: DocumentFile) {
        viewModelScope.launch {
            _statusText.value = "Scanning folder…"

            val known = clipRepository.getAllFilePaths()
            val videoFiles = mutableListOf<DocumentFile>()
            collectVideoFiles(root, videoFiles)

            val newFiles = videoFiles.filter { it.uri.toString() !in known }

            if (newFiles.isEmpty()) {
                _statusText.value = "No new clips found."
                return@launch
            }

            val now = System.currentTimeMillis()
            newFiles.forEach { doc ->
                clipRepository.insert(
                    Clip(
                        filePath = doc.uri.toString(),
                        title = doc.name,
                        // SAF doesn't reliably expose a file-creation time distinct from
                        // last-modified across providers, so createdAt falls back to
                        // lastModified() when present, else "now" (matches prior single-file
                        // import behavior, which stamped both as "now").
                        createdAt = doc.lastModified().takeIf { it > 0 } ?: now,
                        importedAt = now
                    )
                )
            }

            _statusText.value = "${newFiles.size} new clip${if (newFiles.size == 1) "" else "s"} found."
        }
    }

    /** Kept for manual/one-off adds (e.g. a clip living outside the selected folder). */
    fun importSingleFile(filePath: String, displayName: String?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            clipRepository.insert(
                Clip(
                    filePath = filePath,
                    title = displayName,
                    createdAt = now,
                    importedAt = now
                )
            )
            _statusText.value = "Imported: ${displayName ?: filePath}"
        }
    }

    private fun collectVideoFiles(dir: DocumentFile, out: MutableList<DocumentFile>) {
        dir.listFiles().forEach { child ->
            when {
                child.isDirectory -> collectVideoFiles(child, out)
                child.isFile && isVideo(child) -> out.add(child)
            }
        }
    }

    private fun isVideo(doc: DocumentFile): Boolean {
        val mime = doc.type
        if (mime != null && mime.startsWith("video/")) return true
        // Some providers report a generic/null mime type for tree children — fall back to
        // extension sniffing so real clips still get picked up.
        val name = doc.name?.lowercase() ?: return false
        return VIDEO_EXTENSIONS.any { name.endsWith(it) }
    }

    class Factory(private val clipRepository: ClipRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ImportClipViewModel(clipRepository) as T
        }
    }

    companion object {
        private val VIDEO_EXTENSIONS = listOf(
            ".mp4", ".mkv", ".mov", ".webm", ".avi", ".3gp", ".m4v"
        )
    }
}
