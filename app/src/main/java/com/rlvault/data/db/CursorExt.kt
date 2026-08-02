package com.rlvault.data.db

import android.database.Cursor

/** Small null-safe Cursor helpers so repository impls don't repeat getColumnIndexOrThrow boilerplate. */

fun Cursor.getStringOrNull(col: String): String? {
    val i = getColumnIndexOrThrow(col)
    return if (isNull(i)) null else getString(i)
}

fun Cursor.getLongOrNull(col: String): Long? {
    val i = getColumnIndexOrThrow(col)
    return if (isNull(i)) null else getLong(i)
}

fun Cursor.getIntOrNull(col: String): Int? {
    val i = getColumnIndexOrThrow(col)
    return if (isNull(i)) null else getInt(i)
}

fun Cursor.getLongReq(col: String): Long = getLong(getColumnIndexOrThrow(col))
fun Cursor.getIntReq(col: String): Int = getInt(getColumnIndexOrThrow(col))
fun Cursor.getStringReq(col: String): String = getString(getColumnIndexOrThrow(col))
fun Cursor.getBool(col: String): Boolean = getIntReq(col) != 0
