package com.example.catbreeds.breed_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.catbreeds.domain.models.Breed

class NewBreedFormState(
    private val allNames: List<String>
) {
    // Field Value States
    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _origin = mutableStateOf("")
    val origin: State<String> = _origin

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _minLife = mutableStateOf("")
    val minLife: State<String> = _minLife

    private val _maxLife = mutableStateOf("")
    val maxLife: State<String> = _maxLife

    private val _imageUri = mutableStateOf("")
    val imageUri: State<String> = _imageUri

    // Helper States
    private val _isOriginDropdownActive = mutableStateOf(false)
    val isOriginDropdownActive: State<Boolean> = _isOriginDropdownActive

    private val _selectedTemperaments = mutableStateOf(setOf<String>())
    val selectedTemperaments: State<Set<String>> = _selectedTemperaments

    private val _hasSubmitted = mutableStateOf(false)

    private val _createdBreedId = mutableStateOf<String?>(null)
    val createdBreedId: State<String?> = _createdBreedId

    fun onAddNewBreed(id: String) {
        _createdBreedId.value = id
    }

    fun onNavigationHandled() {
        _createdBreedId.value = null
    }

    // Field Validation
    private fun validateField(value: String, customError: String? = null): String? {
        if (customError != null) return customError
        return if (_hasSubmitted.value && value.isBlank()) "Required" else null
    }

    private val isNameDuplicate = derivedStateOf {
        _name.value.trim().isNotEmpty() &&
                allNames.any { it.equals(_name.value.trim(), ignoreCase = true) }
    }

    private val isMaxLessThanMin = derivedStateOf {
        val min = _minLife.value.toIntOrNull()
        val max = _maxLife.value.toIntOrNull()
        min != null && max != null && max < min
    }

    // Error States
    val nameError: State<String?> = derivedStateOf { validateField(_name.value, if (isNameDuplicate.value) "A breed with this name already exists" else null) }
    val originError: State<String?> = derivedStateOf { validateField(_origin.value) }
    val descriptionError: State<String?> = derivedStateOf { validateField(_description.value) }
    val minLifeError: State<String?> = derivedStateOf { validateField(_minLife.value) }
    val maxLifeError: State<String?> = derivedStateOf { validateField(_maxLife.value, if (isMaxLessThanMin.value) "Must be >= Min" else null) }

    fun update(
        name: String? = null,
        origin: String? = null,
        description: String? = null,
        minLife: String? = null,
        maxLife: String? = null,
        imageUri: String? = null,
        isOriginDropdownActive: Boolean? = null,
        selectedTemperaments: Set<String>? = null
    ) {
        name?.let { _name.value = it }
        origin?.let { _origin.value = it }
        description?.let { _description.value = it }
        minLife?.let { _minLife.value = it }
        maxLife?.let { _maxLife.value = it }
        imageUri?.let { _imageUri.value = it }
        isOriginDropdownActive?.let { _isOriginDropdownActive.value = it }
        selectedTemperaments?.let { _selectedTemperaments.value = it }
    }

    fun validate(): Boolean {
        _hasSubmitted.value = true
        return nameError.value == null &&
                originError.value == null &&
                descriptionError.value == null &&
                minLifeError.value == null &&
                maxLifeError.value == null &&
                _selectedTemperaments.value.isNotEmpty()
    }

    fun toBreed(): Breed {
        return Breed(
            id = "",
            name = _name.value.trim(),
            origin = _origin.value.trim(),
            temperament = _selectedTemperaments.value.toList(),
            description = _description.value.trim(),
            lifeSpan = "${_minLife.value.trim()} - ${_maxLife.value.trim()}",
            imageUrl = _imageUri.value.ifEmpty { null },
            isFavorite = false
        )
    }

    fun isDirty(): Boolean {
        return _name.value.isNotBlank() ||
                _origin.value.isNotBlank() ||
                _minLife.value.isNotBlank() ||
                _maxLife.value.isNotBlank() ||
                _description.value.isNotBlank() ||
                _selectedTemperaments.value.isNotEmpty() ||
                _imageUri.value.isNotBlank()
    }
}

@Composable
fun rememberNewBreedFormState(allNames: List<String>): NewBreedFormState {
    return remember(allNames) { NewBreedFormState(allNames) }
}