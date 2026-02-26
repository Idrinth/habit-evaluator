package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.GratitudeEntry
import de.idrinth.habitevaluator.shared.repository.GratitudeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomGratitudeEntryRepository(
    private val dao: GratitudeDao
) : GratitudeEntryRepository {

    override fun save(entry: GratitudeEntry): GratitudeEntry = runBlocking {
        dao.insert(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<GratitudeEntry> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<GratitudeEntry> = runBlocking {
        dao.findAll().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteById(id)
    }

    override fun findByUserId(userId: String): List<GratitudeEntry> = runBlocking {
        dao.findByUserId(userId).map { it.toModel() }
    }

    fun observeByUserId(userId: String): Flow<List<GratitudeEntryEntity>> =
        dao.observeByUserId(userId)

    suspend fun saveSuspend(entry: GratitudeEntry): GratitudeEntry {
        dao.insert(entry.toEntity())
        return entry
    }
}
