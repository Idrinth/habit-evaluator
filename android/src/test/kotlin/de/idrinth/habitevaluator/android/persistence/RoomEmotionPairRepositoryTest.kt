package de.idrinth.habitevaluator.android.persistence

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomEmotionPairRepositoryTest {

    private lateinit var dao: EmotionDao
    private lateinit var repository: RoomEmotionPairRepository

    @BeforeEach
    fun setUp() {
        dao = mock(EmotionDao::class.java)
        repository = RoomEmotionPairRepository(dao)
    }

    private fun createEntity(
        id: String = "p1",
        negativeLabel: String = "Sad",
        positiveLabel: String = "Happy",
        userId: String = "u1"
    ) = EmotionPairEntity(
        id = id, negativeLabel = negativeLabel, positiveLabel = positiveLabel,
        userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsPair() = runTest {
        val pair = de.idrinth.habitevaluator.shared.model.EmotionPair()
        pair.id = "p1"
        pair.negativeLabel = "Sad"
        pair.positiveLabel = "Happy"
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        pair.user = user

        val result = repository.save(pair)
        assertEquals("p1", result.id)
        verify(dao).insertPair(any(EmotionPairEntity::class.java) ?: createEntity())
    }

    @Test
    fun testFindByIdReturnsPair() = runTest {
        val entity = createEntity()
        `when`(dao.findPairById("p1")).thenReturn(entity)

        val found = repository.findById("p1")
        assertTrue(found.isPresent)
        assertEquals("Sad", found.get().negativeLabel)
        assertEquals("Happy", found.get().positiveLabel)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findPairById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createEntity()
        `when`(dao.findPairById("p1")).thenReturn(entity)

        val found = repository.findById("p1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntity(id = "p1", negativeLabel = "Sad", positiveLabel = "Happy")
        val e2 = createEntity(id = "p2", negativeLabel = "Anxious", positiveLabel = "Calm")
        `when`(dao.findAllPairs()).thenReturn(listOf(e1, e2))

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllPairs()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("p1")
        verify(dao).deletePairById("p1")
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createEntity(id = "p1", userId = "u1")
        val e2 = createEntity(id = "p2", userId = "u1")
        `when`(dao.findPairsByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findPairsByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSaveUpdatesExistingPair() {
        val pair = de.idrinth.habitevaluator.shared.model.EmotionPair()
        pair.id = "p1"
        pair.negativeLabel = "Sad"
        pair.positiveLabel = "Happy"
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        pair.user = user

        repository.save(pair)
        pair.negativeLabel = "Depressed"
        pair.positiveLabel = "Elated"
        val result = repository.save(pair)

        assertEquals("Depressed", result.negativeLabel)
        assertEquals("Elated", result.positiveLabel)
    }
}
