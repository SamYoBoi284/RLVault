package com.rlvault.ui.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Session
import com.rlvault.data.repository.SessionRepository
import kotlinx.coroutines.launch

class LogSessionViewModel(private val sessionRepository: SessionRepository) : ViewModel() {

    private val _statusText = MutableLiveData<String?>(null)
    val statusText: LiveData<String?> = _statusText

    fun logSession(wins: Int, losses: Int, rank: String?, notes: String?) {
        viewModelScope.launch {
            sessionRepository.insert(
                Session(
                    isAutomatic = false,
                    date = System.currentTimeMillis(),
                    wins = wins,
                    losses = losses,
                    rank = rank,
                    notes = notes
                )
            )
            _statusText.value = "Saved."
        }
    }

    class Factory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LogSessionViewModel(sessionRepository) as T
        }
    }
}
