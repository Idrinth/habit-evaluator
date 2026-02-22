package de.idrinth.habitevaluator.android.persistence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomEmotionEntryRepositoryTest {

    private lateinit var dao: EmotionDao
    private lateinit var repository: RoomEmotionEntryRepository

    @BeforeEach
    fun setUp() {
        dao = mock(EmotionDao::class.java)
        repository = RoomEmotionEntryRepository(dao)
    }

    private fun createPairEntity(
        id: String = "p1",
        negativeLabel: String = "Sad",
        positiveLabel: String = "Happy",
        userId: String = "u1"
    ) = EmotionPairEntity(
        id = id, negativeLabel = negativeLabel, positiveLabel = positiveLabel,
        userId = userId, userName = "testuser"
    )

    private fun createEntryEntity(
        id: String = "ee1",
        emotionPairId: String = "p1",
        strength: Int = 5,
        notes: String? = null,
        userId: String = "u1"
    ) = EmotionEntryEntity(
        id = id, emotionPairId = emotionPairId, strength = strength,
        recordedAt = "2024-06-15T10:00:00", notes = notes,
        userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsEntry() = runTest {
        val pair = de.idrinth.habitevaluator.shared.model.EmotionPair()
        pair.id = "p1"
        pair.negativeLabel = "Sad"
        pair.positiveLabel = "Happy"
        val entry = de.idrinth.habitevaluator.shared.model.EmotionEntry()
        entry.id = "ee1"
        entry.emotionPair = pair
        entry.strength = 5
        entry.recordedAt = java.time.LocalDateTime.of(2024, 6, 15, 10, 0)
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        entry.user = user

        val result = repository.save(entry)
        assertEquals("ee1", result.id)
        verify(dao).insertEntry(any(EmotionEntryEntity::class.java) ?: createEntryEntity())
    }

    @Test
    fun testFindByIdReturnsEntryWithPair() = runTest {
        val entryEntity = createEntryEntity(strength = 7, notes = "Feeling great")
        val pairEntity = createPairEntity()
        `when`(dao.findEntryById("ee1")).thenReturn(entryEntity)
        `when`(dao.findPairById("p1")).thenReturn(pairEntity)

        val found = repository.findById("ee1")
        assertTrue(found.isPresent)
        assertEquals(7, found.get().strength)
        assertEquals("Feeling great", found.get().notes)
        assertNotNull(found.get().emotionPair)
        assertEquals("p1", found.get().emotionPair.id)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findEntryById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdReturnsEmptyWhenPairNotFound() = runTest {
        val entryEntity = createEntryEntity()
        `when`(dao.findEntryById("ee1")).thenReturn(entryEntity)
        `when`(dao.findPairById("p1")).thenReturn(null)

        val found = repository.findById("ee1")
        assertFalse(found.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entryEntity = createEntryEntity()
        val pairEntity = createPairEntity()
        `when`(dao.findEntryById("ee1")).thenReturn(entryEntity)
        `when`(dao.findPairById("p1")).thenReturn(pairEntity)

        val found = repository.findById("ee1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntryEntity(id = "ee1", strength = 3)
        val e2 = createEntryEntity(id = "ee2", strength = 7)
        val pairEntity = createPairEntity()
        `when`(dao.findAllEntries()).thenReturn(listOf(e1, e2))
        `when`(dao.findPairById("p1")).thenReturn(pairEntity)

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllEntries()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("ee1")
        verify(dao).deleteEntryById("ee1")
    }

    @Test
    fun testFindByUserId() = runTest {
        val pairEntity = createPairEntity(userId = "u1")
        val e1 = createEntryEntity(id = "ee1", userId = "u1")
        val e2 = createEntryEntity(id = "ee2", userId = "u1")
        `when`(dao.findPairsByUserId("u1")).thenReturn(listOf(pairEntity))
        `when`(dao.findEntriesByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findPairsByUserId("u99")).thenReturn(emptyList())
        `when`(dao.findEntriesByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByIdWithNullNotes() = runTest {
        val entryEntity = createEntryEntity(notes = null)
        val pairEntity = createPairEntity()
        `when`(dao.findEntryById("ee1")).thenReturn(entryEntity)
        `when`(dao.findPairById("p1")).thenReturn(pairEntity)

        val found = repository.findById("ee1")
        assertTrue(found.isPresent)
        assertNull(found.get().notes)
    }

    @Test
    fun testFindByIdWithNegativeStrength() = runTest {
        val entryEntity = createEntryEntity(strength = -5)
        val pairEntity = createPairEntity()
        `when`(dao.findEntryById("ee1")).thenReturn(entryEntity)
        `when`(dao.findPairById("p1")).thenReturn(pairEntity)

        val found = repository.findById("ee1")
        assertTrue(found.isPresent)
        assertEquals(-5, found.get().strength)
    }
}
