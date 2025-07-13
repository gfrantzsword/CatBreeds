package com.example.catbreeds.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BreedEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun breedDao(): BreedDao
}