package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.DiaryEntry
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomDiaryEntryRepository(
    private val dao: DiaryDao
) : DiaryEntryRepository {

    override fun save(entry: DiaryEntry): DiaryEntry = runBlocking {
        dao.insertEntry(entry.toEntity())
        entry
    }

    override fun findById(id: String): Optional<DiaryEntry> = runBlocking {
        val entity = dao.findEntryById(id) ?: return@runBlocking Optional.empty()
        val ref = entity.diaryReferenceId?.let { dao.findReferenceById(it)?.toModel() }
        Optional.of(entity.toModel(ref))
    }

    override fun findAll(): List<DiaryEntry> = runBlocking {
        dao.findAllEntries().map { entity ->
            val ref = entity.diaryReferenceId?.let { dao.findReferenceById(it)?.toModel() }
            entity.toModel(ref)
        }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteEntryById(id)
    }

    override fun findByUserId(userId: String): List<DiaryEntry> = runBlocking {
        dao.findEntriesByUserId(userId).map { entity ->
            val ref = entity.diaryReferenceId?.let { dao.findReferenceById(it)?.toModel() }
            entity.toModel(ref)
        }
    }

    override fun findDistinctDescriptionsByUserId(userId: String): List<String> = runBlocking {
        dao.findDistinctDescriptions(userId)
    }

    override fun findEntriesNeedingMigration(userId: String): List<DiaryEntry> = runBlocking {
        dao.findEntriesNeedingMigration(userId).map { entity ->
            val ref = entity.diaryReferenceId?.let { dao.findReferenceById(it)?.toModel() }
            entity.toModel(ref)
        }
    }

    fun observeByUserId(userId: String): Flow<List<DiaryEntryEntity>> =
        dao.observeEntriesByUserId(userId)

    suspend fun saveSuspend(entry: DiaryEntry): DiaryEntry {
        dao.insertEntry(entry.toEntity())
        return entry
    }
}
