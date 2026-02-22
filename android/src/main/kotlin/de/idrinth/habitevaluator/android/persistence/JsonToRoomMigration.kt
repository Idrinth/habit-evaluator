package de.idrinth.habitevaluator.android.persistence

import de.idrinth.habitevaluator.shared.persistence.FileSystemDiaryEntryRepository
import de.idrinth.habitevaluator.shared.persistence.FileSystemDiaryReferenceRepository
import de.idrinth.habitevaluator.shared.persistence.FileSystemEmotionEntryRepository
import de.idrinth.habitevaluator.shared.persistence.FileSystemEmotionPairRepository
import de.idrinth.habitevaluator.shared.persistence.FileSystemHabitCategoryRepository
import de.idrinth.habitevaluator.shared.persistence.FileSystemHabitRepository
import de.idrinth.habitevaluator.shared.persistence.FileSystemSleepEntryRepository
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository
import de.idrinth.habitevaluator.shared.repository.HabitRepository
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository
import java.io.File

class JsonToRoomMigration(
    private val storageDir: File,
    private val habitRepository: HabitRepository,
    private val categoryRepository: HabitCategoryRepository,
    private val diaryReferenceRepository: DiaryReferenceRepository,
    private val diaryEntryRepository: DiaryEntryRepository,
    private val sleepEntryRepository: SleepEntryRepository,
    private val emotionPairRepository: EmotionPairRepository,
    private val emotionEntryRepository: EmotionEntryRepository
) {

    private val jsonFiles = arrayOf(
        "habits.json",
        "categories.json",
        "diary_references.json",
        "diary_entries.json",
        "sleep_entries.json",
        "emotion-pairs.json",
        "emotion-entries.json"
    )

    fun needsMigration(): Boolean {
        if (!storageDir.exists()) return false
        return jsonFiles.any { File(storageDir, it).let { f -> f.exists() && f.length() > 0 } }
    }

    fun migrate() {
        if (!storageDir.exists()) return
        migrateCategories()
        migrateHabits()
        migrateDiaryReferences()
        migrateDiaryEntries()
        migrateSleepEntries()
        migrateEmotionPairs()
        migrateEmotionEntries()
    }

    private fun migrateCategories() {
        val file = File(storageDir, "categories.json")
        if (!file.exists() || file.length() == 0L) return
        val jsonRepo = FileSystemHabitCategoryRepository(storageDir)
        jsonRepo.findAll().forEach { categoryRepository.save(it) }
        file.delete()
    }

    private fun migrateHabits() {
        val file = File(storageDir, "habits.json")
        if (!file.exists() || file.length() == 0L) return
        val jsonRepo = FileSystemHabitRepository(storageDir)
        jsonRepo.findAll().forEach { habitRepository.save(it) }
        file.delete()
    }

    private fun migrateDiaryReferences() {
        val file = File(storageDir, "diary_references.json")
        if (!file.exists() || file.length() == 0L) return
        val jsonRepo = FileSystemDiaryReferenceRepository(storageDir)
        jsonRepo.findAll().forEach { diaryReferenceRepository.save(it) }
        file.delete()
    }

    private fun migrateDiaryEntries() {
        val file = File(storageDir, "diary_entries.json")
        if (!file.exists() || file.length() == 0L) return
        val jsonRepo = FileSystemDiaryEntryRepository(storageDir)
        jsonRepo.setDiaryReferenceRepository(diaryReferenceRepository)
        jsonRepo.findAll().forEach { diaryEntryRepository.save(it) }
        file.delete()
    }

    private fun migrateSleepEntries() {
        val file = File(storageDir, "sleep_entries.json")
        if (!file.exists() || file.length() == 0L) return
        val jsonRepo = FileSystemSleepEntryRepository(storageDir)
        jsonRepo.findAll().forEach { sleepEntryRepository.save(it) }
        file.delete()
    }

    private fun migrateEmotionPairs() {
        val file = File(storageDir, "emotion-pairs.json")
        if (!file.exists() || file.length() == 0L) return
        val jsonRepo = FileSystemEmotionPairRepository(storageDir)
        jsonRepo.findAll().forEach { emotionPairRepository.save(it) }
        file.delete()
    }

    private fun migrateEmotionEntries() {
        val file = File(storageDir, "emotion-entries.json")
        if (!file.exists() || file.length() == 0L) return
        val jsonRepo = FileSystemEmotionEntryRepository(storageDir, emotionPairRepository)
        jsonRepo.findAll().forEach { emotionEntryRepository.save(it) }
        file.delete()
    }
}
