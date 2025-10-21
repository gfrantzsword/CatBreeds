package com.example.catbreeds.domain.repository

import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.flow.Flow

interface BreedRepository {
    fun getBreeds(): Flow<List<Breed>>
    suspend fun getBreedById(breedId: String): Breed?
    suspend fun refreshBreeds()
    suspend fun addBreedToFavorites(breedId : String)
    suspend fun removeBreedFromFavorites(breedId : String)
    fun getFavoriteBreeds(): Flow<List<Breed>>
    fun getSimilarBreeds(breedId: String): Flow<List<Breed>>
}