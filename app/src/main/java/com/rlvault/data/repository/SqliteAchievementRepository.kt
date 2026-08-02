package com.rlvault.data.repository

import android.content.ContentValues
import com.rlvault.data.db.Contract.Achievements
import com.rlvault.data.db.RLVaultDbHelper
import com.rlvault.data.db.getBool
import com.rlvault.data.db.getLongOrNull
import com.rlvault.data.db.getLongReq
import com.rlvault.data.db.getStringOrNull
import com.rlvault.data.db.getStringReq
import com.rlvault.data.model.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqliteAchievementRepository(private val dbHelper: RLVaultDbHelper) : AchievementRepository {

    override suspend fun getAll(): List<Achievement> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(Achievements.TABLE, null, null, null, null, null, "${Achievements.TITLE} ASC").use { c ->
            generateSequence { if (c.moveToNext()) c else null }.map { it.toAchievement() }.toList()
        }
    }

    override suspend fun getUnlocked(): List<Achievement> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(
            Achievements.TABLE, null, "${Achievements.UNLOCKED} = 1", null,
            null, null, "${Achievements.UNLOCKED_AT} DESC"
        ).use { c ->
            generateSequence { if (c.moveToNext()) c else null }.map { it.toAchievement() }.toList()
        }
    }

    override suspend fun getMilestones(): List<Achievement> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(
            Achievements.TABLE, null, "${Achievements.IS_MILESTONE} = 1", null,
            null, null, "${Achievements.TITLE} ASC"
        ).use { c ->
            generateSequence { if (c.moveToNext()) c else null }.map { it.toAchievement() }.toList()
        }
    }

    override suspend fun getById(id: Long): Achievement? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(Achievements.TABLE, null, "${Achievements.ID} = ?", arrayOf(id.toString()), null, null, null)
            .use { c -> if (c.moveToFirst()) c.toAchievement() else null }
    }

    /** Used by Developer Mode create/edit, and by the seed data / rule evaluator upsert path. */
    override suspend fun upsert(achievement: Achievement): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = achievement.toContentValues()
        if (achievement.id != 0L) {
            db.update(Achievements.TABLE, cv, "${Achievements.ID} = ?", arrayOf(achievement.id.toString()))
            achievement.id
        } else {
            db.insertOrThrow(Achievements.TABLE, null, cv)
        }
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete(Achievements.TABLE, "${Achievements.ID} = ?", arrayOf(id.toString()))
        Unit
    }

    override suspend fun markUnlocked(id: Long, unlockedAt: Long) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(Achievements.UNLOCKED, 1)
            put(Achievements.UNLOCKED_AT, unlockedAt)
        }
        dbHelper.writableDatabase.update(Achievements.TABLE, cv, "${Achievements.ID} = ?", arrayOf(id.toString()))
        Unit
    }

    /** Developer Mode "Reset achievement progress" — clears unlock state, keeps definitions. */
    override suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(Achievements.UNLOCKED, 0)
            putNull(Achievements.UNLOCKED_AT)
        }
        dbHelper.writableDatabase.update(Achievements.TABLE, cv, null, null)
        Unit
    }

    private fun android.database.Cursor.toAchievement() = Achievement(
        id = getLongReq(Achievements.ID),
        key = getStringReq(Achievements.KEY),
        title = getStringReq(Achievements.TITLE),
        description = getStringOrNull(Achievements.DESCRIPTION),
        conditionJson = getStringReq(Achievements.CONDITION_JSON),
        unlocked = getBool(Achievements.UNLOCKED),
        unlockedAt = getLongOrNull(Achievements.UNLOCKED_AT),
        isMilestone = getBool(Achievements.IS_MILESTONE),
        createdByDev = getBool(Achievements.CREATED_BY_DEV)
    )

    private fun Achievement.toContentValues() = ContentValues().apply {
        put(Achievements.KEY, key)
        put(Achievements.TITLE, title)
        put(Achievements.DESCRIPTION, description)
        put(Achievements.CONDITION_JSON, conditionJson)
        put(Achievements.UNLOCKED, if (unlocked) 1 else 0)
        put(Achievements.UNLOCKED_AT, unlockedAt)
        put(Achievements.IS_MILESTONE, if (isMilestone) 1 else 0)
        put(Achievements.CREATED_BY_DEV, if (createdByDev) 1 else 0)
    }
}
