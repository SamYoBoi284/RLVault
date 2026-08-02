package com.rlvault.data.model

/**
 * A play session, either auto-tracked (Start/End Session flow) or manually entered.
 * Both flows share this one shape since the end result has the same fields.
 */
data class Session(
    val id: Long = 0,
    val isAutomatic: Boolean = false,
    val startTime: Long? = null,   // set only for automatic sessions
    val endTime: Long? = null,     // set only for automatic sessions
    val durationMs: Long? = null,
    val date: Long,                // epoch millis — the date this session is filed under
    val wins: Int = 0,
    val losses: Int = 0,
    val rank: String? = null,
    val notes: String? = null,
    val clipCount: Int = 0         // denormalized; recalculable via Developer Mode "Recalculate statistics"
)
