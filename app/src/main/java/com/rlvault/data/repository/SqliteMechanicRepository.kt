package com.rlvault.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.rlvault.data.db.Contract.Mechanics
import com.rlvault.data.db.RLVaultDbHelper
import com.rlvault.data.db.getLongReq
import com.rlvault.data.db.getStringReq
import com.rlvault.data.model.Mechanic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqliteMechanicRepository(private val dbHelper: RLVaultDbHelper) : MechanicRepository {

    override suspend fun getAll(): List<Mechanic> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(Mechanics.TABLE, null, null, null, null, null, "${Mechanics.NAME} ASC").use { c ->
            generateSequence { if (c.moveToNext()) c else null }
                .map { Mechanic(id = it.getLongReq(Mechanics.ID), name = it.getStringReq(Mechanics.NAME)) }
                .toList()
        }
    }

    /** Mechanics are freeform tags typed during review — get-or-create avoids duplicate rows. */
    override suspend fun getOrCreate(name: String): Mechanic = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.query(
            Mechanics.TABLE, null, "${Mechanics.NAME} = ?", arrayOf(name), null, null, null
        ).use { c ->
            if (c.moveToFirst()) {
                return@withContext Mechanic(id = c.getLongReq(Mechanics.ID), name = c.getStringReq(Mechanics.NAME))
            }
        }
        val cv = ContentValues().apply { put(Mechanics.NAME, name) }
        val id = db.insertWithOnConflict(Mechanics.TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        Mechanic(id = id, name = name)
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        // clip_mechanics rows cascade-delete via the FK ON DELETE CASCADE in schema.sql.
        db.delete(Mechanics.TABLE, "${Mechanics.ID} = ?", arrayOf(id.toString()))
        Unit
    }
}
