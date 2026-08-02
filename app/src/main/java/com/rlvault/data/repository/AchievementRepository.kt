package com.rlvault.data.repository

import com.rlvault.data.model.Achievement

interface AchievementRepository {
    suspend fun getAll(): List<Achievement>
    suspend fun getUnlocked(): List<Achievement>
    suspend fun getMilestones(): List<Achievement>
    suspend fun getById(id: Long): Achievement?

    /** Developer Mode: create/edit achievement definitions at runtime — never hardcoded. */
    suspend fun upsert(achievement: Achievement): Long
    suspend fun delete(id: Long)

    suspend fun markUnlocked(id: Long, unlockedAt: Long)

    /** Developer Mode "Reset achievement progress". */
    suspend fun resetAllProgress()
}
