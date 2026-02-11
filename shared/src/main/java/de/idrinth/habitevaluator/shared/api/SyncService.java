package de.idrinth.habitevaluator.shared.api;

import com.google.gson.reflect.TypeToken;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles bidirectional sync between local and remote habit data.
 * Connects to a remote server, pushes local habits, and pulls remote habits.
 * Habits are matched by name for merging; entries are merged by ID.
 */
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);
    private static final Type SYNC_DATA_TYPE = new TypeToken<SyncData>() {}.getType();

    private final HabitRepository localHabitRepository;

    public SyncService(HabitRepository localHabitRepository) {
        this.localHabitRepository = localHabitRepository;
    }

    /**
     * Performs a bidirectional sync with the remote server.
     * Validates that the client and server API versions match (major and minor)
     * before syncing. Throws {@link VersionMismatchException} on mismatch.
     *
     * @param serverUrl     the remote server URL
     * @param username      the remote username
     * @param password      the remote password
     * @param localUser     the local user whose habits to sync
     * @param clientVersion the local application version (e.g. "0.1.0-SNAPSHOT")
     * @return a result describing what happened
     * @throws VersionMismatchException if client and server versions do not match
     * @throws IOException              if the connection or sync fails
     */
    public SyncResult sync(String serverUrl, String username, String password, User localUser, String clientVersion) throws IOException {
        ApiClient apiClient = new ApiClient(serverUrl);

        String serverVersion = apiClient.fetchVersion();
        String maskedClientVersion = ApiClient.maskBugfixVersion(clientVersion);
        if (serverVersion == null || !serverVersion.equals(maskedClientVersion)) {
            logger.warn("Version mismatch: client {} (masked: {}) vs server {}", clientVersion, maskedClientVersion, serverVersion);
            throw new VersionMismatchException(maskedClientVersion, serverVersion);
        }

        boolean loggedIn = apiClient.login(username, password);
        if (!loggedIn) {
            throw new IOException("Authentication failed");
        }

        List<Habit> localHabits = localHabitRepository.findByUserId(localUser.getId());

        SyncData outgoing = new SyncData(localHabits);
        SyncData incoming = apiClient.post("/api/sync", outgoing, SYNC_DATA_TYPE);

        List<Habit> remoteHabits = incoming.getHabits();

        return mergeToLocal(localHabits, remoteHabits, localUser);
    }

    private SyncResult mergeToLocal(List<Habit> localHabits, List<Habit> remoteHabits, User localUser) {
        int pushed = 0;
        int pulled = 0;
        int merged = 0;

        Map<String, Habit> localByName = localHabits.stream()
                .collect(Collectors.toMap(Habit::getName, Function.identity(), (a, b) -> a));

        Set<String> processedNames = new HashSet<>();

        for (Habit remoteHabit : remoteHabits) {
            Habit localHabit = localByName.get(remoteHabit.getName());
            if (localHabit == null) {
                // Remote-only habit: pull to local
                Habit newLocal = copyHabitForLocal(remoteHabit, localUser);
                localHabitRepository.save(newLocal);
                pulled++;
            } else {
                // Exists on both sides: merge entries
                boolean hadNewEntries = mergeEntries(localHabit, remoteHabit);
                if (hadNewEntries) {
                    localHabitRepository.save(localHabit);
                    merged++;
                }
                processedNames.add(remoteHabit.getName());
            }
        }

        // Count local-only habits that were pushed to remote
        for (Habit localHabit : localHabits) {
            if (!processedNames.contains(localHabit.getName())) {
                pushed++;
            }
        }

        return new SyncResult(pushed, pulled, merged);
    }

    private Habit copyHabitForLocal(Habit source, User localUser) {
        Habit habit = new Habit(source.getName(), source.getDescription());
        habit.setFrequencyType(source.getFrequencyType());
        habit.setTargetFrequency(source.getTargetFrequency());
        habit.setCategoryId(source.getCategoryId());
        habit.setPositiveScoring(source.isPositiveScoring());
        habit.setUser(localUser);
        for (HabitEntry sourceEntry : source.getEntries()) {
            HabitEntry entry = new HabitEntry();
            entry.setCompletedAt(sourceEntry.getCompletedAt());
            entry.setNotes(sourceEntry.getNotes());
            entry.setValue(sourceEntry.getValue());
            habit.addEntry(entry);
        }
        return habit;
    }

    private boolean mergeEntries(Habit local, Habit remote) {
        Set<String> localEntryIds = local.getEntries().stream()
                .map(HabitEntry::getId)
                .collect(Collectors.toSet());

        List<HabitEntry> newEntries = new ArrayList<>();
        for (HabitEntry remoteEntry : remote.getEntries()) {
            if (!localEntryIds.contains(remoteEntry.getId())) {
                HabitEntry entry = new HabitEntry();
                entry.setId(remoteEntry.getId());
                entry.setCompletedAt(remoteEntry.getCompletedAt());
                entry.setNotes(remoteEntry.getNotes());
                entry.setValue(remoteEntry.getValue());
                newEntries.add(entry);
            }
        }

        for (HabitEntry entry : newEntries) {
            local.addEntry(entry);
        }

        return !newEntries.isEmpty();
    }

    /**
     * Describes the result of a sync operation.
     */
    public static class SyncResult {

        private final int pushed;
        private final int pulled;
        private final int merged;

        public SyncResult(int pushed, int pulled, int merged) {
            this.pushed = pushed;
            this.pulled = pulled;
            this.merged = merged;
        }

        public int getPushed() {
            return pushed;
        }

        public int getPulled() {
            return pulled;
        }

        public int getMerged() {
            return merged;
        }
    }
}
