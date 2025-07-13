package com.example.catbreeds.ui.breedList

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.data.remote.RetrofitInstance
import com.example.catbreeds.domain.models.Breed
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

class BreedListViewModel: ViewModel() {
    private val _breeds = mutableStateOf<List<Breed>>(emptyList())
    val breeds: State<List<Breed>> = _breeds

    init {
        fetchBreeds()
    }

    private fun fetchBreeds() {
        viewModelScope.launch {
            try {
                _breeds.value = RetrofitInstance.api.getBreeds()
            } catch (e: Exception) {
                throw Exception("Error fetching breeds", e)
            }
        }
    }
}