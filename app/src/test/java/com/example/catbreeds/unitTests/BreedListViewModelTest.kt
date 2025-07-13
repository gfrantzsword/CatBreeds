package com.example.catbreeds.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.ui.breedList.BreedListViewModel
import com.example.catbreeds.unitTests.utils.TestUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class BreedListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BreedListViewModel
    private lateinit var breedRepository: BreedRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        breedRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty breeds list`() = runTest {
        // Mock repository to return empty flows
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<Breed>(), viewModel.breeds.value)
        assertEquals(emptyList<Breed>(), viewModel.filteredBreeds.value)
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `observeBreeds updates breeds list on success`() = runTest {
        val sampleBreeds = TestUtils.getSampleBreeds()
        every { breedRepository.getBreeds() } returns flowOf(sampleBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(sampleBreeds, viewModel.breeds.value)
        assertEquals(sampleBreeds, viewModel.filteredBreeds.value)
    }

    @Test
    fun `updateSearchQuery filters breeds correctly`() = runTest {
        val sampleBreeds = TestUtils.getSampleBreeds()
        every { breedRepository.getBreeds() } returns flowOf(sampleBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assuming first breed has name "Persian" for example
        viewModel.updateSearchQuery("Persian")

        val filteredBreeds = viewModel.filteredBreeds.value
        assertTrue(filteredBreeds.all { breed ->
            breed.name.contains("Persian", ignoreCase = true) ||
                    breed.origin.contains("Persian", ignoreCase = true) ||
                    breed.temperament.contains("Persian", ignoreCase = true)
        })
    }

    @Test
    fun `toggleFavorite calls repository methods correctly`() = runTest {
        val sampleBreeds = TestUtils.getSampleBreeds()
        val breedId = sampleBreeds.first().id

        every { breedRepository.getBreeds() } returns flowOf(sampleBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Add to favorites
        viewModel.toggleFavorite(breedId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { breedRepository.addBreedToFavorites(breedId) }
    }

    @Test
    fun `favorite breeds are marked correctly in filtered list`() = runTest {
        val sampleBreeds = TestUtils.getSampleBreeds()
        val favoriteBreed = sampleBreeds.first()

        every { breedRepository.getBreeds() } returns flowOf(sampleBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(listOf(favoriteBreed))
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val filteredBreeds = viewModel.filteredBreeds.value
        val favoriteInFiltered = filteredBreeds.find { it.id == favoriteBreed.id }

        assertNotNull(favoriteInFiltered)
        assertTrue(favoriteInFiltered!!.isFavorite)
    }

    @Test
    fun `refreshBreeds is called during initialization`() = runTest {
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { breedRepository.refreshBreeds() }
    }
}