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

class RoomDiaryEntryRepositoryTest {

    private lateinit var dao: DiaryDao
    private lateinit var repository: RoomDiaryEntryRepository

    @BeforeEach
    fun setUp() {
        dao = mock(DiaryDao::class.java)
        repository = RoomDiaryEntryRepository(dao)
    }

    private fun createEntryEntity(
        id: String = "e1",
        significance: String = "MAJOR",
        eventDate: String = "2024-06-15",
        startTime: String? = null,
        endTime: String? = null,
        diaryReferenceId: String? = "r1",
        userId: String = "u1"
    ) = DiaryEntryEntity(
        id = id, legacyDescription = null, diaryReferenceId = diaryReferenceId,
        significance = significance, eventDate = eventDate,
        startTime = startTime, endTime = endTime,
        createdAt = "2024-06-15T10:00:00", userId = userId, userName = "testuser"
    )

    private fun createReferenceEntity(
        id: String = "r1",
        description: String = "Morning workout",
        userId: String = "u1"
    ) = DiaryReferenceEntity(
        id = id, description = description,
        descriptionLower = description.lowercase(),
        userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsEntry() = runTest {
        val entry = de.idrinth.habitevaluator.shared.model.DiaryEntry()
        entry.id = "e1"
        entry.significance = de.idrinth.habitevaluator.shared.model.EventSignificance.MAJOR
        entry.eventDate = java.time.LocalDate.of(2024, 6, 15)
        entry.createdAt = java.time.LocalDateTime.now()
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        entry.user = user

        val result = repository.save(entry)
        assertEquals("e1", result.id)
        verify(dao).insertEntry(any(DiaryEntryEntity::class.java) ?: createEntryEntity())
    }

    @Test
    fun testFindByIdReturnsEntryWithReference() = runTest {
        val entryEntity = createEntryEntity(diaryReferenceId = "r1")
        val refEntity = createReferenceEntity()
        `when`(dao.findEntryById("e1")).thenReturn(entryEntity)
        `when`(dao.findReferenceById("r1")).thenReturn(refEntity)

        val found = repository.findById("e1")
        assertTrue(found.isPresent)
        assertEquals("MAJOR", found.get().significance.name)
        assertNotNull(found.get().diaryReference)
        assertEquals("Morning workout", found.get().diaryReference.description)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findEntryById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entryEntity = createEntryEntity(diaryReferenceId = null)
        `when`(dao.findEntryById("e1")).thenReturn(entryEntity)

        val found = repository.findById("e1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntryEntity(id = "e1", diaryReferenceId = null)
        val e2 = createEntryEntity(id = "e2", diaryReferenceId = null)
        `when`(dao.findAllEntries()).thenReturn(listOf(e1, e2))

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
        repository.deleteById("e1")
        verify(dao).deleteEntryById("e1")
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createEntryEntity(id = "e1", userId = "u1", diaryReferenceId = null)
        val e2 = createEntryEntity(id = "e2", userId = "u1", diaryReferenceId = null)
        `when`(dao.findEntriesByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findEntriesByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByIdWithStartAndEndTime() = runTest {
        val entryEntity = createEntryEntity(startTime = "10:00", endTime = "11:00", diaryReferenceId = null)
        `when`(dao.findEntryById("e1")).thenReturn(entryEntity)

        val found = repository.findById("e1")
        assertTrue(found.isPresent)
        assertEquals(java.time.LocalTime.of(10, 0), found.get().startTime)
        assertEquals(java.time.LocalTime.of(11, 0), found.get().endTime)
    }

    @Test
    fun testFindDistinctDescriptionsByUserId() = runTest {
        `when`(dao.findDistinctDescriptions("u1")).thenReturn(listOf("Workout", "Meditation"))

        val descriptions = repository.findDistinctDescriptionsByUserId("u1")
        assertEquals(2, descriptions.size)
        assertTrue(descriptions.containsAll(listOf("Workout", "Meditation")))
    }

    @Test
    fun testFindEntriesNeedingMigration() = runTest {
        `when`(dao.findEntriesNeedingMigration("u1")).thenReturn(emptyList())

        val result = repository.findEntriesNeedingMigration("u1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindEntriesNeedingMigrationWithResults() = runTest {
        val legacyEntry = DiaryEntryEntity(
            id = "e1", legacyDescription = "Old desc", diaryReferenceId = null,
            significance = "NORMAL", eventDate = "2024-06-15",
            startTime = null, endTime = null,
            createdAt = "2024-06-15T10:00:00", userId = "u1", userName = "testuser"
        )
        `when`(dao.findEntriesNeedingMigration("u1")).thenReturn(listOf(legacyEntry))

        val result = repository.findEntriesNeedingMigration("u1")
        assertEquals(1, result.size)
    }
}
