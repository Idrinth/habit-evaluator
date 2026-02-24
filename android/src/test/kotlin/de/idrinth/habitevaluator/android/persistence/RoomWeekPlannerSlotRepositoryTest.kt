package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.model.User
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyList
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomWeekPlannerSlotRepositoryTest {

    private lateinit var dao: DayPlannerDao
    private lateinit var repository: RoomWeekPlannerSlotRepository

    @BeforeEach
    fun setUp() {
        dao = mock(DayPlannerDao::class.java)
        repository = RoomWeekPlannerSlotRepository(dao)
    }

    private fun createSlotEntity(
        id: String = "s1",
        dayOfWeek: Int = 1,
        hour: Int = 9,
        duration: Int = 1,
        userId: String = "u1"
    ) = WeekPlannerSlotEntity(
        id = id, dayOfWeek = dayOfWeek, hour = hour, duration = duration,
        userId = userId, userName = "testuser"
    )

    private fun createGroupEntity(
        id: String = "g1",
        name: String = "Exercise",
        userId: String = "u1"
    ) = PlannerGroupEntity(
        id = id, name = name, description = null,
        createdAt = "2024-06-15T10:00:00",
        userId = userId, userName = "testuser"
    )

    private fun createSlot(
        dayOfWeek: Int = 1,
        hour: Int = 9,
        userId: String = "u1"
    ): WeekPlannerSlot {
        val slot = WeekPlannerSlot(dayOfWeek, hour)
        slot.id = "s1"
        val user = User()
        user.id = userId
        user.username = "testuser"
        slot.user = user
        return slot
    }

    @Test
    fun testSaveReturnsSlot() = runTest {
        val slot = createSlot()
        val result = repository.save(slot)
        assertEquals("s1", result.id)
        assertEquals(1, result.dayOfWeek)
        assertEquals(9, result.hour)
        verify(dao).saveSlotWithGroups(
            any(WeekPlannerSlotEntity::class.java) ?: createSlotEntity(),
            anyList()
        )
    }

    @Test
    fun testSaveWithGroupsCallsSaveWithGroups() = runTest {
        val slot = createSlot()
        val group = PlannerGroup("Exercise")
        group.id = "g1"
        slot.groups = hashSetOf(group)

        val result = repository.save(slot)
        assertEquals("s1", result.id)
        verify(dao).saveSlotWithGroups(
            any(WeekPlannerSlotEntity::class.java) ?: createSlotEntity(),
            anyList()
        )
    }

    @Test
    fun testFindByIdReturnsSlotWithGroups() = runTest {
        val entity = createSlotEntity(dayOfWeek = 3, hour = 14)
        `when`(dao.findSlotById("s1")).thenReturn(entity)
        `when`(dao.findGroupIdsForSlot("s1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(createGroupEntity())

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals(3, found.get().dayOfWeek)
        assertEquals(14, found.get().hour)
        assertEquals(1, found.get().groups.size)
        assertEquals("Exercise", found.get().groups.first().name)
    }

    @Test
    fun testFindByIdReturnsSlotWithEmptyGroups() = runTest {
        val entity = createSlotEntity()
        `when`(dao.findSlotById("s1")).thenReturn(entity)
        `when`(dao.findGroupIdsForSlot("s1")).thenReturn(emptyList())

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertTrue(found.get().groups.isEmpty())
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findSlotById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createSlotEntity()
        `when`(dao.findSlotById("s1")).thenReturn(entity)
        `when`(dao.findGroupIdsForSlot("s1")).thenReturn(emptyList())

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createSlotEntity(id = "s1", dayOfWeek = 1, hour = 9)
        val e2 = createSlotEntity(id = "s2", dayOfWeek = 2, hour = 10)
        `when`(dao.findAllSlots()).thenReturn(listOf(e1, e2))
        `when`(dao.findGroupIdsForSlot(anyString())).thenReturn(emptyList())

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllSlots()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("s1")
        verify(dao).deleteSlotWithLinks("s1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.slotExistsById("s1")).thenReturn(true)

        assertTrue(repository.existsById("s1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.slotExistsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createSlotEntity(id = "s1", dayOfWeek = 1, userId = "u1")
        val e2 = createSlotEntity(id = "s2", dayOfWeek = 2, userId = "u1")
        `when`(dao.findSlotsByUserId("u1")).thenReturn(listOf(e1, e2))
        `when`(dao.findGroupIdsForSlot(anyString())).thenReturn(emptyList())

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findSlotsByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByUserIdAndDayOfWeek() = runTest {
        val e1 = createSlotEntity(id = "s1", dayOfWeek = 1, hour = 9)
        val e2 = createSlotEntity(id = "s2", dayOfWeek = 1, hour = 10)
        `when`(dao.findSlotsByUserIdAndDayOfWeek("u1", 1)).thenReturn(listOf(e1, e2))
        `when`(dao.findGroupIdsForSlot(anyString())).thenReturn(emptyList())

        val result = repository.findByUserIdAndDayOfWeek("u1", 1)
        assertEquals(2, result.size)
        assertTrue(result.all { it.dayOfWeek == 1 })
    }

    @Test
    fun testFindByUserIdAndDayOfWeekEmpty() = runTest {
        `when`(dao.findSlotsByUserIdAndDayOfWeek("u1", 7)).thenReturn(emptyList())

        val result = repository.findByUserIdAndDayOfWeek("u1", 7)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByIdWithMultipleGroups() = runTest {
        val entity = createSlotEntity()
        val g1 = createGroupEntity(id = "g1", name = "Exercise")
        val g2 = createGroupEntity(id = "g2", name = "Outdoor")
        `when`(dao.findSlotById("s1")).thenReturn(entity)
        `when`(dao.findGroupIdsForSlot("s1")).thenReturn(listOf("g1", "g2"))
        `when`(dao.findGroupById("g1")).thenReturn(g1)
        `when`(dao.findGroupById("g2")).thenReturn(g2)

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals(2, found.get().groups.size)
    }

    @Test
    fun testFindByIdGroupNotFound() = runTest {
        val entity = createSlotEntity()
        `when`(dao.findSlotById("s1")).thenReturn(entity)
        `when`(dao.findGroupIdsForSlot("s1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(null)

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertTrue(found.get().groups.isEmpty())
    }

    @Test
    fun testSaveSuspend() = runTest {
        val slot = createSlot()
        val result = repository.saveSuspend(slot)
        assertEquals("s1", result.id)
        verify(dao).saveSlotWithGroups(
            any(WeekPlannerSlotEntity::class.java) ?: createSlotEntity(),
            anyList()
        )
    }

    @Test
    fun testFindByUserIdWithGroups() = runTest {
        val entity = createSlotEntity(dayOfWeek = 5, hour = 17)
        `when`(dao.findSlotsByUserId("u1")).thenReturn(listOf(entity))
        `when`(dao.findGroupIdsForSlot("s1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(createGroupEntity())

        val result = repository.findByUserId("u1")
        assertEquals(1, result.size)
        assertEquals(5, result[0].dayOfWeek)
        assertEquals(17, result[0].hour)
        assertEquals(1, result[0].groups.size)
    }

    @Test
    fun testFindByUserIdAndDayOfWeekWithGroups() = runTest {
        val entity = createSlotEntity(dayOfWeek = 3, hour = 8)
        `when`(dao.findSlotsByUserIdAndDayOfWeek("u1", 3)).thenReturn(listOf(entity))
        `when`(dao.findGroupIdsForSlot("s1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(createGroupEntity())

        val result = repository.findByUserIdAndDayOfWeek("u1", 3)
        assertEquals(1, result.size)
        assertEquals(3, result[0].dayOfWeek)
        assertEquals(1, result[0].groups.size)
    }
}
