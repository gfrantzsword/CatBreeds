package com.example.catbreeds.breed_list.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.breed_list.BreedListViewModel
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
object TestBreedListData {
    fun getTestBreeds(): List<Breed> = listOf(
        Breed("sibe", "Siberian", "Siberia", "Playful, Calm", "10 - 15", "desc", "sibe_ref", false),
        Breed("pers", "Persian", "Iran", "Reserved, Quiet", "12 - 14", "desc", "pers_ref", false),
        Breed("beng", "Bengal", "USA", "Active, Energetic", "9 - 12", "desc", "beng_ref", false),
        Breed("mcoo", "Maine Coon", "USA", "Gentle, Playful", "12 - 15", "desc", "mcoo_ref", false)
    )
}

@ExperimentalCoroutinesApi
class BreedListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BreedListViewModel
    private lateinit var breedRepository: BreedRepository
    private val testBreeds = TestBreedListData.getTestBreeds()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        breedRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // CASE: starts empty
    @Test
    fun initialStateWithEmptyRepoIsEmpty() = runTest {
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<Breed>(), viewModel.breeds.value)
        assertEquals(emptyList<Breed>(), viewModel.filteredBreeds.value)
        assertEquals("", viewModel.searchQuery.value)
    }

    // CASE: list is correctly updated
    @Test
    fun initLoadsBreedsCorrectly() = runTest {
        every { breedRepository.getBreeds() } returns flowOf(testBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(testBreeds, viewModel.breeds.value)
        assertEquals(testBreeds, viewModel.filteredBreeds.value)
    }

    // CASE: initialization calls refresh
    @Test
    fun initCallsRefreshBreeds() = runTest {
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { breedRepository.refreshBreeds() }
    }

    // CASE: tries segments or full string of names, origins and temperaments
    @Test
    fun searchQueryFiltersListCorrectly() = runTest {
        val (breed1, breed2, breed3, breed4) = testBreeds
        every { breedRepository.getBreeds() } returns flowOf(testBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Name (Siberian)
        viewModel.updateSearchQuery("sibe")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(breed1), viewModel.filteredBreeds.value)

        // Origin (Iran)
        viewModel.updateSearchQuery("iran")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(breed2), viewModel.filteredBreeds.value)

        // Temperament (Energetic) (Upper case)
        viewModel.updateSearchQuery("ENERGETIC")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(breed3), viewModel.filteredBreeds.value)

        // Temperament (Playful) (Match multiple)
        viewModel.updateSearchQuery("Playful")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.filteredBreeds.value.size)
        assertTrue(viewModel.filteredBreeds.value.containsAll(listOf(breed1, breed4)))

        // No match
        viewModel.updateSearchQuery("Zzz")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(emptyList<Breed>(), viewModel.filteredBreeds.value)
    }

    // CASE: simple testing if fav toggle works
    @Test
    fun toggleFavoriteCallsAddRepositoryMethod() = runTest {
        val breedId = testBreeds.first().id
        every { breedRepository.getBreeds() } returns flowOf(testBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite(breedId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { breedRepository.addBreedToFavorites(breedId) }
    }

    // CASE: spam fav toggle
    @Test
    fun toggleFavoriteRapidlyHandlesStateCorrectly() = runTest {
        val testBreed = testBreeds.first()
        val breedId = testBreed.id

        val favoriteBreedsFlow = MutableStateFlow(emptyList<Breed>())
        every { breedRepository.getBreeds() } returns flowOf(listOf(testBreed))
        every { breedRepository.getFavoriteBreeds() } returns favoriteBreedsFlow
        coEvery { breedRepository.refreshBreeds() } returns Unit
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite(breedId) // add
        viewModel.toggleFavorite(breedId) // remove
        viewModel.toggleFavorite(breedId) // add
        testDispatcher.scheduler.advanceUntilIdle()

        val finalBreed = viewModel.filteredBreeds.value.firstOrNull()
        assertTrue(finalBreed?.isFavorite ?: false)
        coVerify(exactly = 2) { breedRepository.addBreedToFavorites(breedId) }
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
    }

    // CASE: fav toggle updated between pages/back button
    @Test
    fun favoriteStatusUpdatesFromFlow() = runTest {
        val favoriteBreed = testBreeds.first()
        val unfavoriteBreed = testBreeds.last()
        val favoriteFlow = MutableStateFlow(emptyList<Breed>())

        every { breedRepository.getBreeds() } returns flowOf(listOf(favoriteBreed, unfavoriteBreed))
        every { breedRepository.getFavoriteBreeds() } returns favoriteFlow
        coEvery { breedRepository.refreshBreeds() } returns Unit

        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.filteredBreeds.value.first().isFavorite)

        favoriteFlow.value = listOf(favoriteBreed.copy(isFavorite = true))
        testDispatcher.scheduler.advanceUntilIdle()

        val favoriteInFiltered = viewModel.filteredBreeds.value.find { it.id == favoriteBreed.id }
        assertNotNull(favoriteInFiltered)
        assertTrue(favoriteInFiltered!!.isFavorite)
        assertFalse(viewModel.filteredBreeds.value.last().isFavorite)
    }
}