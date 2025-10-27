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
import org.junit.Before
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

    private val favoriteBreedsFlow = MutableStateFlow<List<Breed>>(emptyList())

    private val vmUnderTest: FavoriteListViewModel by lazy {
        spyk(
            FavoriteListViewModel(breedRepository)
        )
    }

    @Before
    fun setup() {
        every { breedRepository.getFavoriteBreeds() } returns favoriteBreedsFlow
    }

    @Test
    fun `WHEN correctly initialized SHOULD load and display favorites correctly`() = runTest {
        // GIVEN
        val initialFavorites = listOf(
            getBreed(id = "id1", name = "Siberian", isFavorite = true),
            getBreed(id = "id2", name = "Persian", isFavorite = true)
        )
        favoriteBreedsFlow.value = initialFavorites

        // WHEN
        val vm = vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(2, vm.favoriteBreeds.value.size)
        assertEquals("Siberian", vm.favoriteBreeds.value[0].name)
        assertEquals("Persian", vm.favoriteBreeds.value[1].name)
    }

    @Test
    fun `WHEN breed is removed from favorites SHOULD update list and call repository`() = runTest {
        // GIVEN
        val favoriteToRemove = getBreed(id = "id_remove", isFavorite = true)
        val remainingFavorite = getBreed(id = "id_keep", isFavorite = true)
        val initialFavorites = listOf(favoriteToRemove, remainingFavorite)
        favoriteBreedsFlow.value = initialFavorites
        coEvery { breedRepository.removeBreedFromFavorites(favoriteToRemove.id) } answers {
            favoriteBreedsFlow.value = listOf(remainingFavorite)
        }
        val vm = vmUnderTest
        advanceUntilIdle()
        assertEquals(2, vm.favoriteBreeds.value.size)

        // WHEN
        vm.removeFromFavorites(favoriteToRemove.id)
        advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(favoriteToRemove.id) }
        assertEquals(1, vm.favoriteBreeds.value.size)
        assertFalse(vm.favoriteBreeds.value.any { it.id == favoriteToRemove.id })
        assertTrue(vm.favoriteBreeds.value.any { it.id == remainingFavorite.id })
    }

    @Test
    fun `WHEN favorites list updates externally SHOULD update the ViewModel list`() = runTest {
        // GIVEN
        val initialFavorites = listOf(getBreed(id = "id1", isFavorite = true))
        favoriteBreedsFlow.value = initialFavorites
        val vm = vmUnderTest
        advanceUntilIdle()
        assertEquals(1, vm.favoriteBreeds.value.size)

        // WHEN (simulate external change)
        favoriteBreedsFlow.value = emptyList()
        advanceUntilIdle()

        // THEN
        assertEquals(0, vm.favoriteBreeds.value.size)
    }
}