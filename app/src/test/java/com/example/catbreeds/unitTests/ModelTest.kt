package com.example.catbreeds.unitTests

import com.example.catbreeds.data.local.BreedEntity
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.unitTests.utils.TestUtils
import org.junit.Test
import org.junit.Assert.*

class ModelTest {

    @Test
    fun `breed model creates correctly with all fields`() {
        val breed = Breed(
            id = "test123",
            name = "Test Cat",
            origin = "Test Country",
            description = "Test description",
            temperament = "Friendly, Playful",
            life_span = "12 - 15",
            reference_image_id = "test_image_id"
        )

        assertEquals("test123", breed.id)
        assertEquals("Test Cat", breed.name)
        assertEquals("Test Country", breed.origin)
        assertEquals("Test description", breed.description)
        assertEquals("Friendly, Playful", breed.temperament)
        assertEquals("12 - 15", breed.life_span)
        assertEquals("test_image_id", breed.reference_image_id)
    }

    @Test
    fun `breed model uses default image when reference_image_id is null`() {
        val breed = Breed(
            id = "test123",
            name = "Test Cat",
            origin = "Test Country",
            description = "Test description",
            temperament = "Friendly",
            life_span = "12 - 15"
        )

        assertEquals("0SxW2SQ_S", breed.reference_image_id)
    }

    @Test
    fun `breedEntity converts to breed correctly`() {
        val entity = BreedEntity(
            id = "test123",
            name = "Test Cat",
            origin = "Test Country",
            description = "Test description",
            temperament = "Friendly, Playful",
            life_span = "12 - 15",
            reference_image_id = "test_image_id"
        )

        val breed = entity.toBreed()

        assertEquals(entity.id, breed.id)
        assertEquals(entity.name, breed.name)
        assertEquals(entity.origin, breed.origin)
        assertEquals(entity.description, breed.description)
        assertEquals(entity.temperament, breed.temperament)
        assertEquals(entity.life_span, breed.life_span)
        assertEquals(entity.reference_image_id, breed.reference_image_id)
    }

    @Test
    fun `sample breed from TestUtils has correct structure`() {
        val sampleBreed = TestUtils.getSampleBreed()

        assertNotNull(sampleBreed.id)
        assertNotNull(sampleBreed.name)
        assertNotNull(sampleBreed.origin)
        assertNotNull(sampleBreed.description)
        assertNotNull(sampleBreed.temperament)
        assertNotNull(sampleBreed.life_span)
        assertNotNull(sampleBreed.reference_image_id)

        assertTrue(sampleBreed.id.isNotEmpty())
        assertTrue(sampleBreed.name.isNotEmpty())
        assertTrue(sampleBreed.description.isNotEmpty())
    }
}