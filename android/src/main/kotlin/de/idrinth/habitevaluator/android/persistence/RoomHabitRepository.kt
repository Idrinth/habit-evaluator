package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomHabitRepository(private val dao: HabitDao) : HabitRepository {

    override fun save(habit: Habit): Habit = runBlocking {
        dao.saveWithDetails(
            habit.toEntity(),
            habit.toEntryEntities(),
            habit.toNameTranslations(),
            habit.toDescTranslations()
        )
        habit
    }

    override fun findById(id: String): Optional<Habit> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        val entries = dao.findEntriesByHabitId(id)
        val names = dao.findNameTranslations(id)
        val descs = dao.findDescTranslations(id)
        Optional.of(entity.toModel(entries, names, descs))
    }

    override fun findAll(): List<Habit> = runBlocking {
        dao.findAll().map { entity ->
            val entries = dao.findEntriesByHabitId(entity.id)
            val names = dao.findNameTranslations(entity.id)
            val descs = dao.findDescTranslations(entity.id)
            entity.toModel(entries, names, descs)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteWithDetails(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.existsById(id)
    }

    override fun findByUserId(userId: String): List<Habit> = runBlocking {
        dao.findByUserId(userId).map { entity ->
            val entries = dao.findEntriesByHabitId(entity.id)
            val names = dao.findNameTranslations(entity.id)
            val descs = dao.findDescTranslations(entity.id)
            entity.toModel(entries, names, descs)
        }
    }

    fun observeByUserId(userId: String): Flow<List<HabitEntity>> =
        dao.observeByUserId(userId)

    suspend fun findByIdSuspend(id: String): Habit? {
        val entity = dao.findById(id) ?: return null
        val entries = dao.findEntriesByHabitId(id)
        val names = dao.findNameTranslations(id)
        val descs = dao.findDescTranslations(id)
        return entity.toModel(entries, names, descs)
    }

    suspend fun findByUserIdSuspend(userId: String): List<Habit> =
        dao.findByUserId(userId).map { entity ->
            val entries = dao.findEntriesByHabitId(entity.id)
            val names = dao.findNameTranslations(entity.id)
            val descs = dao.findDescTranslations(entity.id)
            entity.toModel(entries, names, descs)
        }

    suspend fun saveSuspend(habit: Habit): Habit {
        dao.saveWithDetails(
            habit.toEntity(),
            habit.toEntryEntities(),
            habit.toNameTranslations(),
            habit.toDescTranslations()
        )
        return habit
    }
}
