package com.example.catbreeds.domain.models

data class Breed(
    val id: String,
    val name: String,
    val description: String,
    val temperament: List<String>,
    val origin: String,
    val lifeSpan: String,
    val imageUrl: String? = "",
    val isFavorite: Boolean = false
)