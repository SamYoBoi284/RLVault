package com.rlvault.ui.clip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Clip
import com.rlvault.data.repository.ClipRepository
import kotlinx.coroutines.launch

class ImportClipViewModel(private val clipRepository: ClipRepository) : ViewModel() {

    private val _statusText = MutableLiveData("Pick a video file to index it as a clip.")
    val statusText: LiveData<String> = _statusText

    fun importClip(filePath: String, displayName: String?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            clipRepository.insert(
                Clip(
                    filePath = filePath,
                    title = displayName,
                    createdAt = now, // no reliable file-creation timestamp available via SAF URI;
                    importedAt = now // both stamped as "now" until real metadata reading exists
                )
            )
            _statusText.value = "Imported: ${displayName ?: filePath}"
        }
    }

    class Factory(private val clipRepository: ClipRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ImportClipViewModel(clipRepository) as T
        }
    }
}
