package com.example.catbreeds.domain.repository

import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.flow.Flow

interface BreedRepository {
    fun getBreeds(): Flow<List<Breed>>
    suspend fun getBreedById(breedId: String): Breed?
    suspend fun refreshBreeds()
}