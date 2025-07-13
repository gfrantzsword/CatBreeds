package com.example.catbreeds.ui.breedDetail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
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

    init {
        breedId?.let { id ->
            fetchBreedDetail(id)
            observeFavorites(id)
        }
    }

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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}