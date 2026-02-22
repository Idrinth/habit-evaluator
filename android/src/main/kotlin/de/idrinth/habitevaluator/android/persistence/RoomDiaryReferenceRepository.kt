package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.model.DiaryReference
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository
import kotlinx.coroutines.runBlocking
import java.util.Optional

class RoomDiaryReferenceRepository(private val dao: DiaryDao) : DiaryReferenceRepository {

    override fun save(reference: DiaryReference): DiaryReference = runBlocking {
        dao.insertReference(reference.toEntity())
        reference
    }

    override fun findById(id: String): Optional<DiaryReference> = runBlocking {
        val entity = dao.findReferenceById(id) ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findAll(): List<DiaryReference> = runBlocking {
        dao.findReferencesByUserId("").ifEmpty { emptyList() }.map { it.toModel() }
    }

    override fun deleteById(id: String) = runBlocking {
        dao.deleteReferenceById(id)
    }

    override fun findByUserId(userId: String): List<DiaryReference> = runBlocking {
        dao.findReferencesByUserId(userId).map { it.toModel() }
    }

    override fun findByUserIdAndDescriptionIgnoreCase(
        userId: String,
        description: String
    ): Optional<DiaryReference> = runBlocking {
        val entity = dao.findReferenceByUserIdAndDescLower(userId, description.lowercase())
            ?: return@runBlocking Optional.empty()
        Optional.of(entity.toModel())
    }

    override fun findDistinctDescriptionsByUserId(userId: String): List<String> = runBlocking {
        dao.findDistinctDescriptions(userId)
    }

    suspend fun saveSuspend(reference: DiaryReference): DiaryReference {
        dao.insertReference(reference.toEntity())
        return reference
    }
}
