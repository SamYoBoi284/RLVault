package com.rlvault.data.repository

import android.content.ContentValues
import com.rlvault.data.db.Contract.Sessions
import com.rlvault.data.db.RLVaultDbHelper
import com.rlvault.data.db.getBool
import com.rlvault.data.db.getIntReq
import com.rlvault.data.db.getLongOrNull
import com.rlvault.data.db.getLongReq
import com.rlvault.data.db.getStringOrNull
import com.rlvault.data.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqliteSessionRepository(private val dbHelper: RLVaultDbHelper) : SessionRepository {

    override suspend fun getById(id: Long): Session? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(Sessions.TABLE, null, "${Sessions.ID} = ?", arrayOf(id.toString()), null, null, null)
            .use { c -> if (c.moveToFirst()) c.toSession() else null }
    }

    override suspend fun getAll(): List<Session> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(Sessions.TABLE, null, null, null, null, null, "${Sessions.DATE} DESC").use { c ->
            generateSequence { if (c.moveToNext()) c else null }.map { it.toSession() }.toList()
        }
    }

    override suspend fun getLatest(): Session? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(Sessions.TABLE, null, null, null, null, null, "${Sessions.DATE} DESC", "1").use { c ->
            if (c.moveToFirst()) c.toSession() else null
        }
    }

    override suspend fun insert(session: Session): Long = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.insertOrThrow(Sessions.TABLE, null, session.toContentValues())
    }

    override suspend fun update(session: Session) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.update(
            Sessions.TABLE, session.toContentValues(), "${Sessions.ID} = ?", arrayOf(session.id.toString())
        )
        Unit
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        // clips.session_id is set NULL on delete via FK — clips are never deleted with a session.
        dbHelper.writableDatabase.delete(Sessions.TABLE, "${Sessions.ID} = ?", arrayOf(id.toString()))
        Unit
    }

    override suspend fun startAutomaticSession(startTime: Long): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put(Sessions.IS_AUTOMATIC, 1)
            put(Sessions.START_TIME, startTime)
            put(Sessions.DATE, startTime)
            put(Sessions.CLIP_COUNT, 0)
        }
        dbHelper.writableDatabase.insertOrThrow(Sessions.TABLE, null, cv)
    }

    override suspend fun endAutomaticSession(
        sessionId: Long,
        endTime: Long,
        wins: Int,
        losses: Int,
        rank: String?,
        notes: String?
    ) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val existing = db.query(
            Sessions.TABLE, arrayOf(Sessions.START_TIME), "${Sessions.ID} = ?",
            arrayOf(sessionId.toString()), null, null, null
        ).use { c -> if (c.moveToFirst()) c.getLongOrNull(Sessions.START_TIME) else null }

        val cv = ContentValues().apply {
            put(Sessions.END_TIME, endTime)
            if (existing != null) put(Sessions.DURATION_MS, endTime - existing)
            put(Sessions.WINS, wins)
            put(Sessions.LOSSES, losses)
            put(Sessions.RANK, rank)
            put(Sessions.NOTES, notes)
        }
        db.update(Sessions.TABLE, cv, "${Sessions.ID} = ?", arrayOf(sessionId.toString()))
        Unit
    }

    private fun android.database.Cursor.toSession() = Session(
        id = getLongReq(Sessions.ID),
        isAutomatic = getBool(Sessions.IS_AUTOMATIC),
        startTime = getLongOrNull(Sessions.START_TIME),
        endTime = getLongOrNull(Sessions.END_TIME),
        durationMs = getLongOrNull(Sessions.DURATION_MS),
        date = getLongReq(Sessions.DATE),
        wins = getIntReq(Sessions.WINS),
        losses = getIntReq(Sessions.LOSSES),
        rank = getStringOrNull(Sessions.RANK),
        notes = getStringOrNull(Sessions.NOTES),
        clipCount = getIntReq(Sessions.CLIP_COUNT)
    )

    private fun Session.toContentValues() = ContentValues().apply {
        put(Sessions.IS_AUTOMATIC, if (isAutomatic) 1 else 0)
        put(Sessions.START_TIME, startTime)
        put(Sessions.END_TIME, endTime)
        put(Sessions.DURATION_MS, durationMs)
        put(Sessions.DATE, date)
        put(Sessions.WINS, wins)
        put(Sessions.LOSSES, losses)
        put(Sessions.RANK, rank)
        put(Sessions.NOTES, notes)
        put(Sessions.CLIP_COUNT, clipCount)
    }
}
