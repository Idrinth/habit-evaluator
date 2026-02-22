package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.FoodLog
import de.idrinth.habitevaluator.shared.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomFoodLogRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomFoodLogRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomFoodLogRepository(database.foodLogDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createUser(id: String = "u1", username: String = "testuser"): User {
        val user = User()
        user.id = id
        user.username = username
        return user
    }

    private fun createFoodLog(
        carbohydrates: Double? = 25.0,
        kcal: Int? = 300,
        foodItems: String = "Rice, Chicken",
        notes: String? = null,
        userId: String = "u1"
    ): FoodLog {
        val log = FoodLog()
        log.id = UUID.randomUUID().toString()
        log.carbohydrates = carbohydrates
        log.kcal = kcal
        log.foodItems = foodItems
        log.dateTime = LocalDateTime.of(2024, 6, 15, 12, 0)
        log.createdAt = LocalDateTime.now()
        log.notes = notes
        log.user = createUser(userId)
        return log
    }

    @Test
    fun testSaveReturnsEntry() {
        val log = createFoodLog()
        val result = repository.save(log)
        assertEquals(log.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val log = createFoodLog(
            carbohydrates = 25.5,
            kcal = 350,
            foodItems = "Rice, Chicken",
            notes = "Lunch"
        )
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertEquals(25.5, found.get().carbohydrates!!, 0.01)
        assertEquals(350, found.get().kcal)
        assertEquals("Rice, Chicken", found.get().foodItems)
        assertEquals("Lunch", found.get().notes)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithNullCarbohydratesAndKcal() {
        val log = createFoodLog(carbohydrates = null, kcal = null, foodItems = "Snack")
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNull(found.get().carbohydrates)
        assertNull(found.get().kcal)
    }

    @Test
    fun testFindByIdWithUser() {
        val log = createFoodLog(userId = "u1")
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() {
        val log1 = createFoodLog(foodItems = "Rice")
        val log2 = createFoodLog(foodItems = "Pasta")
        repository.save(log1)
        repository.save(log2)

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() {
        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() {
        val log = createFoodLog()
        repository.save(log)

        assertTrue(repository.findById(log.id).isPresent)

        repository.deleteById(log.id)

        assertFalse(repository.findById(log.id).isPresent)
    }

    @Test
    fun testExistsByIdTrue() {
        val log = createFoodLog()
        repository.save(log)

        assertTrue(repository.existsById(log.id))
    }

    @Test
    fun testExistsByIdFalse() {
        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() {
        val log1 = createFoodLog(foodItems = "Rice", userId = "u1")
        val log2 = createFoodLog(foodItems = "Pasta", userId = "u1")
        val log3 = createFoodLog(foodItems = "Salad", userId = "u2")
        repository.save(log1)
        repository.save(log2)
        repository.save(log3)

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() {
        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSaveWithNullFieldsDoesNotThrow() {
        val log = createFoodLog(carbohydrates = null, kcal = null, notes = null)
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNull(found.get().carbohydrates)
        assertNull(found.get().kcal)
        assertNull(found.get().notes)
    }

    @Test
    fun testSaveWithUserAttached() {
        val log = createFoodLog(userId = "u1")
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }
}
