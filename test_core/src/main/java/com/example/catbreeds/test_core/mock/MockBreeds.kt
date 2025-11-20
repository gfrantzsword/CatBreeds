package com.example.catbreeds.test_core.mock

import com.example.catbreeds.domain.models.Breed

// Mock Objects
val mockSiberianBreed = Breed(
    id = MOCK_SIBERIAN_ID,
    name = MOCK_SIBERIAN_NAME,
    description = MOCK_SIBERIAN_DESCRIPTION,
    temperament = MOCK_SIBERIAN_TEMPERAMENT,
    origin = MOCK_SIBERIAN_ORIGIN,
    lifeSpan = MOCK_SIBERIAN_LIFESPAN,
    imageUrl = MOCK_SIBERIAN_IMG_URL,
    isFavorite = false
)
val mockPersianBreed = Breed(
    id = MOCK_PERSIAN_ID,
    name = MOCK_PERSIAN_NAME,
    description = MOCK_PERSIAN_DESCRIPTION,
    temperament = MOCK_PERSIAN_TEMPERAMENT,
    origin = MOCK_PERSIAN_ORIGIN,
    lifeSpan = MOCK_PERSIAN_LIFESPAN,
    imageUrl = MOCK_PERSIAN_IMG_URL,
    isFavorite = false
)
val mockBengalBreed = Breed(
    id = MOCK_BENGAL_ID,
    name = MOCK_BENGAL_NAME,
    description = MOCK_BENGAL_DESCRIPTION,
    temperament = MOCK_BENGAL_TEMPERAMENT,
    origin = MOCK_BENGAL_ORIGIN,
    lifeSpan = MOCK_BENGAL_LIFESPAN,
    imageUrl = MOCK_BENGAL_IMG_URL,
    isFavorite = false
)
val mockMaineCoonBreed = Breed(
    id = MOCK_MCOON_ID,
    name = MOCK_MCOON_NAME,
    description = MOCK_MCOON_DESCRIPTION,
    temperament = MOCK_MCOON_TEMPERAMENT,
    origin = MOCK_MCOON_ORIGIN,
    lifeSpan = MOCK_MCOON_LIFESPAN,
    imageUrl = MOCK_MCOON_IMG_URL,
    isFavorite = false
)
val mockBreedsList = listOf(
    mockSiberianBreed,
    mockPersianBreed,
    mockBengalBreed,
    mockMaineCoonBreed
)

// Helper Methods
fun getBreeds(): List<Breed> = mockBreedsList

fun getBreed(
    id: String = MOCK_YORK_ID,
    name: String = MOCK_YORK_NAME,
    origin: String = MOCK_YORK_ORIGIN,
    description: String = MOCK_YORK_DESCRIPTION,
    temperament: List<String> = MOCK_YORK_TEMPERAMENT,
    lifeSpan: String = MOCK_YORK_LIFESPAN,
    isFavorite: Boolean = false,
    imageUrl: String? = MOCK_YORK_IMG_URL
): Breed {
    return Breed(
        id = id,
        name = name,
        origin = origin,
        description = description,
        temperament = temperament,
        lifeSpan = lifeSpan,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )
}