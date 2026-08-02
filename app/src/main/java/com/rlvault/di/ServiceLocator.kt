package com.rlvault.di

import android.content.Context
import com.rlvault.data.db.RLVaultDbHelper
import com.rlvault.data.repository.AchievementRepository
import com.rlvault.data.repository.ClipRepository
import com.rlvault.data.repository.MechanicRepository
import com.rlvault.data.repository.SessionRepository
import com.rlvault.data.repository.SqliteAchievementRepository
import com.rlvault.data.repository.SqliteClipRepository
import com.rlvault.data.repository.SqliteMechanicRepository
import com.rlvault.data.repository.SqliteSessionRepository
import com.rlvault.util.AchievementEvaluator

/**
 * Deliberately not Hilt/Dagger — same "no annotation processor, plain Gradle CLI buildable"
 * reasoning as skipping Room (see RLVaultDbHelper). One object, built once off applicationContext,
 * handing out the same repo instances everywhere. [init] must be called once from
 * [com.rlvault.RLVaultApp.onCreate] before anything else touches this object.
 */
object ServiceLocator {

    private lateinit var dbHelper: RLVaultDbHelper

    lateinit var clipRepository: ClipRepository
        private set
    lateinit var sessionRepository: SessionRepository
        private set
    lateinit var achievementRepository: AchievementRepository
        private set
    lateinit var mechanicRepository: MechanicRepository
        private set
    lateinit var achievementEvaluator: AchievementEvaluator
        private set

    fun init(context: Context) {
        if (::dbHelper.isInitialized) return // idempotent — config changes can re-touch this

        dbHelper = RLVaultDbHelper(context.applicationContext)
        clipRepository = SqliteClipRepository(dbHelper)
        sessionRepository = SqliteSessionRepository(dbHelper)
        achievementRepository = SqliteAchievementRepository(dbHelper)
        mechanicRepository = SqliteMechanicRepository(dbHelper)
        achievementEvaluator = AchievementEvaluator(achievementRepository, clipRepository)
    }
}
