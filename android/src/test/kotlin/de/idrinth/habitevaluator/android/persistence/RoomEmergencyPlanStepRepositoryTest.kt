package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction
import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
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
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RoomEmergencyPlanStepRepositoryTest {

    private lateinit var dao: EmergencyPlanDao
    private lateinit var repository: RoomEmergencyPlanStepRepository

    @BeforeEach
    fun setUp() {
        dao = mock(EmergencyPlanDao::class.java)
        repository = RoomEmergencyPlanStepRepository(dao)
    }

    private fun createStepEntity(
        id: String = "s1",
        question: String = "Are you safe?",
        stepOrder: Int = 0,
        userId: String = "u1"
    ) = EmergencyPlanStepEntity(
        id = id, question = question, stepOrder = stepOrder,
        userId = userId, userName = "testuser"
    )

    private fun createActionEntity(
        id: String = "a1",
        actionText: String = "Do something",
        actionOrder: Int = 0,
        phoneNumber: String? = null,
        stepId: String = "s1"
    ) = EmergencyPlanActionEntity(
        id = id, actionText = actionText, phoneNumber = phoneNumber,
        actionOrder = actionOrder, stepId = stepId
    )

    private fun createStep(
        question: String = "Are you safe?",
        stepOrder: Int = 0,
        userId: String = "u1"
    ): EmergencyPlanStep {
        val step = EmergencyPlanStep()
        step.id = "s1"
        step.question = question
        step.stepOrder = stepOrder
        val user = User()
        user.id = userId
        user.username = "testuser"
        step.user = user
        return step
    }

    @Test
    fun testSaveReturnsStep() = runTest {
        val step = createStep()
        val result = repository.save(step)
        assertEquals("s1", result.id)
    }

    @Test
    fun testSaveCallsDaoSaveStepWithActions() = runTest {
        val step = createStep()
        repository.save(step)
        verify(dao).saveStepWithActions(
            any(EmergencyPlanStepEntity::class.java) ?: createStepEntity(),
            anyList()
        )
    }

    @Test
    fun testFindByIdReturnsStep() = runTest {
        val stepEntity = createStepEntity(question = "Are you tired?")
        `when`(dao.findStepById("s1")).thenReturn(stepEntity)
        `when`(dao.findActionsByStepId("s1")).thenReturn(emptyList())

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals("Are you tired?", found.get().question)
        assertEquals(0, found.get().stepOrder)
    }

    @Test
    fun testFindByIdNotFound() = runTest {
        `when`(dao.findStepById("nonexistent")).thenReturn(null)

        val result = repository.findById("nonexistent")
        assertFalse(result.isPresent)
    }

    @Test
    fun testFindByIdWithUser() = runTest {
        val stepEntity = createStepEntity()
        `when`(dao.findStepById("s1")).thenReturn(stepEntity)
        `when`(dao.findActionsByStepId("s1")).thenReturn(emptyList())

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertNotNull(found.get().user)
        assertEquals("u1", found.get().user.id)
    }

    @Test
    fun testFindByIdPersistsActions() = runTest {
        val stepEntity = createStepEntity()
        val actionEntity = createActionEntity(actionText = "Take a nap")
        `when`(dao.findStepById("s1")).thenReturn(stepEntity)
        `when`(dao.findActionsByStepId("s1")).thenReturn(listOf(actionEntity))

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals(1, found.get().actions.size)
        assertEquals("Take a nap", found.get().actions[0].actionText)
    }

    @Test
    fun testFindByIdWithMultipleActions() = runTest {
        val stepEntity = createStepEntity()
        val a1 = createActionEntity(id = "a1", actionText = "Action 1", actionOrder = 0)
        val a2 = createActionEntity(id = "a2", actionText = "Action 2", actionOrder = 1)
        `when`(dao.findStepById("s1")).thenReturn(stepEntity)
        `when`(dao.findActionsByStepId("s1")).thenReturn(listOf(a1, a2))

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals(2, found.get().actions.size)
    }

    @Test
    fun testSaveAll() = runTest {
        val step1 = createStep("Q1?", 0)
        step1.id = "s1"
        val step2 = createStep("Q2?", 1)
        step2.id = "s2"
        repository.saveAll(listOf(step1, step2))
        // saveStepWithActions called twice
        verify(dao, times(2)).saveStepWithActions(
            any(EmergencyPlanStepEntity::class.java) ?: createStepEntity(id = "s1"),
            anyList()
        )
    }

    @Test
    fun testFindAll() = runTest {
        val e1 = createStepEntity(id = "s1", question = "Q1?")
        val e2 = createStepEntity(id = "s2", question = "Q2?")
        `when`(dao.findAllSteps()).thenReturn(listOf(e1, e2))
        `when`(dao.findActionsByStepId(anyString())).thenReturn(emptyList())

        val all = repository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testFindAllEmpty() = runTest {
        `when`(dao.findAllSteps()).thenReturn(emptyList())

        val result = repository.findAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun testDeleteById() = runTest {
        repository.deleteById("s1")
        verify(dao).deleteStepWithActions("s1")
    }

    @Test
    fun testFindByUserId() = runTest {
        val e1 = createStepEntity(id = "s1", userId = "u1")
        val e2 = createStepEntity(id = "s2", userId = "u1")
        `when`(dao.findStepsByUserId("u1")).thenReturn(listOf(e1, e2))
        `when`(dao.findActionsByStepId(anyString())).thenReturn(emptyList())

        val result = repository.findByUserId("u1")
        assertEquals(2, result.size)
        assertTrue(result.all { it.user.id == "u1" })
    }

    @Test
    fun testFindByUserIdEmpty() = runTest {
        `when`(dao.findStepsByUserId("u99")).thenReturn(emptyList())

        val result = repository.findByUserId("u99")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testLoadedActionsHaveStepBackReference() = runTest {
        val stepEntity = createStepEntity()
        val actionEntity = createActionEntity(actionText = "Call someone")
        `when`(dao.findStepById("s1")).thenReturn(stepEntity)
        `when`(dao.findActionsByStepId("s1")).thenReturn(listOf(actionEntity))

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        val action = found.get().actions[0]
        assertNotNull(action.step)
        assertEquals("s1", action.step.id)
    }

    @Test
    fun testFindByUserIdActionsHaveStepBackReference() = runTest {
        val stepEntity = createStepEntity()
        val actionEntity = createActionEntity(actionText = "Call someone")
        `when`(dao.findStepsByUserId("u1")).thenReturn(listOf(stepEntity))
        `when`(dao.findActionsByStepId("s1")).thenReturn(listOf(actionEntity))

        val result = repository.findByUserId("u1")
        assertEquals(1, result.size)
        val action = result[0].actions[0]
        assertNotNull(action.step)
        assertEquals("s1", action.step.id)
    }

    @Test
    fun testSaveAfterLoadPreservesActions() = runTest {
        val stepEntity = createStepEntity()
        val actionEntity = createActionEntity(actionText = "Take a break")
        `when`(dao.findStepsByUserId("u1")).thenReturn(listOf(stepEntity))
        `when`(dao.findActionsByStepId("s1")).thenReturn(listOf(actionEntity))

        val loaded = repository.findByUserId("u1")
        val step = loaded[0]
        assertEquals(1, step.actions.size)
        assertNotNull(step.actions[0].step)
        assertEquals("s1", step.actions[0].step.id)

        step.stepOrder = 5
        repository.save(step)

        verify(dao, times(1)).saveStepWithActions(
            any(EmergencyPlanStepEntity::class.java) ?: createStepEntity(),
            anyList()
        )
    }

    @Test
    fun testActionWithPhoneNumber() = runTest {
        val stepEntity = createStepEntity()
        val actionEntity = createActionEntity(actionText = "Call emergency", phoneNumber = "+49123456")
        `when`(dao.findStepById("s1")).thenReturn(stepEntity)
        `when`(dao.findActionsByStepId("s1")).thenReturn(listOf(actionEntity))

        val found = repository.findById("s1")
        assertTrue(found.isPresent)
        assertEquals("+49123456", found.get().actions[0].phoneNumber)
    }
}
