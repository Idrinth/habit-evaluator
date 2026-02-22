package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.DiaryReference
import de.idrinth.habitevaluator.shared.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomDiaryReferenceRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomDiaryReferenceRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomDiaryReferenceRepository(database.diaryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createUser(id: String = "u1", username: String = "testuser"): User {
        val user = User()
        user.id = id
        user.username = username
        return user
    }

    private fun createReference(
        description: String = "Morning workout",
        userId: String = "u1"
    ): DiaryReference {
        val ref = DiaryReference()
        ref.id = UUID.randomUUID().toString()
        ref.description = description
        ref.user = createUser(userId)
        return ref
    }

    @Test
    fun testSaveReturnsReference() {
        val ref = createReference()
        val result = repository.save(ref)
        assertEquals(ref.id, result.id)
        assertEquals(ref.description, result.description)
    }

    @Test
    fun testSaveAndFindById() {
        val ref = createReference("Morning workout")
        repository.save(ref)

        val found = repository.findById(ref.id)
        assertTrue(found.isPresent)
        assertEquals("Morning workout", found.get().description)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val ref = createReference(userId = "u1")
        repository.save(ref)

        val found = repository.findById(ref.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindByUserId() {
        val ref1 = createReference("Workout", "u1")
        val ref2 = createReference("Meditation", "u1")
        val ref3 = createReference("Running", "u2")
        repository.save(ref1)
        repository.save(ref2)
        repository.save(ref3)

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() {
        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() {
        val ref = createReference()
        repository.save(ref)

        assertTrue(repository.findById(ref.id).isPresent)

        repository.deleteById(ref.id)

        assertFalse(repository.findById(ref.id).isPresent)
    }

    @Test
    fun testFindByUserIdAndDescriptionIgnoreCaseFound() {
        val ref = createReference("Morning Workout", "u1")
        repository.save(ref)

        val found = repository.findByUserIdAndDescriptionIgnoreCase("u1", "Morning Workout")
        assertTrue(found.isPresent)
        assertEquals("Morning Workout", found.get().description)
    }

    @Test
    fun testFindByUserIdAndDescriptionIgnoreCaseCaseInsensitive() {
        val ref = createReference("Morning Workout", "u1")
        repository.save(ref)

        val found = repository.findByUserIdAndDescriptionIgnoreCase("u1", "morning workout")
        assertTrue(found.isPresent)
        assertEquals("Morning Workout", found.get().description)
    }

    @Test
    fun testFindByUserIdAndDescriptionIgnoreCaseNotFound() {
        val result = repository.findByUserIdAndDescriptionIgnoreCase("u1", "nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindDistinctDescriptionsByUserId() {
        val ref1 = createReference("Morning workout", "u1")
        val ref2 = createReference("Evening walk", "u1")
        repository.save(ref1)
        repository.save(ref2)

        val descriptions = repository.findDistinctDescriptionsByUserId("u1")
        assertEquals(2, descriptions.size)
        assertTrue(descriptions.containsAll(listOf("Morning workout", "Evening walk")))
    }

    @Test
    fun testFindDistinctDescriptionsByUserIdEmpty() {
        val result = repository.findDistinctDescriptionsByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDescriptionLowerIsPersisted() {
        val ref = createReference("Morning Workout")
        repository.save(ref)

        // Verify case-insensitive lookup works (implies descriptionLower was stored)
        val found = repository.findByUserIdAndDescriptionIgnoreCase("u1", "MORNING WORKOUT")
        assertTrue(found.isPresent)
    }
}
