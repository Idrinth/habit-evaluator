package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.HabitCategory
import de.idrinth.habitevaluator.shared.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomHabitCategoryRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomHabitCategoryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomHabitCategoryRepository(database.habitCategoryDao())
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
        category.user = createUser(userId)
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
    fun testSaveAndFindById() {
        val category = createCategory("Health", "Health habits", "#FF0000")
        repository.save(category)

        val found = repository.findById(category.id)
        assertTrue(found.isPresent)
        assertEquals("Health", found.get().name)
        assertEquals("Health habits", found.get().description)
        assertEquals("#FF0000", found.get().color)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val category = createCategory(userId = "u1")
        repository.save(category)

        val found = repository.findById(category.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
        assertEquals("testuser", found.get().user.username)
    }

    @Test
    fun testFindAll() {
        val cat1 = createCategory("Category A")
        val cat2 = createCategory("Category B")
        repository.save(cat1)
        repository.save(cat2)

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
        val category = createCategory()
        repository.save(category)

        assertTrue(repository.findById(category.id).isPresent)

        repository.deleteById(category.id)

        assertFalse(repository.findById(category.id).isPresent)
    }

    @Test
    fun testExistsByIdTrue() {
        val category = createCategory()
        repository.save(category)

        assertTrue(repository.existsById(category.id))
    }

    @Test
    fun testExistsByIdFalse() {
        assertFalse(repository.existsById("nonexistent"))
    }

    @Test
    fun testFindByUserId() {
        val cat1 = createCategory("Cat A", userId = "u1")
        val cat2 = createCategory("Cat B", userId = "u1")
        val cat3 = createCategory("Cat C", userId = "u2")
        repository.save(cat1)
        repository.save(cat2)
        repository.save(cat3)

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
    fun testSaveWithNameTranslations() {
        val category = createCategory()
        category.nameTranslations = hashMapOf("de" to "Gesundheit")
        repository.save(category)

        val found = repository.findById(category.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().nameTranslations)
        assertEquals("Gesundheit", found.get().nameTranslations["de"])
    }

    @Test
    fun testSaveWithDescriptionTranslations() {
        val category = createCategory()
        category.descriptionTranslations = hashMapOf("de" to "Gesundheitsbezogene Gewohnheiten")
        repository.save(category)

        val found = repository.findById(category.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().descriptionTranslations)
        assertEquals("Gesundheitsbezogene Gewohnheiten", found.get().descriptionTranslations["de"])
    }

    @Test
    fun testSaveUpdatesExistingCategory() {
        val category = createCategory("Original Name")
        repository.save(category)

        category.name = "Updated Name"
        repository.save(category)

        val found = repository.findById(category.id)
        assertTrue(found.isPresent)
        assertEquals("Updated Name", found.get().name)
    }
}
