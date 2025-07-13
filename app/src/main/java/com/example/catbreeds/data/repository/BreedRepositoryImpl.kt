package com.example.catbreeds.data.repository

import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import kotlinx.coroutines.flow.Flow

class BreedRepositoryImpl(): BreedRepository {
    override fun getBreeds(): Flow<List<Breed>> {
        TODO("Not yet implemented")
    }
}