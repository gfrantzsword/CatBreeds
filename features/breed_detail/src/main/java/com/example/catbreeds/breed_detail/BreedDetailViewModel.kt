package com.example.catbreeds.breed_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.core.util.ErrorMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedDetailViewModel @Inject constructor(
    private val breedRepository: BreedRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _breed = mutableStateOf<Breed?>(null)
    val breed: State<Breed?> = _breed
    private val breedId = savedStateHandle.get<String>("breedId")

    private val _similarBreeds = mutableStateOf<List<Breed>>(emptyList())
    val similarBreeds: State<List<Breed>> = _similarBreeds

    private val _errorMessage = mutableStateOf<Int?>(null)
    val errorMessage: State<Int?> = _errorMessage

    init {
        breedId?.let { id ->
            fetchBreedDetail(id)
            observeFavorites(id)
            fetchSimilarBreeds(id)
        }
    }

    // Fetches the data from local database
    private fun fetchBreedDetail(breedId: String) {
        viewModelScope.launch {
            try {
                val breedData = breedRepository.getBreedById(breedId)
                _breed.value = breedData
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Observes for changes on the favorites button
    private fun observeFavorites(breedId: String) {
        viewModelScope.launch {
            breedRepository.getFavoriteBreeds().collectLatest { favoriteBreeds ->
                val currentBreed = _breed.value
                if (currentBreed != null) {
                    val isFavorite = favoriteBreeds.any { it.id == breedId }
                    _breed.value = currentBreed.copy(isFavorite = isFavorite)
                }
            }
        }
    }

    private fun fetchSimilarBreeds(breedId: String) {
        viewModelScope.launch {
            breedRepository.getSimilarBreeds(breedId).collectLatest {
                _similarBreeds.value = it
            }
        }
    }

    fun toggleFavorite(breedId: String) {
        viewModelScope.launch {
            try {
                val currentBreed = _breed.value
                if (currentBreed?.isFavorite == true) {
                    breedRepository.removeBreedFromFavorites(breedId)
                    _breed.value = currentBreed.copy(isFavorite = false)
                } else {
                    breedRepository.addBreedToFavorites(breedId)
                    currentBreed?.let {
                        _breed.value = it.copy(isFavorite = true)
                    }
                }
            } catch (_: Exception) {
                _errorMessage.value = ErrorMessages.LOCAL_ERROR
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}