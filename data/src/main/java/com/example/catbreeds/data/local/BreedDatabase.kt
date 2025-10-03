package com.example.catbreeds.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Separate 'favorites' database to avoid data loss when refreshing the local database breeds
@Database(entities = [BreedEntity::class, FavoriteEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun breedDao(): BreedDao
    abstract fun favoriteDao(): FavoriteDao
}