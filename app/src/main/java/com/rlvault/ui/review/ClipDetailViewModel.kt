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

/** One-shot signal for the Activity to react to — never re-delivered on config change re-observe. */
sealed class ClipDetailEvent {
    data class Saved(val message: String) : ClipDetailEvent()
    object DiscardedUnsavedChanges : ClipDetailEvent()
    object QueueComplete : ClipDetailEvent()
}

/**
 * [queueIds] is null in single-edit mode (opened from ReviewedClipListActivity, or any other
 * caller that doesn't pass queue extras) — Save just finishes immediately, no nav row.
 *
 * When non-null, this is the full unreviewed id list snapshotted once at queue-entry by
 * ClipListActivity, so it doesn't reshuffle under the user as clips drop out of the underlying
 * list mid-review. [queuePosition] is the index into it the Activity is currently showing.
 */
class ClipDetailViewModel(
    private val queueIds: LongArray?,
    private var queuePosition: Int,
    private val clipRepository: ClipRepository,
    private val mechanicRepository: MechanicRepository,
    private val achievementEvaluator: AchievementEvaluator
) : ViewModel() {

    val isQueueMode: Boolean get() = queueIds != null
    val queueSize: Int get() = queueIds?.size ?: 0
    /** 1-based position for display, e.g. "Clip 1 of 5". */
    val queuePositionDisplay: Int get() = queuePosition + 1

    private val _clip = MutableLiveData<Clip?>(null)
    val clip: LiveData<Clip?> = _clip

    private val _event = MutableLiveData<ClipDetailEvent?>(null)
    val event: LiveData<ClipDetailEvent?> = _event

    /** Only used in single-edit mode, where there's no queue array to read the id from. */
    var initialClipId: Long = -1L

    init {
        loadCurrent()
    }

    private fun currentClipId(): Long = queueIds?.get(queuePosition) ?: initialClipId

    private fun loadCurrent() {
        viewModelScope.launch {
            _clip.value = clipRepository.getById(currentClipId())
        }
    }

    /**
     * Parses [mechanicsCsv] as comma-separated freeform tags (get-or-create, so re-typing an
     * existing mechanic name reuses its row rather than duplicating it), saves rating/favorite/
     * notes, marks the clip reviewed, then re-runs the achievement evaluator — this is the one
     * path that can make a `mechanic_count` rule actually fire for the first time.
     *
     * Single-edit mode: signals Saved so the Activity finishes.
     * Queue mode: signals Saved, then auto-advances to the next clip (or QueueComplete if this
     * was the last one).
     */
    fun saveAndMarkReviewed(rating: Int?, favorite: Boolean, notes: String?, mechanicsCsv: String) {
        viewModelScope.launch {
            val current = _clip.value ?: return@launch
            val clipId = currentClipId()

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
            val message = if (newlyUnlocked.isEmpty()) {
                "Saved."
            } else {
                "Saved — unlocked: ${newlyUnlocked.joinToString(", ") { it.title }}"
            }

            if (!isQueueMode) {
                _event.value = ClipDetailEvent.Saved(message)
                return@launch
            }

            _event.value = ClipDetailEvent.Saved(message)
            advance()
        }
    }

    /** Advances without saving. In single-edit mode this is unreachable (nav row is hidden). */
    fun skipOrNext() {
        advance()
    }

    private fun advance() {
        val ids = queueIds ?: return
        if (queuePosition >= ids.size - 1) {
            _event.value = ClipDetailEvent.QueueComplete
            return
        }
        queuePosition += 1
        loadCurrent()
    }

    /**
     * Steps back one position. No-op at position 0. Discards any unsaved edits in the current
     * form — the Activity re-observes [clip] for the previous entry and repopulates fields from
     * that, so nothing needs to be persisted here.
     */
    fun previous() {
        val ids = queueIds ?: return
        if (queuePosition == 0) return
        queuePosition -= 1
        loadCurrent()
        _event.value = ClipDetailEvent.DiscardedUnsavedChanges
    }

    /** Activity calls this once it's handled an event, so it isn't re-delivered on rotation. */
    fun consumeEvent() {
        _event.value = null
    }

    class Factory(
        private val clipId: Long,
        private val queueIds: LongArray?,
        private val queuePosition: Int,
        private val clipRepository: ClipRepository,
        private val mechanicRepository: MechanicRepository,
        private val achievementEvaluator: AchievementEvaluator
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ClipDetailViewModel(
                queueIds,
                queuePosition,
                clipRepository,
                mechanicRepository,
                achievementEvaluator
            ).also { it.initialClipId = clipId } as T
        }
    }
}
