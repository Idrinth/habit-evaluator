package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.HabitCategory
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomHabitCategoryRepository(private val dao: HabitCategoryDao) : HabitCategoryRepository {

    override fun save(category: HabitCategory): HabitCategory = runBlocking {
        dao.saveWithDetails(
            category.toEntity(),
            category.toNameTranslations(),
            category.toDescTranslations()
        )
        category
    }

    override fun findById(id: String): Optional<HabitCategory> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        val names = dao.findNameTranslations(id)
        val descs = dao.findDescTranslations(id)
        Optional.of(entity.toModel(names, descs))
    }

    override fun findAll(): List<HabitCategory> = runBlocking {
        dao.findAll().map { entity ->
            val names = dao.findNameTranslations(entity.id)
            val descs = dao.findDescTranslations(entity.id)
            entity.toModel(names, descs)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteWithDetails(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.existsById(id)
    }

    override fun findByUserId(userId: String): List<HabitCategory> = runBlocking {
        dao.findByUserId(userId).map { entity ->
            val names = dao.findNameTranslations(entity.id)
            val descs = dao.findDescTranslations(entity.id)
            entity.toModel(names, descs)
        }
    }

    fun observeByUserId(userId: String): Flow<List<HabitCategoryEntity>> =
        dao.observeByUserId(userId)

    suspend fun findByUserIdSuspend(userId: String): List<HabitCategory> =
        dao.findByUserId(userId).map { entity ->
            val names = dao.findNameTranslations(entity.id)
            val descs = dao.findDescTranslations(entity.id)
            entity.toModel(names, descs)
        }

    suspend fun saveSuspend(category: HabitCategory): HabitCategory {
        dao.saveWithDetails(
            category.toEntity(),
            category.toNameTranslations(),
            category.toDescTranslations()
        )
        return category
    }
}
