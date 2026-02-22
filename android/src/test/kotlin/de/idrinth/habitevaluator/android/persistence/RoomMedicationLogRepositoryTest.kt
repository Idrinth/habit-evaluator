package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.Medication
import de.idrinth.habitevaluator.shared.model.MedicationLog
import de.idrinth.habitevaluator.shared.model.MedicationProvisionType
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
class RoomMedicationLogRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomMedicationLogRepository
    private lateinit var medicationRepository: RoomMedicationRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomMedicationLogRepository(database.medicationDao())
        medicationRepository = RoomMedicationRepository(database.medicationDao())
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

    private fun createMedication(userId: String = "u1"): Medication {
        val medication = Medication()
        medication.id = UUID.randomUUID().toString()
        medication.name = "Ibuprofen"
        medication.provisionType = MedicationProvisionType.PILL
        medication.user = createUser(userId)
        return medication
    }

    private fun createLog(
        medication: Medication,
        amount: Double = 400.0,
        takenAt: LocalDateTime = LocalDateTime.of(2024, 6, 15, 8, 0),
        notes: String? = null,
        userId: String = "u1"
    ): MedicationLog {
        val log = MedicationLog()
        log.id = UUID.randomUUID().toString()
        log.medication = medication
        log.amount = amount
        log.takenAt = takenAt
        log.createdAt = LocalDateTime.now()
        log.notes = notes
        log.user = createUser(userId)
        return log
    }

    @Test
    fun testSaveReturnsLog() {
        val medication = createMedication()
        medicationRepository.save(medication)
        val log = createLog(medication)
        val result = repository.save(log)
        assertEquals(log.id, result.id)
    }

    @Test
    fun testSaveAndFindByIdWithMedication() {
        val medication = createMedication()
        medicationRepository.save(medication)
        val log = createLog(medication, amount = 400.0, notes = "Before meal")
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertEquals(400.0, found.get().amount, 0.01)
        assertEquals("Before meal", found.get().notes)
        assertNotNull(found.get().medication)
        assertEquals(medication.id, found.get().medication.id)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithoutMedication() {
        val medication = createMedication()
        // Don't save medication to database
        val log = createLog(medication)
        repository.save(log)

        val found = repository.findById(log.id)
        // Log exists but medication is null (not found in DB)
        assertTrue(found.isPresent)
        assertNull(found.get().medication)
    }

    @Test
    fun testFindByIdWithUser() {
        val medication = createMedication("u1")
        medicationRepository.save(medication)
        val log = createLog(medication, userId = "u1")
        repository.save(log)

        val found = repository.findById(log.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() {
        val medication = createMedication()
        medicationRepository.save(medication)
        val log1 = createLog(medication, amount = 200.0)
        val log2 = createLog(medication, amount = 400.0)
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
        val medication = createMedication()
        medicationRepository.save(medication)
        val log = createLog(medication)
        repository.save(log)

        assertTrue(repository.findById(log.id).isPresent)

        repository.deleteById(log.id)

        assertFalse(repository.findById(log.id).isPresent)
    }

    @Test
    fun testFindByUserId() {
        val medication = createMedication()
        medicationRepository.save(medication)
        val log1 = createLog(medication, userId = "u1")
        val log2 = createLog(medication, userId = "u1")
        val log3 = createLog(medication, userId = "u2")
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
    fun testFindByUserIdPagedEmpty() {
        val result = repository.findByUserIdPaged("u1", 10, 0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByUserIdPagedReturnsLimitedResults() {
        val medication = createMedication()
        medicationRepository.save(medication)
        for (i in 1..5) {
            val log = createLog(medication, amount = i.toDouble(), userId = "u1")
            repository.save(log)
        }

        val result = repository.findByUserIdPaged("u1", 3, 0)
        assertEquals(3, result.size)
    }

    @Test
    fun testCountByUserIdZero() {
        assertEquals(0, repository.countByUserId("u1"))
    }

    @Test
    fun testCountByUserIdWithResults() {
        val medication = createMedication()
        medicationRepository.save(medication)
        val log1 = createLog(medication, userId = "u1")
        val log2 = createLog(medication, userId = "u1")
        repository.save(log1)
        repository.save(log2)

        assertEquals(2, repository.countByUserId("u1"))
    }
}
