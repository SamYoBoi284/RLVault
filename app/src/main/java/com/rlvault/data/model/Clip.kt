package com.rlvault.data.model

/**
 * A single indexed video clip. [filePath] is the only link to the actual file on disk —
 * RL Vault never moves or copies clips, it only references them.
 */
data class Clip(
    val id: Long = 0,
    val filePath: String,
    val title: String? = null,
    val notes: String? = null,
    val rating: Int? = null,       // numerator only; UI always renders "$rating/10". Can exceed 10.
    val favorite: Boolean = false,
    val durationMs: Long? = null,
    val createdAt: Long,           // file's own creation timestamp (epoch millis)
    val importedAt: Long,          // when RL Vault indexed it (epoch millis)
    val reviewed: Boolean = false, // false = still sitting in the review queue
    val sessionId: Long? = null,   // session this clip was imported during, if any
    val mechanics: List<Mechanic> = emptyList()
)
