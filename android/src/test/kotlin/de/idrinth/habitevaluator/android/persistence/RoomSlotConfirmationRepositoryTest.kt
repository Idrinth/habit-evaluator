package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.SlotConfirmation
import de.idrinth.habitevaluator.shared.model.User
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

class RoomSlotConfirmationRepositoryTest {

    private lateinit var dao: DayPlannerDao
    private lateinit var repository: RoomSlotConfirmationRepository

    @BeforeEach
    fun setUp() {
        dao = mock(DayPlannerDao::class.java)
        repository = RoomSlotConfirmationRepository(dao)
    }

    private fun createConfirmationEntity(
        id: String = "c1",
        slotId: String? = "s1",
        activityId: String? = null,
        groupId: String? = null,
        confirmed: Boolean = false,
        date: String = "2024-06-15",
        userId: String = "u1"
    ) = SlotConfirmationEntity(
        id = id, slotId = slotId, activityId = activityId,
        groupId = groupId, confirmed = confirmed,
        date = date, createdAt = "2024-06-15T10:00:00",
        userId = userId, userName = "testuser"
    )

    private fun createConfirmation(userId: String = "u1"): SlotConfirmation {
        val confirmation = SlotConfirmation()
        confirmation.id = "c1"
        val user = User()
        user.id = userId
        user.username = "testuser"
        confirmation.user = user
        return confirmation
    }

    @Test
    fun testSaveReturnsConfirmation() = runTest {
        val confirmation = createConfirmation()
        val result = repository.save(confirmation)
        assertEquals("c1", result.id)
        verify(dao).insertConfirmation(
            any(SlotConfirmationEntity::class.java) ?: createConfirmationEntity()
        )
    }

    @Test
    fun testFindByIdReturnsConfirmation() = runTest {
        val entity = createConfirmationEntity(confirmed = true)
        `when`(dao.findConfirmationById("c1")).thenReturn(entity)

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertTrue(found.get().isConfirmed)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findConfirmationById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createConfirmationEntity()
        `when`(dao.findConfirmationById("c1")).thenReturn(entity)

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindByIdNotConfirmed() = runTest {
        val entity = createConfirmationEntity(confirmed = false)
        `when`(dao.findConfirmationById("c1")).thenReturn(entity)

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertFalse(found.get().isConfirmed)
    }

    @Test
    fun testFindByIdWithDate() = runTest {
        val entity = createConfirmationEntity(date = "2024-12-25")
        `when`(dao.findConfirmationById("c1")).thenReturn(entity)

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().date)
        assertEquals(25, found.get().date.dayOfMonth)
        assertEquals(12, found.get().date.monthValue)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createConfirmationEntity(id = "c1")
        val e2 = createConfirmationEntity(id = "c2")
        `when`(dao.findAllConfirmations()).thenReturn(listOf(e1, e2))

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllConfirmations()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("c1")
        verify(dao).deleteConfirmationById("c1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.confirmationExistsById("c1")).thenReturn(true)

        assertTrue(repository.existsById("c1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.confirmationExistsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createConfirmationEntity(id = "c1", userId = "u1")
        val e2 = createConfirmationEntity(id = "c2", userId = "u1")
        `when`(dao.findConfirmationsByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findConfirmationsByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSaveSuspend() = runTest {
        val confirmation = createConfirmation()
        val result = repository.saveSuspend(confirmation)
        assertEquals("c1", result.id)
        verify(dao).insertConfirmation(
            any(SlotConfirmationEntity::class.java) ?: createConfirmationEntity()
        )
    }

    @Test
    fun testFindByIdWithNullSlotId() = runTest {
        val entity = createConfirmationEntity(slotId = null)
        `when`(dao.findConfirmationById("c1")).thenReturn(entity)

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertNull(found.get().slot)
    }

    @Test
    fun testFindByIdWithActivityId() = runTest {
        val entity = createConfirmationEntity(activityId = "act1")
        `when`(dao.findConfirmationById("c1")).thenReturn(entity)

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        // The simple toModel() doesn't resolve activity; it remains null
        assertNull(found.get().activity)
    }

    @Test
    fun testFindByIdWithGroupId() = runTest {
        val entity = createConfirmationEntity(groupId = "grp1")
        `when`(dao.findConfirmationById("c1")).thenReturn(entity)

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        // The simple toModel() doesn't resolve group; it remains null
        assertNull(found.get().group)
    }

    @Test
    fun testFindByUserIdReturnsConfirmedAndUnconfirmed() = runTest {
        val e1 = createConfirmationEntity(id = "c1", confirmed = true, userId = "u1")
        val e2 = createConfirmationEntity(id = "c2", confirmed = false, userId = "u1")
        `when`(dao.findConfirmationsByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertEquals(1, result.count { it.isConfirmed })
        assertEquals(1, result.count { !it.isConfirmed })
    }
}
