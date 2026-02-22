package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.FrequencyType
import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.HabitEntry
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
class RoomHabitRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomHabitRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomHabitRepository(database.habitDao())
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

    private fun createHabit(
        name: String = "Exercise",
        description: String = "Daily exercise",
        frequencyType: FrequencyType = FrequencyType.DAILY,
        userId: String = "u1"
    ): Habit {
        val habit = Habit()
        habit.id = UUID.randomUUID().toString()
        habit.name = name
        habit.description = description
        habit.frequencyType = frequencyType
        habit.targetFrequency = 1
        habit.maxEntriesPerDay = 1
        habit.isPositiveScoring = true
        habit.user = createUser(userId)
        return habit
    }

    @Test
    fun testSaveReturnsHabit() {
        val habit = createHabit()
        val result = repository.save(habit)
        assertEquals(habit.id, result.id)
        assertEquals(habit.name, result.name)
    }

    @Test
    fun testSaveAndFindById() {
        val habit = createHabit("Running", "Morning run")
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertEquals("Running", found.get().name)
        assertEquals("Morning run", found.get().description)
        assertEquals(FrequencyType.DAILY, found.get().frequencyType)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val habit = createHabit(userId = "u1")
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() {
        val habit1 = createHabit("Habit A")
        val habit2 = createHabit("Habit B")
        repository.save(habit1)
        repository.save(habit2)

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
        val habit = createHabit()
        repository.save(habit)

        assertTrue(repository.findById(habit.id).isPresent)

        repository.deleteById(habit.id)

        assertFalse(repository.findById(habit.id).isPresent)
    }

    @Test
    fun testDeleteByIdRemovesEntries() {
        val habit = createHabit()
        val entry = HabitEntry(habit.id)
        entry.id = UUID.randomUUID().toString()
        entry.completedAt = LocalDateTime.of(2024, 1, 15, 10, 0)
        habit.addEntry(entry)
        repository.save(habit)

        repository.deleteById(habit.id)

        assertFalse(repository.findById(habit.id).isPresent)
    }

    @Test
    fun testExistsByIdTrue() {
        val habit = createHabit()
        repository.save(habit)

        assertTrue(repository.existsById(habit.id))
    }

    @Test
    fun testExistsByIdFalse() {
        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() {
        val habit1 = createHabit("Habit A", userId = "u1")
        val habit2 = createHabit("Habit B", userId = "u1")
        val habit3 = createHabit("Habit C", userId = "u2")
        repository.save(habit1)
        repository.save(habit2)
        repository.save(habit3)

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
    fun testSaveWithEntries() {
        val habit = createHabit()
        val entry = HabitEntry(habit.id)
        entry.id = UUID.randomUUID().toString()
        entry.completedAt = LocalDateTime.of(2024, 1, 15, 10, 0)
        habit.addEntry(entry)
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertEquals(1, found.get().entries.size)
        assertEquals(entry.id, found.get().entries[0].id)
    }

    @Test
    fun testSaveWithNameTranslations() {
        val habit = createHabit()
        habit.nameTranslations = hashMapOf("de" to "Ubung")
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().nameTranslations)
        assertEquals("Ubung", found.get().nameTranslations["de"])
    }

    @Test
    fun testSaveWithDescriptionTranslations() {
        val habit = createHabit()
        habit.descriptionTranslations = hashMapOf("de" to "Tagliche Ubung")
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().descriptionTranslations)
        assertEquals("Tagliche Ubung", found.get().descriptionTranslations["de"])
    }

    @Test
    fun testSaveWithWeeklyFrequencyType() {
        val habit = createHabit(frequencyType = FrequencyType.WEEKLY)
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertEquals(FrequencyType.WEEKLY, found.get().frequencyType)
    }

    @Test
    fun testSaveWithMonthlyFrequencyType() {
        val habit = createHabit(frequencyType = FrequencyType.MONTHLY)
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertEquals(FrequencyType.MONTHLY, found.get().frequencyType)
    }

    @Test
    fun testSaveUpdatesExistingHabit() {
        val habit = createHabit("Original Name")
        repository.save(habit)

        habit.name = "Updated Name"
        repository.save(habit)

        val found = repository.findById(habit.id)
        assertTrue(found.isPresent)
        assertEquals("Updated Name", found.get().name)
    }
}
