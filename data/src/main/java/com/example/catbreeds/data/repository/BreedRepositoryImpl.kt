package com.example.catbreeds.data.repository

import com.example.catbreeds.data.local.BreedDao
import com.example.catbreeds.data.local.BreedEntity
import com.example.catbreeds.data.local.FavoriteDao
import com.example.catbreeds.data.local.FavoriteEntity
import com.example.catbreeds.data.remote.RemoteService
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.core.util.ConnectivityChecker
import com.example.catbreeds.core.util.ErrorMessages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class BreedRepositoryImpl(
    private val remoteSource: RemoteService,
    private val localSource: BreedDao,
    private val favoriteLocalSource: FavoriteDao,
    private val connectivityChecker: ConnectivityChecker
) : BreedRepository {

    // Fetches the breeds from the local database
    override fun getBreeds(): Flow<List<Breed>> {
        return localSource.getAll().map { entities ->
            entities.map { it.toBreed() }
        }
    }

    // Fetches a single breed from the local database
    override suspend fun getBreedById(breedId: String): Breed? {
        return localSource.getById(breedId)?.toBreed()
    }

    // Fetches the breeds from the API and updates de local database with it
    override suspend fun refreshBreeds() {
        if (!connectivityChecker.isConnected()) {
            throw Exception(ErrorMessages.NO_INTERNET_CONNECTION)
        }

        try {
            val networkBreeds = remoteSource.getBreeds()

            val breedEntities = networkBreeds.map { breedDto ->
                BreedEntity(
                    id = breedDto.id,
                    name = breedDto.name,
                    origin = breedDto.origin,
                    description = breedDto.description,
                    temperament = breedDto.temperament,
                    lifeSpan = breedDto.lifeSpan,
                    imageUrl = "https://cdn2.thecatapi.com/images/${breedDto.referenceImageId}.jpg"
                )
            }
            localSource.insertAll(breedEntities)
        } catch (e: Exception) {
            throw Exception(ErrorMessages.NETWORK_ERROR, e)
        }
    }

    // Adds a breed's id to the 'favorites' local database
    override suspend fun addBreedToFavorites(breedId: String) {
        favoriteLocalSource.insert(FavoriteEntity(breedId))
    }

    // Removes a breed's id from the 'favorites' local database
    override suspend fun removeBreedFromFavorites(breedId: String) {
        favoriteLocalSource.delete(FavoriteEntity(breedId))
    }

    // Gets all the breeds from the local database that are marked as favorites
    override fun getFavoriteBreeds(): Flow<List<Breed>> {
        return favoriteLocalSource.getAll().map { favoriteEntities ->
            favoriteEntities.mapNotNull { favoriteEntity ->
                localSource.getById(favoriteEntity.id)?.toBreed(isFavorite = true)
            }
        }
    }

    override fun getSimilarBreeds(breedId: String): Flow<List<Breed>> {
        return flow {
            val breed = getBreedById(breedId)
            if (breed != null) {
                val allBreeds = getBreeds().first()
                val similar = allBreeds.filter {
                    it.id != breed.id && (it.origin == breed.origin || it.temperament.count { temp -> breed.temperament.contains(temp) } >= 3)
                }.take(6)
                emit(similar)
            } else {
                emit(emptyList())
            }
        }
    }
}