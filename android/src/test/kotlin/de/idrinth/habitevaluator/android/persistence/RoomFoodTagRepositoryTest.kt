package de.idrinth.habitevaluator.android.persistence

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

class RoomFoodTagRepositoryTest {

    private lateinit var dao: FoodLogDao
    private lateinit var repository: RoomFoodTagRepository

    @BeforeEach
    fun setUp() {
        dao = mock(FoodLogDao::class.java)
        repository = RoomFoodTagRepository(dao)
    }

    private fun createTagEntity(
        id: String = "t1",
        name: String = "Vegetarian",
        userId: String = "u1"
    ) = FoodTagEntity(
        id = id, name = name, nameLower = name.lowercase(),
        userId = userId, userName = "testuser"
    )

    @Test
    fun testSaveReturnsTag() = runTest {
        val tag = de.idrinth.habitevaluator.shared.model.FoodTag()
        tag.id = "t1"
        tag.name = "Vegetarian"
        val user = de.idrinth.habitevaluator.shared.model.User()
        user.id = "u1"
        user.username = "testuser"
        tag.user = user

        val result = repository.save(tag)
        assertEquals("t1", result.id)
        assertEquals("Vegetarian", result.name)
        verify(dao).insertTag(any(FoodTagEntity::class.java) ?: createTagEntity())
    }

    @Test
    fun testFindByIdReturnsTag() = runTest {
        val entity = createTagEntity()
        `when`(dao.findTagById("t1")).thenReturn(entity)

        val found = repository.findById("t1")
        assertTrue(found.isPresent)
        assertEquals("Vegetarian", found.get().name)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findTagById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val entity = createTagEntity()
        `when`(dao.findTagById("t1")).thenReturn(entity)

        val found = repository.findById("t1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createTagEntity(id = "t1", name = "Vegetarian", userId = "u1")
        val e2 = createTagEntity(id = "t2", name = "Vegan", userId = "u1")
        `when`(dao.findTagsByUserId("u1")).thenReturn(listOf(e1, e2))

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findTagsByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFindByNameLowerAndUserIdFound() = runTest {
        val entity = createTagEntity(name = "Vegetarian")
        `when`(dao.findTagByNameLowerAndUserId("vegetarian", "u1")).thenReturn(entity)

        val found = repository.findByNameLowerAndUserId("vegetarian", "u1")
        assertTrue(found.isPresent)
        assertEquals("Vegetarian", found.get().name)
    }

    @Test
    fun testFindByNameLowerAndUserIdNotFound() = runTest {
        `when`(dao.findTagByNameLowerAndUserId("nonexistent", "u1")).thenReturn(null)

        val result = repository.findByNameLowerAndUserId("nonexistent", "u1")
        assertFalse(result.isPresent)
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("t1")
        verify(dao).deleteTagById("t1")
    }

    @Test
    fun testDeleteEmptyTags() = runTest {
        repository.deleteEmptyTags("u1")
        verify(dao).deleteEmptyTags("u1")
    }

    @Test
    fun testLinkTagToFoodLog() = runTest {
        repository.linkTagToFoodLog("t1", "f1")
        verify(dao).linkTagToFoodLog(FoodLogTagCrossRef("f1", "t1"))
    }

    @Test
    fun testUnlinkAllTagsFromFoodLog() = runTest {
        repository.unlinkAllTagsFromFoodLog("f1")
        verify(dao).unlinkAllTagsFromFoodLog("f1")
    }
}
