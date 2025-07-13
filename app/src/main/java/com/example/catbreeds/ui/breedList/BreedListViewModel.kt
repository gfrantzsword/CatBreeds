package com.example.catbreeds.ui.breedList

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.example.catbreeds.domain.repository.BreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class BreedListViewModel @Inject constructor(
    private val breedRepository: BreedRepository
): ViewModel() {
    private val _breeds = mutableStateOf<List<Breed>>(emptyList())
    val breeds: State<List<Breed>> = _breeds

    init {
        observeBreeds()
        refreshBreeds()
    }

    private fun observeBreeds() {
        viewModelScope.launch {
            breedRepository.getBreeds().collectLatest { breedList ->
                _breeds.value = breedList
            }
        }
    }

    private fun refreshBreeds() {
        viewModelScope.launch {
            try {
                breedRepository.refreshBreeds()
            } catch (e: Exception) {
                TODO("Handle exception")
                e.printStackTrace()
            }
        }
    }
}