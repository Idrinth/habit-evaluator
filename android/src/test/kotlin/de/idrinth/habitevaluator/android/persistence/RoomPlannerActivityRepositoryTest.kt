package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.PlannerActivity
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.model.User
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

class RoomPlannerActivityRepositoryTest {

    private lateinit var dao: DayPlannerDao
    private lateinit var repository: RoomPlannerActivityRepository

    @BeforeEach
    fun setUp() {
        dao = mock(DayPlannerDao::class.java)
        repository = RoomPlannerActivityRepository(dao)
    }

    private fun createActivityEntity(
        id: String = "a1",
        name: String = "Morning Run",
        description: String? = null,
        userId: String = "u1"
    ) = PlannerActivityEntity(
        id = id, name = name, description = description,
        createdAt = "2024-06-15T10:00:00",
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

    private fun createActivity(
        name: String = "Morning Run",
        userId: String = "u1"
    ): PlannerActivity {
        val activity = PlannerActivity(name)
        activity.id = "a1"
        val user = User()
        user.id = userId
        user.username = "testuser"
        activity.user = user
        return activity
    }

    @Test
    fun testSaveReturnsActivity() = runTest {
        val activity = createActivity()
        val result = repository.save(activity)
        assertEquals("a1", result.id)
        assertEquals("Morning Run", result.name)
        verify(dao).saveActivityWithLinks(
            any(PlannerActivityEntity::class.java) ?: createActivityEntity(),
            anyList()
        )
    }

    @Test
    fun testSaveWithGroupsCallsSaveWithLinks() = runTest {
        val activity = createActivity()
        val group = PlannerGroup("Exercise")
        group.id = "g1"
        activity.groups = hashSetOf(group)

        val result = repository.save(activity)
        assertEquals("a1", result.id)
        verify(dao).saveActivityWithLinks(
            any(PlannerActivityEntity::class.java) ?: createActivityEntity(),
            anyList()
        )
    }

    @Test
    fun testFindByIdReturnsActivityWithGroups() = runTest {
        val entity = createActivityEntity(description = "Daily morning run")
        `when`(dao.findActivityById("a1")).thenReturn(entity)
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(createGroupEntity())

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertEquals("Morning Run", found.get().name)
        assertEquals("Daily morning run", found.get().description)
        assertEquals(1, found.get().groups.size)
        assertEquals("Exercise", found.get().groups.first().name)
    }

    @Test
    fun testFindByIdReturnsActivityWithEmptyGroups() = runTest {
        val entity = createActivityEntity()
        `when`(dao.findActivityById("a1")).thenReturn(entity)
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(emptyList())

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertTrue(found.get().groups.isEmpty())
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findActivityById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createActivityEntity()
        `when`(dao.findActivityById("a1")).thenReturn(entity)
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(emptyList())

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createActivityEntity(id = "a1", name = "Morning Run")
        val e2 = createActivityEntity(id = "a2", name = "Meditation")
        `when`(dao.findAllActivities()).thenReturn(listOf(e1, e2))
        `when`(dao.findGroupIdsForActivity(anyString())).thenReturn(emptyList())

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllActivities()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("a1")
        verify(dao).deleteActivityWithLinks("a1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.activityExistsById("a1")).thenReturn(true)

        assertTrue(repository.existsById("a1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.activityExistsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createActivityEntity(id = "a1", name = "Morning Run", userId = "u1")
        val e2 = createActivityEntity(id = "a2", name = "Meditation", userId = "u1")
        `when`(dao.findActivitiesByUserId("u1")).thenReturn(listOf(e1, e2))
        `when`(dao.findGroupIdsForActivity(anyString())).thenReturn(emptyList())

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findActivitiesByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByGroupId() = runTest {
        val e1 = createActivityEntity(id = "a1", name = "Morning Run")
        `when`(dao.findActivitiesByGroupId("g1")).thenReturn(listOf(e1))
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(createGroupEntity())

        val result = repository.findByGroupId("g1")
        assertEquals(1, result.size)
        assertEquals("Morning Run", result[0].name)
        assertEquals(1, result[0].groups.size)
    }

    @Test
    fun testFindByGroupIdEmpty() = runTest {
        `when`(dao.findActivitiesByGroupId("g99")).thenReturn(emptyList())

        val result = repository.findByGroupId("g99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByIdWithMultipleGroups() = runTest {
        val entity = createActivityEntity()
        val g1 = createGroupEntity(id = "g1", name = "Exercise")
        val g2 = createGroupEntity(id = "g2", name = "Outdoor")
        `when`(dao.findActivityById("a1")).thenReturn(entity)
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(listOf("g1", "g2"))
        `when`(dao.findGroupById("g1")).thenReturn(g1)
        `when`(dao.findGroupById("g2")).thenReturn(g2)

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertEquals(2, found.get().groups.size)
    }

    @Test
    fun testFindByIdWithNullDescription() = runTest {
        val entity = createActivityEntity(description = null)
        `when`(dao.findActivityById("a1")).thenReturn(entity)
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(emptyList())

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertEquals(null, found.get().description)
    }

    @Test
    fun testFindByIdGroupNotFound() = runTest {
        val entity = createActivityEntity()
        `when`(dao.findActivityById("a1")).thenReturn(entity)
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(null)

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertTrue(found.get().groups.isEmpty())
    }

    @Test
    fun testSaveSuspend() = runTest {
        val activity = createActivity()
        val result = repository.saveSuspend(activity)
        assertEquals("a1", result.id)
        verify(dao).saveActivityWithLinks(
            any(PlannerActivityEntity::class.java) ?: createActivityEntity(),
            anyList()
        )
    }

    @Test
    fun testFindByUserIdWithGroups() = runTest {
        val entity = createActivityEntity(name = "Yoga")
        `when`(dao.findActivitiesByUserId("u1")).thenReturn(listOf(entity))
        `when`(dao.findGroupIdsForActivity("a1")).thenReturn(listOf("g1"))
        `when`(dao.findGroupById("g1")).thenReturn(createGroupEntity())

        val result = repository.findByUserId("u1")
        assertEquals(1, result.size)
        assertEquals("Yoga", result[0].name)
        assertEquals(1, result[0].groups.size)
        assertEquals("Exercise", result[0].groups.first().name)
    }
}
