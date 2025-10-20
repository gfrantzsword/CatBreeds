package com.example.catbreeds.data.unitTests

import com.example.catbreeds.data.local.BreedDao
import com.example.catbreeds.data.local.BreedEntity
import com.example.catbreeds.data.local.FavoriteDao
import com.example.catbreeds.data.remote.RemoteService
import com.example.catbreeds.data.repository.BreedRepositoryImpl
import com.example.catbreeds.core.util.ConnectivityChecker
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.test_core.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Mock data
object TestRepositoryData {
    fun getSampleBreeds(): List<Breed> = listOf(
        Breed("sibe", "Siberian", "desc", "Playful, Calm", "Siberia", "10 - 15", "sibe_ref", false),
        Breed("pers", "Persian", "desc", "Reserved, Quiet", "Iran", "12 - 14", "pers_ref", false)
    )

    fun getSampleBreedEntity(id: String = "sibe", name: String = "Siberian") = BreedEntity(
        id = id,
        name = name,
        origin = "Test Origin",
        description = "Test Desc",
        temperament = "Test Temperament",
        life_span = "10 - 12",
        reference_image_id = "test_ref"
    )
}

@ExperimentalCoroutinesApi
class RepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @RelaxedMockK
    private lateinit var remoteService: RemoteService

    @RelaxedMockK
    private lateinit var breedDao: BreedDao

    @RelaxedMockK
    private lateinit var favoriteDao: FavoriteDao

    @RelaxedMockK
    private lateinit var connectivityChecker: ConnectivityChecker

    private lateinit var repository: BreedRepositoryImpl

    @Before
    fun setup() {
        repository = BreedRepositoryImpl(remoteService, breedDao, favoriteDao, connectivityChecker)
    }

    @Test
    fun `WHEN getBreeds is called SHOULD return mapped breeds from local database`() = runTest {
        // GIVEN
        val breedEntities = listOf(TestRepositoryData.getSampleBreedEntity())
        every { breedDao.getAll() } returns flowOf(breedEntities)

        // WHEN
        val result = repository.getBreeds().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("sibe", result[0].id)
        assertEquals("Siberian", result[0].name)
    }

    @Test
    fun `WHEN getBreedById is called with existing id SHOULD return correct breed`() = runTest {
        // GIVEN
        val breedEntity = TestRepositoryData.getSampleBreedEntity(id = "pers", name = "Persian")
        coEvery { breedDao.getById("pers") } returns breedEntity

        // WHEN
        val result = repository.getBreedById("pers")

        // THEN
        assertNotNull(result)
        assertEquals("pers", result?.id)
        assertEquals("Persian", result?.name)
    }

    @Test
    fun `WHEN getBreedById is called with non-existent id SHOULD return null`() = runTest {
        // GIVEN
        coEvery { breedDao.getById("nonexistent") } returns null

        // WHEN
        val result = repository.getBreedById("nonexistent")

        // THEN
        assertNull(result)
    }

    @Test
    fun `WHEN refreshBreeds is called without internet SHOULD throw an exception`() = runTest {
        // GIVEN
        every { connectivityChecker.isConnected() } returns false

        // WHEN & THEN
        try {
            repository.refreshBreeds()
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals("No internet connection", e.message)
        }
    }

    @Test
    fun `WHEN refreshBreeds is called with internet SHOULD fetch and save breeds`() = runTest {
        // GIVEN
        val networkBreeds = TestRepositoryData.getSampleBreeds()
        every { connectivityChecker.isConnected() } returns true
        coEvery { remoteService.getBreeds() } returns networkBreeds
        coEvery { breedDao.insertAll(any()) } returns Unit

        // WHEN
        repository.refreshBreeds()

        // THEN
        coVerify { remoteService.getBreeds() }
        coVerify { breedDao.insertAll(any()) }
    }

    @Test
    fun `WHEN refreshBreeds is called and network fails SHOULD throw an exception`() = runTest {
        // GIVEN
        every { connectivityChecker.isConnected() } returns true
        coEvery { remoteService.getBreeds() } throws RuntimeException("Network error")

        try { // WHEN
            repository.refreshBreeds()
            fail("Expected exception to be thrown")
        } catch (e: Exception) { // THEN
            assertEquals("Something went wrong. Please try again later", e.message)
        }
    }
}