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

    @Test
    fun `WHEN initialized with empty repository SHOULD have empty state`() = runTest {
        // GIVEN
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        // WHEN
        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        assertEquals(emptyList<Breed>(), viewModel.breeds.value)
        assertEquals(emptyList<Breed>(), viewModel.filteredBreeds.value)
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `WHEN correctly initialized SHOULD load breeds correctly`() = runTest {
        // GIVEN
        every { breedRepository.getBreeds() } returns flowOf(testBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        // WHEN
        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        assertEquals(testBreeds, viewModel.breeds.value)
        assertEquals(testBreeds, viewModel.filteredBreeds.value)
    }

    @Test
    fun `WHEN initialized SHOULD call refreshBreeds`() = runTest {
        // GIVEN
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        // WHEN
        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify { breedRepository.refreshBreeds() }
    }

    @Test
    fun `WHEN search query is updated SHOULD filter list correctly by name origin or temperament`() = runTest {
        // GIVEN
        val (breed1, breed2, breed3, breed4) = testBreeds
        every { breedRepository.getBreeds() } returns flowOf(testBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit
        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN (search by Name segment)
        viewModel.updateSearchQuery("sibe")
        testDispatcher.scheduler.advanceUntilIdle()
        // THEN (name match)
        assertEquals(listOf(breed1), viewModel.filteredBreeds.value)

        // WHEN (search by origin)
        viewModel.updateSearchQuery("iran")
        testDispatcher.scheduler.advanceUntilIdle()
        // THEN (origin match)
        assertEquals(listOf(breed2), viewModel.filteredBreeds.value)

        // WHEN (search by temperament (Upper case))
        viewModel.updateSearchQuery("ENERGETIC")
        testDispatcher.scheduler.advanceUntilIdle()
        // THEN (temperament match)
        assertEquals(listOf(breed3), viewModel.filteredBreeds.value)

        // WHEN (search by temperament (multiple matches))
        viewModel.updateSearchQuery("Playful")
        testDispatcher.scheduler.advanceUntilIdle()
        // THEN (multiple matches)
        assertEquals(2, viewModel.filteredBreeds.value.size)
        assertTrue(viewModel.filteredBreeds.value.containsAll(listOf(breed1, breed4)))

        // WHEN (no matches)
        viewModel.updateSearchQuery("Zzz")
        testDispatcher.scheduler.advanceUntilIdle()
        // THEN (empty list)
        assertEquals(emptyList<Breed>(), viewModel.filteredBreeds.value)
    }

    @Test
    fun `WHEN toggleFavorite is called on an unfavorite breed SHOULD call addBreedToFavorites repository method`() = runTest {
        // GIVEN
        val breedId = testBreeds.first().id
        every { breedRepository.getBreeds() } returns flowOf(testBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit
        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN
        viewModel.toggleFavorite(breedId)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        coVerify { breedRepository.addBreedToFavorites(breedId) }
        coVerify(exactly = 0) { breedRepository.removeBreedFromFavorites(any()) }
    }

    @Test
    fun `WHEN rapidly toggling favorite SHOULD result in correct final state and repository calls`() = runTest {
        // GIVEN
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

        // WHEN (spam toggle)
        viewModel.toggleFavorite(breedId) // add
        viewModel.toggleFavorite(breedId) // remove
        viewModel.toggleFavorite(breedId) // add
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        val finalBreed = viewModel.filteredBreeds.value.firstOrNull()
        assertTrue(finalBreed?.isFavorite ?: false)
        coVerify(exactly = 2) { breedRepository.addBreedToFavorites(breedId) }
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
    }

    @Test
    fun `WHEN favorite status updates from flow SHOULD correctly update favorite status in filtered list`() = runTest {
        // GIVEN
        val favoriteBreed = testBreeds.first()
        val unfavoriteBreed = testBreeds.last()
        val favoriteFlow = MutableStateFlow(emptyList<Breed>())

        every { breedRepository.getBreeds() } returns flowOf(listOf(favoriteBreed, unfavoriteBreed))
        every { breedRepository.getFavoriteBreeds() } returns favoriteFlow
        coEvery { breedRepository.refreshBreeds() } returns Unit
        viewModel = BreedListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN
        favoriteFlow.value = listOf(favoriteBreed.copy(isFavorite = true))
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        val favoriteInFiltered = viewModel.filteredBreeds.value.find { it.id == favoriteBreed.id }
        assertNotNull(favoriteInFiltered)
        assertTrue(favoriteInFiltered!!.isFavorite)
        assertFalse(viewModel.filteredBreeds.value.last().isFavorite)
    }
}