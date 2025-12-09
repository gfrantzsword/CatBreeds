package com.example.catbreeds.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.catbreeds.domain.models.Breed

@Entity(tableName = "breeds")
data class BreedEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "origin") val origin: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "temperament") val temperament: String,
    @ColumnInfo(name = "life_span") val lifeSpan: String,
    @ColumnInfo(name = "image_url") val imageUrl: String
) {
    fun toBreed(isFavorite: Boolean = false): Breed {
        return Breed(
            id = id,
            name = name,
            origin = origin,
            description = description,
            temperament = temperament.split(",").map { it.trim() },
            lifeSpan = lifeSpan,
            imageUrl = imageUrl,
            isFavorite = isFavorite
        )
    }
}