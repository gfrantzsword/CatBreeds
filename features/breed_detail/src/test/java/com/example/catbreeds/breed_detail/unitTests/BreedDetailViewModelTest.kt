package com.example.catbreeds.breed_detail.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.breed_detail.BreedDetailViewModel
import com.example.catbreeds.test_core.MainDispatcherRule
import com.example.catbreeds.test_core.mock.getBreed
import io.mockk.coEvery
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class BreedDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var breedRepository: BreedRepository

    private lateinit var savedStateHandle: SavedStateHandle

    private val vmUnderTest: BreedDetailViewModel by lazy {
        spyk(
            BreedDetailViewModel(
                breedRepository = breedRepository,
                savedStateHandle = savedStateHandle
            ),
        )
    }

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle()
    }

    // Helper Methods
    private fun setupInitialState(breed: Breed, favoriteBreeds: List<Breed> = emptyList()) {
        val breedId = breed.id
        savedStateHandle["breedId"] = breedId
        coEvery { breedRepository.getBreedById(breedId) } returns breed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(favoriteBreeds)
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
    fun `WHEN initialized with valid ID SHOULD load breed`() = runTest {
        // GIVEN
        val targetBreed = getBreed()
        setupInitialState(targetBreed)

        // WHEN
        val vm = vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(targetBreed.copy(isFavorite = false), vm.breed.value)
    }

    @Test
    fun `WHEN initialized with invalid ID SHOULD handle not found by setting breed to null`() = runTest {
        // GIVEN
        val nonExistentId = "non_existent"
        savedStateHandle["breedId"] = nonExistentId
        coEvery { breedRepository.getBreedById(nonExistentId) } returns null
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())

        // WHEN
        val vm = vmUnderTest
        advanceUntilIdle()

        // THEN
        assertNull(vm.breed.value)
    }

    @Test
    fun `WHEN favorite status updates from flow SHOULD update the ViewModel's breed favorite status`() = runTest {
        // GIVEN
        val targetBreed = getBreed()
        val favoriteFlow = MutableStateFlow(emptyList<Breed>())
        setupInitialState(targetBreed)
        every { breedRepository.getFavoriteBreeds() } returns favoriteFlow
        val vm = vmUnderTest
        advanceUntilIdle()
        assertFalse(vm.breed.value?.isFavorite ?: true)

        // WHEN
        favoriteFlow.value = listOf(targetBreed.copy(isFavorite = true))
        advanceUntilIdle()

        // THEN
        assertTrue(vm.breed.value?.isFavorite ?: false)
    }

    @Test
    fun `GIVEN breed is not favorite WHEN toggleFavorite is called SHOULD add it to favorites`() = runTest {
        // GIVEN
        val targetBreed = getBreed(isFavorite = false)
        val breedId = targetBreed.id
        setupInitialState(targetBreed)
        setupToggleFavorites(breedId)
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN
        vm.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN
        verifyAddBreedToFavorites(breedId, 1)
        verifyRemoveBreedFromFavorites(breedId, 0)
        assertTrue(vm.breed.value?.isFavorite ?: false)
    }

    @Test
    fun `GIVEN breed is favorite WHEN toggleFavorite is called SHOULD remove it from favorites`() = runTest {
        // GIVEN
        val targetBreed = getBreed(isFavorite = true)
        val breedId = targetBreed.id
        setupInitialState(targetBreed, listOf(targetBreed))
        setupToggleFavorites(breedId)
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN
        vm.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN
        verifyRemoveBreedFromFavorites(breedId, 1)
        verifyAddBreedToFavorites(breedId, 0)
        assertFalse(vm.breed.value?.isFavorite ?: true)
    }

    @Test
    fun `WHEN rapidly toggling favorite SHOULD result in correct final state AND repository calls`() = runTest {
        // GIVEN
        val targetBreed = getBreed()
        val breedId = targetBreed.id
        setupInitialState(targetBreed)
        setupToggleFavorites(breedId)
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN (Spam toggle)
        with (vm) {
            repeat(3) {
                toggleFavorite(breedId)
            }
        }
        advanceUntilIdle()

        // THEN
        assertTrue(vm.breed.value?.isFavorite ?: false)
        verifyAddBreedToFavorites(breedId, 2)
        verifyRemoveBreedFromFavorites(breedId, 1)
    }
}