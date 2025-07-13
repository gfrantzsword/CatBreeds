package com.example.catbreeds.unitTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.catbreeds.data.local.AppDatabase
import com.example.catbreeds.data.local.BreedDao
import com.example.catbreeds.unitTests.utils.TestUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class LocalSourceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var dao: BreedDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.breedDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertAll and getAll works correctly`() = runTest {
        val breedEntities = listOf(TestUtils.getSampleBreedEntity())

        dao.insertAll(breedEntities)
        val result = dao.getAll().first()

        assertEquals(1, result.size)
        assertEquals("ycho", result[0].id)
        assertEquals("York Chocolate", result[0].name)
    }

    @Test
    fun `getById returns correct breed`() = runTest {
        val breedEntity = TestUtils.getSampleBreedEntity()
        dao.insertAll(listOf(breedEntity))

        val result = dao.getById("ycho")

        assertNotNull(result)
        assertEquals("ycho", result?.id)
        assertEquals("York Chocolate", result?.name)
    }

    @Test
    fun `getById returns null for non-existent id`() = runTest {
        val result = dao.getById("nonexistent")

        assertNull(result)
    }

    @Test
    fun `delete removes breed from database`() = runTest {
        val breedEntity = TestUtils.getSampleBreedEntity()
        dao.insertAll(listOf(breedEntity))

        dao.delete(breedEntity)
        val result = dao.getAll().first()

        assertTrue(result.isEmpty())
    }
}