package com.example.catbreeds.data.repository

import com.example.catbreeds.data.local.BreedDao
import com.example.catbreeds.data.local.BreedEntity
import com.example.catbreeds.data.remote.RemoteService
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.domain.utils.ConnectivityChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BreedRepositoryImpl(
    private val remoteSource: RemoteService,
    private val localSource: BreedDao,
    private val connectivityChecker: ConnectivityChecker
): BreedRepository {
    override fun getBreeds(): Flow<List<Breed>> {
        return localSource.getAll().map { entities ->
            entities.map { it.toBreed() }
        }
    }

    override suspend fun getBreedById(breedId: String): Breed? {
        return localSource.getById(breedId)?.toBreed()
    }

    override suspend fun refreshBreeds() {
        if (!connectivityChecker.isConnected()) {
            throw Exception("No internet connection")
        }

        try {
            val networkBreeds = remoteSource.getBreeds()

            val breedEntities = networkBreeds.map { breed ->
                BreedEntity(
                    id = breed.id,
                    name = breed.name,
                    origin = breed.origin,
                    description = breed.description,
                    temperament = breed.temperament,
                    life_span = breed.life_span,
                    reference_image_id = breed.reference_image_id.toString()
                )
            }

            localSource.insertAll(breedEntities)
        } catch (e: Exception) {
            throw Exception("Error refreshing breeds", e)
        }
    }
}