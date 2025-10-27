package com.example.catbreeds.breed_detail.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.breed_detail.BreedDetailViewModel
import com.example.catbreeds.test_core.MainDispatcherRule
import com.example.catbreeds.test_core.mock.getBreed
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

    @Test
    fun `WHEN initialized with valid ID SHOULD load breed`() = runTest {
        // GIVEN
        val targetBreed = getBreed()
        val breedId = targetBreed.id
        savedStateHandle["breedId"] = breedId
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())

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
        val breedId = targetBreed.id
        savedStateHandle["breedId"] = breedId
        val favoriteFlow = MutableStateFlow(emptyList<Breed>())
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
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
    fun `WHEN toggling favorite SHOULD update state and call repository correctly`() = runTest {
        // GIVEN
        val targetBreed = getBreed()
        val breedId = targetBreed.id
        savedStateHandle["breedId"] = breedId
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN (Add to fav)
        vm.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN (Verify)
        coVerify(exactly = 1) { breedRepository.addBreedToFavorites(breedId) }
        assertTrue(vm.breed.value?.isFavorite ?: false)

        // WHEN (Remove from fav)
        vm.toggleFavorite(breedId)
        advanceUntilIdle()

        // THEN (Verify)
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
        assertFalse(vm.breed.value?.isFavorite ?: true)
    }

    @Test
    fun `WHEN rapidly toggling favorite SHOULD result in correct final state and repository calls`() = runTest {
        // GIVEN
        val targetBreed = getBreed()
        val breedId = targetBreed.id
        savedStateHandle["breedId"] = breedId
        coEvery { breedRepository.getBreedById(breedId) } returns targetBreed
        every { breedRepository.getFavoriteBreeds() } returns flowOf(emptyList())
        coEvery { breedRepository.addBreedToFavorites(breedId) } returns Unit
        coEvery { breedRepository.removeBreedFromFavorites(breedId) } returns Unit
        val vm = vmUnderTest
        advanceUntilIdle()

        // WHEN (Spam toggle)
        vm.toggleFavorite(breedId) // add
        vm.toggleFavorite(breedId) // remove
        vm.toggleFavorite(breedId) // add
        advanceUntilIdle()

        // THEN
        assertTrue(vm.breed.value?.isFavorite ?: false)
        coVerify(exactly = 2) { breedRepository.addBreedToFavorites(breedId) }
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(breedId) }
    }
}