package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.DiaryEntry
import de.idrinth.habitevaluator.shared.model.DiaryReference
import de.idrinth.habitevaluator.shared.model.EventSignificance
import de.idrinth.habitevaluator.shared.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomDiaryEntryRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomDiaryEntryRepository
    private lateinit var referenceRepository: RoomDiaryReferenceRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomDiaryEntryRepository(database.diaryDao())
        referenceRepository = RoomDiaryReferenceRepository(database.diaryDao())
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

    private fun createReference(
        description: String = "Morning workout",
        userId: String = "u1"
    ): DiaryReference {
        val ref = DiaryReference()
        ref.id = UUID.randomUUID().toString()
        ref.description = description
        ref.user = createUser(userId)
        return ref
    }

    private fun createEntry(
        significance: EventSignificance = EventSignificance.MAJOR,
        eventDate: LocalDate = LocalDate.of(2024, 6, 15),
        userId: String = "u1",
        reference: DiaryReference? = null
    ): DiaryEntry {
        val entry = DiaryEntry()
        entry.id = UUID.randomUUID().toString()
        entry.significance = significance
        entry.eventDate = eventDate
        entry.createdAt = LocalDateTime.now()
        entry.user = createUser(userId)
        entry.diaryReference = reference
        return entry
    }

    @Test
    fun testSaveReturnsEntry() {
        val ref = createReference()
        referenceRepository.save(ref)
        val entry = createEntry(reference = ref)
        val result = repository.save(entry)
        assertEquals(entry.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val ref = createReference()
        referenceRepository.save(ref)
        val entry = createEntry(
            significance = EventSignificance.MAJOR,
            eventDate = LocalDate.of(2024, 6, 15),
            reference = ref
        )
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertEquals(EventSignificance.MAJOR, found.get().significance)
        assertEquals(LocalDate.of(2024, 6, 15), found.get().eventDate)
        assertNotNull(found.get().diaryReference)
        assertEquals("Morning workout", found.get().diaryReference.description)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val ref = createReference(userId = "u1")
        referenceRepository.save(ref)
        val entry = createEntry(userId = "u1", reference = ref)
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindAll() {
        val ref = createReference()
        referenceRepository.save(ref)
        val entry1 = createEntry(reference = ref)
        val entry2 = createEntry(reference = ref)
        repository.save(entry1)
        repository.save(entry2)

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
        val ref = createReference()
        referenceRepository.save(ref)
        val entry = createEntry(reference = ref)
        repository.save(entry)

        assertTrue(repository.findById(entry.id).isPresent)

        repository.deleteById(entry.id)

        assertFalse(repository.findById(entry.id).isPresent)
    }

    @Test
    fun testFindByUserId() {
        val ref1 = createReference(userId = "u1")
        val ref2 = createReference(userId = "u2")
        referenceRepository.save(ref1)
        referenceRepository.save(ref2)
        val entry1 = createEntry(userId = "u1", reference = ref1)
        val entry2 = createEntry(userId = "u1", reference = ref1)
        val entry3 = createEntry(userId = "u2", reference = ref2)
        repository.save(entry1)
        repository.save(entry2)
        repository.save(entry3)

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
    fun testSaveWithStartAndEndTime() {
        val ref = createReference()
        referenceRepository.save(ref)
        val entry = createEntry(reference = ref)
        entry.startTime = LocalTime.of(10, 0)
        entry.endTime = LocalTime.of(11, 0)
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertEquals(LocalTime.of(10, 0), found.get().startTime)
        assertEquals(LocalTime.of(11, 0), found.get().endTime)
    }

    @Test
    fun testFindDistinctDescriptionsByUserId() {
        val ref1 = createReference("Workout", "u1")
        val ref2 = createReference("Meditation", "u1")
        referenceRepository.save(ref1)
        referenceRepository.save(ref2)
        val entry1 = createEntry(userId = "u1", reference = ref1)
        val entry2 = createEntry(userId = "u1", reference = ref2)
        repository.save(entry1)
        repository.save(entry2)

        val descriptions = repository.findDistinctDescriptionsByUserId("u1")
        assertEquals(2, descriptions.size)
        assertTrue(descriptions.containsAll(listOf("Workout", "Meditation")))
    }

    @Test
    fun testFindEntriesNeedingMigration() {
        val ref = createReference()
        referenceRepository.save(ref)
        val entry = createEntry(reference = ref)
        repository.save(entry)

        // Entries saved with a reference don't need migration
        val needsMigration = repository.findEntriesNeedingMigration("u1")
        assertTrue(needsMigration.isEmpty())
    }
}
