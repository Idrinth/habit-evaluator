package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.Medication
import de.idrinth.habitevaluator.shared.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomMedicationRepository(private val dao: MedicationDao) : MedicationRepository {

    override fun save(medication: Medication): Medication = runBlocking {
        dao.insert(medication.toEntity())
        medication
    }

    override fun findById(id: String): Optional<Medication> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<Medication> = runBlocking {
        dao.findAll().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteById(id)
    }

    override fun findByUserId(userId: String): List<Medication> = runBlocking {
        dao.findByUserId(userId).map { it.toModel() }
    }

    fun observeByUserId(userId: String): Flow<List<MedicationEntity>> =
        dao.observeByUserId(userId)

    suspend fun saveSuspend(medication: Medication): Medication {
        dao.insert(medication.toEntity())
        return medication
    }
}
