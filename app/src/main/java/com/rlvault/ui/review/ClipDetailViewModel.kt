package com.rlvault.ui.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rlvault.data.model.Clip
import com.rlvault.data.repository.ClipRepository
import com.rlvault.data.repository.MechanicRepository
import com.rlvault.util.AchievementEvaluator
import kotlinx.coroutines.launch

class ClipDetailViewModel(
    private val clipId: Long,
    private val clipRepository: ClipRepository,
    private val mechanicRepository: MechanicRepository,
    private val achievementEvaluator: AchievementEvaluator
) : ViewModel() {

    private val _clip = MutableLiveData<Clip?>(null)
    val clip: LiveData<Clip?> = _clip

    /** Non-null once a save completes — Activity observes this to know when to finish(). */
    private val _saveResult = MutableLiveData<String?>(null)
    val saveResult: LiveData<String?> = _saveResult

    init {
        viewModelScope.launch {
            _clip.value = clipRepository.getById(clipId)
        }
    }

    /**
     * Parses [mechanicsCsv] as comma-separated freeform tags (get-or-create, so re-typing an
     * existing mechanic name reuses its row rather than duplicating it), saves rating/favorite/
     * notes, marks the clip reviewed, then re-runs the achievement evaluator — this is the one
     * path that can make a `mechanic_count` rule actually fire for the first time.
     */
    fun saveAndMarkReviewed(rating: Int?, favorite: Boolean, notes: String?, mechanicsCsv: String) {
        viewModelScope.launch {
            val current = _clip.value ?: return@launch

            val mechanicNames = mechanicsCsv.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val mechanics = mechanicNames.map { mechanicRepository.getOrCreate(it) }

            clipRepository.update(
                current.copy(rating = rating, favorite = favorite, notes = notes)
            )
            clipRepository.setMechanics(clipId, mechanics.map { it.id })
            clipRepository.markReviewed(clipId)

            val newlyUnlocked = achievementEvaluator.evaluateAll()
            _saveResult.value = if (newlyUnlocked.isEmpty()) {
                "Saved."
            } else {
                "Saved — unlocked: ${newlyUnlocked.joinToString(", ") { it.title }}"
            }
        }
    }

    class Factory(
        private val clipId: Long,
        private val clipRepository: ClipRepository,
        private val mechanicRepository: MechanicRepository,
        private val achievementEvaluator: AchievementEvaluator
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ClipDetailViewModel(
                clipId,
                clipRepository,
                mechanicRepository,
                achievementEvaluator
            ) as T
        }
    }
}
