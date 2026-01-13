package com.example.catbreeds.breed_list.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.breed_list.BreedListViewModel
import com.example.catbreeds.test_core.MainDispatcherRule
import com.example.catbreeds.test_core.mock.getBreeds
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class BreedListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var breedRepository: BreedRepository

    private val testBreeds = getBreeds()
    private val breed1 = testBreeds[0] // Siberian
    private val breed2 = testBreeds[1] // Persian
    private val breed3 = testBreeds[2] // Bengal
    private val breed4 = testBreeds[3] // MaineCoon

    private val vmUnderTest: BreedListViewModel by lazy {
        spyk(
            BreedListViewModel(breedRepository)
        )
    }

    // Helper Methods
    private fun setupRepository(breeds: List<Breed>, favoriteBreeds: List<Breed> = emptyList()) {
        every { breedRepository.getBreeds() } returns flowOf(breeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(favoriteBreeds)
        coJustRun { breedRepository.refreshBreeds() }
    }

    private fun setupToggleFavorites(breedId: String) {
        coJustRun { breedRepository.addBreedToFavorites(breedId) }
        coJustRun { breedRepository.removeBreedFromFavorites(breedId) }
    }

    private fun verifyAddBreedToFavorites(breedId: String, times: Int = 1) {
        coVerify(exactly = times) { breedRepository.addBreedToFavorites(breedId) }
    }

    private fun verifyRemoveBreedFromFavorites(breedId: String, times: Int = 1) {
        coVerify(exactly = times) { breedRepository.removeBreedFromFavorites(breedId) }
    }

    // Tests
    @Test
    fun `WHEN initialized with empty repository SHOULD have empty state`() = runTest {
        // GIVEN
        setupRepository(emptyList())

        // WHEN
        vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(emptyList<Breed>(), vmUnderTest.breeds.value)
        assertEquals(emptyList<Breed>(), vmUnderTest.filteredBreeds.value)
        assertEquals("", vmUnderTest.searchQuery.value)
    }

    @Test
    fun `WHEN correctly initialized SHOULD load breeds correctly`() = runTest {
        // GIVEN
        setupRepository(testBreeds)

        // WHEN
        vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(testBreeds, vmUnderTest.breeds.value)
        assertEquals(testBreeds, vmUnderTest.filteredBreeds.value)
    }

    @Test
    fun `WHEN search query is updated with name segment SHOULD filter by name`() = runTest {
        // GIVEN
        setupRepository(testBreeds)
        vmUnderTest
        advanceUntilIdle()

        // WHEN (search by Name segment)
        vmUnderTest.updateSearchQuery("sibe")
        advanceUntilIdle()

        // THEN (name match)
        assertEquals(listOf(breed1), vmUnderTest.filteredBreeds.value)
    }

    @Test
    fun `WHEN search query is updated with origin SHOULD filter by origin`() = runTest {
        // GIVEN
        setupRepository(testBreeds)
        vmUnderTest
        advanceUntilIdle()

        // WHEN (search by origin)
        vmUnderTest.updateSearchQuery("iran")
        advanceUntilIdle()

        // THEN (origin match)
        assertEquals(listOf(breed2), vmUnderTest.filteredBreeds.value)
    }

    @Test
    fun `WHEN search query is updated with uppercase temperament SHOULD filter by temperament`() = runTest {
        // GIVEN
        setupRepository(testBreeds)
        vmUnderTest
        advanceUntilIdle()

        // WHEN (search by temperament (Upper case))
        vmUnderTest.updateSearchQuery("ENERGETIC")
        advanceUntilIdle()

        // THEN (temperament match)
        assertEquals(listOf(breed3), vmUnderTest.filteredBreeds.value)
    }

    @Test
    fun `WHEN search query is updated with common term SHOULD return multiple matches`() = runTest {
        // GIVEN
        setupRepository(testBreeds)
        vmUnderTest
        advanceUntilIdle()

        // WHEN (search by temperament (multiple matches))
        vmUnderTest.updateSearchQuery("Playful")
        advanceUntilIdle()

        // THEN (multiple matches)
        val expected = listOf(breed1, breed4)
        val actual = vmUnderTest.filteredBreeds.value
        assertEquals(expected.size, actual.size)
        assertTrue(actual.containsAll(expected))
    }

    @Test
    fun `WHEN search query has no matches SHOULD return empty list`() = runTest {
        // GIVEN
        setupRepository(testBreeds)
        vmUnderTest
        advanceUntilIdle()

        // WHEN (no matches)
        vmUnderTest.updateSearchQuery("Zzzzz")
        advanceUntilIdle()

        // THEN (empty list)
        assertEquals(emptyList<Breed>(), vmUnderTest.filteredBreeds.value)
    }

    @Test
    fun `WHEN toggleFavorite is called on an unfavorite breed SHOULD call addBreedToFavorites repository method`() = runTest {
        // GIVEN
        val breedId = testBreeds.first().id
        setupRepository(testBreeds)
        setupToggleFavorites(breedId)
        vmUnderTest
        advanceUntilIdle()

        // WHEN
        vmUnderTest.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN
        verifyAddBreedToFavorites(breedId)
    }

    @Test
    fun `WHEN rapidly toggling favorite SHOULD result in correct final state AND repository calls`() = runTest {
        // GIVEN
        val testBreed = testBreeds.first()
        val breedId = testBreed.id
        val favoriteBreedsFlow = MutableStateFlow(emptyList<Breed>())
        setupRepository(listOf(testBreed))
        every { breedRepository.getFavoriteBreeds() } returns favoriteBreedsFlow
        setupToggleFavorites(breedId)
        vmUnderTest
        advanceUntilIdle()

        // WHEN (Spam toggle)
        with (vmUnderTest) {
            repeat(3) {
                toggleFavorite(breedId)
            }
        }
        advanceUntilIdle()

        // THEN
        val finalBreed = vmUnderTest.filteredBreeds.value.firstOrNull()
        assertTrue(finalBreed?.isFavorite ?: false)
        verifyAddBreedToFavorites(breedId, 2)
        verifyRemoveBreedFromFavorites(breedId)
    }

    @Test
    fun `WHEN favorite status updates from flow SHOULD correctly update favorite status in filtered list`() = runTest {
        // GIVEN
        val favoriteBreed = testBreeds.first()
        val unfavoriteBreed = testBreeds.last()
        val favoriteFlow = MutableStateFlow(emptyList<Breed>())
        setupRepository(listOf(favoriteBreed, unfavoriteBreed))
        every { breedRepository.getFavoriteBreeds() } returns favoriteFlow
        vmUnderTest
        advanceUntilIdle()

        // WHEN
        favoriteFlow.value = listOf(favoriteBreed.copy(isFavorite = true))
        advanceUntilIdle()

        // THEN
        val favoriteInFiltered = vmUnderTest.filteredBreeds.value.find { it.id == favoriteBreed.id }
        assertNotNull(favoriteInFiltered)
        assertTrue(favoriteInFiltered!!.isFavorite)
        assertFalse(vmUnderTest.filteredBreeds.value.last().isFavorite)
    }
}