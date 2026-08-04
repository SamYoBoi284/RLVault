package com.rlvault.ui.session


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Session
import com.rlvault.data.repository.SessionRepository
import kotlinx.coroutines.launch



class SessionHistoryViewModel(
    private val repository: SessionRepository
) : ViewModel() {


    private val _sessions =
        MutableLiveData<List<Session>>()


    val sessions:
            LiveData<List<Session>> =
        _sessions



    fun loadSessions() {


        viewModelScope.launch {


            val history =
                repository.getAll()
                    .filter {
                        it.endTime != null
                    }
                    .sortedByDescending {
                        it.date
                    }


            _sessions.value =
                history
        }
    }



    class Factory(
        private val repository: SessionRepository
    ) : ViewModelProvider.Factory {


        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {


            if (
                modelClass.isAssignableFrom(
                    SessionHistoryViewModel::class.java
                )
            ) {


                @Suppress("UNCHECKED_CAST")

                return SessionHistoryViewModel(
                    repository
                ) as T

            }


            throw IllegalArgumentException(
                "Unknown ViewModel"
            )
        }
    }
}