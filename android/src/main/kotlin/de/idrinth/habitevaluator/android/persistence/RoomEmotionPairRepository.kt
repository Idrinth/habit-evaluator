package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.EmotionPair
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomEmotionPairRepository(private val dao: EmotionDao) : EmotionPairRepository {

    override fun save(pair: EmotionPair): EmotionPair = runBlocking {
        dao.insertPair(pair.toEntity())
        pair
    }

    override fun findById(id: String): Optional<EmotionPair> = runBlocking {
        val entity = dao.findPairById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<EmotionPair> = runBlocking {
        dao.findAllPairs().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deletePairById(id)
    }

    override fun findByUserId(userId: String): List<EmotionPair> = runBlocking {
        dao.findPairsByUserId(userId).map { it.toModel() }
    }

    fun observeByUserId(userId: String): Flow<List<EmotionPairEntity>> =
        dao.observePairsByUserId(userId)

    suspend fun saveSuspend(pair: EmotionPair): EmotionPair {
        dao.insertPair(pair.toEntity())
        return pair
    }
}
