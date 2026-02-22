package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.SportLog
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
class RoomSportLogRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomSportLogRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomSportLogRepository(database.sportLogDao())
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

    private fun createSportLog(
        name: String = "Running",
        measurement: Double = 5.0,
        measurementUnit: String = "km",
        startTime: LocalTime? = LocalTime.of(8, 0),
        endTime: LocalTime? = LocalTime.of(9, 0),
        date: LocalDate = LocalDate.of(2024, 6, 15),
        notes: String? = null,
        userId: String = "u1"
    ): SportLog {
        val log = SportLog()
        log.id = UUID.randomUUID().toString()
        log.name = name
        log.measurement = measurement
        log.measurementUnit = measurementUnit
        log.startTime = startTime
        log.endTime = endTime
        log.date = date
        log.createdAt = LocalDateTime.now()
        log.notes = notes
        log.user = createUser(userId)
        return log
    }

    @Test
    fun testSaveReturnsEntry() {
        val log = createSportLog()
        val result = repository.save(log)
        assertEquals(log.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val log = createSportLog(
            name = "Running",
            measurement = 5.5,
            measurementUnit = "km",
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(9, 0),
            date = LocalDate.of(2024, 6, 15),
            notes = "Morning run"
        )
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertEquals("Running", found.get().name)
        assertEquals(5.5, found.get().measurement, 0.01)
        assertEquals("km", found.get().measurementUnit)
        assertEquals(LocalTime.of(8, 0), found.get().startTime)
        assertEquals(LocalTime.of(9, 0), found.get().endTime)
        assertEquals(LocalDate.of(2024, 6, 15), found.get().date)
        assertEquals("Morning run", found.get().notes)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val log = createSportLog(userId = "u1")
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindByIdWithNullTimesAndNotes() {
        val log = createSportLog(startTime = null, endTime = null, notes = null)
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNull(found.get().startTime)
        assertNull(found.get().endTime)
        assertNull(found.get().notes)
    }

    @Test
    fun testFindAll() {
        val log1 = createSportLog("Running")
        val log2 = createSportLog("Swimming")
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
        val log = createSportLog()
        repository.save(log)

        assertTrue(repository.findById(log.id).isPresent)

        repository.deleteById(log.id)

        assertFalse(repository.findById(log.id).isPresent)
    }

    @Test
    fun testExistsByIdTrue() {
        val log = createSportLog()
        repository.save(log)

        assertTrue(repository.existsById(log.id))
    }

    @Test
    fun testExistsByIdFalse() {
        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() {
        val log1 = createSportLog("Running", userId = "u1")
        val log2 = createSportLog("Swimming", userId = "u1")
        val log3 = createSportLog("Cycling", userId = "u2")
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
    fun testFindDistinctNamesByUserId() {
        val log1 = createSportLog("Running", userId = "u1")
        val log2 = createSportLog("Swimming", userId = "u1")
        repository.save(log1)
        repository.save(log2)

        val names = repository.findDistinctNamesByUserId("u1")
        assertEquals(2, names.size)
        assertTrue(names.containsAll(listOf("Running", "Swimming")))
    }

    @Test
    fun testFindDistinctNamesByUserIdEmpty() {
        val result = repository.findDistinctNamesByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindDistinctMeasurementUnitsByUserId() {
        val log1 = createSportLog("Running", measurementUnit = "km", userId = "u1")
        val log2 = createSportLog("Swimming", measurementUnit = "m", userId = "u1")
        repository.save(log1)
        repository.save(log2)

        val units = repository.findDistinctMeasurementUnitsByUserId("u1")
        assertEquals(2, units.size)
        assertTrue(units.containsAll(listOf("km", "m")))
    }

    @Test
    fun testFindDistinctMeasurementUnitsByUserIdEmpty() {
        val result = repository.findDistinctMeasurementUnitsByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSaveWithNullTimesDoesNotThrow() {
        val log = createSportLog(startTime = null, endTime = null)
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertEquals("Running", found.get().name)
    }
}
