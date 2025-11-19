package com.example.catbreeds.data.remote

data class BreedDto(
    val id: String,
    val name: String,
    val description: String,
    val temperament: String,
    val origin: String,
    val life_span: String,
    val reference_image_id: String?
)