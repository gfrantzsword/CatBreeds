package com.example.catbreeds.breed_list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.core.util.ErrorMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class BreedListViewModel @Inject constructor(
    private val breedRepository: BreedRepository
): ViewModel() {
    private val _breeds = mutableStateOf<List<Breed>>(emptyList())
    val breeds: State<List<Breed>> = _breeds

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _filteredBreeds = mutableStateOf<List<Breed>>(emptyList())
    val filteredBreeds: State<List<Breed>> = _filteredBreeds

    private val _favoriteBreedIds = mutableStateOf<Set<String>>(emptySet())

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        observeBreeds()
        observeFavorites()
        refreshBreeds()
    }

    private fun observeBreeds() {
        viewModelScope.launch {
            breedRepository.getBreeds().collectLatest { breedList ->
                _breeds.value = breedList
                filterBreeds()
            }
        }
    }

    // Observes for changes on the favorites button
    private fun observeFavorites() {
        viewModelScope.launch {
            breedRepository.getFavoriteBreeds().collectLatest { favoriteBreeds ->
                _favoriteBreedIds.value = favoriteBreeds.map { it.id }.toSet()
                updateFilteredBreedsWithFavorites()
            }
        }
    }

    private fun updateFilteredBreedsWithFavorites() {
        val favoriteIds = _favoriteBreedIds.value
        _filteredBreeds.value = _filteredBreeds.value.map { breed ->
            breed.copy(isFavorite = favoriteIds.contains(breed.id))
        }
    }

    // Fetches new data from the API and loads into the local database
    private fun refreshBreeds() {
        viewModelScope.launch {
            try {
                breedRepository.refreshBreeds()
            } catch (e: Exception) {
                _errorMessage.value = when (e.message) {
                    ErrorMessages.NO_INTERNET_CONNECTION -> ErrorMessages.NO_INTERNET_CONNECTION
                    else -> ErrorMessages.NETWORK_ERROR
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterBreeds()
    }

    private fun filterBreeds() {
        val query = _searchQuery.value.lowercase()
        val favoriteIds = _favoriteBreedIds.value

        _filteredBreeds.value = if (query.isEmpty()) {
            _breeds.value.map { breed ->
                breed.copy(isFavorite = favoriteIds.contains(breed.id))
            }
        } else {
            _breeds.value.filter { breed ->
                breed.name.lowercase().contains(query) ||
                        breed.origin.lowercase().contains(query) ||
                        breed.temperament.lowercase().contains(query)
            }.map { breed ->
                breed.copy(isFavorite = favoriteIds.contains(breed.id))
            }
        }
    }

    fun toggleFavorite(breedId: String) {
        viewModelScope.launch {
            try {
                val currentFavorites = _favoriteBreedIds.value
                if (currentFavorites.contains(breedId)) {
                    breedRepository.removeBreedFromFavorites(breedId)
                } else {
                    breedRepository.addBreedToFavorites(breedId)
                }
                val newFavorites = if (currentFavorites.contains(breedId)) {
                    currentFavorites - breedId
                } else {
                    currentFavorites + breedId
                }
                _favoriteBreedIds.value = newFavorites
                updateFilteredBreedsWithFavorites()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}