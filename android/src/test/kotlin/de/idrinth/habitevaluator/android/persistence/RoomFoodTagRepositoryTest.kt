package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.FoodLog
import de.idrinth.habitevaluator.shared.model.FoodTag
import de.idrinth.habitevaluator.shared.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomFoodTagRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomFoodTagRepository
    private lateinit var foodLogRepository: RoomFoodLogRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomFoodTagRepository(database.foodLogDao())
        foodLogRepository = RoomFoodLogRepository(database.foodLogDao())
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

    private fun createTag(
        name: String = "Vegetarian",
        userId: String = "u1"
    ): FoodTag {
        val tag = FoodTag()
        tag.id = UUID.randomUUID().toString()
        tag.name = name
        tag.user = createUser(userId)
        return tag
    }

    private fun createFoodLog(userId: String = "u1"): FoodLog {
        val log = FoodLog()
        log.id = UUID.randomUUID().toString()
        log.foodItems = "Rice, Chicken"
        log.dateTime = LocalDateTime.of(2024, 6, 15, 12, 0)
        log.createdAt = LocalDateTime.now()
        log.user = createUser(userId)
        return log
    }

    @Test
    fun testSaveReturnsTag() {
        val tag = createTag()
        val result = repository.save(tag)
        assertEquals(tag.id, result.id)
        assertEquals(tag.name, result.name)
    }

    @Test
    fun testSaveAndFindById() {
        val tag = createTag("Vegetarian")
        repository.save(tag)

        val found = repository.findById(tag.id)
        assertTrue(found.isPresent)
        assertEquals("Vegetarian", found.get().name)
        assertEquals("vegetarian", found.get().nameLower)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val tag = createTag(userId = "u1")
        repository.save(tag)

        val found = repository.findById(tag.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindByUserId() {
        val tag1 = createTag("Vegetarian", "u1")
        val tag2 = createTag("Vegan", "u1")
        val tag3 = createTag("Organic", "u2")
        repository.save(tag1)
        repository.save(tag2)
        repository.save(tag3)

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
    fun testFindByNameLowerAndUserIdFound() {
        val tag = createTag("Vegetarian", "u1")
        repository.save(tag)

        val found = repository.findByNameLowerAndUserId("vegetarian", "u1")
        assertTrue(found.isPresent)
        assertEquals("Vegetarian", found.get().name)
    }

    @Test
    fun testFindByNameLowerAndUserIdNotFound() {
        val result = repository.findByNameLowerAndUserId("nonexistent", "u1")
        assertFalse(result.isPresent)
    }

    @Test
    fun testDeleteById() {
        val tag = createTag()
        repository.save(tag)

        assertTrue(repository.findById(tag.id).isPresent)

        repository.deleteById(tag.id)

        assertFalse(repository.findById(tag.id).isPresent)
    }

    @Test
    fun testNameLowerIsAutomaticallySet() {
        val tag = createTag("VeGeTaRiAn")
        repository.save(tag)

        val found = repository.findById(tag.id)
        assertTrue(found.isPresent)
        assertEquals("vegetarian", found.get().nameLower)
    }

    @Test
    fun testLinkTagToFoodLog() {
        val log = createFoodLog()
        foodLogRepository.save(log)
        val tag = createTag()
        repository.save(tag)

        repository.linkTagToFoodLog(log.id, tag.id)

        val tags = repository.findTagsByFoodLogId(log.id)
        assertEquals(1, tags.size)
        assertEquals(tag.id, tags[0].id)
    }

    @Test
    fun testUnlinkAllTagsFromFoodLog() {
        val log = createFoodLog()
        foodLogRepository.save(log)
        val tag = createTag()
        repository.save(tag)

        repository.linkTagToFoodLog(log.id, tag.id)
        assertEquals(1, repository.findTagsByFoodLogId(log.id).size)

        repository.unlinkAllTagsFromFoodLog(log.id)
        assertEquals(0, repository.findTagsByFoodLogId(log.id).size)
    }

    @Test
    fun testFindTagsByFoodLogIdEmpty() {
        val log = createFoodLog()
        foodLogRepository.save(log)

        val tags = repository.findTagsByFoodLogId(log.id)
        assertTrue(tags.isEmpty())
    }
}
