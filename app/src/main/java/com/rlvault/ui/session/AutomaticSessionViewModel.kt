package com.rlvault.ui.session

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Session
import com.rlvault.data.repository.SessionRepository
import kotlinx.coroutines.launch
import java.util.Locale


class AutomaticSessionViewModel(
    private val repository: SessionRepository
) : ViewModel() {


    private val _activeSession = MutableLiveData<Session?>()
    val activeSession: LiveData<Session?> = _activeSession


    private val _elapsedTime =
        MutableLiveData("00:00:00")

    val elapsedTime: LiveData<String> =
        _elapsedTime


    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status


    private val handler = Handler(Looper.getMainLooper())


    private val timerRunnable = object : Runnable {

        override fun run() {

            val session = _activeSession.value

            if (session != null) {

                val elapsed =
                    System.currentTimeMillis() -
                            session.startTime


                _elapsedTime.value =
                    formatDuration(elapsed)


                handler.postDelayed(
                    this,
                    1000
                )
            }
        }
    }

    private fun startTimer() {

    handler.removeCallbacks(
        timerRunnable
    )

    handler.post(
        timerRunnable
    )
}

    init {
        loadActiveSession()
    }


    fun loadActiveSession() {

        viewModelScope.launch {

            val sessions =
                repository.getAll()


            val active =
                sessions.firstOrNull {

                    it.isAutomatic &&
                    it.endTime == null

                }


            _activeSession.value = active


            if (active != null) {

    startTimer()

}
        }
    }


    fun startSession() {

        viewModelScope.launch {


            val id =
                repository.startAutomaticSession(
                    System.currentTimeMillis()
                )


            val session =
                repository.getById(id)


            _activeSession.value = session


            startTimer()


            _status.value =
                "Session started"
        }
    }


    fun endSession(
        wins: Int,
        losses: Int,
        rank: String?,
        notes: String?
    ) {

        viewModelScope.launch {


            val session =
                _activeSession.value
                    ?: return@launch


            repository.endAutomaticSession(

                sessionId = session.id,

                endTime =
                    System.currentTimeMillis(),

                wins = wins,

                losses = losses,

                rank = rank,

                notes = notes
            )


            _activeSession.value = null


            _elapsedTime.value =
                "00:00:00"


            handler.removeCallbacks(
                timerRunnable
            )


            _status.value =
                "Session saved"
        }
    }


    private fun formatDuration(
        milliseconds: Long
    ): String {


        val totalSeconds =
            milliseconds / 1000


        val hours =
            totalSeconds / 3600


        val minutes =
            (totalSeconds % 3600) / 60


        val seconds =
            totalSeconds % 60


        return String.format(

            Locale.getDefault(),

            "%02d:%02d:%02d",

            hours,

            minutes,

            seconds
        )
    }


    override fun onCleared() {

        super.onCleared()

        handler.removeCallbacks(
            timerRunnable
        )
    }



    class Factory(
        private val repository: SessionRepository
    ) : ViewModelProvider.Factory {


        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {


            if (
                modelClass.isAssignableFrom(
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