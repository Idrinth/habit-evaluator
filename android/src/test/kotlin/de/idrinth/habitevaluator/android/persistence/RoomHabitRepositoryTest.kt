package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.FrequencyType
import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.HabitEntry
import de.idrinth.habitevaluator.shared.model.User
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyList
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.UUID

class RoomHabitRepositoryTest {

    private lateinit var dao: HabitDao
    private lateinit var repository: RoomHabitRepository

    @BeforeEach
    fun setUp() {
        dao = mock(HabitDao::class.java)
        repository = RoomHabitRepository(dao)
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
    fun testSaveCallsDaoSaveWithDetails() = runTest {
        val habit = createHabit()
        repository.save(habit)
        verify(dao).saveWithDetails(
            any(HabitEntity::class.java) ?: habit.toEntity(),
            anyList(),
            anyList(),
            anyList()
        )
    }

    @Test
    fun testFindByIdReturnsHabitWhenFound() = runTest {
        val habitId = "h1"
        val entity = HabitEntity(
            id = habitId, name = "Running", description = "Morning run",
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = "2024-01-15T10:00:00",
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findById(habitId)).thenReturn(entity)
        `when`(dao.findEntriesByHabitId(habitId)).thenReturn(emptyList())
        `when`(dao.findNameTranslations(habitId)).thenReturn(emptyList())
        `when`(dao.findDescTranslations(habitId)).thenReturn(emptyList())

        val result = repository.findById(habitId)
        assertTrue(result.isPresent)
        assertEquals("Running", result.get().name)
        assertEquals("Morning run", result.get().description)
    }

    @Test
    fun testFindByIdReturnsEmptyWhenNotFound() = runTest {
        `when`(dao.findById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdReturnsUserInfo() = runTest {
        val habitId = "h1"
        val entity = HabitEntity(
            id = habitId, name = "Test", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findById(habitId)).thenReturn(entity)
        `when`(dao.findEntriesByHabitId(habitId)).thenReturn(emptyList())
        `when`(dao.findNameTranslations(habitId)).thenReturn(emptyList())
        `when`(dao.findDescTranslations(habitId)).thenReturn(emptyList())

        val result = repository.findById(habitId)
        assertTrue(result.isPresent)
        assertNotNull(result.get().user)
        assertEquals("u1", result.get().user.id)
        assertEquals("testuser", result.get().user.username)
    }

    @Test
    fun testFindAllReturnsAllHabits() = runTest {
        val entity1 = HabitEntity(
            id = "h1", name = "Habit A", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        val entity2 = HabitEntity(
            id = "h2", name = "Habit B", description = null,
            categoryId = null, frequencyType = "WEEKLY", targetFrequency = 3,
            maxEntriesPerDay = 1, positiveScoring = 0, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findAll()).thenReturn(listOf(entity1, entity2))
        `when`(dao.findEntriesByHabitId(anyString())).thenReturn(emptyList())
        `when`(dao.findNameTranslations(anyString())).thenReturn(emptyList())
        `when`(dao.findDescTranslations(anyString())).thenReturn(emptyList())

        val result = repository.findAll()
        assertEquals(2, result.size)
    }

    @Test
    fun testFindAllReturnsEmptyList() = runTest {
        `when`(dao.findAll()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteByIdCallsDao() = runTest {
        repository.deleteById("h1")
        verify(dao).deleteWithDetails("h1")
    }

    @Test
    fun testExistsByIdReturnsTrue() = runTest {
        `when`(dao.existsById("h1")).thenReturn(true)

        assertTrue(repository.existsById("h1"))
    }

    @Test
    fun testExistsByIdReturnsFalse() = runTest {
        `when`(dao.existsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserIdReturnsMatchingHabits() = runTest {
        val entity = HabitEntity(
            id = "h1", name = "Habit A", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findByUserId("u1")).thenReturn(listOf(entity))
        `when`(dao.findEntriesByHabitId("h1")).thenReturn(emptyList())
        `when`(dao.findNameTranslations("h1")).thenReturn(emptyList())
        `when`(dao.findDescTranslations("h1")).thenReturn(emptyList())

        val result = repository.findByUserId("u1")
        assertEquals(1, result.size)
        assertEquals("u1", result[0].user.id)
    }

    @Test
    fun testFindByUserIdReturnsEmptyList() = runTest {
        `when`(dao.findByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByIdWithEntries() = runTest {
        val habitId = "h1"
        val entity = HabitEntity(
            id = habitId, name = "Test", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        val entryEntity = HabitEntryEntity(
            id = "e1", habitId = habitId,
            completedAt = "2024-01-15T10:00:00", notes = null, value = 1.0
        )
        `when`(dao.findById(habitId)).thenReturn(entity)
        `when`(dao.findEntriesByHabitId(habitId)).thenReturn(listOf(entryEntity))
        `when`(dao.findNameTranslations(habitId)).thenReturn(emptyList())
        `when`(dao.findDescTranslations(habitId)).thenReturn(emptyList())

        val result = repository.findById(habitId)
        assertTrue(result.isPresent)
        assertEquals(1, result.get().entries.size)
        assertEquals("e1", result.get().entries[0].id)
    }

    @Test
    fun testFindByIdWithNameTranslations() = runTest {
        val habitId = "h1"
        val entity = HabitEntity(
            id = habitId, name = "Exercise", description = null,
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        val nameTranslation = HabitNameTranslationEntity(habitId, "de", "Ubung")
        `when`(dao.findById(habitId)).thenReturn(entity)
        `when`(dao.findEntriesByHabitId(habitId)).thenReturn(emptyList())
        `when`(dao.findNameTranslations(habitId)).thenReturn(listOf(nameTranslation))
        `when`(dao.findDescTranslations(habitId)).thenReturn(emptyList())

        val result = repository.findById(habitId)
        assertTrue(result.isPresent)
        assertNotNull(result.get().nameTranslations)
        assertEquals("Ubung", result.get().nameTranslations["de"])
    }

    @Test
    fun testFindByIdWithDescriptionTranslations() = runTest {
        val habitId = "h1"
        val entity = HabitEntity(
            id = habitId, name = "Exercise", description = "Daily exercise",
            categoryId = null, frequencyType = "DAILY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        val descTranslation = HabitDescriptionTranslationEntity(habitId, "de", "Tagliche Ubung")
        `when`(dao.findById(habitId)).thenReturn(entity)
        `when`(dao.findEntriesByHabitId(habitId)).thenReturn(emptyList())
        `when`(dao.findNameTranslations(habitId)).thenReturn(emptyList())
        `when`(dao.findDescTranslations(habitId)).thenReturn(listOf(descTranslation))

        val result = repository.findById(habitId)
        assertTrue(result.isPresent)
        assertNotNull(result.get().descriptionTranslations)
        assertEquals("Tagliche Ubung", result.get().descriptionTranslations["de"])
    }

    @Test
    fun testFindByIdWithWeeklyFrequencyType() = runTest {
        val habitId = "h1"
        val entity = HabitEntity(
            id = habitId, name = "Test", description = null,
            categoryId = null, frequencyType = "WEEKLY", targetFrequency = 3,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findById(habitId)).thenReturn(entity)
        `when`(dao.findEntriesByHabitId(habitId)).thenReturn(emptyList())
        `when`(dao.findNameTranslations(habitId)).thenReturn(emptyList())
        `when`(dao.findDescTranslations(habitId)).thenReturn(emptyList())

        val result = repository.findById(habitId)
        assertTrue(result.isPresent)
        assertEquals(FrequencyType.WEEKLY, result.get().frequencyType)
    }

    @Test
    fun testFindByIdWithMonthlyFrequencyType() = runTest {
        val habitId = "h1"
        val entity = HabitEntity(
            id = habitId, name = "Test", description = null,
            categoryId = null, frequencyType = "MONTHLY", targetFrequency = 1,
            maxEntriesPerDay = 1, positiveScoring = 1, createdAt = null,
            scoringRuleId = null, scoringRuleName = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findById(habitId)).thenReturn(entity)
        `when`(dao.findEntriesByHabitId(habitId)).thenReturn(emptyList())
        `when`(dao.findNameTranslations(habitId)).thenReturn(emptyList())
        `when`(dao.findDescTranslations(habitId)).thenReturn(emptyList())

        val result = repository.findById(habitId)
        assertTrue(result.isPresent)
        assertEquals(FrequencyType.MONTHLY, result.get().frequencyType)
    }

    @Test
    fun testSaveUpdatesExistingHabit() {
        val habit = createHabit("Original Name")
        val result1 = repository.save(habit)
        assertEquals("Original Name", result1.name)

        habit.name = "Updated Name"
        val result2 = repository.save(habit)
        assertEquals("Updated Name", result2.name)
    }
}
