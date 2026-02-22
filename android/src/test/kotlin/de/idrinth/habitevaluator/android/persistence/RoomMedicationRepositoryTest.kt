package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.Medication
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
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomMedicationRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomMedicationRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomMedicationRepository(database.medicationDao())
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

    private fun createMedication(
        name: String = "Ibuprofen",
        provisionType: MedicationProvisionType? = MedicationProvisionType.PILL,
        wikipediaLink: String? = null,
        userId: String = "u1"
    ): Medication {
        val medication = Medication()
        medication.id = UUID.randomUUID().toString()
        medication.name = name
        medication.provisionType = provisionType
        medication.wikipediaLink = wikipediaLink
        medication.user = createUser(userId)
        return medication
    }

    @Test
    fun testSaveReturnsMedication() {
        val medication = createMedication()
        val result = repository.save(medication)
        assertEquals(medication.id, result.id)
        assertEquals(medication.name, result.name)
    }

    @Test
    fun testSaveAndFindById() {
        val medication = createMedication(
            name = "Ibuprofen",
            provisionType = MedicationProvisionType.PILL,
            wikipediaLink = "https://en.wikipedia.org/wiki/Ibuprofen"
        )
        repository.save(medication)

        val found = repository.findById(medication.id)
        assertTrue(found.isPresent)
        assertEquals("Ibuprofen", found.get().name)
        assertEquals(MedicationProvisionType.PILL, found.get().provisionType)
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", found.get().wikipediaLink)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithDefaultProvisionTypeWhenNullInput() {
        // When provisionType is null, the mapper defaults to PILL during storage
        val medication = createMedication(provisionType = null)
        repository.save(medication)

        val found = repository.findById(medication.id)
        assertTrue(found.isPresent)
        // Mapper defaults null provisionType to PILL (see toEntity())
        assertEquals(MedicationProvisionType.PILL, found.get().provisionType)
    }

    @Test
    fun testFindByIdWithUser() {
        val medication = createMedication(userId = "u1")
        repository.save(medication)

        val found = repository.findById(medication.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() {
        val med1 = createMedication("Ibuprofen")
        val med2 = createMedication("Aspirin")
        repository.save(med1)
        repository.save(med2)

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
        repository.save(medication)

        assertTrue(repository.findById(medication.id).isPresent)

        repository.deleteById(medication.id)

        assertFalse(repository.findById(medication.id).isPresent)
    }

    @Test
    fun testFindByUserId() {
        val med1 = createMedication("Ibuprofen", userId = "u1")
        val med2 = createMedication("Aspirin", userId = "u1")
        val med3 = createMedication("Paracetamol", userId = "u2")
        repository.save(med1)
        repository.save(med2)
        repository.save(med3)

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
    fun testFindByIdWithLiquidDropsProvisionType() {
        val medication = createMedication(
            name = "Eye Drops",
            provisionType = MedicationProvisionType.LIQUID_DROPS
        )
        repository.save(medication)

        val found = repository.findById(medication.id)
        assertTrue(found.isPresent)
        assertEquals(MedicationProvisionType.LIQUID_DROPS, found.get().provisionType)
    }

    @Test
    fun testFindByIdWithLiquidMlProvisionType() {
        val medication = createMedication(
            name = "Cough Syrup",
            provisionType = MedicationProvisionType.LIQUID_ML
        )
        repository.save(medication)

        val found = repository.findById(medication.id)
        assertTrue(found.isPresent)
        assertEquals(MedicationProvisionType.LIQUID_ML, found.get().provisionType)
    }

    @Test
    fun testSaveUpdatesExistingMedication() {
        val medication = createMedication("Ibuprofen")
        repository.save(medication)

        medication.name = "Ibuprofen 400mg"
        medication.wikipediaLink = "https://en.wikipedia.org/wiki/Ibuprofen"
        repository.save(medication)

        val found = repository.findById(medication.id)
        assertTrue(found.isPresent)
        assertEquals("Ibuprofen 400mg", found.get().name)
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", found.get().wikipediaLink)
    }
}
