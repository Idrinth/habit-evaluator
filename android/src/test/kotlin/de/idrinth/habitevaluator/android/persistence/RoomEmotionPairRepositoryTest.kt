package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.EmotionPair
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
class RoomEmotionPairRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomEmotionPairRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomEmotionPairRepository(database.emotionDao())
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

    private fun createEmotionPair(
        negativeLabel: String = "Sad",
        positiveLabel: String = "Happy",
        userId: String = "u1"
    ): EmotionPair {
        val pair = EmotionPair()
        pair.id = UUID.randomUUID().toString()
        pair.negativeLabel = negativeLabel
        pair.positiveLabel = positiveLabel
        pair.user = createUser(userId)
        return pair
    }

    @Test
    fun testSaveReturnsEmotionPair() {
        val pair = createEmotionPair()
        val result = repository.save(pair)
        assertEquals(pair.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val pair = createEmotionPair("Sad", "Happy")
        repository.save(pair)

        val found = repository.findById(pair.id)
        assertTrue(found.isPresent)
        assertEquals("Sad", found.get().negativeLabel)
        assertEquals("Happy", found.get().positiveLabel)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val pair = createEmotionPair(userId = "u1")
        repository.save(pair)

        val found = repository.findById(pair.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() {
        val pair1 = createEmotionPair("Sad", "Happy")
        val pair2 = createEmotionPair("Anxious", "Calm")
        repository.save(pair1)
        repository.save(pair2)

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() {
        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() {
        val pair = createEmotionPair()
        repository.save(pair)

        assertTrue(repository.findById(pair.id).isPresent)

        repository.deleteById(pair.id)

        assertFalse(repository.findById(pair.id).isPresent)
    }

    @Test
    fun testFindByUserId() {
        val pair1 = createEmotionPair("Sad", "Happy", "u1")
        val pair2 = createEmotionPair("Anxious", "Calm", "u1")
        val pair3 = createEmotionPair("Angry", "Peaceful", "u2")
        repository.save(pair1)
        repository.save(pair2)
        repository.save(pair3)

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
    fun testSaveUpdatesExistingPair() {
        val pair = createEmotionPair("Sad", "Happy")
        repository.save(pair)

        pair.negativeLabel = "Depressed"
        pair.positiveLabel = "Elated"
        repository.save(pair)

        val found = repository.findById(pair.id)
        assertTrue(found.isPresent)
        assertEquals("Depressed", found.get().negativeLabel)
        assertEquals("Elated", found.get().positiveLabel)
    }
}
