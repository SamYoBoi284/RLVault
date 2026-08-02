package com.rlvault.data.repository

import com.rlvault.data.model.Clip

/**
 * Single source of truth for clip data. ViewModels depend on this interface, never on
 * RLVaultDbHelper directly — keeps ui/ layer testable without a real SQLite database.
 * Implementation (SqliteClipRepository) arrives with the DAO wiring in a later milestone.
 */
interface ClipRepository {
    suspend fun getById(id: Long): Clip?
    suspend fun getAll(): List<Clip>
    suspend fun getUnreviewed(): List<Clip>
    suspend fun getBySession(sessionId: Long): List<Clip>
    suspend fun insert(clip: Clip): Long
    suspend fun update(clip: Clip)
    suspend fun delete(id: Long)
    suspend fun markReviewed(id: Long)
    suspend fun setMechanics(clipId: Long, mechanicIds: List<Long>)
    suspend fun count(): Int
    suspend fun countWithMechanic(mechanicName: String): Int
}
