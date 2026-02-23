package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.repository.PlannerGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomPlannerGroupRepository(private val dao: DayPlannerDao) : PlannerGroupRepository {

    override fun save(group: PlannerGroup): PlannerGroup = runBlocking {
        dao.insertGroup(group.toEntity())
        group
    }

    override fun findById(id: String): Optional<PlannerGroup> = runBlocking {
        val entity = dao.findGroupById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<PlannerGroup> = runBlocking {
        dao.findAllGroups().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteGroupWithLinks(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.groupExistsById(id)
    }

    override fun findByUserId(userId: String): List<PlannerGroup> = runBlocking {
        dao.findGroupsByUserId(userId).map { it.toModel() }
    }

    fun observeByUserId(userId: String): Flow<List<PlannerGroupEntity>> =
        dao.observeGroupsByUserId(userId)

    suspend fun saveSuspend(group: PlannerGroup): PlannerGroup {
        dao.insertGroup(group.toEntity())
        return group
    }
}
