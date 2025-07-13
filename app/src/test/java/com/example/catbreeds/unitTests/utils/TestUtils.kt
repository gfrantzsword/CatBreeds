package com.example.catbreeds.unitTests.utils

import com.example.catbreeds.domain.models.Breed

object TestUtils {
    fun getSampleBreed(): Breed {
        // Sample with real info
        return Breed(
            id = "ycho",
            name = "York Chocolate",
            origin = "United States",
            description = "York Chocolate cats are known to be true lap cats with a sweet temperament. They love to be cuddled and petted. Their curious nature makes them follow you all the time and participate in almost everything you do, even if it's related to water: unlike many other cats, York Chocolates love it.",
            temperament = "Playful, Social, Intelligent, Curious, Friendly",
            life_span = "13 - 15",
            reference_image_id = "0SxW2SQ_S"
        )
    }
}