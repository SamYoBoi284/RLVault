package com.rlvault.ui.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Clip
import com.rlvault.data.repository.ClipRepository
import kotlinx.coroutines.launch

class ReviewedClipListViewModel(private val clipRepository: ClipRepository) : ViewModel() {

    private val _clips = MutableLiveData<List<Clip>>(emptyList())
    val clips: LiveData<List<Clip>> = _clips

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _clips.value = clipRepository.getReviewed()
        }
    }

    class Factory(private val clipRepository: ClipRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ReviewedClipListViewModel(clipRepository) as T
        }
    }
}
