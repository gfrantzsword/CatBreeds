package com.example.catbreeds.breed_list.unitTests

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.catbreeds.breed_list.NewBreedViewModel
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.domain.repository.BreedRepository
import com.example.catbreeds.test_core.MainDispatcherRule
import com.example.catbreeds.test_core.mock.getBreed
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class NewBreedViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var breedRepository: BreedRepository
    @RelaxedMockK
    private lateinit var context: Context

    private val vmUnderTest: NewBreedViewModel by lazy {
        spyk(
            NewBreedViewModel(breedRepository, context)
        )
    }

    // Helper Method
    private fun setupMockk() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
    }

    @Test
    fun `WHEN addNewBreed is called SHOULD call addBreed`() = runTest {
        // GIVEN
        setupMockk()
        coEvery { breedRepository.addBreed(any()) } just Runs

        // WHEN
        vmUnderTest.addNewBreed(getBreed())
        advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { breedRepository.addBreed(any()) }
    }

    @Test
    fun `WHEN saving breed SHOULD save with correct name`() = runTest {
        // GIVEN
        setupMockk()

        val testName = "testName"
        val testBreed = getBreed(name = testName, imageUrl = "")
        val breedSlot = slot<Breed>()
        coEvery { breedRepository.addBreed(capture(breedSlot)) } just Runs

        // WHEN
        vmUnderTest.addNewBreed(testBreed)
        advanceUntilIdle()

        // THEN
        assertEquals(testName, breedSlot.captured.name)
    }
}