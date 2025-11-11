package com.example.catbreeds.favorites.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.favorite_list.FavoriteListViewModel
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FavoriteListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var breedRepository: BreedRepository

    private val vmUnderTest: FavoriteListViewModel by lazy {
        spyk(
            FavoriteListViewModel(breedRepository)
        )
    }

    // Helper Methods
    private fun whenGetFavoriteBreedsReturns(breedList: List<Breed>): MutableStateFlow<List<Breed>> {
        val flow = MutableStateFlow(breedList)
        every { breedRepository.getFavoriteBreeds() } returns flow
        return flow
    }

    private fun verifyRemoveFromFavorites(breedId: String, times: Int = 1) {
        coVerify(exactly = times) { breedRepository.removeBreedFromFavorites(breedId) }
    }

    // Tests
    @Test
    fun `WHEN correctly initialized SHOULD load AND display favorites correctly`() = runTest {
        // GIVEN
        val initialFavorites = listOf(
            getBreed(id = "id1", name = "Siberian", isFavorite = true),
            getBreed(id = "id2", name = "Persian", isFavorite = true)
        )
        whenGetFavoriteBreedsReturns(initialFavorites)

        // WHEN
        vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(2, vmUnderTest.favoriteBreeds.value.size)
        assertEquals("Siberian", vmUnderTest.favoriteBreeds.value[0].name)
        assertEquals("Persian", vmUnderTest.favoriteBreeds.value[1].name)
    }

    @Test
    fun `WHEN breed is removed from favorites SHOULD update list AND call repository`() = runTest {
        // GIVEN
        val favoriteToRemove = getBreed(id = "id_remove", isFavorite = true)
        val remainingFavorite = getBreed(id = "id_keep", isFavorite = true)
        val initialFavorites = listOf(favoriteToRemove, remainingFavorite)
        val favoriteFlow = whenGetFavoriteBreedsReturns(initialFavorites)

        coEvery { breedRepository.removeBreedFromFavorites(favoriteToRemove.id) } answers {
            favoriteFlow.value = listOf(remainingFavorite)
        }
        vmUnderTest
        advanceUntilIdle()
        assertEquals(2, vmUnderTest.favoriteBreeds.value.size)

        // WHEN
        vmUnderTest.removeFromFavorites(favoriteToRemove.id)
        advanceUntilIdle()

        // THEN
        verifyRemoveFromFavorites(favoriteToRemove.id)
        assertEquals(1, vmUnderTest.favoriteBreeds.value.size)
        assertFalse(vmUnderTest.favoriteBreeds.value.any { it.id == favoriteToRemove.id })
        assertTrue(vmUnderTest.favoriteBreeds.value.any { it.id == remainingFavorite.id })
    }

    @Test
    fun `WHEN favorites list updates externally SHOULD update the ViewModel list`() = runTest {
        // GIVEN
        val initialFavorites = listOf(getBreed(id = "id1", isFavorite = true))
        val favoriteFlow = whenGetFavoriteBreedsReturns(initialFavorites)

        vmUnderTest
        advanceUntilIdle()
        assertEquals(1, vmUnderTest.favoriteBreeds.value.size)

        // WHEN (simulate external change)
        favoriteFlow.value = emptyList()
        advanceUntilIdle()

        // THEN
        assertEquals(0, vmUnderTest.favoriteBreeds.value.size)
    }
}