package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.ActivityLog
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomActivityLogRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomActivityLogRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomActivityLogRepository(database.activityLogDao())
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

    private fun createActivityLog(
        persons: String = "Alice, Bob",
        location: String = "Office",
        startTime: LocalTime? = LocalTime.of(9, 0),
        endTime: LocalTime? = LocalTime.of(10, 0),
        date: LocalDate = LocalDate.of(2024, 6, 15),
        activity: String? = null,
        userId: String = "u1"
    ): ActivityLog {
        val log = ActivityLog()
        log.id = UUID.randomUUID().toString()
        log.persons = persons
        log.location = location
        log.startTime = startTime
        log.endTime = endTime
        log.date = date
        log.createdAt = LocalDateTime.now()
        log.activity = activity
        log.user = createUser(userId)
        return log
    }

    @Test
    fun testSaveReturnsEntry() {
        val log = createActivityLog()
        val result = repository.save(log)
        assertEquals(log.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val log = createActivityLog(
            persons = "Alice, Bob",
            location = "Office",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            date = LocalDate.of(2024, 6, 15),
            activity = "Team meeting"
        )
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertEquals("Alice, Bob", found.get().persons)
        assertEquals("Office", found.get().location)
        assertEquals(LocalTime.of(9, 0), found.get().startTime)
        assertEquals(LocalTime.of(10, 0), found.get().endTime)
        assertEquals(LocalDate.of(2024, 6, 15), found.get().date)
        assertEquals("Team meeting", found.get().activity)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val log = createActivityLog(userId = "u1")
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindByIdWithNullActivity() {
        val log = createActivityLog(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            activity = null
        )
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNull(found.get().activity)
    }

    @Test
    fun testFindAll() {
        val log1 = createActivityLog("Alice", "Office")
        val log2 = createActivityLog("Bob", "Park")
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
        val log = createActivityLog()
        repository.save(log)

        assertTrue(repository.findById(log.id).isPresent)

        repository.deleteById(log.id)

        assertFalse(repository.findById(log.id).isPresent)
    }

    @Test
    fun testExistsByIdTrue() {
        val log = createActivityLog()
        repository.save(log)

        assertTrue(repository.existsById(log.id))
    }

    @Test
    fun testExistsByIdFalse() {
        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() {
        val log1 = createActivityLog("Alice", "Office", userId = "u1")
        val log2 = createActivityLog("Bob", "Park", userId = "u1")
        val log3 = createActivityLog("Charlie", "Cafe", userId = "u2")
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
    fun testFindAllWithMultipleEntries() {
        val log1 = createActivityLog("Alice", "Office", activity = "Lunch")
        val log2 = createActivityLog("Bob", "Park", activity = "Walk")
        repository.save(log1)
        repository.save(log2)

        val result = repository.findAll()
        assertEquals(2, result.size)
    }

    @Test
    fun testFindByUserIdWithResults() {
        val log = createActivityLog("Alice, Bob", "Office", activity = "Meeting", userId = "u1")
        repository.save(log)

        val result = repository.findByUserId("u1")
        assertEquals(1, result.size)
        assertEquals("Alice, Bob", result[0].persons)
        assertNotNull(result[0].user)
    }

    @Test
    fun testSaveWithTimesDoesNotThrow() {
        val log = createActivityLog(startTime = LocalTime.of(8, 30), endTime = LocalTime.of(9, 30))
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertEquals("Alice, Bob", found.get().persons)
        assertEquals("Office", found.get().location)
        assertEquals(LocalTime.of(8, 30), found.get().startTime)
        assertEquals(LocalTime.of(9, 30), found.get().endTime)
    }

    @Test
    fun testFindDistinctLocationsByUserId() {
        val log1 = createActivityLog(location = "Cafe", userId = "u1")
        val log2 = createActivityLog(location = "Office", userId = "u1")
        repository.save(log1)
        repository.save(log2)

        val locations = repository.findDistinctLocationsByUserId("u1")
        assertEquals(2, locations.size)
        assertTrue(locations.containsAll(listOf("Cafe", "Office")))
    }

    @Test
    fun testFindDistinctLocationsByUserIdEmpty() {
        val result = repository.findDistinctLocationsByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindDistinctActivitiesByUserId() {
        val log1 = createActivityLog(activity = "Lunch", userId = "u1")
        val log2 = createActivityLog(activity = "Team meeting", userId = "u1")
        repository.save(log1)
        repository.save(log2)

        val activities = repository.findDistinctActivitiesByUserId("u1")
        assertEquals(2, activities.size)
        assertTrue(activities.containsAll(listOf("Lunch", "Team meeting")))
    }

    @Test
    fun testFindDistinctActivitiesByUserIdEmpty() {
        val result = repository.findDistinctActivitiesByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindDistinctActivitiesExcludesNullActivity() {
        val log1 = createActivityLog(activity = null, userId = "u1")
        val log2 = createActivityLog(activity = "Lunch", userId = "u1")
        repository.save(log1)
        repository.save(log2)

        val activities = repository.findDistinctActivitiesByUserId("u1")
        assertEquals(1, activities.size)
        assertEquals("Lunch", activities[0])
    }
}
