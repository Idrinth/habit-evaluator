package de.idrinth.habitevaluator.android

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import de.idrinth.habitevaluator.android.persistence.AppDatabase
import de.idrinth.habitevaluator.android.persistence.JsonToRoomMigration
import de.idrinth.habitevaluator.android.persistence.RoomActivityLogRepository
import de.idrinth.habitevaluator.android.persistence.RoomDiaryEntryRepository
import de.idrinth.habitevaluator.android.persistence.RoomDiaryReferenceRepository
import de.idrinth.habitevaluator.android.persistence.RoomEmergencyPlanActionRepository
import de.idrinth.habitevaluator.android.persistence.RoomEmergencyPlanStepRepository
import de.idrinth.habitevaluator.android.persistence.RoomEmotionEntryRepository
import de.idrinth.habitevaluator.android.persistence.RoomEmotionPairRepository
import de.idrinth.habitevaluator.android.persistence.RoomFoodLogRepository
import de.idrinth.habitevaluator.android.persistence.RoomFoodTagRepository
import de.idrinth.habitevaluator.android.persistence.RoomHabitCategoryRepository
import de.idrinth.habitevaluator.android.persistence.RoomHabitRepository
import de.idrinth.habitevaluator.android.persistence.RoomMedicationLogRepository
import de.idrinth.habitevaluator.android.persistence.RoomMedicationRepository
import de.idrinth.habitevaluator.android.persistence.RoomSleepEntryRepository
import de.idrinth.habitevaluator.android.persistence.RoomSportLogRepository
import de.idrinth.habitevaluator.shared.api.ApiClient
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository
import de.idrinth.habitevaluator.shared.api.SyncService
import de.idrinth.habitevaluator.shared.api.VersionMismatchException
import de.idrinth.habitevaluator.shared.backup.BackupException
import de.idrinth.habitevaluator.shared.backup.BackupService
import de.idrinth.habitevaluator.shared.model.EmotionPair
import de.idrinth.habitevaluator.shared.model.Habit
import de.idrinth.habitevaluator.shared.model.HabitCategory
import de.idrinth.habitevaluator.shared.model.HabitEntry
import de.idrinth.habitevaluator.shared.model.Medication
import de.idrinth.habitevaluator.shared.model.SleepEntry
import de.idrinth.habitevaluator.shared.model.User
import de.idrinth.habitevaluator.shared.persistence.FileSystemHabitRepository
import de.idrinth.habitevaluator.shared.repository.ActivityLogRepository
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository
import de.idrinth.habitevaluator.shared.repository.DiaryReferenceRepository
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanActionRepository
import de.idrinth.habitevaluator.shared.repository.EmergencyPlanStepRepository
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository
import de.idrinth.habitevaluator.shared.repository.FoodLogRepository
import de.idrinth.habitevaluator.shared.repository.FoodTagRepository
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository
import de.idrinth.habitevaluator.shared.repository.HabitRepository
import de.idrinth.habitevaluator.shared.repository.MedicationLogRepository
import de.idrinth.habitevaluator.shared.repository.MedicationRepository
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository
import de.idrinth.habitevaluator.shared.repository.SportLogRepository
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Collections

class AppViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PLACEHOLDER_USERNAME = "android_user"
    }

    private val db: AppDatabase = AppDatabase.getInstance(application)

    // Room repositories (always local)
    val roomHabitRepository = RoomHabitRepository(db.habitDao())
    val roomCategoryRepository = RoomHabitCategoryRepository(db.habitCategoryDao())
    val roomDiaryEntryRepository = RoomDiaryEntryRepository(db.diaryDao())
    val roomDiaryReferenceRepository = RoomDiaryReferenceRepository(db.diaryDao())
    val roomSleepEntryRepository = RoomSleepEntryRepository(db.sleepEntryDao())
    val roomEmotionPairRepository = RoomEmotionPairRepository(db.emotionDao())
    val roomEmotionEntryRepository = RoomEmotionEntryRepository(db.emotionDao())
    val roomFoodLogRepository = RoomFoodLogRepository(db.foodLogDao())
    val roomFoodTagRepository = RoomFoodTagRepository(db.foodLogDao())
    val roomSportLogRepository = RoomSportLogRepository(db.sportLogDao())
    val roomMedicationRepository = RoomMedicationRepository(db.medicationDao())
    val roomMedicationLogRepository = RoomMedicationLogRepository(db.medicationDao())
    val roomEmergencyPlanStepRepository = RoomEmergencyPlanStepRepository(db.emergencyPlanDao())
    val roomEmergencyPlanActionRepository = RoomEmergencyPlanActionRepository(db.emergencyPlanDao())
    val roomActivityLogRepository = RoomActivityLogRepository(db.activityLogDao())

    // Active repositories (may point to remote when in remote mode)
    private val _habitRepository = MutableStateFlow<HabitRepository>(roomHabitRepository)
    val habitRepository: StateFlow<HabitRepository> = _habitRepository.asStateFlow()

    private val _categoryRepository = MutableStateFlow<HabitCategoryRepository?>(roomCategoryRepository)
    val categoryRepository: StateFlow<HabitCategoryRepository?> = _categoryRepository.asStateFlow()

    val diaryEntryRepository: DiaryEntryRepository get() = roomDiaryEntryRepository
    val diaryReferenceRepository: DiaryReferenceRepository get() = roomDiaryReferenceRepository
    val sleepEntryRepository: SleepEntryRepository get() = roomSleepEntryRepository
    val emotionPairRepository: EmotionPairRepository get() = roomEmotionPairRepository
    val emotionEntryRepository: EmotionEntryRepository get() = roomEmotionEntryRepository
    val foodLogRepository: FoodLogRepository get() = roomFoodLogRepository
    val foodTagRepository: FoodTagRepository get() = roomFoodTagRepository
    val sportLogRepository: SportLogRepository get() = roomSportLogRepository
    val medicationRepository: MedicationRepository get() = roomMedicationRepository
    val medicationLogRepository: MedicationLogRepository get() = roomMedicationLogRepository
    val emergencyPlanStepRepository: EmergencyPlanStepRepository get() = roomEmergencyPlanStepRepository
    val emergencyPlanActionRepository: EmergencyPlanActionRepository get() = roomEmergencyPlanActionRepository
    val activityLogRepository: ActivityLogRepository get() = roomActivityLogRepository

    // Observable state
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _categories = MutableStateFlow<List<HabitCategory>>(emptyList())
    val categories: StateFlow<List<HabitCategory>> = _categories.asStateFlow()

    private val _sleepEntries = MutableStateFlow<List<SleepEntry>>(emptyList())
    val sleepEntries: StateFlow<List<SleepEntry>> = _sleepEntries.asStateFlow()

    private val _emotionPairs = MutableStateFlow<List<EmotionPair>>(emptyList())
    val emotionPairs: StateFlow<List<EmotionPair>> = _emotionPairs.asStateFlow()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _usingRemoteStorage = MutableStateFlow(false)
    val usingRemoteStorage: StateFlow<Boolean> = _usingRemoteStorage.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _localUser = MutableStateFlow<User?>(null)
    val localUser: StateFlow<User?> = _localUser.asStateFlow()

    private val _storageInitialized = MutableStateFlow(false)
    val storageInitialized: StateFlow<Boolean> = _storageInitialized.asStateFlow()

    var apiClient: ApiClient? = null
        private set

    private var localBackupRepository: HabitRepository? = null
    private var localBackupUser: User? = null
    private val backupServiceInstance = BackupService()

    fun initializeStorage() {
        val prefs = getApplication<Application>().getSharedPreferences(
            SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE
        )
        val mode = prefs.getString(SettingsConstants.KEY_STORAGE_MODE, SettingsConstants.MODE_LOCAL)

        if (SettingsConstants.MODE_REMOTE == mode) {
            initializeRemoteStorage(prefs)
        } else {
            initializeLocalStorage()
        }
    }

    fun initializeLocalStorage() {
        _usingRemoteStorage.value = false
        localBackupRepository = null
        localBackupUser = null
        apiClient = null

        val user = getOrCreateLocalUser()
        _currentUser.value = user
        _localUser.value = user
        _habitRepository.value = roomHabitRepository
        _categoryRepository.value = roomCategoryRepository

        // Migrate legacy JSON files to Room if they exist
        val storageDir = File(getApplication<Application>().filesDir, "habit-data")
        val migration = JsonToRoomMigration(
            storageDir,
            roomHabitRepository,
            roomCategoryRepository,
            roomDiaryReferenceRepository,
            roomDiaryEntryRepository,
            roomSleepEntryRepository,
            roomEmotionPairRepository,
            roomEmotionEntryRepository
        )
        if (migration.needsMigration()) {
            migration.migrate()
        }

        _storageInitialized.value = true
        loadCategories()
        loadHabits()
        loadSleepEntries()
        loadEmotionPairs()
        loadMedications()
    }

    private fun initializeRemoteStorage(prefs: android.content.SharedPreferences) {
        val url = prefs.getString(SettingsConstants.KEY_API_URL, "") ?: ""
        val username = prefs.getString(SettingsConstants.KEY_API_USERNAME, "") ?: ""
        val password = prefs.getString(SettingsConstants.KEY_API_PASSWORD, "") ?: ""

        // Set local user for local-only data
        _localUser.value = getOrCreateLocalUser()

        // Migrate legacy JSON files
        val migrationDir = File(getApplication<Application>().filesDir, "habit-data")
        val migration = JsonToRoomMigration(
            migrationDir,
            roomHabitRepository,
            roomCategoryRepository,
            roomDiaryReferenceRepository,
            roomDiaryEntryRepository,
            roomSleepEntryRepository,
            roomEmotionPairRepository,
            roomEmotionEntryRepository
        )
        if (migration.needsMigration()) {
            migration.migrate()
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = ApiClient(url)
                val loggedIn = client.login(username, password)
                if (loggedIn) {
                    apiClient = client
                    val remoteHabitRepo = RemoteHabitRepository(client)
                    val userRepo = RemoteUserRepository(client)
                    val user = userRepo.findAll().firstOrNull()

                    _habitRepository.value = remoteHabitRepo
                    _categoryRepository.value = null
                    _usingRemoteStorage.value = true
                    _currentUser.value = user

                    val storageDir = File(getApplication<Application>().filesDir, "habit-data")
                    localBackupRepository = FileSystemHabitRepository(storageDir)
                    localBackupUser = getOrCreateLocalUser()

                    syncOnStart(url, username, password)

                    withContext(Dispatchers.Main) {
                        _storageInitialized.value = true
                    }
                    loadCategories()
                    loadHabits()
                    loadSleepEntries()
                    loadEmotionPairs()
                    loadMedications()
                    return@launch
                }
            } catch (_: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Failed to connect to remote API, using local storage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            withContext(Dispatchers.Main) {
                initializeLocalStorage()
            }
        }
    }

    private fun syncOnStart(url: String, username: String, password: String) {
        val backupRepo = localBackupRepository ?: return
        val backupUser = localBackupUser ?: return
        try {
            val syncService = SyncService(backupRepo)
            syncService.sync(url, username, password, backupUser, BuildConfig.VERSION_NAME)
        } catch (e: VersionMismatchException) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    "Version mismatch: your version (${e.clientVersion}) does not match server (${e.serverVersion}). Please update before syncing.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (_: IOException) {
            // Sync failure on start is non-fatal
        }
    }

    fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            val cats = if (_usingRemoteStorage.value && apiClient != null) {
                try {
                    apiClient?.get<List<HabitCategory>>(
                        "/api/categories",
                        object : TypeToken<List<HabitCategory>>() {}.type
                    ) ?: emptyList()
                } catch (_: IOException) {
                    emptyList()
                }
            } else {
                val user = _currentUser.value ?: return@launch
                _categoryRepository.value?.findByUserId(user.id) ?: emptyList()
            }
            _categories.value = cats.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name ?: "" })
        }
    }

    fun loadHabits() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value ?: return@launch
            val repo = _habitRepository.value
            _habits.value = repo.findByUserId(user.id)
        }
    }

    fun loadSleepEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _localUser.value ?: return@launch
            _sleepEntries.value = roomSleepEntryRepository.findByUserId(user.id)
        }
    }

    fun loadEmotionPairs() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _localUser.value ?: return@launch
            _emotionPairs.value = roomEmotionPairRepository.findByUserId(user.id)
        }
    }

    fun loadMedications() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _localUser.value ?: return@launch
            _medications.value = roomMedicationRepository.findByUserId(user.id)
        }
    }

    fun saveAllHabits() {
        viewModelScope.launch(Dispatchers.IO) {
            val repo = _habitRepository.value
            _habits.value.forEach { repo.save(it) }
        }
    }

    fun loadDefaults() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = getApplication<Application>().getSharedPreferences(
                    SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE
                )
                val languageSetting = prefs.getString(SettingsConstants.KEY_LANGUAGE, SettingsConstants.LANGUAGE_SYSTEM)
                val language = SettingsConstants.getEffectiveLanguage(languageSetting)

                if (_usingRemoteStorage.value && apiClient != null) {
                    apiClient?.post(
                        "/api/init-defaults?language=$language",
                        Collections.emptyMap<String, Any>(),
                        object : TypeToken<Map<String, Any>>() {}.type
                    )
                } else {
                    val catRepo = _categoryRepository.value
                    val habRepo = _habitRepository.value
                    val user = _currentUser.value
                    if (catRepo != null && user != null) {
                        val initializer = DefaultDataInitializer(catRepo, habRepo)
                        initializer.initializeDefaults(user, language)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), R.string.defaults_loaded, Toast.LENGTH_SHORT).show()
                }
                loadCategories()
                loadHabits()
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.load_defaults_failed, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun performOnStopSync() {
        if (!_usingRemoteStorage.value) return
        val backupRepo = localBackupRepository ?: return
        val backupUser = localBackupUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _habits.value.forEach { habit ->
                val backupCopy = copyHabitForBackup(habit, backupUser)
                backupRepo.save(backupCopy)
            }
            val prefs = getApplication<Application>().getSharedPreferences(
                SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE
            )
            val url = prefs.getString(SettingsConstants.KEY_API_URL, "") ?: ""
            val username = prefs.getString(SettingsConstants.KEY_API_USERNAME, "") ?: ""
            val password = prefs.getString(SettingsConstants.KEY_API_PASSWORD, "") ?: ""
            try {
                val syncService = SyncService(backupRepo)
                syncService.sync(url, username, password, backupUser, BuildConfig.VERSION_NAME)
            } catch (_: VersionMismatchException) {
                // Non-fatal on stop
            } catch (_: IOException) {
                // Best effort
            }
        }
    }

    fun performDailyBackupIfEnabled() {
        val prefs = getApplication<Application>().getSharedPreferences(
            SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE
        )
        val backupEnabled = prefs.getBoolean(SettingsConstants.KEY_BACKUP_ENABLED, false)
        if (!backupEnabled) return
        val password = prefs.getString(SettingsConstants.KEY_BACKUP_PASSWORD, "") ?: ""
        if (password.isEmpty()) return
        val backupLocationUri = prefs.getString(SettingsConstants.KEY_BACKUP_LOCATION_URI, "") ?: ""
        if (backupLocationUri.isNotEmpty()) {
            performDailyBackupToSaf(Uri.parse(backupLocationUri), password)
        } else {
            performDailyBackupToInternal(password)
        }
    }

    private fun performDailyBackupToInternal(password: String) {
        val backupDir = File(getApplication<Application>().filesDir, "backups")
        if (backupServiceInstance.hasTodaysBackup(backupDir)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                backupServiceInstance.createBackup(
                    backupDir, password, _currentUser.value,
                    _habitRepository.value, _categoryRepository.value,
                    diaryEntryRepository, sleepEntryRepository
                )
            } catch (e: BackupException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Backup failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun performDailyBackupToSaf(treeUri: Uri, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val treeDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@launch
                val todaysFilename = backupServiceInstance.todaysBackupFilename
                if (treeDoc.findFile(todaysFilename) != null) return@launch

                val backupBytes = backupServiceInstance.createBackupBytes(
                    password, _currentUser.value,
                    _habitRepository.value, _categoryRepository.value,
                    diaryEntryRepository, sleepEntryRepository
                )
                val newFile = treeDoc.createFile("application/octet-stream", todaysFilename) ?: return@launch
                context.contentResolver.openOutputStream(newFile.uri)?.use { it.write(backupBytes) }
                cleanupOldSafBackups(treeDoc)
            } catch (e: BackupException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cleanupOldSafBackups(treeDoc: DocumentFile) {
        val backups = treeDoc.listFiles()
            .filter { it.isFile && it.name?.endsWith(".backup") == true }
            .sortedByDescending { it.name ?: "" }
            .toMutableList()
        while (backups.size > 30) {
            backups.removeAt(backups.size - 1).delete()
        }
    }

    private fun copyHabitForBackup(source: Habit, backupUser: User): Habit {
        val habit = Habit(source.name, source.description)
        habit.frequencyType = source.frequencyType
        habit.targetFrequency = source.targetFrequency
        habit.maxEntriesPerDay = source.maxEntriesPerDay
        habit.categoryId = source.categoryId
        habit.isPositiveScoring = source.isPositiveScoring
        habit.scoringRule = source.scoringRule
        habit.user = backupUser
        for (sourceEntry in source.entries) {
            val entry = HabitEntry()
            entry.id = sourceEntry.id
            entry.completedAt = sourceEntry.completedAt
            entry.notes = sourceEntry.notes
            entry.value = sourceEntry.value
            habit.addEntry(entry)
        }
        return habit
    }

    private fun getOrCreateLocalUser(): User {
        val prefs = getApplication<Application>().getSharedPreferences(
            SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE
        )
        val userId = prefs.getString("local_user_id", null)
        val user = User(PLACEHOLDER_USERNAME, "placeholder")
        if (userId != null) {
            user.id = userId
        } else {
            prefs.edit().putString("local_user_id", user.id).apply()
        }
        return user
    }
}
