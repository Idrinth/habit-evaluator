package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.MedicationLog
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomMedicationLogRepository(private val dao: MedicationDao) : MedicationLogRepository {

    override fun save(entry: MedicationLog): MedicationLog = runBlocking {
        dao.insertLog(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<MedicationLog> = runBlocking {
        val entity = dao.findLogById(id) ?: return@runBlocking Optional.empty()
        val medication = dao.findById(entity.medicationId)?.toModel()
        Optional.of(entity.toModel(medication))
    }

    override fun findAll(): List<MedicationLog> = runBlocking {
        dao.findAllLogs().map { entity ->
            val medication = dao.findById(entity.medicationId)?.toModel()
            entity.toModel(medication)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteLogById(id)
    }

    override fun findByUserId(userId: String): List<MedicationLog> = runBlocking {
        val meds = dao.findByUserId(userId).associate { it.id to it.toModel() }
        dao.findLogsByUserId(userId).map { entity ->
            entity.toModel(meds[entity.medicationId])
        }
    }

    override fun findByUserIdPaged(userId: String, limit: Int, offset: Int): List<MedicationLog> = runBlocking {
        val meds = dao.findByUserId(userId).associate { it.id to it.toModel() }
        dao.findLogsByUserIdPaged(userId, limit, offset).map { entity ->
            entity.toModel(meds[entity.medicationId])
        }
    }

    override fun countByUserId(userId: String): Int = runBlocking {
        dao.countLogsByUserId(userId)
    }

    fun observeByUserId(userId: String): Flow<List<MedicationLogEntity>> =
        dao.observeLogsByUserId(userId)

    suspend fun saveSuspend(entry: MedicationLog): MedicationLog {
        dao.insertLog(entry.toEntity())
        return entry
    }
}
