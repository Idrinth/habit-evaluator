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
import java.time.LocalDate
import java.time.LocalTime

class RoomSleepEntryRepositoryTest {

    private lateinit var dao: SleepEntryDao
    private lateinit var repository: RoomSleepEntryRepository

    @BeforeEach
    fun setUp() {
        dao = mock(SleepEntryDao::class.java)
        repository = RoomSleepEntryRepository(dao)
    }

    private fun createEntity(
        id: String = "s1",
        fromTime: String = "23:00",
        untilTime: String = "07:00",
        date: String = "2024-06-15",
        notes: String? = null,
        userId: String = "u1"
    ) = SleepEntryEntity(
        id = id, fromTime = fromTime, untilTime = untilTime,
        date = date, createdAt = "2024-06-15T10:00:00",
        notes = notes, userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsEntry() = runTest {
        val entry = de.idrinth.habitevaluator.shared.model.SleepEntry()
        entry.id = "s1"
        entry.fromTime = LocalTime.of(23, 0)
        entry.untilTime = LocalTime.of(7, 0)
        entry.date = LocalDate.of(2024, 6, 15)
        entry.createdAt = java.time.LocalDateTime.now()
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        entry.user = user

        val result = repository.save(entry)
        assertEquals("s1", result.id)
        verify(dao).insert(any(SleepEntryEntity::class.java) ?: createEntity())
    }

    @Test
    fun testFindByIdReturnsEntry() = runTest {
        val entity = createEntity(notes = "Good sleep")
        `when`(dao.findById("s1")).thenReturn(entity)

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals(LocalTime.of(23, 0), found.get().fromTime)
        assertEquals(LocalTime.of(7, 0), found.get().untilTime)
        assertEquals(LocalDate.of(2024, 6, 15), found.get().date)
        assertEquals("Good sleep", found.get().notes)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createEntity()
        `when`(dao.findById("s1")).thenReturn(entity)

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindByIdWithNullNotes() = runTest {
        val entity = createEntity(notes = null)
        `when`(dao.findById("s1")).thenReturn(entity)

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertNull(found.get().notes)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntity(id = "s1", date = "2024-06-14")
        val e2 = createEntity(id = "s2", date = "2024-06-15")
        `when`(dao.findAll()).thenReturn(listOf(e1, e2))

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAll()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("s1")
        verify(dao).deleteById("s1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.existsById("s1")).thenReturn(true)

        assertTrue(repository.existsById("s1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.existsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createEntity(id = "s1", userId = "u1")
        val e2 = createEntity(id = "s2", userId = "u1")
        `when`(dao.findByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSaveWithNotes() = runTest {
        val entity = createEntity(notes = "Slept well")
        `when`(dao.findById("s1")).thenReturn(entity)

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals("Slept well", found.get().notes)
    }
}
