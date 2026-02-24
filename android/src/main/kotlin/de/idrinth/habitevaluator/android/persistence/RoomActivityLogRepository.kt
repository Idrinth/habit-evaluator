package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.ActivityGroup
import de.idrinth.habitevaluator.shared.model.ActivityLog
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomActivityLogRepository(private val dao: ActivityLogDao) : ActivityLogRepository {

    override fun save(entry: ActivityLog): ActivityLog = runBlocking {
        dao.saveActivityLogWithLinks(entry.toEntity(), entry.toGroupIds())
        entry
    }

    override fun findById(id: String): Optional<ActivityLog> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        val groups = dao.findGroupsByActivityLogId(id).map { it.toModel() }.toSet()
        Optional.of(entity.toModel(groups))
    }

    override fun findAll(): List<ActivityLog> = runBlocking {
        dao.findAll().map { entity ->
            val groups = dao.findGroupsByActivityLogId(entity.id).map { it.toModel() }.toSet()
            entity.toModel(groups)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteActivityLogWithLinks(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.existsById(id)
    }

    override fun findByUserId(userId: String): List<ActivityLog> = runBlocking {
        dao.findByUserId(userId).map { entity ->
            val groups = dao.findGroupsByActivityLogId(entity.id).map { it.toModel() }.toSet()
            entity.toModel(groups)
        }
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
        dao.saveActivityLogWithLinks(entry.toEntity(), entry.toGroupIds())
        return entry
    }

    suspend fun findGroupsForActivityLog(activityLogId: String): Set<ActivityGroup> {
        return dao.findGroupsByActivityLogId(activityLogId).map { it.toModel() }.toSet()
    }
}
