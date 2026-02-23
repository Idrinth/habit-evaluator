package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.SlotConfirmation
import de.idrinth.habitevaluator.shared.repository.SlotConfirmationRepository
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomSlotConfirmationRepository(private val dao: DayPlannerDao) : SlotConfirmationRepository {

    override fun save(confirmation: SlotConfirmation): SlotConfirmation = runBlocking {
        dao.insertConfirmation(confirmation.toEntity())
        confirmation
    }

    override fun findById(id: String): Optional<SlotConfirmation> = runBlocking {
        val entity = dao.findConfirmationById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<SlotConfirmation> = runBlocking {
        dao.findAllConfirmations().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteConfirmationById(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.confirmationExistsById(id)
    }

    override fun findByUserId(userId: String): List<SlotConfirmation> = runBlocking {
        dao.findConfirmationsByUserId(userId).map { it.toModel() }
    }

    suspend fun saveSuspend(confirmation: SlotConfirmation): SlotConfirmation {
        dao.insertConfirmation(confirmation.toEntity())
        return confirmation
    }
}
