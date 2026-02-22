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
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomMedicationLogRepositoryTest {

    private lateinit var dao: MedicationDao
    private lateinit var repository: RoomMedicationLogRepository

    @BeforeEach
    fun setUp() {
        dao = mock(MedicationDao::class.java)
        repository = RoomMedicationLogRepository(dao)
    }

    private fun createMedicationEntity(
        id: String = "med1",
        name: String = "Ibuprofen",
        userId: String = "u1"
    ) = MedicationEntity(
        id = id, name = name, wikipediaLink = null,
        provisionType = "PILL", userId = userId, userName = "testuser"
    )

    private fun createLogEntity(
        id: String = "ml1",
        medicationId: String = "med1",
        amount: Double = 400.0,
        notes: String? = null,
        userId: String = "u1"
    ) = MedicationLogEntity(
        id = id, medicationId = medicationId, amount = amount,
        takenAt = "2024-06-15T08:00:00", createdAt = "2024-06-15T08:00:00",
        notes = notes, userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsLog() = runTest {
        val med = de.idrinth.habitevaluator.shared.model.Medication()
        med.id = "med1"
        med.name = "Ibuprofen"
        med.provisionType = de.idrinth.habitevaluator.shared.model.MedicationProvisionType.PILL
        val log = de.idrinth.habitevaluator.shared.model.MedicationLog()
        log.id = "ml1"
        log.medication = med
        log.amount = 400.0
        log.takenAt = java.time.LocalDateTime.of(2024, 6, 15, 8, 0)
        log.createdAt = java.time.LocalDateTime.now()
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        log.user = user

        val result = repository.save(log)
        assertEquals("ml1", result.id)
        verify(dao).insertLog(any(MedicationLogEntity::class.java) ?: createLogEntity())
    }

    @Test
    fun testFindByIdReturnsLogWithMedication() = runTest {
        val logEntity = createLogEntity(amount = 400.0, notes = "Before meal")
        val medEntity = createMedicationEntity()
        `when`(dao.findLogById("ml1")).thenReturn(logEntity)
        `when`(dao.findById("med1")).thenReturn(medEntity)

        val found = repository.findById("ml1")
        assertTrue(found.isPresent)
        assertEquals(400.0, found.get().amount, 0.01)
        assertEquals("Before meal", found.get().notes)
        assertNotNull(found.get().medication)
        assertEquals("med1", found.get().medication.id)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findLogById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithoutMedication() = runTest {
        val logEntity = createLogEntity()
        `when`(dao.findLogById("ml1")).thenReturn(logEntity)
        `when`(dao.findById("med1")).thenReturn(null)

        val found = repository.findById("ml1")
        assertTrue(found.isPresent)
        assertNull(found.get().medication)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val logEntity = createLogEntity()
        val medEntity = createMedicationEntity()
        `when`(dao.findLogById("ml1")).thenReturn(logEntity)
        `when`(dao.findById("med1")).thenReturn(medEntity)

        val found = repository.findById("ml1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() = runTest {
        val l1 = createLogEntity(id = "ml1", amount = 200.0)
        val l2 = createLogEntity(id = "ml2", amount = 400.0)
        val medEntity = createMedicationEntity()
        `when`(dao.findAllLogs()).thenReturn(listOf(l1, l2))
        `when`(dao.findById(anyString())).thenReturn(medEntity)

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllLogs()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("ml1")
        verify(dao).deleteLogById("ml1")
    }

    @Test
    fun testFindByUserId() = runTest {
        val medEntity = createMedicationEntity(userId = "u1")
        val l1 = createLogEntity(id = "ml1", userId = "u1")
        val l2 = createLogEntity(id = "ml2", userId = "u1")
        `when`(dao.findByUserId("u1")).thenReturn(listOf(medEntity))
        `when`(dao.findLogsByUserId("u1")).thenReturn(listOf(l1, l2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findByUserId("u99")).thenReturn(emptyList())
        `when`(dao.findLogsByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByUserIdPagedEmpty() = runTest {
        `when`(dao.findByUserId("u1")).thenReturn(emptyList())
        `when`(dao.findLogsByUserIdPaged("u1", 10, 0)).thenReturn(emptyList())

        val result = repository.findByUserIdPaged("u1", 10, 0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByUserIdPagedReturnsLimitedResults() = runTest {
        val medEntity = createMedicationEntity(userId = "u1")
        val logs = (1..3).map { createLogEntity(id = "ml$it", amount = it.toDouble(), userId = "u1") }
        `when`(dao.findByUserId("u1")).thenReturn(listOf(medEntity))
        `when`(dao.findLogsByUserIdPaged("u1", 3, 0)).thenReturn(logs)

        val result = repository.findByUserIdPaged("u1", 3, 0)
        assertEquals(3, result.size)
    }

    @Test
    fun testCountByUserIdZero() = runTest {
        `when`(dao.countLogsByUserId("u1")).thenReturn(0)

        assertEquals(0, repository.countByUserId("u1"))
    }

    @Test
    fun testCountByUserIdWithResults() = runTest {
        `when`(dao.countLogsByUserId("u1")).thenReturn(2)

        assertEquals(2, repository.countByUserId("u1"))
    }
}
