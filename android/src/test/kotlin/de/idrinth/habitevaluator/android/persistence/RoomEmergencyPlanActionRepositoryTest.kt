package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomEmergencyPlanActionRepositoryTest {

    private lateinit var dao: EmergencyPlanDao
    private lateinit var repository: RoomEmergencyPlanActionRepository

    @BeforeEach
    fun setUp() {
        dao = mock(EmergencyPlanDao::class.java)
        repository = RoomEmergencyPlanActionRepository(dao)
    }

    private fun createActionEntity(
        id: String = "a1",
        actionText: String = "Do something",
        actionOrder: Int = 0,
        phoneNumber: String? = null,
        stepId: String = ""
    ) = EmergencyPlanActionEntity(
        id = id, actionText = actionText, phoneNumber = phoneNumber,
        actionOrder = actionOrder, stepId = stepId
    )

    private fun createAction(
        actionText: String = "Do something",
        actionOrder: Int = 0,
        phoneNumber: String? = null,
        stepId: String? = null
    ): EmergencyPlanAction {
        val action = EmergencyPlanAction()
        action.id = "a1"
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
    fun testSaveReturnsAction() = runTest {
        val action = createAction()
        val result = repository.save(action)
        assertEquals("a1", result.id)
        verify(dao).insertAction(any(EmergencyPlanActionEntity::class.java) ?: createActionEntity())
    }

    @Test
    fun testFindByIdReturnsAction() = runTest {
        val entity = createActionEntity(actionText = "Take a walk")
        `when`(dao.findActionById("a1")).thenReturn(entity)

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertEquals("Take a walk", found.get().actionText)
        assertNull(found.get().phoneNumber)
        assertEquals(0, found.get().actionOrder)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findActionById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithPhoneNumber() = runTest {
        val entity = createActionEntity(actionText = "Call doctor", phoneNumber = "+49123456", actionOrder = 1)
        `when`(dao.findActionById("a1")).thenReturn(entity)

        val found = repository.findById("a1")
        assertTrue(found.isPresent)
        assertEquals("+49123456", found.get().phoneNumber)
    }

    @Test
    fun testFindByStepId() = runTest {
        val a1 = createActionEntity(id = "a1", actionText = "Action 1", actionOrder = 0, stepId = "step1")
        val a2 = createActionEntity(id = "a2", actionText = "Action 2", actionOrder = 1, stepId = "step1")
        `when`(dao.findActionsByStepId("step1")).thenReturn(listOf(a1, a2))

        val actions = repository.findByStepId("step1")
        assertEquals(2, actions.size)
        assertTrue(actions.any { it.actionText == "Action 1" })
        assertTrue(actions.any { it.actionText == "Action 2" })
    }

    @Test
    fun testFindByStepIdEmpty() = runTest {
        `when`(dao.findActionsByStepId("s1")).thenReturn(emptyList())

        val result = repository.findByStepId("s1")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("a1")
        verify(dao).deleteActionById("a1")
    }

    @Test
    fun testDeleteByStepId() = runTest {
        repository.deleteByStepId("step1")
        verify(dao).deleteActionsByStepId("step1")
    }

    @Test
    fun testSaveAll() = runTest {
        val action1 = createAction("Action 1", 0)
        action1.id = "a1"
        val action2 = createAction("Action 2", 1)
        action2.id = "a2"
        repository.saveAll(listOf(action1, action2))
        verify(dao, times(2)).insertAction(any(EmergencyPlanActionEntity::class.java) ?: createActionEntity())
    }

    @Test
    fun testFindByStepIdOrderedByActionOrder() = runTest {
        val a0 = createActionEntity(id = "a0", actionText = "First", actionOrder = 0, stepId = "step1")
        val a1 = createActionEntity(id = "a1", actionText = "Second", actionOrder = 1, stepId = "step1")
        val a2 = createActionEntity(id = "a2", actionText = "Third", actionOrder = 2, stepId = "step1")
        `when`(dao.findActionsByStepId("step1")).thenReturn(listOf(a0, a1, a2))

        val actions = repository.findByStepId("step1")
        assertEquals(3, actions.size)
        assertEquals("First", actions[0].actionText)
        assertEquals("Second", actions[1].actionText)
        assertEquals("Third", actions[2].actionText)
    }
}
