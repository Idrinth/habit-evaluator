package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.FoodTag
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomFoodTagRepository(private val dao: FoodLogDao) : FoodTagRepository {

    override fun save(tag: FoodTag): FoodTag = runBlocking {
        dao.insertTag(tag.toEntity())
        tag
    }

    override fun findById(id: String): Optional<FoodTag> = runBlocking {
        val entity = dao.findTagById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findByUserId(userId: String): List<FoodTag> = runBlocking {
        dao.findTagsByUserId(userId).map { it.toModel() }
    }

    override fun findByNameLowerAndUserId(nameLower: String, userId: String): Optional<FoodTag> = runBlocking {
        val entity = dao.findTagByNameLowerAndUserId(nameLower, userId)
            ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteTagById(id)
    }

    override fun deleteEmptyTags(userId: String) = runBlocking {
        dao.deleteEmptyTags(userId)
    }

    fun linkTagToFoodLog(tagId: String, foodLogId: String) = runBlocking {
        dao.linkTagToFoodLog(FoodLogTagCrossRef(foodLogId, tagId))
    }

    fun unlinkAllTagsFromFoodLog(foodLogId: String) = runBlocking {
        dao.unlinkAllTagsFromFoodLog(foodLogId)
    }

    suspend fun saveSuspend(tag: FoodTag): FoodTag {
        dao.insertTag(tag.toEntity())
        return tag
    }
}
