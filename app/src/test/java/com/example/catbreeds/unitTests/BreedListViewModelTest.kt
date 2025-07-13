package com.example.catbreeds.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.data.remote.RemoteService
import com.example.catbreeds.ui.breedList.BreedListViewModel
import com.example.catbreeds.unitTests.utils.TestUtils
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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
    private lateinit var remoteService: RemoteService

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        remoteService = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty breeds list`() {
        coEvery { remoteService.getBreeds() } returns emptyList()

        viewModel = BreedListViewModel()

        assertEquals(emptyList<Any>(), viewModel.breeds.value)
    }

    @Test
    fun `fetchBreeds updates breeds list on success`() {
        val sampleBreeds = TestUtils.getSampleBreeds()
        coEvery { remoteService.getBreeds() } returns sampleBreeds

        viewModel = BreedListViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(sampleBreeds, viewModel.breeds.value)
    }
}