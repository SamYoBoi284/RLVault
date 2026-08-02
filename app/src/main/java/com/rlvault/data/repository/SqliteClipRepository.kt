package com.rlvault.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.rlvault.data.db.Contract.ClipMechanics
import com.rlvault.data.db.Contract.Clips
import com.rlvault.data.db.Contract.Mechanics
import com.rlvault.data.db.RLVaultDbHelper
import com.rlvault.data.db.getBool
import com.rlvault.data.db.getIntOrNull
import com.rlvault.data.db.getLongOrNull
import com.rlvault.data.db.getLongReq
import com.rlvault.data.db.getStringOrNull
import com.rlvault.data.db.getStringReq
import com.rlvault.data.model.Clip
import com.rlvault.data.model.Mechanic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SQLite-backed [ClipRepository]. All calls hop to Dispatchers.IO — SQLiteDatabase calls are
 * blocking, and repositories must never be called from the main thread.
 */
class SqliteClipRepository(private val dbHelper: RLVaultDbHelper) : ClipRepository {

    override suspend fun getById(id: Long): Clip? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(
            Clips.TABLE, null, "${Clips.ID} = ?", arrayOf(id.toString()),
            null, null, null
        ).use { c -> if (c.moveToFirst()) c.toClip(db) else null }
    }

    override suspend fun getAll(): List<Clip> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(Clips.TABLE, null, null, null, null, null, "${Clips.IMPORTED_AT} DESC").use { c ->
            generateSequence { if (c.moveToNext()) c else null }.map { it.toClip(db) }.toList()
        }
    }

    override suspend fun getUnreviewed(): List<Clip> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(
            Clips.TABLE, null, "${Clips.REVIEWED} = 0", null,
            null, null, "${Clips.IMPORTED_AT} ASC"
        ).use { c ->
            generateSequence { if (c.moveToNext()) c else null }.map { it.toClip(db) }.toList()
        }
    }

    override suspend fun getBySession(sessionId: Long): List<Clip> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(
            Clips.TABLE, null, "${Clips.SESSION_ID} = ?", arrayOf(sessionId.toString()),
            null, null, "${Clips.IMPORTED_AT} ASC"
        ).use { c ->
            generateSequence { if (c.moveToNext()) c else null }.map { it.toClip(db) }.toList()
        }
    }

    override suspend fun insert(clip: Clip): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val id = db.insertOrThrow(Clips.TABLE, null, clip.toContentValues())
        if (clip.mechanics.isNotEmpty()) {
            setMechanicsInternal(db, id, clip.mechanics.map { it.id })
        }
        id
    }

    override suspend fun update(clip: Clip) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.update(Clips.TABLE, clip.toContentValues(), "${Clips.ID} = ?", arrayOf(clip.id.toString()))
        Unit
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(Clips.TABLE, "${Clips.ID} = ?", arrayOf(id.toString()))
        Unit
    }

    override suspend fun markReviewed(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply { put(Clips.REVIEWED, 1) }
        db.update(Clips.TABLE, cv, "${Clips.ID} = ?", arrayOf(id.toString()))
        Unit
    }

    override suspend fun setMechanics(clipId: Long, mechanicIds: List<Long>) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        setMechanicsInternal(db, clipId, mechanicIds)
    }

    override suspend fun count(): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM ${Clips.TABLE}", null).use { c ->
            c.moveToFirst(); c.getInt(0)
        }
    }

    override suspend fun countWithMechanic(mechanicName: String): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT COUNT(DISTINCT cm.${ClipMechanics.CLIP_ID})
            FROM ${ClipMechanics.TABLE} cm
            JOIN ${Mechanics.TABLE} m ON m.${Mechanics.ID} = cm.${ClipMechanics.MECHANIC_ID}
            WHERE m.${Mechanics.NAME} = ?
        """.trimIndent()
        db.rawQuery(sql, arrayOf(mechanicName)).use { c -> c.moveToFirst(); c.getInt(0) }
    }

    // --- internal helpers ---

    private fun setMechanicsInternal(db: SQLiteDatabase, clipId: Long, mechanicIds: List<Long>) {
        db.delete(ClipMechanics.TABLE, "${ClipMechanics.CLIP_ID} = ?", arrayOf(clipId.toString()))
        mechanicIds.forEach { mechanicId ->
            val cv = ContentValues().apply {
                put(ClipMechanics.CLIP_ID, clipId)
                put(ClipMechanics.MECHANIC_ID, mechanicId)
            }
            db.insertWithOnConflict(ClipMechanics.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun android.database.Cursor.toClip(db: SQLiteDatabase): Clip {
        val id = getLongReq(Clips.ID)
        val mechanics = loadMechanicsForClip(db, id)
        return Clip(
            id = id,
            filePath = getStringReq(Clips.FILE_PATH),
            title = getStringOrNull(Clips.TITLE),
            notes = getStringOrNull(Clips.NOTES),
            rating = getIntOrNull(Clips.RATING),
            favorite = getBool(Clips.FAVORITE),
            durationMs = getLongOrNull(Clips.DURATION_MS),
            createdAt = getLongReq(Clips.CREATED_AT),
            importedAt = getLongReq(Clips.IMPORTED_AT),
            reviewed = getBool(Clips.REVIEWED),
            sessionId = getLongOrNull(Clips.SESSION_ID),
            mechanics = mechanics
        )
    }

    private fun loadMechanicsForClip(db: SQLiteDatabase, clipId: Long): List<Mechanic> {
        val sql = """
            SELECT m.${Mechanics.ID}, m.${Mechanics.NAME}
            FROM ${Mechanics.TABLE} m
            JOIN ${ClipMechanics.TABLE} cm ON cm.${ClipMechanics.MECHANIC_ID} = m.${Mechanics.ID}
            WHERE cm.${ClipMechanics.CLIP_ID} = ?
        """.trimIndent()
        return db.rawQuery(sql, arrayOf(clipId.toString())).use { c ->
            generateSequence { if (c.moveToNext()) c else null }
                .map { Mechanic(id = it.getLongReq(Mechanics.ID), name = it.getStringReq(Mechanics.NAME)) }
                .toList()
        }
    }

    private fun Clip.toContentValues(): ContentValues = ContentValues().apply {
        put(Clips.FILE_PATH, filePath)
        put(Clips.TITLE, title)
        put(Clips.NOTES, notes)
        put(Clips.RATING, rating)
        put(Clips.FAVORITE, if (favorite) 1 else 0)
        put(Clips.DURATION_MS, durationMs)
        put(Clips.CREATED_AT, createdAt)
        put(Clips.IMPORTED_AT, importedAt)
        put(Clips.REVIEWED, if (reviewed) 1 else 0)
        put(Clips.SESSION_ID, sessionId)
    }
}
