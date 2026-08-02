package com.rlvault.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.repository.AchievementRepository
import com.rlvault.data.repository.ClipRepository
import com.rlvault.data.repository.SessionRepository
import com.rlvault.util.AchievementEvaluator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private const val PLACEHOLDER = "—"

/**
 * Loads real Home screen data from the repositories. On init: runs the achievement evaluator
 * first (so "latest achievement" reflects anything just unlocked), then pulls the three stats.
 * All three LiveData fields start at [PLACEHOLDER] and flip to real values once the coroutine
 * finishes — no data means the app is empty, not broken (e.g. before any clips are imported).
 */
class HomeViewModel(
    private val clipRepository: ClipRepository,
    private val sessionRepository: SessionRepository,
    private val achievementRepository: AchievementRepository,
    private val achievementEvaluator: AchievementEvaluator
) : ViewModel() {

    private val _pendingReviewText = MutableLiveData(PLACEHOLDER)
    val pendingReviewText: LiveData<String> = _pendingReviewText

    private val _lastSessionText = MutableLiveData(PLACEHOLDER)
    val lastSessionText: LiveData<String> = _lastSessionText

    private val _latestAchievementText = MutableLiveData(PLACEHOLDER)
    val latestAchievementText: LiveData<String> = _latestAchievementText

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    init {
        refresh()
    }

    /** Re-runs the evaluator and reloads all three stats. Safe to call repeatedly (e.g. onResume). */
    fun refresh() {
        viewModelScope.launch {
            achievementEvaluator.evaluateAll()

            val unreviewedCount = clipRepository.getUnreviewed().size
            _pendingReviewText.value = unreviewedCount.toString()

            val latestSession = sessionRepository.getLatest()
            _lastSessionText.value = if (latestSession != null) {
                "${dateFormat.format(java.util.Date(latestSession.date))} — ${latestSession.wins}W / ${latestSession.losses}L"
            } else {
                "No sessions yet"
            }

            val latestAchievement = achievementRepository.getUnlocked().firstOrNull()
            _latestAchievementText.value = latestAchievement?.title ?: "None yet"
        }
    }

    /** Manual factory — ViewModel has constructor args, so the default no-arg factory won't do. */
    class Factory(
        private val clipRepository: ClipRepository,
        private val sessionRepository: SessionRepository,
        private val achievementRepository: AchievementRepository,
        private val achievementEvaluator: AchievementEvaluator
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                clipRepository,
                sessionRepository,
                achievementRepository,
                achievementEvaluator
            ) as T
        }
    }
}
