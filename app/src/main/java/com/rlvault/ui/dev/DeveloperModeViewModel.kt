package com.rlvault.ui.dev

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.repository.AchievementRepository
import com.rlvault.util.AchievementEvaluator
import kotlinx.coroutines.launch

class DeveloperModeViewModel(
    private val achievementEvaluator: AchievementEvaluator,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _statusText = MutableLiveData("")
    val statusText: LiveData<String> = _statusText

    fun recalculateStatistics() {
        viewModelScope.launch {
            val newlyUnlocked = achievementEvaluator.evaluateAll()
            _statusText.value = if (newlyUnlocked.isEmpty()) {
                "Recalculated — no new achievements unlocked."
            } else {
                "Recalculated — unlocked: ${newlyUnlocked.joinToString(", ") { it.title }}"
            }
        }
    }

    fun resetAchievementProgress() {
        viewModelScope.launch {
            achievementRepository.resetAllProgress()
            _statusText.value = "Achievement progress reset."
        }
    }

    class Factory(
        private val achievementEvaluator: AchievementEvaluator,
        private val achievementRepository: AchievementRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DeveloperModeViewModel(achievementEvaluator, achievementRepository) as T
        }
    }
}
