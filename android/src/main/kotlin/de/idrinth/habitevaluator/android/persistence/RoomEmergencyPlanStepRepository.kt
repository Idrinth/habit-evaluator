package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.EmergencyPlanStep
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomEmergencyPlanStepRepository(private val dao: EmergencyPlanDao) : EmergencyPlanStepRepository {

    override fun save(step: EmergencyPlanStep): EmergencyPlanStep = runBlocking {
        val actionEntities = step.actions?.map { it.toEntity() } ?: emptyList()
        dao.saveStepWithActions(step.toEntity(), actionEntities)
        step
    }

    override fun saveAll(steps: List<EmergencyPlanStep>) = runBlocking {
        steps.forEach { step ->
            val actionEntities = step.actions?.map { it.toEntity() } ?: emptyList()
            dao.saveStepWithActions(step.toEntity(), actionEntities)
        }
    }

    override fun findById(id: String): Optional<EmergencyPlanStep> = runBlocking {
        val entity = dao.findStepById(id) ?: return@runBlocking Optional.empty()
        val actions = dao.findActionsByStepId(id).map { it.toModel() }
        Optional.of(entity.toModel(actions))
    }

    override fun findAll(): List<EmergencyPlanStep> = runBlocking {
        dao.findAllSteps().map { entity ->
            val actions = dao.findActionsByStepId(entity.id).map { it.toModel() }
            entity.toModel(actions)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteStepWithActions(id)
    }

    override fun findByUserId(userId: String): List<EmergencyPlanStep> = runBlocking {
        dao.findStepsByUserId(userId).map { entity ->
            val actions = dao.findActionsByStepId(entity.id).map { it.toModel() }
            entity.toModel(actions)
        }
    }

    fun observeByUserId(userId: String): Flow<List<EmergencyPlanStepEntity>> =
        dao.observeStepsByUserId(userId)

    suspend fun saveSuspend(step: EmergencyPlanStep): EmergencyPlanStep {
        val actionEntities = step.actions?.map { it.toEntity() } ?: emptyList()
        dao.saveStepWithActions(step.toEntity(), actionEntities)
        return step
    }
}
