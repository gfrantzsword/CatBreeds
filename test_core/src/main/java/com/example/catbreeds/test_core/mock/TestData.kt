package com.example.catbreeds.test_core.mock

import com.example.catbreeds.domain.models.Breed

fun getBreed(
    id: String = "ycho",
    name: String = "York Chocolate",
    origin: String = "United States",
    description: String = "A test description for $name.",
    temperament: String = "Playful, Social, Intelligent",
    life_span: String = "13 - 15",
    isFavorite: Boolean = false,
    reference_image_id: String? = "0SxW2SQ_S"
): Breed {
    return Breed(
        id = id,
        name = name,
        origin = origin,
        description = description,
        temperament = temperament,
        life_span = life_span,
        reference_image_id = reference_image_id,
        isFavorite = isFavorite
    )
}

fun getBreeds(): List<Breed> {
    return listOf(
        getBreed(
            id = "sibe",
            name = "Siberian",
            description = "desc",
            temperament = "Playful, Calm",
            origin = "Siberia",
            life_span = "10 - 15",
            reference_image_id = "sibe_ref",
            isFavorite = false
        ),
        getBreed(
            id = "pers",
            name = "Persian",
            description = "desc",
            temperament = "Reserved, Quiet",
            origin = "Iran",
            life_span = "12 - 14",
            reference_image_id = "pers_ref",
            isFavorite = false
        ),
        getBreed(
            id = "beng",
            name = "Bengal",
            description = "desc",
            temperament = "Active, Energetic",
            origin = "USA",
            life_span = "9 - 12",
            reference_image_id = "beng_ref",
            isFavorite = false
        ),
        getBreed(
            id = "mcoo",
            name = "Maine Coon",
            description = "desc",
            temperament = "Gentle, Playful",
            origin = "USA",
            life_span = "12 - 15",
            reference_image_id = "mcoo_ref",
            isFavorite = false
        )
    )
}