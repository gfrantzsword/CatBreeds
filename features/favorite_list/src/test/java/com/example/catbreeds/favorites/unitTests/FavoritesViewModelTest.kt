package com.example.catbreeds.favorites.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.favorite_list.FavoriteListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
object TestFavoriteListData {
    fun getFavoriteBreed(id: String, name: String, origin: String, life_span: String) = Breed(
        id = id, name = name, origin = origin, temperament = "Any", life_span = life_span,
        description = "Test Desc", reference_image_id = "test", isFavorite = true
    )
}

@ExperimentalCoroutinesApi
class FavoriteListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FavoriteListViewModel
    private lateinit var breedRepository: BreedRepository
    private val favoriteBreedsFlow = MutableStateFlow<List<Breed>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        breedRepository = mockk(relaxed = true)
        every { breedRepository.getFavoriteBreeds() } returns favoriteBreedsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // CASE: displays correct info (ideal)
    @Test
    fun initLoadsFavoritesCorrectly() = runTest {
        val initialFavorites = listOf(
            TestFavoriteListData.getFavoriteBreed("id1", "Siberian", "Russia", "10 - 15"),
            TestFavoriteListData.getFavoriteBreed("id2", "Persian", "Iran", "12 - 14")
        )
        favoriteBreedsFlow.value = initialFavorites

        viewModel = FavoriteListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.favoriteBreeds.value.size)
        val firstFavorite = viewModel.favoriteBreeds.value.first()
        assertEquals("Siberian", firstFavorite.name)
        assertEquals("Russia", firstFavorite.origin)
        assertEquals("10 - 15", firstFavorite.life_span)
    }

    // CASE: fav toggle immediately eliminates from list
    @Test
    fun removeFromFavoritesUpdatesListAndCallsRepo() = runTest {
        val favoriteToRemove = TestFavoriteListData.getFavoriteBreed("id_remove", "A", "O", "1-1")
        val remainingFavorite = TestFavoriteListData.getFavoriteBreed("id_keep", "B", "O", "1-1")
        val initialFavorites = listOf(favoriteToRemove, remainingFavorite)
        favoriteBreedsFlow.value = initialFavorites

        coEvery { breedRepository.removeBreedFromFavorites(favoriteToRemove.id) } answers {
            favoriteBreedsFlow.value = listOf(remainingFavorite)
        }

        viewModel = FavoriteListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.favoriteBreeds.value.size)

        viewModel.removeFromFavorites(favoriteToRemove.id)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { breedRepository.removeBreedFromFavorites(favoriteToRemove.id) }
        assertEquals(1, viewModel.favoriteBreeds.value.size)
        assertFalse(viewModel.favoriteBreeds.value.any { it.id == favoriteToRemove.id })
        assertTrue(viewModel.favoriteBreeds.value.any { it.id == remainingFavorite.id })
    }

    // CASE: fav toggle updated between pages/back button
    @Test
    fun favoritesUpdateFromExternalFlow() = runTest {
        val initialFavorites = listOf(TestFavoriteListData.getFavoriteBreed("id1", "A", "O", "1-1"))
        favoriteBreedsFlow.value = initialFavorites

        viewModel = FavoriteListViewModel(breedRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.favoriteBreeds.value.size)

        // Simulate external change
        favoriteBreedsFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.favoriteBreeds.value.size)
    }
}