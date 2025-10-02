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
    @ColumnInfo(name = "life_span") val life_span: String,
    @ColumnInfo(name = "reference_image_id") val reference_image_id: String
) {
    fun toBreed(isFavorite: Boolean = false): Breed {
        return Breed(
            id = id,
            name = name,
            origin = origin,
            description = description,
            temperament = temperament,
            life_span = life_span,
            reference_image_id = reference_image_id,
            isFavorite = isFavorite
        )
    }
}