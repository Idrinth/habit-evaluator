package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.EmergencyPlanAction
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomEmergencyPlanActionRepository(private val dao: EmergencyPlanDao) : EmergencyPlanActionRepository {

    override fun save(action: EmergencyPlanAction): EmergencyPlanAction = runBlocking {
        dao.insertAction(action.toEntity())
        action
    }

    override fun saveAll(actions: List<EmergencyPlanAction>) = runBlocking {
        actions.forEach { dao.insertAction(it.toEntity()) }
    }

    override fun findById(id: String): Optional<EmergencyPlanAction> = runBlocking {
        val entity = dao.findActionById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findByStepId(stepId: String): List<EmergencyPlanAction> = runBlocking {
        dao.findActionsByStepId(stepId).map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteActionById(id)
    }

    override fun deleteByStepId(stepId: String) = runBlocking {
        dao.deleteActionsByStepId(stepId)
    }

    suspend fun saveSuspend(action: EmergencyPlanAction): EmergencyPlanAction {
        dao.insertAction(action.toEntity())
        return action
    }
}
