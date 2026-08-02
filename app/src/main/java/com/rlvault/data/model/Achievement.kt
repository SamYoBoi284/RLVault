package com.rlvault.data.model

/**
 * An achievement OR a milestone (distinguished by [isMilestone]). Unlock logic is never
 * hardcoded in Kotlin — [conditionJson] is a small rule the achievement engine (in `util/`)
 * evaluates against clip/session data. Editable at runtime via Developer Mode.
 *
 * Example conditionJson: {"type":"mechanic_count","mechanic":"Flip Reset","threshold":100}
 * Example conditionJson: {"type":"clip_count","threshold":500}
 */
data class Achievement(
    val id: Long = 0,
    val key: String,               // stable id, e.g. "flip_reset_100"
    val title: String,
    val description: String? = null,
    val conditionJson: String,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val isMilestone: Boolean = false,
    val createdByDev: Boolean = false // true if authored/edited via Developer Mode vs shipped default
)
