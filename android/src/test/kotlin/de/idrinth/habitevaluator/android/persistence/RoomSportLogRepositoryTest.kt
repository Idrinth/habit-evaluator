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
import java.time.LocalDate
import java.time.LocalTime

class RoomSportLogRepositoryTest {

    private lateinit var dao: SportLogDao
    private lateinit var repository: RoomSportLogRepository

    @BeforeEach
    fun setUp() {
        dao = mock(SportLogDao::class.java)
        repository = RoomSportLogRepository(dao)
    }

    private fun createEntity(
        id: String = "sl1",
        name: String = "Running",
        measurement: Double? = 5.0,
        measurementUnit: String? = "km",
        startTime: String? = "08:00",
        endTime: String? = "09:00",
        date: String = "2024-06-15",
        notes: String? = null,
        userId: String = "u1"
    ) = SportLogEntity(
        id = id, name = name, measurement = measurement,
        measurementUnit = measurementUnit, startTime = startTime,
        endTime = endTime, date = date, createdAt = "2024-06-15T10:00:00",
        notes = notes, userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsEntry() = runTest {
        val log = de.idrinth.habitevaluator.shared.model.SportLog()
        log.id = "sl1"
        log.name = "Running"
        log.measurement = 5.0
        log.measurementUnit = "km"
        log.date = LocalDate.of(2024, 6, 15)
        log.createdAt = java.time.LocalDateTime.now()
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        log.user = user

        val result = repository.save(log)
        assertEquals("sl1", result.id)
        verify(dao).insert(any(SportLogEntity::class.java) ?: createEntity())
    }

    @Test
    fun testFindByIdReturnsEntry() = runTest {
        val entity = createEntity(
            name = "Running", measurement = 5.5, measurementUnit = "km",
            startTime = "08:00", endTime = "09:00", date = "2024-06-15", notes = "Morning run"
        )
        `when`(dao.findById("sl1")).thenReturn(entity)

        val found = repository.findById("sl1")
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
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createEntity()
        `when`(dao.findById("sl1")).thenReturn(entity)

        val found = repository.findById("sl1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindByIdWithNullTimesAndNotes() = runTest {
        val entity = createEntity(startTime = null, endTime = null, notes = null)
        `when`(dao.findById("sl1")).thenReturn(entity)

        val found = repository.findById("sl1")
        assertTrue(found.isPresent)
        assertNull(found.get().startTime)
        assertNull(found.get().endTime)
        assertNull(found.get().notes)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntity(id = "sl1", name = "Running")
        val e2 = createEntity(id = "sl2", name = "Swimming")
        `when`(dao.findAll()).thenReturn(listOf(e1, e2))

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
        repository.deleteById("sl1")
        verify(dao).deleteById("sl1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.existsById("sl1")).thenReturn(true)

        assertTrue(repository.existsById("sl1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.existsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createEntity(id = "sl1", name = "Running", userId = "u1")
        val e2 = createEntity(id = "sl2", name = "Swimming", userId = "u1")
        `when`(dao.findByUserId("u1")).thenReturn(listOf(e1, e2))

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
    fun testFindDistinctNamesByUserId() = runTest {
        `when`(dao.findDistinctNames("u1")).thenReturn(listOf("Running", "Swimming"))

        val names = repository.findDistinctNamesByUserId("u1")
        assertEquals(2, names.size)
        assertTrue(names.containsAll(listOf("Running", "Swimming")))
    }

    @Test
    fun testFindDistinctNamesByUserIdEmpty() = runTest {
        `when`(dao.findDistinctNames("u99")).thenReturn(emptyList())

        val result = repository.findDistinctNamesByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindDistinctMeasurementUnitsByUserId() = runTest {
        `when`(dao.findDistinctMeasurementUnits("u1")).thenReturn(listOf("km", "m"))

        val units = repository.findDistinctMeasurementUnitsByUserId("u1")
        assertEquals(2, units.size)
        assertTrue(units.containsAll(listOf("km", "m")))
    }

    @Test
    fun testFindDistinctMeasurementUnitsByUserIdEmpty() = runTest {
        `when`(dao.findDistinctMeasurementUnits("u99")).thenReturn(emptyList())

        val result = repository.findDistinctMeasurementUnitsByUserId("u99")
        assertTrue(result.isEmpty())
    }
}
