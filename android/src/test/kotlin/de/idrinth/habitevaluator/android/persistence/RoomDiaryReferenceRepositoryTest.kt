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

class RoomDiaryReferenceRepositoryTest {

    private lateinit var dao: DiaryDao
    private lateinit var repository: RoomDiaryReferenceRepository

    @BeforeEach
    fun setUp() {
        dao = mock(DiaryDao::class.java)
        repository = RoomDiaryReferenceRepository(dao)
    }

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
    fun testSaveReturnsReference() = runTest {
        val ref = de.idrinth.habitevaluator.shared.model.DiaryReference()
        ref.id = "r1"
        ref.description = "Morning workout"
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        ref.user = user

        val result = repository.save(ref)
        assertEquals("r1", result.id)
        assertEquals("Morning workout", result.description)
        verify(dao).insertReference(any(DiaryReferenceEntity::class.java) ?: createReferenceEntity())
    }

    @Test
    fun testFindByIdReturnsReference() = runTest {
        val entity = createReferenceEntity()
        `when`(dao.findReferenceById("r1")).thenReturn(entity)

        val found = repository.findById("r1")
        assertTrue(found.isPresent)
        assertEquals("Morning workout", found.get().description)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findReferenceById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createReferenceEntity(userId = "u1")
        `when`(dao.findReferenceById("r1")).thenReturn(entity)

        val found = repository.findById("r1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindByUserId() = runTest {
        val ref1 = createReferenceEntity(id = "r1", description = "Workout", userId = "u1")
        val ref2 = createReferenceEntity(id = "r2", description = "Meditation", userId = "u1")
        `when`(dao.findReferencesByUserId("u1")).thenReturn(listOf(ref1, ref2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findReferencesByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("r1")
        verify(dao).deleteReferenceById("r1")
    }

    @Test
    fun testFindByUserIdAndDescriptionIgnoreCaseFound() = runTest {
        val entity = createReferenceEntity(description = "Morning Workout")
        `when`(dao.findReferenceByUserIdAndDescLower("u1", "morning workout")).thenReturn(entity)

        val found = repository.findByUserIdAndDescriptionIgnoreCase("u1", "Morning Workout")
        assertTrue(found.isPresent)
        assertEquals("Morning Workout", found.get().description)
    }

    @Test
    fun testFindByUserIdAndDescriptionIgnoreCaseCaseInsensitive() = runTest {
        val entity = createReferenceEntity(description = "Morning Workout")
        `when`(dao.findReferenceByUserIdAndDescLower("u1", "morning workout")).thenReturn(entity)

        val found = repository.findByUserIdAndDescriptionIgnoreCase("u1", "morning workout")
        assertTrue(found.isPresent)
        assertEquals("Morning Workout", found.get().description)
    }

    @Test
    fun testFindByUserIdAndDescriptionIgnoreCaseNotFound() = runTest {
        `when`(dao.findReferenceByUserIdAndDescLower("u1", "nonexistent")).thenReturn(null)

        val result = repository.findByUserIdAndDescriptionIgnoreCase("u1", "nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindDistinctDescriptionsByUserId() = runTest {
        `when`(dao.findDistinctDescriptions("u1")).thenReturn(listOf("Morning workout", "Evening walk"))

        val descriptions = repository.findDistinctDescriptionsByUserId("u1")
        assertEquals(2, descriptions.size)
        assertTrue(descriptions.containsAll(listOf("Morning workout", "Evening walk")))
    }

    @Test
    fun testFindDistinctDescriptionsByUserIdEmpty() = runTest {
        `when`(dao.findDistinctDescriptions("u99")).thenReturn(emptyList())

        val result = repository.findDistinctDescriptionsByUserId("u99")
        assertTrue(result.isEmpty())
    }
}
