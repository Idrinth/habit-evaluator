package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.GratitudeEntry
import de.idrinth.habitevaluator.shared.repository.GratitudeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomGratitudeEntryRepository(
    private val dao: GratitudeDao
) : GratitudeEntryRepository {

    override fun save(entry: GratitudeEntry): GratitudeEntry = runBlocking {
        saveSuspend(entry)
    }

    override fun findById(id: String): Optional<GratitudeEntry> = runBlocking {
        findByIdSuspend(id)
    }

    override fun findAll(): List<GratitudeEntry> = runBlocking {
        findAllSuspend()
    }

    override fun deleteById(id: String) = runBlocking {
        deleteByIdSuspend(id)
    }

    override fun findByUserId(userId: String): List<GratitudeEntry> = runBlocking {
        findByUserIdSuspend(userId)
    }

    fun observeByUserId(userId: String): Flow<List<GratitudeEntry>> =
        dao.observeByUserId(userId).map { entities -> entities.map { it.toModel() } }

    suspend fun saveSuspend(entry: GratitudeEntry): GratitudeEntry {
        dao.insert(entry.toEntity())
        return entry
    }

    suspend fun findByIdSuspend(id: String): Optional<GratitudeEntry> {
        val entity = dao.findById(id) ?: return Optional.empty()
        return Optional.of(entity.toModel())
    }

    suspend fun findAllSuspend(): List<GratitudeEntry> {
        return dao.findAll().map { it.toModel() }
    }

    suspend fun deleteByIdSuspend(id: String) {
        dao.deleteById(id)
    }

    suspend fun findByUserIdSuspend(userId: String): List<GratitudeEntry> {
        return dao.findByUserId(userId).map { it.toModel() }
    }
}
