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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomEmergencyPlanActionRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomEmergencyPlanActionRepository
    private lateinit var stepRepository: RoomEmergencyPlanStepRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomEmergencyPlanActionRepository(database.emergencyPlanDao())
        stepRepository = RoomEmergencyPlanStepRepository(database.emergencyPlanDao())
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

    private fun createStep(stepId: String = UUID.randomUUID().toString()): EmergencyPlanStep {
        val step = EmergencyPlanStep()
        step.id = stepId
        step.question = "Test question?"
        step.stepOrder = 0
        step.user = createUser()
        return step
    }

    private fun createAction(
        actionText: String = "Do something",
        actionOrder: Int = 0,
        phoneNumber: String? = null,
        stepId: String? = null
    ): EmergencyPlanAction {
        val action = EmergencyPlanAction()
        action.id = UUID.randomUUID().toString()
        action.actionText = actionText
        action.actionOrder = actionOrder
        action.phoneNumber = phoneNumber
        if (stepId != null) {
            val step = EmergencyPlanStep()
            step.id = stepId
            action.step = step
        }
        return action
    }

    @Test
    fun testSaveReturnsAction() {
        val action = createAction()
        val result = repository.save(action)
        assertEquals(action.id, result.id)
    }

    @Test
    fun testSaveAndFindById() {
        val action = createAction("Take a walk", 0)
        repository.save(action)

        val found = repository.findById(action.id)
        assertTrue(found.isPresent)
        assertEquals("Take a walk", found.get().actionText)
        assertNull(found.get().phoneNumber)
        assertEquals(0, found.get().actionOrder)
    }

    @Test
    fun testFindByIdNotFound() {
        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithPhoneNumber() {
        val action = createAction("Call doctor", 1, "+49123456")
        repository.save(action)

        val found = repository.findById(action.id)
        assertTrue(found.isPresent)
        assertEquals("+49123456", found.get().phoneNumber)
    }

    @Test
    fun testFindByStepId() {
        val step = createStep("step1")
        stepRepository.save(step)

        val action1 = createAction("Action 1", 0, stepId = "step1")
        val action2 = createAction("Action 2", 1, stepId = "step1")
        repository.save(action1)
        repository.save(action2)

        val actions = repository.findByStepId("step1")
        assertEquals(2, actions.size)
        assertTrue(actions.any { it.actionText == "Action 1" })
        assertTrue(actions.any { it.actionText == "Action 2" })
    }

    @Test
    fun testFindByStepIdEmpty() {
        val result = repository.findByStepId("s1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() {
        val action = createAction()
        repository.save(action)

        assertTrue(repository.findById(action.id).isPresent)

        repository.deleteById(action.id)

        assertFalse(repository.findById(action.id).isPresent)
    }

    @Test
    fun testDeleteByStepId() {
        val step = createStep("step1")
        stepRepository.save(step)

        val action1 = createAction("Action 1", 0, stepId = "step1")
        val action2 = createAction("Action 2", 1, stepId = "step1")
        repository.save(action1)
        repository.save(action2)

        assertEquals(2, repository.findByStepId("step1").size)

        repository.deleteByStepId("step1")

        assertEquals(0, repository.findByStepId("step1").size)
    }

    @Test
    fun testSaveAll() {
        val action1 = createAction("Action 1", 0)
        val action2 = createAction("Action 2", 1)
        repository.saveAll(listOf(action1, action2))

        assertTrue(repository.findById(action1.id).isPresent)
        assertTrue(repository.findById(action2.id).isPresent)
    }

    @Test
    fun testActionOrderIsPreservedByFindByStepId() {
        val step = createStep("step1")
        stepRepository.save(step)

        val action0 = createAction("First", 0, stepId = "step1")
        val action1 = createAction("Second", 1, stepId = "step1")
        val action2 = createAction("Third", 2, stepId = "step1")
        // Insert in reverse order to verify ordering
        repository.save(action2)
        repository.save(action0)
        repository.save(action1)

        val actions = repository.findByStepId("step1")
        assertEquals(3, actions.size)
        assertEquals("First", actions[0].actionText)
        assertEquals("Second", actions[1].actionText)
        assertEquals("Third", actions[2].actionText)
    }
}
