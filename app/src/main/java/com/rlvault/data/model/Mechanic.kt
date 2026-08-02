package com.rlvault.data.model

/** A tag like "Flip Reset", "Musty", "Pinch". Many-to-many with Clip. */
data class Mechanic(
    val id: Long = 0,
    val name: String
)
