package com.rlvault.data.db

/** Table/column name constants, kept separate from schema.sql so Kotlin code never hand-types
 *  raw string literals for SQL identifiers. Must stay in sync with schema.sql. */
object Contract {

    object Clips {
        const val TABLE = "clips"
        const val ID = "id"
        const val FILE_PATH = "file_path"
        const val TITLE = "title"
        const val NOTES = "notes"
        const val RATING = "rating"
        const val FAVORITE = "favorite"
        const val DURATION_MS = "duration_ms"
        const val CREATED_AT = "created_at"
        const val IMPORTED_AT = "imported_at"
        const val REVIEWED = "reviewed"
        const val SESSION_ID = "session_id"
    }

    object Mechanics {
        const val TABLE = "mechanics"
        const val ID = "id"
        const val NAME = "name"
    }

    object ClipMechanics {
        const val TABLE = "clip_mechanics"
        const val CLIP_ID = "clip_id"
        const val MECHANIC_ID = "mechanic_id"
    }

    object Sessions {
        const val TABLE = "sessions"
        const val ID = "id"
        const val IS_AUTOMATIC = "is_automatic"
        const val START_TIME = "start_time"
        const val END_TIME = "end_time"
        const val DURATION_MS = "duration_ms"
        const val DATE = "date"
        const val WINS = "wins"
        const val LOSSES = "losses"
        const val RANK = "rank"
        const val NOTES = "notes"
        const val CLIP_COUNT = "clip_count"
    }

    object Achievements {
        const val TABLE = "achievements"
        const val ID = "id"
        const val KEY = "key"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CONDITION_JSON = "condition_json"
        const val UNLOCKED = "unlocked"
        const val UNLOCKED_AT = "unlocked_at"
        const val IS_MILESTONE = "is_milestone"
        const val CREATED_BY_DEV = "created_by_dev"
    }
}
