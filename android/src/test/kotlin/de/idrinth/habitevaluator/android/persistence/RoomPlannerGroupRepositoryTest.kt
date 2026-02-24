package de.idrinth.habitevaluator.android.persistence

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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomPlannerGroupRepositoryTest {

    private lateinit var dao: DayPlannerDao
    private lateinit var repository: RoomPlannerGroupRepository

    @BeforeEach
    fun setUp() {
        dao = mock(DayPlannerDao::class.java)
        repository = RoomPlannerGroupRepository(dao)
    }

    private fun createGroupEntity(
        id: String = "g1",
        name: String = "Exercise",
        description: String? = null,
        userId: String = "u1"
    ) = PlannerGroupEntity(
        id = id, name = name, description = description,
        createdAt = "2024-06-15T10:00:00",
        userId = userId, userName = "testuser"
    )

    private fun createGroup(
        name: String = "Exercise",
        userId: String = "u1"
    ): PlannerGroup {
        val group = PlannerGroup(name)
        group.id = "g1"
        val user = User()
        user.id = userId
        user.username = "testuser"
        group.user = user
        return group
    }

    @Test
    fun testSaveReturnsGroup() = runTest {
        val group = createGroup()
        val result = repository.save(group)
        assertEquals("g1", result.id)
        assertEquals("Exercise", result.name)
        verify(dao).insertGroup(any(PlannerGroupEntity::class.java) ?: createGroupEntity())
    }

    @Test
    fun testFindByIdReturnsGroup() = runTest {
        val entity = createGroupEntity(description = "Physical activities")
        `when`(dao.findGroupById("g1")).thenReturn(entity)

        val found = repository.findById("g1")
        assertTrue(found.isPresent)
        assertEquals("Exercise", found.get().name)
        assertEquals("Physical activities", found.get().description)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findGroupById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createGroupEntity()
        `when`(dao.findGroupById("g1")).thenReturn(entity)

        val found = repository.findById("g1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createGroupEntity(id = "g1", name = "Exercise")
        val e2 = createGroupEntity(id = "g2", name = "Study")
        `when`(dao.findAllGroups()).thenReturn(listOf(e1, e2))

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllGroups()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("g1")
        verify(dao).deleteGroupWithLinks("g1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.groupExistsById("g1")).thenReturn(true)

        assertTrue(repository.existsById("g1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.groupExistsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createGroupEntity(id = "g1", name = "Exercise", userId = "u1")
        val e2 = createGroupEntity(id = "g2", name = "Study", userId = "u1")
        `when`(dao.findGroupsByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findGroupsByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByUserIdReturnsCorrectNames() = runTest {
        val e1 = createGroupEntity(id = "g1", name = "Fitness")
        val e2 = createGroupEntity(id = "g2", name = "Education")
        `when`(dao.findGroupsByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Fitness" })
        assertTrue(result.any { it.name == "Education" })
    }

    @Test
    fun testFindByIdWithNullDescription() = runTest {
        val entity = createGroupEntity(description = null)
        `when`(dao.findGroupById("g1")).thenReturn(entity)

        val found = repository.findById("g1")
        assertTrue(found.isPresent)
        assertEquals(null, found.get().description)
    }

    @Test
    fun testFindByIdWithDescription() = runTest {
        val entity = createGroupEntity(description = "Cardio and strength")
        `when`(dao.findGroupById("g1")).thenReturn(entity)

        val found = repository.findById("g1")
        assertTrue(found.isPresent)
        assertEquals("Cardio and strength", found.get().description)
    }

    @Test
    fun testSaveSuspend() = runTest {
        val group = createGroup()
        val result = repository.saveSuspend(group)
        assertEquals("g1", result.id)
        verify(dao).insertGroup(any(PlannerGroupEntity::class.java) ?: createGroupEntity())
    }

    @Test
    fun testFindAllReturnsCorrectModels() = runTest {
        val e1 = createGroupEntity(id = "g1", name = "Exercise", description = "Physical")
        val e2 = createGroupEntity(id = "g2", name = "Study", description = "Learning")
        `when`(dao.findAllGroups()).thenReturn(listOf(e1, e2))

        val all = repository.findAll()
        assertEquals(2, all.size)
        assertTrue(all.any { it.name == "Exercise" && it.description == "Physical" })
        assertTrue(all.any { it.name == "Study" && it.description == "Learning" })
    }
}
