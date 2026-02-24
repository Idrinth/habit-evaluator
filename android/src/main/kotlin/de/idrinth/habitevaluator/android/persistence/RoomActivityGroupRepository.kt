package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.ActivityGroup
import de.idrinth.habitevaluator.shared.repository.ActivityGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomActivityGroupRepository(private val dao: ActivityLogDao) : ActivityGroupRepository {

    override fun save(group: ActivityGroup): ActivityGroup = runBlocking {
        dao.insertGroup(group.toEntity())
        group
    }

    override fun findById(id: String): Optional<ActivityGroup> = runBlocking {
        val entity = dao.findGroupById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<ActivityGroup> = runBlocking {
        dao.findAllGroups().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteGroupWithLinks(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.groupExistsById(id)
    }

    override fun findByUserId(userId: String): List<ActivityGroup> = runBlocking {
        dao.findGroupsByUserId(userId).map { it.toModel() }
    }

    fun observeByUserId(userId: String): Flow<List<ActivityGroupEntity>> =
        dao.observeGroupsByUserId(userId)

    suspend fun saveSuspend(group: ActivityGroup): ActivityGroup {
        dao.insertGroup(group.toEntity())
        return group
    }
}
