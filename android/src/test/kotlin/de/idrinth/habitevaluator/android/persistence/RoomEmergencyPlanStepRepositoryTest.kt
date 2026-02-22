package de.idrinth.habitevaluator.android.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
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
class RoomEmergencyPlanStepRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomEmergencyPlanStepRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomEmergencyPlanStepRepository(database.emergencyPlanDao())
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

    private fun createStep(
        question: String = "Are you safe?",
        stepOrder: Int = 0,
        userId: String = "u1",
        vararg actions: EmergencyPlanAction
    ): EmergencyPlanStep {
        val step = EmergencyPlanStep()
        step.id = UUID.randomUUID().toString()
        step.question = question
        step.stepOrder = stepOrder
        step.user = createUser(userId)
        actions.forEach { step.addAction(it) }
        return step
    }

    private fun createAction(
        actionText: String = "Do something",
        actionOrder: Int = 0,
        phoneNumber: String? = null
    ): EmergencyPlanAction {
        val action = EmergencyPlanAction()
        action.id = UUID.randomUUID().toString()
        action.actionText = actionText
        action.actionOrder = actionOrder
        action.phoneNumber = phoneNumber
        return action
    }

    @Test
    fun testSaveReturnsStep() {
        val step = createStep()
        val result = repository.save(step)
        assertEquals(step.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val step = createStep("Are you tired?", stepOrder = 0)
        repository.save(step)

        val found = repository.findById(step.id)
        assertTrue(found.isPresent)
        assertEquals("Are you tired?", found.get().question)
        assertEquals(0, found.get().stepOrder)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() {
        val step = createStep(userId = "u1")
        repository.save(step)

        val found = repository.findById(step.id)
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testSavePersistsActions() {
        val action = createAction("Take a nap", 0)
        val step = createStep("Are you tired?", actions = arrayOf(action))
        repository.save(step)

        val found = repository.findById(step.id)
        assertTrue(found.isPresent)
        assertEquals(1, found.get().actions.size)
        assertEquals("Take a nap", found.get().actions[0].actionText)
    }

    @Test
    fun testSavePersistsMultipleActions() {
        val action1 = createAction("Action 1", 0)
        val action2 = createAction("Action 2", 1)
        val step = createStep(actions = arrayOf(action1, action2))
        repository.save(step)

        val found = repository.findById(step.id)
        assertTrue(found.isPresent)
        assertEquals(2, found.get().actions.size)
    }

    @Test
    fun testSaveAll() {
        val step1 = createStep("Q1?", 0)
        val step2 = createStep("Q2?", 1)
        repository.saveAll(listOf(step1, step2))

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAll() {
        val step1 = createStep("Q1?", 0)
        val step2 = createStep("Q2?", 1)
        repository.save(step1)
        repository.save(step2)

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
        val step = createStep()
        repository.save(step)

        assertTrue(repository.findById(step.id).isPresent)

        repository.deleteById(step.id)

        assertFalse(repository.findById(step.id).isPresent)
    }

    @Test
    fun testDeleteByIdAlsoDeletesActions() {
        val action = createAction("Do something", 0)
        val step = createStep(actions = arrayOf(action))
        repository.save(step)

        repository.deleteById(step.id)

        assertFalse(repository.findById(step.id).isPresent)
    }

    @Test
    fun testFindByUserId() {
        val step1 = createStep("Q1?", 0, "u1")
        val step2 = createStep("Q2?", 1, "u1")
        val step3 = createStep("Q3?", 0, "u2")
        repository.save(step1)
        repository.save(step2)
        repository.save(step3)

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
    fun testSaveUpdatesActionsOnReSave() {
        val action1 = createAction("Action 1", 0)
        val step = createStep(actions = arrayOf(action1))
        repository.save(step)

        // Now replace actions
        step.actions.clear()
        val action2 = createAction("Action 2", 0)
        step.addAction(action2)
        repository.save(step)

        val found = repository.findById(step.id)
        assertTrue(found.isPresent)
        assertEquals(1, found.get().actions.size)
        assertEquals("Action 2", found.get().actions[0].actionText)
    }

    @Test
    fun testActionWithPhoneNumber() {
        val action = createAction("Call emergency", 0, "+49123456")
        val step = createStep(actions = arrayOf(action))
        repository.save(step)

        val found = repository.findById(step.id)
        assertTrue(found.isPresent)
        assertEquals("+49123456", found.get().actions[0].phoneNumber)
    }
}
