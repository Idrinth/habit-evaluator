package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.EmotionEntry
import de.idrinth.habitevaluator.shared.model.EmotionPair
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomEmotionEntryRepository(private val dao: EmotionDao) : EmotionEntryRepository {

    override fun save(entry: EmotionEntry): EmotionEntry = runBlocking {
        dao.insertEntry(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<EmotionEntry> = runBlocking {
        val entity = dao.findEntryById(id) ?: return@runBlocking Optional.empty()
        val pair = dao.findPairById(entity.emotionPairId)?.toModel()
        val model = entity.toModel(pair)
        if (model != null) Optional.of(model) else Optional.empty()
    }

    override fun findAll(): List<EmotionEntry> = runBlocking {
        dao.findAllEntries().mapNotNull { entity ->
            val pair = dao.findPairById(entity.emotionPairId)?.toModel()
            entity.toModel(pair)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteEntryById(id)
    }

    override fun findByUserId(userId: String): List<EmotionEntry> = runBlocking {
        val pairs = dao.findPairsByUserId(userId).associate { it.id to it.toModel() }
        dao.findEntriesByUserId(userId).mapNotNull { entity ->
            entity.toModel(pairs[entity.emotionPairId])
        }
    }

    fun observeByUserId(userId: String): Flow<List<EmotionEntryEntity>> =
        dao.observeEntriesByUserId(userId)

    suspend fun saveSuspend(entry: EmotionEntry): EmotionEntry {
        dao.insertEntry(entry.toEntity())
        return entry
    }

    suspend fun findByUserIdWithPairs(userId: String, pairs: Map<String, EmotionPair>): List<EmotionEntry> =
        dao.findEntriesByUserId(userId).mapNotNull { entity ->
            entity.toModel(pairs[entity.emotionPairId])
        }
}
