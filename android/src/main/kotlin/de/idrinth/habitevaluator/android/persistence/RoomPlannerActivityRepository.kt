package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.PlannerActivity
import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.repository.PlannerActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomPlannerActivityRepository(private val dao: DayPlannerDao) : PlannerActivityRepository {

    override fun save(activity: PlannerActivity): PlannerActivity = runBlocking {
        dao.saveActivityWithLinks(activity.toEntity(), activity.toGroupIds())
        activity
    }

    override fun findById(id: String): Optional<PlannerActivity> = runBlocking {
        val entity = dao.findActivityById(id) ?: return@runBlocking Optional.empty()
        val groupIds = dao.findGroupIdsForActivity(id)
        val groups = groupIds.mapNotNull { dao.findGroupById(it)?.toModel() }.toSet()
        Optional.of(entity.toModel(groups))
    }

    override fun findAll(): List<PlannerActivity> = runBlocking {
        dao.findAllActivities().map { entity ->
            val groupIds = dao.findGroupIdsForActivity(entity.id)
            val groups = groupIds.mapNotNull { dao.findGroupById(it)?.toModel() }.toSet()
            entity.toModel(groups)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteActivityWithLinks(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.activityExistsById(id)
    }

    override fun findByUserId(userId: String): List<PlannerActivity> = runBlocking {
        dao.findActivitiesByUserId(userId).map { entity ->
            val groupIds = dao.findGroupIdsForActivity(entity.id)
            val groups = groupIds.mapNotNull { dao.findGroupById(it)?.toModel() }.toSet()
            entity.toModel(groups)
        }
    }

    override fun findByGroupId(groupId: String): List<PlannerActivity> = runBlocking {
        dao.findActivitiesByGroupId(groupId).map { entity ->
            val groupIds = dao.findGroupIdsForActivity(entity.id)
            val groups = groupIds.mapNotNull { dao.findGroupById(it)?.toModel() }.toSet()
            entity.toModel(groups)
        }
    }

    fun observeByUserId(userId: String): Flow<List<PlannerActivityEntity>> =
        dao.observeActivitiesByUserId(userId)

    suspend fun saveSuspend(activity: PlannerActivity): PlannerActivity {
        dao.saveActivityWithLinks(activity.toEntity(), activity.toGroupIds())
        return activity
    }
}
