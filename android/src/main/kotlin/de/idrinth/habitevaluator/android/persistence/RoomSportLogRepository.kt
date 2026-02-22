package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.SportLog
import de.idrinth.habitevaluator.shared.repository.SportLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomSportLogRepository(private val dao: SportLogDao) : SportLogRepository {

    override fun save(entry: SportLog): SportLog = runBlocking {
        dao.insert(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<SportLog> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<SportLog> = runBlocking {
        dao.findAll().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteById(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.existsById(id)
    }

    override fun findByUserId(userId: String): List<SportLog> = runBlocking {
        dao.findByUserId(userId).map { it.toModel() }
    }

    override fun findDistinctNamesByUserId(userId: String): List<String> = runBlocking {
        dao.findDistinctNames(userId)
    }

    override fun findDistinctMeasurementUnitsByUserId(userId: String): List<String> = runBlocking {
        dao.findDistinctMeasurementUnits(userId)
    }

    fun observeByUserId(userId: String): Flow<List<SportLogEntity>> =
        dao.observeByUserId(userId)

    suspend fun saveSuspend(entry: SportLog): SportLog {
        dao.insert(entry.toEntity())
        return entry
    }
}
