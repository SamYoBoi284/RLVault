package com.rlvault.data.repository

import com.rlvault.data.model.Session

interface SessionRepository {
    suspend fun getById(id: Long): Session?
    suspend fun getAll(): List<Session>
    suspend fun getLatest(): Session?
    suspend fun insert(session: Session): Long
    suspend fun update(session: Session)
    suspend fun delete(id: Long)

    /** Called when an automatic session starts; returns the new row id to attach imported clips to. */
    suspend fun startAutomaticSession(startTime: Long): Long

    /** Called when the user ends an automatic session and fills in wins/losses/rank/notes. */
    suspend fun endAutomaticSession(
        sessionId: Long,
        endTime: Long,
        wins: Int,
        losses: Int,
        rank: String?,
        notes: String?
    )
}
