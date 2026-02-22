package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.SleepEntry
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
class RoomSleepEntryRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomSleepEntryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomSleepEntryRepository(database.sleepEntryDao())
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

    private fun createSleepEntry(
        fromTime: LocalTime = LocalTime.of(23, 0),
        untilTime: LocalTime = LocalTime.of(7, 0),
        date: LocalDate = LocalDate.of(2024, 6, 15),
        notes: String? = null,
        userId: String = "u1"
    ): SleepEntry {
        val entry = SleepEntry()
        entry.id = UUID.randomUUID().toString()
        entry.fromTime = fromTime
        entry.untilTime = untilTime
        entry.date = date
        entry.createdAt = LocalDateTime.now()
        entry.notes = notes
        entry.user = createUser(userId)
        return entry
    }

    @Test
    fun testSaveReturnsEntry() {
        val entry = createSleepEntry()
        val result = repository.save(entry)
        assertEquals(entry.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val entry = createSleepEntry(
            fromTime = LocalTime.of(23, 0),
            untilTime = LocalTime.of(7, 0),
            date = LocalDate.of(2024, 6, 15),
            notes = "Good sleep"
        )
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertEquals(LocalTime.of(23, 0), found.get().fromTime)
        assertEquals(LocalTime.of(7, 0), found.get().untilTime)
        assertEquals(LocalDate.of(2024, 6, 15), found.get().date)
        assertEquals("Good sleep", found.get().notes)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val entry = createSleepEntry(userId = "u1")
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindByIdWithNullNotes() {
        val entry = createSleepEntry(notes = null)
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertNull(found.get().notes)
    }

    @Test
    fun testFindAll() {
        val entry1 = createSleepEntry(date = LocalDate.of(2024, 6, 14))
        val entry2 = createSleepEntry(date = LocalDate.of(2024, 6, 15))
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
        val entry = createSleepEntry()
        repository.save(entry)

        assertTrue(repository.findById(entry.id).isPresent)

        repository.deleteById(entry.id)

        assertFalse(repository.findById(entry.id).isPresent)
    }

    @Test
    fun testExistsByIdTrue() {
        val entry = createSleepEntry()
        repository.save(entry)

        assertTrue(repository.existsById(entry.id))
    }

    @Test
    fun testExistsByIdFalse() {
        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() {
        val entry1 = createSleepEntry(date = LocalDate.of(2024, 6, 14), userId = "u1")
        val entry2 = createSleepEntry(date = LocalDate.of(2024, 6, 15), userId = "u1")
        val entry3 = createSleepEntry(date = LocalDate.of(2024, 6, 16), userId = "u2")
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
    fun testSaveWithNotes() {
        val entry = createSleepEntry(notes = "Slept well")
        repository.save(entry)

        val found = repository.findById(entry.id)
        assertTrue(found.isPresent)
        assertEquals("Slept well", found.get().notes)
    }
}
