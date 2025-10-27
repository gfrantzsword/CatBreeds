package com.example.catbreeds.breed_list.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.breed_list.BreedListViewModel
import com.example.catbreeds.test_core.MainDispatcherRule
import com.example.catbreeds.test_core.mock.getBreeds
import io.mockk.coEvery
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

    private val vmUnderTest: BreedListViewModel by lazy {
        spyk(
            BreedListViewModel(breedRepository)
        )
    }

    @Test
    fun `WHEN initialized with empty repository SHOULD have empty state`() = runTest {
        // GIVEN
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        // WHEN
        val vm = vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(emptyList<Breed>(), vm.breeds.value)
        assertEquals(emptyList<Breed>(), vm.filteredBreeds.value)
        assertEquals("", vm.searchQuery.value)
    }

    @Test
    fun `WHEN correctly initialized SHOULD load breeds correctly`() = runTest {
        // GIVEN
        every { breedRepository.getBreeds() } returns flowOf(testBreeds)
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        // WHEN
        val vm = vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(testBreeds, vm.breeds.value)
        assertEquals(testBreeds, vm.filteredBreeds.value)
    }

    @Test
    fun `WHEN initialized SHOULD call refreshBreeds`() = runTest {
        // GIVEN
        every { breedRepository.getBreeds() } returns flowOf(emptyList())
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.refreshBreeds() } returns Unit

        // WHEN
        val vm = vmUnderTest // Triggers the lazy initialization of vmUnderTest
        advanceUntilIdle()

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
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN (search by Name segment)
        vm.updateSearchQuery("sibe")
        advanceUntilIdle()
        // THEN (name match)
        assertEquals(listOf(breed1), vm.filteredBreeds.value)

        // WHEN (search by origin)
        vm.updateSearchQuery("iran")
        advanceUntilIdle()
        // THEN (origin match)
        assertEquals(listOf(breed2), vm.filteredBreeds.value)

        // WHEN (search by temperament (Upper case))
        vm.updateSearchQuery("ENERGETIC")
        advanceUntilIdle()
        // THEN (temperament match)
        assertEquals(listOf(breed3), vm.filteredBreeds.value)

        // WHEN (search by temperament (multiple matches))
        vm.updateSearchQuery("Playful")
        advanceUntilIdle()
        // THEN (multiple matches)
        assertEquals(2, vm.filteredBreeds.value.size)
        assertTrue(vm.filteredBreeds.value.containsAll(listOf(breed1, breed4)))

        // WHEN (no matches)
        vm.updateSearchQuery("Zzz")
        advanceUntilIdle()
        // THEN (empty list)
        assertEquals(emptyList<Breed>(), vm.filteredBreeds.value)
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
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN
        vm.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN
        coVerify { breedRepository.addBreedToFavorites(breedId) }
        coVerify(exactly = 0) { breedRepository.removeBreedFromFavorites(any()) }
    }

    @Test
    fun `WHEN rapidly toggling favorite SHOULD result in correct final state AND repository calls`() = runTest {
        // GIVEN
        val testBreed = testBreeds.first()
        val breedId = testBreed.id
        val favoriteBreedsFlow = MutableStateFlow(emptyList<Breed>())

        every { breedRepository.getBreeds() } returns flowOf(listOf(testBreed))
        every { breedRepository.getFavoriteBreeds() } returns favoriteBreedsFlow
        coEvery { breedRepository.refreshBreeds() } returns Unit
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit
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
        val finalBreed = vm.filteredBreeds.value.firstOrNull()
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
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN
        favoriteFlow.value = listOf(favoriteBreed.copy(isFavorite = true))
        advanceUntilIdle()

        // THEN
        val favoriteInFiltered = vm.filteredBreeds.value.find { it.id == favoriteBreed.id }
        assertNotNull(favoriteInFiltered)
        assertTrue(favoriteInFiltered!!.isFavorite)
        assertFalse(vm.filteredBreeds.value.last().isFavorite)
    }
}