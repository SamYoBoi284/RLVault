package com.rlvault.util

import android.content.Context

/**
 * Persists the single SAF tree URI the user picks as their clip folder (spec: "user selects ONE
 * folder on first launch"). Deliberately a plain SharedPreferences wrapper, not a DB table —
 * it's one string, and keeping it out of schema.sql avoids a migration for something this small.
 */
object ClipFolderPrefs {

    private const val PREFS_NAME = "clip_folder_prefs"
    private const val KEY_TREE_URI = "tree_uri"

    fun getFolderUri(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TREE_URI, null)

    fun setFolderUri(context: Context, uriString: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TREE_URI, uriString)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_TREE_URI)
            .apply()
    }
}
