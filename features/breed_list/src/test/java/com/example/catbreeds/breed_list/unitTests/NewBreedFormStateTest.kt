package com.example.catbreeds.breed_list.unitTests

import com.example.catbreeds.breed_list.NewBreedFormState
import com.example.catbreeds.core.R
import com.example.catbreeds.test_core.mock.getBreeds
import org.junit.Assert.assertEquals
import org.junit.Test

class NewBreedFormStateTest {

    private val mockBreeds = getBreeds()
    private val existingNames = mockBreeds.map { it.name }
    private val state = NewBreedFormState(existingNames)
    
    // Name Field
    @Test
    fun `Name field SHOULD display error WHEN empty AND submitted`() {
        // GIVEN
        state.update(name = "")

        // WHEN
        state.validate()

        // THEN
        assertEquals(R.string.error_required, state.nameError.value)
    }

    @Test
    fun `Name field SHOULD return error WHEN duplicate`() {
        // WHEN
        state.update(name = existingNames[0])

        // THEN
        assertEquals(R.string.error_name_exists, state.nameError.value)
    }

    // Origin Field
    @Test
    fun `Origin field SHOULD display error WHEN empty AND submitted`() {
        // GIVEN
        state.update(origin = "")

        // WHEN
        state.validate()

        // THEN
        assertEquals(R.string.error_required, state.originError.value)
    }

    // Life Expectancy Field
    /// Min
    @Test
    fun `MinLife field SHOULD display error WHEN empty AND submitted`() {
        //GIVEN
        state.update(minLife = "")

        // WHEN
        state.validate()

        // THEN
        assertEquals(R.string.error_required, state.minLifeError.value)
    }

    /// Max
    @Test
    fun `MaxLife field SHOULD display error WHEN empty AND submitted`() {
        // GIVEN
        state.update(maxLife = "")

        // WHEN
        state.validate()

        // THEN
        assertEquals(R.string.error_required, state.maxLifeError.value)
    }

    @Test
    fun `MaxLife field SHOULD return error WHEN less than MinLife`() {
        // GIVEN
        state.update(minLife = "15", maxLife = "10")

        // THEN
        assertEquals(R.string.error_min_max_validation, state.maxLifeError.value)
    }

    // Description Field
    @Test
    fun `Description field SHOULD display error WHEN empty AND submitted`() {
        // GIVEN
        state.update(description = "")

        // WHEN
        state.validate()

        // THEN
        assertEquals(R.string.error_required, state.descriptionError.value)
    }
}