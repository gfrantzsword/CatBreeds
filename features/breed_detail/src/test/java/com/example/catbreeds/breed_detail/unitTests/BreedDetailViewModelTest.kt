package com.example.catbreeds.breed_detail.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.breed_detail.BreedDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var breedRepository: BreedRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        breedRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // CASE: everything is fine and load ok
    @Test
    fun initWithValidIdLoadsBreed() = runTest {
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))

        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())

        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(targetBreed.copy(isFavorite = false), viewModel.breed.value)
    }

    // CASE: breed not found
    @Test
    fun initWithInvalidIdHandlesNotFound() = runTest {
        val nonExistentId = "non_existent"
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to nonExistentId))

        coEvery { breedRepository.getBreedById(nonExistentId) } returns null
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())

        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.breed.value)
    }

    // CASE: favorites display correctly
    @Test
    fun favoriteStatusUpdatesFromFlow() = runTest {
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))
        val favoriteFlow = MutableStateFlow(emptyList<Breed>())

        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns favoriteFlow

        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.breed.value?.isFavorite ?: true)

        favoriteFlow.value = listOf(targetBreed.copy(isFavorite = true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.breed.value?.isFavorite ?: false)
    }

    // CASE: favorite/unfav button on detail works
    @Test
    fun toggleFavoriteUpdatesStateAndCallsRepository() = runTest {
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))

        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit

        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        // Add to fav
        viewModel.toggleFavorite(breedId)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { breedRepository.addBreedToFavorites(breedId) }
        assertTrue(viewModel.breed.value?.isFavorite ?: false)

        // Remove from fav
        viewModel.toggleFavorite(breedId)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
        assertFalse(viewModel.breed.value?.isFavorite ?: true)
    }

    // CASE: spam fav toggle handles fine
    @Test
    fun toggleFavoriteRapidlyHandlesStateCorrectly() = runTest {
        val targetBreed = TestBreedDetailData.testBreed
        val breedId = targetBreed.id
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to breedId))

        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit

        val viewModel = BreedDetailViewModel(breedRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        // Spamming toggle
        viewModel.toggleFavorite(breedId) // add
        viewModel.toggleFavorite(breedId) // remove
        viewModel.toggleFavorite(breedId) // add
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.breed.value?.isFavorite ?: false)
        coVerify(exactly = 2) { breedRepository.addBreedToFavorites(breedId) }
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
    }
}