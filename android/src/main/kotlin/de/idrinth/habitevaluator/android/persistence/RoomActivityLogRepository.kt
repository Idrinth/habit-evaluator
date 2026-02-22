package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.ActivityLog
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomActivityLogRepository(private val dao: ActivityLogDao) : ActivityLogRepository {

    override fun save(entry: ActivityLog): ActivityLog = runBlocking {
        dao.insert(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<ActivityLog> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<ActivityLog> = runBlocking {
        dao.findAll().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteById(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.existsById(id)
    }

    override fun findByUserId(userId: String): List<ActivityLog> = runBlocking {
        dao.findByUserId(userId).map { it.toModel() }
    }

    override fun findDistinctLocationsByUserId(userId: String): List<String> = runBlocking {
        dao.findDistinctLocations(userId)
    }

    override fun findDistinctActivitiesByUserId(userId: String): List<String> = runBlocking {
        dao.findDistinctActivities(userId)
    }

    fun observeByUserId(userId: String): Flow<List<ActivityLogEntity>> =
        dao.observeByUserId(userId)

    suspend fun saveSuspend(entry: ActivityLog): ActivityLog {
        dao.insert(entry.toEntity())
        return entry
    }
}
