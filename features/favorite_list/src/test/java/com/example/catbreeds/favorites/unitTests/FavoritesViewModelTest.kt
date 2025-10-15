package com.example.catbreeds.favorites.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.favorite_list.FavoriteListViewModel
import com.example.catbreeds.test_core.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var breedRepository: BreedRepository
    private val favoriteBreedsFlow = MutableStateFlow<List<Breed>>(emptyList())

    private val vmUnderTest: FavoriteListViewModel by lazy {
        spyk(
            FavoriteListViewModel(breedRepository)
        )
    }

    @Before
    fun setup() {
        breedRepository = mockk(relaxed = true)
        every { breedRepository.getFavoriteBreeds() } returns favoriteBreedsFlow
    }

    @Test
    fun `WHEN correctly initialized SHOULD load and display favorites correctly`() = runTest {
        // GIVEN
        val initialFavorites = listOf(
            TestFavoriteListData.getFavoriteBreed("id1", "Siberian", "Russia", "10 - 15"),
            TestFavoriteListData.getFavoriteBreed("id2", "Persian", "Iran", "12 - 14")
        )
        favoriteBreedsFlow.value = initialFavorites

        // WHEN
        val vm = vmUnderTest
        advanceUntilIdle()

        // THEN
        assertEquals(2, vm.favoriteBreeds.value.size)
        val firstFavorite = vm.favoriteBreeds.value.first()
        assertEquals("Siberian", firstFavorite.name)
        assertEquals("Russia", firstFavorite.origin)
        assertEquals("10 - 15", firstFavorite.life_span)
    }

    @Test
    fun `WHEN breed is removed from favorites SHOULD update list and call repository`() = runTest {
        // GIVEN
        val favoriteToRemove = TestFavoriteListData.getFavoriteBreed("id_remove", "A", "O", "1-1")
        val remainingFavorite = TestFavoriteListData.getFavoriteBreed("id_keep", "B", "O", "1-1")
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
        val initialFavorites = listOf(TestFavoriteListData.getFavoriteBreed("id1", "A", "O", "1-1"))
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