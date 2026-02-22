package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.EmotionEntry
import de.idrinth.habitevaluator.shared.model.EmotionPair
import de.idrinth.habitevaluator.shared.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomEmotionEntryRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomEmotionEntryRepository
    private lateinit var pairRepository: RoomEmotionPairRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomEmotionEntryRepository(database.emotionDao())
        pairRepository = RoomEmotionPairRepository(database.emotionDao())
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

    private fun createPair(userId: String = "u1"): EmotionPair {
        val pair = EmotionPair()
        pair.id = UUID.randomUUID().toString()
        pair.negativeLabel = "Sad"
        pair.positiveLabel = "Happy"
        pair.user = createUser(userId)
        return pair
    }

    private fun createEntry(
        pair: EmotionPair,
        strength: Int = 5,
        notes: String? = null,
        userId: String = "u1"
    ): EmotionEntry {
        val entry = EmotionEntry()
        entry.id = UUID.randomUUID().toString()
        entry.emotionPair = pair
        entry.strength = strength
        entry.recordedAt = LocalDateTime.of(2024, 6, 15, 10, 0)
        entry.notes = notes
        entry.user = createUser(userId)
        return entry
    }

    @Test
    fun testSaveReturnsEntry() {
        val pair = createPair()
        pairRepository.save(pair)
        val entry = createEntry(pair)
        val result = repository.save(entry)
        assertEquals(entry.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val pair = createPair()
        pairRepository.save(pair)
        val entry = createEntry(pair, strength = 7, notes = "Feeling great")
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertEquals(7, found.get().strength)
        assertEquals("Feeling great", found.get().notes)
        assertNotNull(found.get().emotionPair)
        assertEquals(pair.id, found.get().emotionPair.id)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdReturnsEmptyWhenPairNotFound() {
        // Save an entry referencing a pair that doesn't exist in DB
        val pair = createPair()
        // Do NOT save the pair to the database
        val entry = createEntry(pair)
        repository.save(entry)

        val found = repository.findById(entry.id)
        // The entity exists but the pair cannot be resolved, so result is empty
        assertFalse(found.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val pair = createPair(userId = "u1")
        pairRepository.save(pair)
        val entry = createEntry(pair, userId = "u1")
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() {
        val pair = createPair()
        pairRepository.save(pair)
        val entry1 = createEntry(pair, strength = 3)
        val entry2 = createEntry(pair, strength = 7)
        repository.save(entry1)
        repository.save(entry2)

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
        val pair = createPair()
        pairRepository.save(pair)
        val entry = createEntry(pair)
        repository.save(entry)

        assertTrue(repository.findById(entry.id).isPresent)

        repository.deleteById(entry.id)

        assertFalse(repository.findById(entry.id).isPresent)
    }

    @Test
    fun testFindByUserId() {
        val pair1 = createPair("u1")
        val pair2 = createPair("u2")
        pairRepository.save(pair1)
        pairRepository.save(pair2)

        val entry1 = createEntry(pair1, userId = "u1")
        val entry2 = createEntry(pair1, userId = "u1")
        val entry3 = createEntry(pair2, userId = "u2")
        repository.save(entry1)
        repository.save(entry2)
        repository.save(entry3)

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
    fun testSaveWithNullNotes() {
        val pair = createPair()
        pairRepository.save(pair)
        val entry = createEntry(pair, notes = null)
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertNull(found.get().notes)
    }

    @Test
    fun testSaveWithNegativeStrength() {
        val pair = createPair()
        pairRepository.save(pair)
        val entry = createEntry(pair, strength = -5)
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertEquals(-5, found.get().strength)
    }
}
