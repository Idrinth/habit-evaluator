package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.PlannerGroup
import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomWeekPlannerSlotRepository(private val dao: DayPlannerDao) : WeekPlannerSlotRepository {

    override fun save(slot: WeekPlannerSlot): WeekPlannerSlot = runBlocking {
        dao.saveSlotWithGroups(slot.toEntity(), slot.toGroupIds())
        slot
    }

    override fun findById(id: String): Optional<WeekPlannerSlot> = runBlocking {
        val entity = dao.findSlotById(id) ?: return@runBlocking Optional.empty()
        val groups = resolveGroups(entity.id)
        Optional.of(entity.toModel(groups))
    }

    override fun findAll(): List<WeekPlannerSlot> = runBlocking {
        dao.findAllSlots().map { entity ->
            val groups = resolveGroups(entity.id)
            entity.toModel(groups)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteSlotWithLinks(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.slotExistsById(id)
    }

    override fun findByUserId(userId: String): List<WeekPlannerSlot> = runBlocking {
        dao.findSlotsByUserId(userId).map { entity ->
            val groups = resolveGroups(entity.id)
            entity.toModel(groups)
        }
    }

    override fun findByUserIdAndDayOfWeek(userId: String, dayOfWeek: Int): List<WeekPlannerSlot> = runBlocking {
        dao.findSlotsByUserIdAndDayOfWeek(userId, dayOfWeek).map { entity ->
            val groups = resolveGroups(entity.id)
            entity.toModel(groups)
        }
    }

    fun observeByUserId(userId: String): Flow<List<WeekPlannerSlotEntity>> =
        dao.observeSlotsByUserId(userId)

    suspend fun saveSuspend(slot: WeekPlannerSlot): WeekPlannerSlot {
        dao.saveSlotWithGroups(slot.toEntity(), slot.toGroupIds())
        return slot
    }

    private suspend fun resolveGroups(slotId: String): Set<PlannerGroup> {
        val groupIds = dao.findGroupIdsForSlot(slotId)
        val groups = mutableSetOf<PlannerGroup>()
        for (gid in groupIds) {
            dao.findGroupById(gid)?.toModel()?.let { groups.add(it) }
        }
        return groups
    }
}
