package com.rlvault.data.repository

import com.rlvault.data.model.Mechanic

interface MechanicRepository {
    suspend fun getAll(): List<Mechanic>
    suspend fun getOrCreate(name: String): Mechanic
    suspend fun delete(id: Long)
}
