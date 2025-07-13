package com.example.catbreeds.unitTests.utils

import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.data.local.BreedEntity

object TestUtils {
    fun getSampleBreed(): Breed {
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

    fun getSampleBreeds(): List<Breed> {
        return listOf(
            getSampleBreed(),
            Breed(
                id = "abys",
                name = "Abyssinian",
                origin = "Egypt",
                description = "The Abyssinian is easy to care for, and a joy to have in your home. They're affectionate cats and love both people and other animals.",
                temperament = "Active, Energetic, Independent, Intelligent, Gentle",
                life_span = "14 - 15",
                reference_image_id = "0XYvRd7oD"
            ),
            Breed(
                id = "aege",
                name = "Aegean",
                origin = "Greece",
                description = "Native to the Greek islands known as the Cyclades in the Aegean Sea, these are natural cats, meaning they developed without humans getting involved in their breeding.",
                temperament = "Affectionate, Social, Intelligent, Playful, Active",
                life_span = "9 - 12",
                reference_image_id = "ozEvzdVM-"
            )
        )
    }

    fun getSampleBreedEntity(): BreedEntity {
        return BreedEntity(
            id = "ycho",
            name = "York Chocolate",
            origin = "United States",
            description = "York Chocolate cats are known to be true lap cats with a sweet temperament.",
            temperament = "Playful, Social, Intelligent, Curious, Friendly",
            life_span = "13 - 15",
            reference_image_id = "0SxW2SQ_S"
        )
    }

    fun getSampleBreedEntities(): List<BreedEntity> {
        return listOf(
            getSampleBreedEntity(),
            BreedEntity(
                id = "abys",
                name = "Abyssinian",
                origin = "Egypt",
                description = "The Abyssinian is easy to care for, and a joy to have in your home.",
                temperament = "Active, Energetic, Independent, Intelligent, Gentle",
                life_span = "14 - 15",
                reference_image_id = "0XYvRd7oD"
            )
        )
    }

    fun getBreedWithNullImageId(): Breed {
        return Breed(
            id = "test123",
            name = "Test Cat",
            origin = "Test Country",
            description = "Test description",
            temperament = "Friendly",
            life_span = "12 - 15",
            reference_image_id = null
        )
    }
}