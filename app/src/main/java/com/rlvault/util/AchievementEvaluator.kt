package com.rlvault.util

import com.rlvault.data.repository.AchievementRepository
import com.rlvault.data.repository.ClipRepository
import org.json.JSONObject

/**
 * Evaluates achievement/milestone unlock rules against live clip data. Rules are pure data
 * (`Achievement.conditionJson`) — this class never hardcodes a specific achievement, only the
 * rule *types* it knows how to check. New achievements with existing rule types need zero code
 * changes here; new rule types need one new `when` branch.
 *
 * Callable both automatically (e.g. after every clip save) and manually (Developer Mode
 * "Recalculate statistics") — [evaluateAll] is idempotent either way.
 */
class AchievementEvaluator(
    private val achievementRepository: AchievementRepository,
    private val clipRepository: ClipRepository
) {

    /** Rule types this evaluator currently understands. Add more as new conditionJson shapes ship. */
    private object RuleType {
        const val CLIP_COUNT = "clip_count"
        const val MECHANIC_COUNT = "mechanic_count"
    }

    /**
     * Checks every unlocked-eligible achievement's rule against current data and marks any
     * newly-met ones unlocked. Returns the list of achievements that were newly unlocked by this
     * call (empty if nothing changed) — useful for surfacing a "new achievement!" toast/snackbar.
     */
    suspend fun evaluateAll(nowMillis: Long = System.currentTimeMillis()): List<com.rlvault.data.model.Achievement> {
        val newlyUnlocked = mutableListOf<com.rlvault.data.model.Achievement>()

        for (achievement in achievementRepository.getAll()) {
            if (achievement.unlocked) continue

            val met = try {
                isRuleMet(achievement.conditionJson)
            } catch (e: Exception) {
                // Malformed conditionJson (e.g. hand-edited via Developer Mode) shouldn't crash
                // evaluation of every other achievement — skip just this one.
                false
            }

            if (met) {
                achievementRepository.markUnlocked(achievement.id, nowMillis)
                newlyUnlocked.add(achievement.copy(unlocked = true, unlockedAt = nowMillis))
            }
        }

        return newlyUnlocked
    }

    private suspend fun isRuleMet(conditionJson: String): Boolean {
        val rule = JSONObject(conditionJson)
        return when (rule.getString("type")) {
            RuleType.CLIP_COUNT -> {
                val threshold = rule.getInt("threshold")
                clipRepository.count() >= threshold
            }
            RuleType.MECHANIC_COUNT -> {
                val mechanic = rule.getString("mechanic")
                val threshold = rule.getInt("threshold")
                clipRepository.countWithMechanic(mechanic) >= threshold
            }
            else -> false // unknown rule type — treat as never-met rather than throwing
        }
    }
}
