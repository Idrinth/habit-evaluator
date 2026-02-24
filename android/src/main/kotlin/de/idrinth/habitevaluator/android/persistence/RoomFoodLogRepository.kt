package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.FoodLog
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomFoodLogRepository(private val dao: FoodLogDao) : FoodLogRepository {

    override fun save(entry: FoodLog): FoodLog = runBlocking {
        dao.insert(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<FoodLog> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        val tags = dao.findTagsByFoodLogId(id).map { it.toModel() }.toSet()
        Optional.of(entity.toModel(tags))
    }

    override fun findAll(): List<FoodLog> = runBlocking {
        dao.findAll().map { entity ->
            val tags = dao.findTagsByFoodLogId(entity.id).map { it.toModel() }.toSet()
            entity.toModel(tags)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.unlinkAllTagsFromFoodLog(id)
        dao.deleteById(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.existsById(id)
    }

    override fun findByUserId(userId: String): List<FoodLog> = runBlocking {
        dao.findByUserId(userId).map { entity ->
            val tags = dao.findTagsByFoodLogId(entity.id).map { it.toModel() }.toSet()
            entity.toModel(tags)
        }
    }

    fun observeByUserId(userId: String): Flow<List<FoodLogEntity>> =
        dao.observeByUserId(userId)

    suspend fun saveSuspend(entry: FoodLog): FoodLog {
        dao.insert(entry.toEntity())
        return entry
    }
}
