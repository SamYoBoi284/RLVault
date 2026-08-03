package com.rlvault.ui.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Session
import com.rlvault.data.repository.SessionRepository
import kotlinx.coroutines.launch

class AutomaticSessionViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    private val _activeSession = MutableLiveData<Session?>()
    val activeSession: LiveData<Session?> = _activeSession

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status


    init {
        loadActiveSession()
    }


    fun loadActiveSession() {
        viewModelScope.launch {

            val sessions = repository.getAll()

            val active = sessions.firstOrNull {
                it.isAutomatic && it.endTime == null
            }

            _activeSession.value = active
        }
    }


    fun startSession() {
        viewModelScope.launch {

            val id = repository.startAutomaticSession(
                System.currentTimeMillis()
            )

            val session = repository.getById(id)

            _activeSession.value = session

            _status.value = "Session started"
        }
    }


    fun endSession(
        wins: Int,
        losses: Int,
        rank: String?,
        notes: String?
    ) {
        viewModelScope.launch {

            val session = _activeSession.value ?: return@launch


            repository.endAutomaticSession(
                sessionId = session.id,
                endTime = System.currentTimeMillis(),
                wins = wins,
                losses = losses,
                rank = rank,
                notes = notes
            )


            _activeSession.value = null

            _status.value = "Session saved"
        }
    }


    class Factory(
        private val repository: SessionRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (modelClass.isAssignableFrom(
                    AutomaticSessionViewModel::class.java
                )
            ) {
                @Suppress("UNCHECKED_CAST")
                return AutomaticSessionViewModel(repository)
                    as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel"
            )
        }
    }
}