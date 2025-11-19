package com.example.catbreeds.domain.models

data class Breed(
    val id: String,
    val name: String,
    val description: String,
    val temperament: List<String>,
    val origin: String,
    val life_span: String,
    val reference_image_id: String? = "0SxW2SQ_S", //Generic image
    val isFavorite: Boolean = false
)