package com.example.catbreeds.unitTests

import com.example.catbreeds.data.local.BreedDao
import com.example.catbreeds.data.remote.RemoteService
import com.example.catbreeds.data.repository.BreedRepositoryImpl
import com.example.catbreeds.domain.utils.ConnectivityChecker
import com.example.catbreeds.unitTests.utils.TestUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class RepositoryTest {

    private lateinit var repository: BreedRepositoryImpl
    private lateinit var remoteService: RemoteService
    private lateinit var breedDao: BreedDao
    private lateinit var connectivityChecker: ConnectivityChecker

    @Before
    fun setup() {
        remoteService = mockk()
        breedDao = mockk()
        connectivityChecker = mockk()
        repository = BreedRepositoryImpl(remoteService, breedDao, connectivityChecker)
    }

    @Test
    fun `getBreeds returns mapped breeds from local database`() = runTest {
        val breedEntities = listOf(TestUtils.getSampleBreedEntity())
        every { breedDao.getAll() } returns flowOf(breedEntities)

        val result = repository.getBreeds().first()

        assertEquals(1, result.size)
        assertEquals("ycho", result[0].id)
        assertEquals("York Chocolate", result[0].name)
    }

    @Test
    fun `getBreedById returns breed when found`() = runTest {
        val breedEntity = TestUtils.getSampleBreedEntity()
        coEvery { breedDao.getById("ycho") } returns breedEntity

        val result = repository.getBreedById("ycho")

        assertNotNull(result)
        assertEquals("ycho", result?.id)
        assertEquals("York Chocolate", result?.name)
    }

    @Test
    fun `getBreedById returns null when not found`() = runTest {
        coEvery { breedDao.getById("nonexistent") } returns null

        val result = repository.getBreedById("nonexistent")

        assertNull(result)
    }

    @Test
    fun `refreshBreeds throws exception when no internet connection`() = runTest {
        every { connectivityChecker.isConnected() } returns false

        try {
            repository.refreshBreeds()
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals("No internet connection", e.message)
        }
    }

    @Test
    fun `refreshBreeds fetches and saves breeds when connected`() = runTest {
        val networkBreeds = TestUtils.getSampleBreeds()
        every { connectivityChecker.isConnected() } returns true
        coEvery { remoteService.getBreeds() } returns networkBreeds
        coEvery { breedDao.insertAll(any()) } returns Unit

        repository.refreshBreeds()

        coVerify { remoteService.getBreeds() }
        coVerify { breedDao.insertAll(any()) }
    }

    @Test
    fun `refreshBreeds throws exception when network call fails`() = runTest {
        every { connectivityChecker.isConnected() } returns true
        coEvery { remoteService.getBreeds() } throws RuntimeException("Network error")

        try {
            repository.refreshBreeds()
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals("Error refreshing breeds", e.message)
        }
    }
}