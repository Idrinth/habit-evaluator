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

class RoomFoodLogRepositoryTest {

    private lateinit var dao: FoodLogDao
    private lateinit var repository: RoomFoodLogRepository

    @BeforeEach
    fun setUp() {
        dao = mock(FoodLogDao::class.java)
        repository = RoomFoodLogRepository(dao)
    }

    private fun createEntity(
        id: String = "f1",
        carbohydrates: Double? = 25.0,
        kcal: Int? = 300,
        foodItems: String? = "Rice, Chicken",
        notes: String? = null,
        userId: String = "u1"
    ) = FoodLogEntity(
        id = id, carbohydrates = carbohydrates, kcal = kcal,
        dateTime = "2024-06-15T12:00:00", foodItems = foodItems,
        createdAt = "2024-06-15T12:00:00", notes = notes,
        userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsEntry() = runTest {
        val log = de.idrinth.habitevaluator.shared.model.FoodLog()
        log.id = "f1"
        log.carbohydrates = 25.0
        log.kcal = 300
        log.foodItems = "Rice, Chicken"
        log.dateTime = java.time.LocalDateTime.of(2024, 6, 15, 12, 0)
        log.createdAt = java.time.LocalDateTime.now()
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        log.user = user

        val result = repository.save(log)
        assertEquals("f1", result.id)
        verify(dao).insert(any(FoodLogEntity::class.java) ?: createEntity())
    }

    @Test
    fun testFindByIdReturnsEntry() = runTest {
        val entity = createEntity(carbohydrates = 25.5, kcal = 350, foodItems = "Rice, Chicken", notes = "Lunch")
        `when`(dao.findById("f1")).thenReturn(entity)
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(emptyList())

        val found = repository.findById("f1")
        assertTrue(found.isPresent)
        assertEquals(25.5, found.get().carbohydrates!!, 0.01)
        assertEquals(350, found.get().kcal)
        assertEquals("Rice, Chicken", found.get().foodItems)
        assertEquals("Lunch", found.get().notes)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithNullCarbohydratesAndKcal() = runTest {
        val entity = createEntity(carbohydrates = null, kcal = null, foodItems = "Snack")
        `when`(dao.findById("f1")).thenReturn(entity)
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(emptyList())

        val found = repository.findById("f1")
        assertTrue(found.isPresent)
        assertNull(found.get().carbohydrates)
        assertNull(found.get().kcal)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createEntity()
        `when`(dao.findById("f1")).thenReturn(entity)
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(emptyList())

        val found = repository.findById("f1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntity(id = "f1", foodItems = "Rice")
        val e2 = createEntity(id = "f2", foodItems = "Pasta")
        `when`(dao.findAll()).thenReturn(listOf(e1, e2))
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(emptyList())
        `when`(dao.findTagsByFoodLogId("f2")).thenReturn(emptyList())

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
    fun testDeleteByIdUnlinksTagsThenDeletes() = runTest {
        repository.deleteById("f1")
        verify(dao).unlinkAllTagsFromFoodLog("f1")
        verify(dao).deleteById("f1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.existsById("f1")).thenReturn(true)

        assertTrue(repository.existsById("f1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.existsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createEntity(id = "f1", foodItems = "Rice", userId = "u1")
        val e2 = createEntity(id = "f2", foodItems = "Pasta", userId = "u1")
        `when`(dao.findByUserId("u1")).thenReturn(listOf(e1, e2))
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(emptyList())
        `when`(dao.findTagsByFoodLogId("f2")).thenReturn(emptyList())

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
    fun testFindByIdWithAllNullOptionalFields() = runTest {
        val entity = createEntity(carbohydrates = null, kcal = null, notes = null)
        `when`(dao.findById("f1")).thenReturn(entity)
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(emptyList())

        val found = repository.findById("f1")
        assertTrue(found.isPresent)
        assertNull(found.get().carbohydrates)
        assertNull(found.get().kcal)
        assertNull(found.get().notes)
    }

    @Test
    fun testFindByIdUserAttached() = runTest {
        val entity = createEntity()
        `when`(dao.findById("f1")).thenReturn(entity)
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(emptyList())

        val found = repository.findById("f1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindByIdLoadsTags() = runTest {
        val entity = createEntity()
        val tagEntities = listOf(
            FoodTagEntity(id = "t1", name = "Rice", nameLower = "rice", userId = "u1", userName = "testuser"),
            FoodTagEntity(id = "t2", name = "Chicken", nameLower = "chicken", userId = "u1", userName = "testuser")
        )
        `when`(dao.findById("f1")).thenReturn(entity)
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(tagEntities)

        val found = repository.findById("f1")
        assertTrue(found.isPresent)
        assertEquals(2, found.get().tags.size)
        assertTrue(found.get().tags.any { it.name == "Rice" })
        assertTrue(found.get().tags.any { it.name == "Chicken" })
    }

    @Test
    fun testFindByUserIdLoadsTags() = runTest {
        val e1 = createEntity(id = "f1", foodItems = "Rice", userId = "u1")
        val tagEntities = listOf(
            FoodTagEntity(id = "t1", name = "Rice", nameLower = "rice", userId = "u1", userName = "testuser")
        )
        `when`(dao.findByUserId("u1")).thenReturn(listOf(e1))
        `when`(dao.findTagsByFoodLogId("f1")).thenReturn(tagEntities)

        val result = repository.findByUserId("u1")
        assertEquals(1, result.size)
        assertEquals(1, result[0].tags.size)
        assertEquals("Rice", result[0].tags.first().name)
    }
}
