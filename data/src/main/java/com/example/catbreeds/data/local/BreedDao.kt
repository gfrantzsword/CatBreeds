package com.example.catbreeds.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BreedDao {
    @Query("SELECT * FROM breeds")
    fun getAll(): Flow<List<BreedEntity>>

    @Query("SELECT * FROM breeds WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BreedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breeds: List<BreedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(breed: BreedEntity)

    @Delete
    suspend fun delete(breed: BreedEntity)

}