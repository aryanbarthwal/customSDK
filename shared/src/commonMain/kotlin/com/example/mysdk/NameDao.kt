package com.example.mysdk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface NameDao {
    @Insert
    suspend fun insert(item: NameEntity)

    @Query("SELECT * FROM NameEntity ORDER BY id DESC")
    suspend fun getAll(): List<NameEntity>

    @Query("DELETE FROM NameEntity WHERE id = :id")
    suspend fun deleteById(id: Long)
}
