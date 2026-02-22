package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.MedicationProvisionType
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

class RoomMedicationRepositoryTest {

    private lateinit var dao: MedicationDao
    private lateinit var repository: RoomMedicationRepository

    @BeforeEach
    fun setUp() {
        dao = mock(MedicationDao::class.java)
        repository = RoomMedicationRepository(dao)
    }

    private fun createEntity(
        id: String = "m1",
        name: String = "Ibuprofen",
        provisionType: String = "PILL",
        wikipediaLink: String? = null,
        userId: String = "u1"
    ) = MedicationEntity(
        id = id, name = name, wikipediaLink = wikipediaLink,
        provisionType = provisionType, userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsMedication() = runTest {
        val medication = de.idrinth.habitevaluator.shared.model.Medication()
        medication.id = "m1"
        medication.name = "Ibuprofen"
        medication.provisionType = MedicationProvisionType.PILL
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        medication.user = user

        val result = repository.save(medication)
        assertEquals("m1", result.id)
        assertEquals("Ibuprofen", result.name)
        verify(dao).insert(any(MedicationEntity::class.java) ?: createEntity())
    }

    @Test
    fun testFindByIdReturnsMedication() = runTest {
        val entity = createEntity(
            name = "Ibuprofen", provisionType = "PILL",
            wikipediaLink = "https://en.wikipedia.org/wiki/Ibuprofen"
        )
        `when`(dao.findById("m1")).thenReturn(entity)

        val found = repository.findById("m1")
        assertTrue(found.isPresent)
        assertEquals("Ibuprofen", found.get().name)
        assertEquals(MedicationProvisionType.PILL, found.get().provisionType)
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", found.get().wikipediaLink)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithDefaultProvisionType() = runTest {
        // Entity stored with PILL provisionType (mapper default when null)
        val entity = createEntity(provisionType = "PILL")
        `when`(dao.findById("m1")).thenReturn(entity)

        val found = repository.findById("m1")
        assertTrue(found.isPresent)
        assertEquals(MedicationProvisionType.PILL, found.get().provisionType)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createEntity()
        `when`(dao.findById("m1")).thenReturn(entity)

        val found = repository.findById("m1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createEntity(id = "m1", name = "Ibuprofen")
        val e2 = createEntity(id = "m2", name = "Aspirin")
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
        repository.deleteById("m1")
        verify(dao).deleteById("m1")
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createEntity(id = "m1", name = "Ibuprofen", userId = "u1")
        val e2 = createEntity(id = "m2", name = "Aspirin", userId = "u1")
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
    fun testFindByIdWithLiquidDropsProvisionType() = runTest {
        val entity = createEntity(name = "Eye Drops", provisionType = "LIQUID_DROPS")
        `when`(dao.findById("m1")).thenReturn(entity)

        val found = repository.findById("m1")
        assertTrue(found.isPresent)
        assertEquals(MedicationProvisionType.LIQUID_DROPS, found.get().provisionType)
    }

    @Test
    fun testFindByIdWithLiquidMlProvisionType() = runTest {
        val entity = createEntity(name = "Cough Syrup", provisionType = "LIQUID_ML")
        `when`(dao.findById("m1")).thenReturn(entity)

        val found = repository.findById("m1")
        assertTrue(found.isPresent)
        assertEquals(MedicationProvisionType.LIQUID_ML, found.get().provisionType)
    }

    @Test
    fun testSaveUpdatesExistingMedication() {
        val medication = de.idrinth.habitevaluator.shared.model.Medication()
        medication.id = "m1"
        medication.name = "Ibuprofen"
        medication.provisionType = MedicationProvisionType.PILL
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        medication.user = user

        repository.save(medication)
        medication.name = "Ibuprofen 400mg"
        medication.wikipediaLink = "https://en.wikipedia.org/wiki/Ibuprofen"
        val result = repository.save(medication)

        assertEquals("Ibuprofen 400mg", result.name)
        assertEquals("https://en.wikipedia.org/wiki/Ibuprofen", result.wikipediaLink)
    }
}
