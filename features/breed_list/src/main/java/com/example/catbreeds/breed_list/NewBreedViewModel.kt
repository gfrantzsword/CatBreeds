package com.example.catbreeds.breed_list

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.core.util.ErrorMessages
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewBreedViewModel @Inject constructor(
    private val breedRepository: BreedRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _errorMessage = mutableStateOf<String?>(null)

    fun addNewBreed(
        breed: Breed,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newId = UUID.randomUUID().toString()

                val imagePath = if (!breed.imageUrl.isNullOrEmpty()) {
                    saveImage(Uri.parse(breed.imageUrl), newId)
                } else {
                    ""
                }

                val newBreed = breed.copy(id = newId, imageUrl = imagePath)
                breedRepository.addBreed(newBreed)
                onSuccess(newBreed.id)
            } catch (_: Exception) {
                _errorMessage.value = ErrorMessages.LOCAL_ERROR
            }
        }
    }

    private suspend fun saveImage(uri: Uri, id: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "breed_${id}.jpg"
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputFile = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(outputFile)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                outputFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}