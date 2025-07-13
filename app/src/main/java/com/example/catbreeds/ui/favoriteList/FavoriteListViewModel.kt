package com.example.catbreeds.ui.favoriteList

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteListViewModel @Inject constructor(
    private val breedRepository: BreedRepository
) : ViewModel() {

    private val _favoriteBreeds = mutableStateOf<List<Breed>>(emptyList())
    val favoriteBreeds: State<List<Breed>> = _favoriteBreeds

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadFavoriteBreeds()
    }

    private fun loadFavoriteBreeds() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                breedRepository.getFavoriteBreeds().collectLatest { favorites ->
                    _favoriteBreeds.value = favorites
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun removeFromFavorites(breedId: String) {
        viewModelScope.launch {
            try {
                breedRepository.removeBreedFromFavorites(breedId)
                _favoriteBreeds.value = _favoriteBreeds.value.filter { it.id != breedId }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}