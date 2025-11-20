package com.example.catbreeds.data.unitTests

import com.example.catbreeds.core.util.ConnectivityChecker
import com.example.catbreeds.data.local.BreedDao
import com.example.catbreeds.data.local.BreedEntity
import com.example.catbreeds.data.local.FavoriteDao
import com.example.catbreeds.data.remote.BreedDto
import com.example.catbreeds.data.remote.RemoteService
import com.example.catbreeds.data.repository.BreedRepositoryImpl
import com.example.catbreeds.domain.models.Breed
import com.example.catbreeds.test_core.MainDispatcherRule
import com.example.catbreeds.test_core.mock.MOCK_PERSIAN_ID
import com.example.catbreeds.test_core.mock.MOCK_PERSIAN_NAME
import com.example.catbreeds.test_core.mock.MOCK_SIBERIAN_ID
import com.example.catbreeds.test_core.mock.MOCK_SIBERIAN_NAME
import com.example.catbreeds.test_core.mock.mockBreedsList
import io.mockk.coEvery
import io.mockk.coJustRun
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

private fun getSampleBreedEntity(
    id: String = MOCK_SIBERIAN_ID,
    name: String = MOCK_SIBERIAN_NAME
) = BreedEntity(
    id = id,
    name = name,
    origin = "Test Origin",
    description = "Test Desc",
    temperament = "Test Temperament",
    lifeSpan = "10 - 12",
    referenceImageId = "test_ref"
)

private fun getSampleBreedEntities() = listOf(getSampleBreedEntity())

private val networkBreeds = mockBreedsList

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

    // Helper Methods
    private fun setupConnectivity(isConnected: Boolean) {
        every { connectivityChecker.isConnected() } returns isConnected
    }

    private fun setupLocalDatabase(breedFlow: List<BreedEntity>) {
        every { breedDao.getAll() } returns flowOf(breedFlow)
    }

    private fun setupLocalDatabase(breedEntity: BreedEntity?) {
        coEvery { breedDao.getById(any()) } returns null // Default
        if (breedEntity != null) {
            coEvery { breedDao.getById(breedEntity.id) } returns breedEntity
        }
    }

    private fun getMockBreedDto(setupBreeds: List<Breed>): List<BreedDto> {
        return setupBreeds.map { breed ->
            BreedDto(
                id = breed.id,
                name = breed.name,
                description = breed.description,
                temperament = breed.temperament.joinToString(", "),
                origin = breed.origin,
                lifeSpan = breed.lifeSpan,
                referenceImageId = breed.referenceImageId
            )
        }
    }

    private fun setupRemoteService(setupBreeds: List<Breed> = networkBreeds) {
        coEvery { remoteService.getBreeds() } returns getMockBreedDto(setupBreeds)
        coJustRun { breedDao.insertAll(any()) }
    }

    private fun setupRemoteServiceFailure(exception: Exception) {
        coEvery { remoteService.getBreeds() } throws exception
    }

    // Tests
    @Test
    fun `WHEN getBreeds is called SHOULD return mapped breeds from local database`() = runTest {
        // GIVEN
        val breedEntities = getSampleBreedEntities()
        setupLocalDatabase(breedEntities)

        // WHEN
        val result = repository.getBreeds().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals(MOCK_SIBERIAN_ID, result[0].id)
        assertEquals(MOCK_SIBERIAN_NAME, result[0].name)
    }

    @Test
    fun `WHEN getBreedById is called with existing id SHOULD return correct breed`() = runTest {
        // GIVEN
        val breedEntity = getSampleBreedEntity(id = MOCK_PERSIAN_ID, name = MOCK_PERSIAN_NAME)
        setupLocalDatabase(breedEntity)

        // WHEN
        val result = repository.getBreedById(MOCK_PERSIAN_ID)

        // THEN
        assertNotNull(result)
        assertEquals(MOCK_PERSIAN_ID, result?.id)
        assertEquals(MOCK_PERSIAN_NAME, result?.name)
    }

    @Test
    fun `WHEN getBreedById is called with non-existent id SHOULD return null`() = runTest {
        // GIVEN
        setupLocalDatabase(breedEntity = null)

        // WHEN
        val result = repository.getBreedById("nonexistent")

        // THEN
        assertNull(result)
    }

    @Test
    fun `WHEN refreshBreeds is called without internet SHOULD throw an exception`() = runTest {
        // GIVEN
        setupConnectivity(isConnected = false)

        try { // WHEN
            repository.refreshBreeds()
            fail("Expected exception to be thrown")
        } catch (e: Exception) { // THEN
            assertEquals("No internet connection", e.message)
        }
    }

    @Test
    fun `WHEN refreshBreeds is called with internet SHOULD fetch AND save breeds`() = runTest {
        // GIVEN
        setupConnectivity(isConnected = true)
        setupRemoteService()

        // WHEN
        repository.refreshBreeds()

        // THEN
        coVerify(exactly = 1) { remoteService.getBreeds() }
        coVerify(exactly = 1) { breedDao.insertAll(any()) }
    }

    @Test
    fun `WHEN refreshBreeds is called AND network fails SHOULD throw an exception`() = runTest {
        // GIVEN
        setupConnectivity(isConnected = true)
        setupRemoteServiceFailure(RuntimeException("Network error"))

        try { // WHEN
            repository.refreshBreeds()
            fail("Expected exception to be thrown")
        } catch (e: Exception) { // THEN
            assertEquals("Something went wrong. Please try again later", e.message)
        }
    }
}