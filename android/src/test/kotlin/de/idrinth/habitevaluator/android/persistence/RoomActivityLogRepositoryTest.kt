package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.ActivityGroup
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalTime

class RoomActivityLogRepositoryTest {

    private lateinit var dao: ActivityLogDao
    private lateinit var repository: RoomActivityLogRepository

    @BeforeEach
    fun setUp() {
        dao = mock(ActivityLogDao::class.java)
        repository = RoomActivityLogRepository(dao)
    }

    private fun createEntity(
        id: String = "al1",
        persons: String = "Alice, Bob",
        location: String = "Office",
        startTime: String = "09:00",
        endTime: String = "10:00",
        date: String = "2024-06-15",
        activity: String? = null,
        userId: String = "u1"
    ) = ActivityLogEntity(
        id = id, persons = persons, location = location,
        startTime = startTime, endTime = endTime, date = date,
        activity = activity, createdAt = "2024-06-15T10:00:00",
        userId = userId, userName = "testuser"
    )

    private fun createGroupEntity(
        id: String = "g1",
        name: String = "Social",
        userId: String = "u1"
    ) = ActivityGroupEntity(
        id = id, name = name, description = null,
        createdAt = "2024-06-15T10:00:00",
        userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsEntry() = runTest {
        val log = de.idrinth.habitevaluator.shared.model.ActivityLog()
        log.id = "al1"
        log.persons = "Alice, Bob"
        log.location = "Office"
        log.startTime = LocalTime.of(9, 0)
        log.endTime = LocalTime.of(10, 0)
        log.date = LocalDate.of(2024, 6, 15)
        log.createdAt = java.time.LocalDateTime.now()
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        log.user = user

        val result = repository.save(log)
        assertEquals("al1", result.id)
        verify(dao).saveActivityLogWithLinks(
            any(ActivityLogEntity::class.java) ?: createEntity(),
            anyList() ?: emptyList()
        )
    }

    @Test
    fun testSaveWithGroupsCallsSaveWithLinks() = runTest {
        val log = de.idrinth.habitevaluator.shared.model.ActivityLog()
        log.id = "al1"
        log.persons = "Alice"
        log.location = "Park"
        log.startTime = LocalTime.of(9, 0)
        log.endTime = LocalTime.of(10, 0)
        log.date = LocalDate.of(2024, 6, 15)
        log.createdAt = java.time.LocalDateTime.now()
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        log.user = user

        val group = ActivityGroup("Social")
        group.id = "g1"
        log.groups = hashSetOf(group)

        val result = repository.save(log)
        assertEquals("al1", result.id)
        verify(dao).saveActivityLogWithLinks(
            any(ActivityLogEntity::class.java) ?: createEntity(),
            anyList() ?: emptyList()
        )
    }

    @Test
    fun testFindByIdReturnsEntryWithGroups() = runTest {
        val entity = createEntity(activity = "Team meeting")
        val groupEntities = listOf(createGroupEntity())
        `when`(dao.findById("al1")).thenReturn(entity)
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(groupEntities)

        val found = repository.findById("al1")
        assertTrue(found.isPresent)
        assertEquals("Alice, Bob", found.get().persons)
        assertEquals("Office", found.get().location)
        assertEquals(LocalTime.of(9, 0), found.get().startTime)
        assertEquals(LocalTime.of(10, 0), found.get().endTime)
        assertEquals(LocalDate.of(2024, 6, 15), found.get().date)
        assertEquals("Team meeting", found.get().activity)
        assertEquals(1, found.get().groups.size)
        assertEquals("Social", found.get().groups.first().name)
    }

    @Test
    fun testFindByIdReturnsEntryWithEmptyGroups() = runTest {
        val entity = createEntity()
        `when`(dao.findById("al1")).thenReturn(entity)
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(emptyList())

        val found = repository.findById("al1")
        assertTrue(found.isPresent)
        assertTrue(found.get().groups.isEmpty())
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
        `when`(dao.findById("al1")).thenReturn(entity)
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(emptyList())

        val found = repository.findById("al1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindByIdWithNullActivity() = runTest {
        val entity = createEntity(activity = null)
        `when`(dao.findById("al1")).thenReturn(entity)
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(emptyList())

        val found = repository.findById("al1")
        assertTrue(found.isPresent)
        assertNull(found.get().activity)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntity(id = "al1", persons = "Alice", location = "Office")
        val e2 = createEntity(id = "al2", persons = "Bob", location = "Park")
        `when`(dao.findAll()).thenReturn(listOf(e1, e2))
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(emptyList())
        `when`(dao.findGroupsByActivityLogId("al2")).thenReturn(emptyList())

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
        repository.deleteById("al1")
        verify(dao).deleteActivityLogWithLinks("al1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.existsById("al1")).thenReturn(true)

        assertTrue(repository.existsById("al1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.existsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createEntity(id = "al1", persons = "Alice", location = "Office", userId = "u1")
        val e2 = createEntity(id = "al2", persons = "Bob", location = "Park", userId = "u1")
        `when`(dao.findByUserId("u1")).thenReturn(listOf(e1, e2))
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(emptyList())
        `when`(dao.findGroupsByActivityLogId("al2")).thenReturn(emptyList())

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
    fun testFindByUserIdWithGroups() = runTest {
        val entity = createEntity(persons = "Alice, Bob", location = "Office", activity = "Meeting")
        val groupEntities = listOf(
            createGroupEntity(id = "g1", name = "Work"),
            createGroupEntity(id = "g2", name = "Social")
        )
        `when`(dao.findByUserId("u1")).thenReturn(listOf(entity))
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(groupEntities)

        val result = repository.findByUserId("u1")
        assertEquals(1, result.size)
        assertEquals("Alice, Bob", result[0].persons)
        assertEquals(2, result[0].groups.size)
    }

    @Test
    fun testFindByIdWithTimes() = runTest {
        val entity = createEntity(startTime = "08:30", endTime = "09:30")
        `when`(dao.findById("al1")).thenReturn(entity)
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(emptyList())

        val found = repository.findById("al1")
        assertTrue(found.isPresent)
        assertEquals(LocalTime.of(8, 30), found.get().startTime)
        assertEquals(LocalTime.of(9, 30), found.get().endTime)
    }

    @Test
    fun testFindDistinctLocationsByUserId() = runTest {
        `when`(dao.findDistinctLocations("u1")).thenReturn(listOf("Cafe", "Office"))

        val locations = repository.findDistinctLocationsByUserId("u1")
        assertEquals(2, locations.size)
        assertTrue(locations.containsAll(listOf("Cafe", "Office")))
    }

    @Test
    fun testFindDistinctLocationsByUserIdEmpty() = runTest {
        `when`(dao.findDistinctLocations("u99")).thenReturn(emptyList())

        val result = repository.findDistinctLocationsByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindDistinctActivitiesByUserId() = runTest {
        `when`(dao.findDistinctActivities("u1")).thenReturn(listOf("Lunch", "Team meeting"))

        val activities = repository.findDistinctActivitiesByUserId("u1")
        assertEquals(2, activities.size)
        assertTrue(activities.containsAll(listOf("Lunch", "Team meeting")))
    }

    @Test
    fun testFindDistinctActivitiesByUserIdEmpty() = runTest {
        `when`(dao.findDistinctActivities("u99")).thenReturn(emptyList())

        val result = repository.findDistinctActivitiesByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindGroupsForActivityLog() = runTest {
        val groupEntities = listOf(
            createGroupEntity(id = "g1", name = "Work"),
            createGroupEntity(id = "g2", name = "Social")
        )
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(groupEntities)

        val groups = repository.findGroupsForActivityLog("al1")
        assertEquals(2, groups.size)
        assertTrue(groups.any { it.name == "Work" })
        assertTrue(groups.any { it.name == "Social" })
    }

    @Test
    fun testFindGroupsForActivityLogEmpty() = runTest {
        `when`(dao.findGroupsByActivityLogId("al1")).thenReturn(emptyList())

        val groups = repository.findGroupsForActivityLog("al1")
        assertTrue(groups.isEmpty())
    }
}
