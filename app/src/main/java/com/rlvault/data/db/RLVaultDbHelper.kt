package com.rlvault.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Opens/creates the RL Vault SQLite database.
 *
 * The schema itself lives in `res/raw/schema.sql` (single source of truth, also readable/
 * versionable outside Kotlin — see data/db/schema.sql in source control, which is copied into
 * res/raw at build time... actually simplest: we inline-read the raw resource copy below).
 *
 * Deliberately NOT using Room: this keeps the project buildable with plain Gradle CLI + Android
 * SDK tools only, with no annotation-processor/KSP setup required in an IDE. See ARCHITECTURE.md.
 */
class RLVaultDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        val schemaSql = readSchemaSql()
        // schema.sql contains multiple ';'-terminated statements — split and execute each.
        // PRAGMA lines are skipped here; foreign_keys is enabled per-connection in onConfigure.
        schemaSql.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("--") && !it.uppercase().startsWith("PRAGMA") }
            .forEach { statement -> db.execSQL(statement) }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No migrations exist yet — Milestone 1 ships at version 1.
        // Future versions must add explicit ALTER/CREATE statements per version step here,
        // rather than dropping tables, to preserve the user's local archive.
    }

    private fun readSchemaSql(): String {
        val input = appContext.resources.openRawResource(com.rlvault.R.raw.schema)
        return BufferedReader(InputStreamReader(input)).use { it.readText() }
    }

    companion object {
        const val DATABASE_NAME = "rlvault.db"
        const val DATABASE_VERSION = 1
    }
}
