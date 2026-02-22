package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.HabitCategory
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
import java.util.UUID

class RoomHabitCategoryRepositoryTest {

    private lateinit var dao: HabitCategoryDao
    private lateinit var repository: RoomHabitCategoryRepository

    @BeforeEach
    fun setUp() {
        dao = mock(HabitCategoryDao::class.java)
        repository = RoomHabitCategoryRepository(dao)
    }

    private fun createCategory(
        name: String = "Health",
        description: String = "Health-related habits",
        color: String = "#FF0000",
        userId: String = "u1"
    ): HabitCategory {
        val category = HabitCategory()
        category.id = UUID.randomUUID().toString()
        category.name = name
        category.description = description
        category.color = color
        val user = User()
        user.id = userId
        user.username = "testuser"
        category.user = user
        return category
    }

    @Test
    fun testSaveReturnsCategory() {
        val category = createCategory()
        val result = repository.save(category)
        assertEquals(category.id, result.id)
        assertEquals(category.name, result.name)
    }

    @Test
    fun testSaveCallsDaoSaveWithDetails() = runTest {
        val category = createCategory()
        repository.save(category)
        verify(dao).saveWithDetails(
            any(HabitCategoryEntity::class.java) ?: category.toEntity(),
            anyList(),
            anyList()
        )
    }

    @Test
    fun testFindByIdReturnsCategory() = runTest {
        val entity = HabitCategoryEntity(
            id = "c1", name = "Health", description = "Health habits",
            color = "#FF0000", userId = "u1", userName = "testuser"
        )
        `when`(dao.findById("c1")).thenReturn(entity)
        `when`(dao.findNameTranslations("c1")).thenReturn(emptyList())
        `when`(dao.findDescTranslations("c1")).thenReturn(emptyList())

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertEquals("Health", found.get().name)
        assertEquals("Health habits", found.get().description)
        assertEquals("#FF0000", found.get().color)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = HabitCategoryEntity(
            id = "c1", name = "Health", description = null,
            color = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findById("c1")).thenReturn(entity)
        `when`(dao.findNameTranslations("c1")).thenReturn(emptyList())
        `when`(dao.findDescTranslations("c1")).thenReturn(emptyList())

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() = runTest {
        val entity1 = HabitCategoryEntity(
            id = "c1", name = "Category A", description = null,
            color = null, userId = "u1", userName = "testuser"
        )
        val entity2 = HabitCategoryEntity(
            id = "c2", name = "Category B", description = null,
            color = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findAll()).thenReturn(listOf(entity1, entity2))
        `when`(dao.findNameTranslations(anyString())).thenReturn(emptyList())
        `when`(dao.findDescTranslations(anyString())).thenReturn(emptyList())

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
        repository.deleteById("c1")
        verify(dao).deleteWithDetails("c1")
    }

    @Test
    fun testExistsByIdTrue() = runTest {
        `when`(dao.existsById("c1")).thenReturn(true)

        assertTrue(repository.existsById("c1"))
    }

    @Test
    fun testExistsByIdFalse() = runTest {
        `when`(dao.existsById("nonexistent")).thenReturn(false)

        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() = runTest {
        val entity1 = HabitCategoryEntity(
            id = "c1", name = "Cat A", description = null,
            color = null, userId = "u1", userName = "testuser"
        )
        val entity2 = HabitCategoryEntity(
            id = "c2", name = "Cat B", description = null,
            color = null, userId = "u1", userName = "testuser"
        )
        `when`(dao.findByUserId("u1")).thenReturn(listOf(entity1, entity2))
        `when`(dao.findNameTranslations(anyString())).thenReturn(emptyList())
        `when`(dao.findDescTranslations(anyString())).thenReturn(emptyList())

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
    fun testFindByIdWithNameTranslations() = runTest {
        val entity = HabitCategoryEntity(
            id = "c1", name = "Health", description = null,
            color = null, userId = "u1", userName = "testuser"
        )
        val nameTranslation = CategoryNameTranslationEntity("c1", "de", "Gesundheit")
        `when`(dao.findById("c1")).thenReturn(entity)
        `when`(dao.findNameTranslations("c1")).thenReturn(listOf(nameTranslation))
        `when`(dao.findDescTranslations("c1")).thenReturn(emptyList())

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().nameTranslations)
        assertEquals("Gesundheit", found.get().nameTranslations["de"])
    }

    @Test
    fun testFindByIdWithDescriptionTranslations() = runTest {
        val entity = HabitCategoryEntity(
            id = "c1", name = "Health", description = "Health related",
            color = null, userId = "u1", userName = "testuser"
        )
        val descTranslation = CategoryDescriptionTranslationEntity("c1", "de", "Gesundheitsbezogene Gewohnheiten")
        `when`(dao.findById("c1")).thenReturn(entity)
        `when`(dao.findNameTranslations("c1")).thenReturn(emptyList())
        `when`(dao.findDescTranslations("c1")).thenReturn(listOf(descTranslation))

        val found = repository.findById("c1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().descriptionTranslations)
        assertEquals("Gesundheitsbezogene Gewohnheiten", found.get().descriptionTranslations["de"])
    }

    @Test
    fun testSaveUpdatesExistingCategory() {
        val category = createCategory("Original Name")
        val result1 = repository.save(category)
        assertEquals("Original Name", result1.name)

        category.name = "Updated Name"
        val result2 = repository.save(category)
        assertEquals("Updated Name", result2.name)
    }
}
