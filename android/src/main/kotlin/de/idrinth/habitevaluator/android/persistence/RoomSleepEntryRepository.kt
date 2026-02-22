package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.SleepEntry
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomSleepEntryRepository(private val dao: SleepEntryDao) : SleepEntryRepository {

    override fun save(entry: SleepEntry): SleepEntry = runBlocking {
        dao.insert(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<SleepEntry> = runBlocking {
        val entity = dao.findById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<SleepEntry> = runBlocking {
        dao.findAll().map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteById(id)
    }

    override fun existsById(id: String): Boolean = runBlocking {
        dao.existsById(id)
    }

    override fun findByUserId(userId: String): List<SleepEntry> = runBlocking {
        dao.findByUserId(userId).map { it.toModel() }
    }

    fun observeByUserId(userId: String): Flow<List<SleepEntryEntity>> =
        dao.observeByUserId(userId)

    suspend fun saveSuspend(entry: SleepEntry): SleepEntry {
        dao.insert(entry.toEntity())
        return entry
    }
}
