package com.example.catbreeds.breed_detail.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.breed_detail.BreedDetailViewModel
import com.example.catbreeds.test_core.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Mock data
object TestBreedDetailData {
    fun getTestBreed(id: String, isFav: Boolean = false) = Breed(
        id = id,
        name = "Siberian $id",
        origin = "Russia",
        temperament = "Active, Playful",
        life_span = "10 - 15",
        description = "A strong, moderately large cat.",
        reference_image_id = "sibe",
        isFavorite = isFav
    )
    val testBreed = getTestBreed("testId", false)
}

@ExperimentalCoroutinesApi
class BreedDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var breedRepository: BreedRepository

    @Before
    fun setup() {
        breedRepository = mockk(relaxed = true)
    }

    @Test
    fun `WHEN initialized with valid ID SHOULD load breed`() = runTest {
        // GIVEN
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())

        // WHEN
        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        advanceUntilIdle()

        // THEN
        assertEquals(targetBreed.copy(isFavorite = false), viewModel.breed.value)
    }

    @Test
    fun `WHEN initialized with invalid ID SHOULD handle not found by setting breed to null`() = runTest {
        // GIVEN
        val nonExistentId = "non_existent"
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to nonExistentId))
        coEvery { breedRepository.getBreedById(nonExistentId) } returns null
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())

        // WHEN
        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        advanceUntilIdle()

        // THEN
        assertNull(viewModel.breed.value)
    }

    @Test
    fun `WHEN favorite status updates from flow SHOULD update the ViewModel's breed favorite status`() = runTest {
        // GIVEN
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))
        val favoriteFlow = MutableStateFlow(emptyList<Breed>())
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns favoriteFlow
        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        advanceUntilIdle()
        assertFalse(viewModel.breed.value?.isFavorite ?: true)

        // WHEN
        favoriteFlow.value = listOf(targetBreed.copy(isFavorite = true))
        advanceUntilIdle()

        // THEN
        assertTrue(viewModel.breed.value?.isFavorite ?: false)
    }

    @Test
    fun `WHEN toggling favorite SHOULD update state and call repository correctly`() = runTest {
        // GIVEN
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit
        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        advanceUntilIdle()

        // WHEN (Add to fav)
        viewModel.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN (Verify)
        coVerify(exactly = 1) { breedRepository.addBreedToFavorites(breedId) }
        assertTrue(viewModel.breed.value?.isFavorite ?: false)

        // WHEN (Remove from fav)
        viewModel.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN (Verify)
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
        assertFalse(viewModel.breed.value?.isFavorite ?: true)
    }

    @Test
    fun `WHEN rapidly toggling favorite SHOULD result in correct final state and repository calls`() = runTest {
        // GIVEN
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit
        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        advanceUntilIdle()

        // WHEN (Spam toggle)
        viewModel.toggleFavorite(breedId) // add
        viewModel.toggleFavorite(breedId) // remove
        viewModel.toggleFavorite(breedId) // add
        advanceUntilIdle()

        // THEN
        assertTrue(viewModel.breed.value?.isFavorite ?: false)
        coVerify(exactly = 2) { breedRepository.addBreedToFavorites(breedId) }
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
    }
}