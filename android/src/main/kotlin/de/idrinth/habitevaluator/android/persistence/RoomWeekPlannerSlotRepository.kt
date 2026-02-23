package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.WeekPlannerSlot
import de.idrinth.habitevaluator.shared.repository.WeekPlannerSlotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomWeekPlannerSlotRepository(private val dao: DayPlannerDao) : WeekPlannerSlotRepository {

    override fun save(slot: WeekPlannerSlot): WeekPlannerSlot = runBlocking {
        dao.insertSlot(slot.toEntity())
        slot
    }

    override fun findById(id: String): Optional<WeekPlannerSlot> = runBlocking {
        val entity = dao.findSlotById(id) ?: return@runBlocking Optional.empty()
        val group = entity.groupId?.let { dao.findGroupById(it)?.toModel() }
        Optional.of(entity.toModel(group))
    }

    override fun findAll(): List<WeekPlannerSlot> = runBlocking {
        dao.findAllSlots().map { entity ->
            val group = entity.groupId?.let { dao.findGroupById(it)?.toModel() }
            entity.toModel(group)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteSlotById(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.slotExistsById(id)
    }

    override fun findByUserId(userId: String): List<WeekPlannerSlot> = runBlocking {
        dao.findSlotsByUserId(userId).map { entity ->
            val group = entity.groupId?.let { dao.findGroupById(it)?.toModel() }
            entity.toModel(group)
        }
    }

    override fun findByUserIdAndDayOfWeek(userId: String, dayOfWeek: Int): List<WeekPlannerSlot> = runBlocking {
        dao.findSlotsByUserIdAndDayOfWeek(userId, dayOfWeek).map { entity ->
            val group = entity.groupId?.let { dao.findGroupById(it)?.toModel() }
            entity.toModel(group)
        }
    }

    fun observeByUserId(userId: String): Flow<List<WeekPlannerSlotEntity>> =
        dao.observeSlotsByUserId(userId)

    suspend fun saveSuspend(slot: WeekPlannerSlot): WeekPlannerSlot {
        dao.insertSlot(slot.toEntity())
        return slot
    }
}
