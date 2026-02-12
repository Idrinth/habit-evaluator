package de.idrinth.habitevaluator.shared.api;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SyncServiceTest {

    private InMemoryHabitRepository repository;
    private SyncService syncService;
    private User localUser;

    @BeforeEach
    void setUp() {
        repository = new InMemoryHabitRepository();
        syncService = new SyncService(repository);
        localUser = new User("testuser", "password");
    }

    // --- SyncResult tests ---

    @Test
    void testSyncResultConstructorAndGetters() {
        SyncService.SyncResult result = new SyncService.SyncResult(3, 2, 1);
        assertEquals(3, result.getPushed());
        assertEquals(2, result.getPulled());
        assertEquals(1, result.getMerged());
    }

    @Test
    void testSyncResultZeros() {
        SyncService.SyncResult result = new SyncService.SyncResult(0, 0, 0);
        assertEquals(0, result.getPushed());
        assertEquals(0, result.getPulled());
        assertEquals(0, result.getMerged());
    }

    // --- mergeToLocal tests ---

    @Test
    void testMergeToLocalBothEmpty() throws Exception {
        SyncService.SyncResult result = invokeMergeToLocal(
                Collections.emptyList(), Collections.emptyList(), localUser);

        assertEquals(0, result.getPushed());
        assertEquals(0, result.getPulled());
        assertEquals(0, result.getMerged());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void testMergeToLocalOnlyLocalHabits() throws Exception {
        Habit localHabit1 = createHabit("Exercise", "Daily exercise");
        localHabit1.setUser(localUser);
        Habit localHabit2 = createHabit("Reading", "Read a book");
        localHabit2.setUser(localUser);

        SyncService.SyncResult result = invokeMergeToLocal(
                Arrays.asList(localHabit1, localHabit2), Collections.emptyList(), localUser);

        assertEquals(2, result.getPushed());
        assertEquals(0, result.getPulled());
        assertEquals(0, result.getMerged());
    }

    @Test
    void testMergeToLocalOnlyRemoteHabits() throws Exception {
        Habit remoteHabit1 = createHabit("Meditation", "Morning meditation");
        HabitEntry entry1 = createEntry("entry-1");
        remoteHabit1.addEntry(entry1);

        Habit remoteHabit2 = createHabit("Journaling", "Evening journaling");

        SyncService.SyncResult result = invokeMergeToLocal(
                Collections.emptyList(), Arrays.asList(remoteHabit1, remoteHabit2), localUser);

        assertEquals(0, result.getPushed());
        assertEquals(2, result.getPulled());
        assertEquals(0, result.getMerged());

        // Verify habits were saved to local repository
        List<Habit> saved = repository.findAll();
        assertEquals(2, saved.size());
    }

    @Test
    void testMergeToLocalPulledHabitHasCorrectUser() throws Exception {
        Habit remoteHabit = createHabit("Meditation", "Morning meditation");
        User remoteUser = new User("remoteuser", "pass");
        remoteHabit.setUser(remoteUser);

        SyncService.SyncResult result = invokeMergeToLocal(
                Collections.emptyList(), Collections.singletonList(remoteHabit), localUser);

        assertEquals(1, result.getPulled());

        Habit saved = repository.findAll().get(0);
        assertEquals(localUser, saved.getUser());
    }

    @Test
    void testMergeToLocalPulledHabitCopiesProperties() throws Exception {
        Habit remoteHabit = createHabit("Meditation", "Morning meditation");
        remoteHabit.setFrequencyType(FrequencyType.WEEKLY);
        remoteHabit.setTargetFrequency(3);
        remoteHabit.setCategoryId("cat-123");
        remoteHabit.setPositiveScoring(false);

        HabitEntry remoteEntry = createEntry("remote-entry-1");
        remoteEntry.setNotes("test note");
        remoteEntry.setValue(5);
        remoteEntry.setCompletedAt(LocalDateTime.of(2025, 1, 15, 10, 0));
        remoteHabit.addEntry(remoteEntry);

        invokeMergeToLocal(Collections.emptyList(), Collections.singletonList(remoteHabit), localUser);

        Habit saved = repository.findAll().get(0);
        assertEquals("Meditation", saved.getName());
        assertEquals("Morning meditation", saved.getDescription());
        assertEquals(FrequencyType.WEEKLY, saved.getFrequencyType());
        assertEquals(3, saved.getTargetFrequency());
        assertEquals("cat-123", saved.getCategoryId());
        assertFalse(saved.isPositiveScoring());
        assertEquals(localUser, saved.getUser());

        // Verify entry was copied
        assertEquals(1, saved.getEntries().size());
        HabitEntry copiedEntry = saved.getEntries().get(0);
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 0), copiedEntry.getCompletedAt());
        assertEquals("test note", copiedEntry.getNotes());
        assertEquals(5, copiedEntry.getValue());
    }

    @Test
    void testMergeToLocalPulledHabitGetsNewId() throws Exception {
        Habit remoteHabit = createHabit("Meditation", "Morning meditation");
        String remoteId = remoteHabit.getId();

        invokeMergeToLocal(Collections.emptyList(), Collections.singletonList(remoteHabit), localUser);

        Habit saved = repository.findAll().get(0);
        // copyHabitForLocal creates a new Habit with a new UUID
        assertNotEquals(remoteId, saved.getId());
    }

    @Test
    void testMergeToLocalMatchingHabitsNoNewEntries() throws Exception {
        Habit localHabit = createHabit("Exercise", "Daily exercise");
        localHabit.setUser(localUser);
        HabitEntry localEntry = createEntry("shared-entry-1");
        localHabit.addEntry(localEntry);

        Habit remoteHabit = createHabit("Exercise", "Daily exercise");
        HabitEntry remoteEntry = createEntry("shared-entry-1");
        remoteHabit.addEntry(remoteEntry);

        SyncService.SyncResult result = invokeMergeToLocal(
                Collections.singletonList(localHabit), Collections.singletonList(remoteHabit), localUser);

        assertEquals(0, result.getPushed());
        assertEquals(0, result.getPulled());
        assertEquals(0, result.getMerged());
    }

    @Test
    void testMergeToLocalMatchingHabitsWithNewRemoteEntries() throws Exception {
        Habit localHabit = createHabit("Exercise", "Daily exercise");
        localHabit.setUser(localUser);
        HabitEntry localEntry = createEntry("entry-1");
        localHabit.addEntry(localEntry);
        repository.save(localHabit);

        Habit remoteHabit = createHabit("Exercise", "Daily exercise");
        HabitEntry existingEntry = createEntry("entry-1");
        remoteHabit.addEntry(existingEntry);
        HabitEntry newEntry = createEntry("entry-2");
        newEntry.setCompletedAt(LocalDateTime.of(2025, 2, 1, 9, 0));
        newEntry.setNotes("remote note");
        newEntry.setValue(3);
        remoteHabit.addEntry(newEntry);

        SyncService.SyncResult result = invokeMergeToLocal(
                Collections.singletonList(localHabit), Collections.singletonList(remoteHabit), localUser);

        assertEquals(0, result.getPushed());
        assertEquals(0, result.getPulled());
        assertEquals(1, result.getMerged());

        // Verify the local habit got the new entry
        assertEquals(2, localHabit.getEntries().size());
    }

    @Test
    void testMergeToLocalMergedEntryCopiesFields() throws Exception {
        Habit localHabit = createHabit("Exercise", "Daily exercise");
        localHabit.setUser(localUser);
        repository.save(localHabit);

        Habit remoteHabit = createHabit("Exercise", "Daily exercise");
        HabitEntry remoteEntry = createEntry("new-entry-1");
        remoteEntry.setCompletedAt(LocalDateTime.of(2025, 3, 10, 14, 30));
        remoteEntry.setNotes("merged note");
        remoteEntry.setValue(7);
        remoteHabit.addEntry(remoteEntry);

        invokeMergeToLocal(
                Collections.singletonList(localHabit), Collections.singletonList(remoteHabit), localUser);

        assertEquals(1, localHabit.getEntries().size());
        HabitEntry mergedEntry = localHabit.getEntries().get(0);
        assertEquals("new-entry-1", mergedEntry.getId());
        assertEquals(LocalDateTime.of(2025, 3, 10, 14, 30), mergedEntry.getCompletedAt());
        assertEquals("merged note", mergedEntry.getNotes());
        assertEquals(7, mergedEntry.getValue());
    }

    @Test
    void testMergeToLocalMixedScenario() throws Exception {
        // Local-only habit
        Habit localOnly = createHabit("Running", "Morning run");
        localOnly.setUser(localUser);

        // Habit on both sides with new remote entries
        Habit localShared = createHabit("Meditation", "Daily meditation");
        localShared.setUser(localUser);
        HabitEntry sharedEntry = createEntry("shared-1");
        localShared.addEntry(sharedEntry);
        repository.save(localShared);

        Habit remoteShared = createHabit("Meditation", "Daily meditation");
        HabitEntry remoteSharedExisting = createEntry("shared-1");
        remoteShared.addEntry(remoteSharedExisting);
        HabitEntry remoteSharedNew = createEntry("new-remote-1");
        remoteShared.addEntry(remoteSharedNew);

        // Remote-only habit
        Habit remoteOnly = createHabit("Yoga", "Evening yoga");
        HabitEntry yogaEntry = createEntry("yoga-entry-1");
        remoteOnly.addEntry(yogaEntry);

        SyncService.SyncResult result = invokeMergeToLocal(
                Arrays.asList(localOnly, localShared),
                Arrays.asList(remoteShared, remoteOnly),
                localUser);

        assertEquals(1, result.getPushed());   // Running
        assertEquals(1, result.getPulled());   // Yoga
        assertEquals(1, result.getMerged());   // Meditation
    }

    @Test
    void testMergeToLocalMatchingHabitsOnlyNewRemoteEntriesMerged() throws Exception {
        Habit localHabit = createHabit("Exercise", "Daily exercise");
        localHabit.setUser(localUser);
        HabitEntry local1 = createEntry("entry-1");
        HabitEntry local2 = createEntry("entry-2");
        localHabit.addEntry(local1);
        localHabit.addEntry(local2);
        repository.save(localHabit);

        Habit remoteHabit = createHabit("Exercise", "Daily exercise");
        HabitEntry remote1 = createEntry("entry-1"); // exists locally
        HabitEntry remote2 = createEntry("entry-3"); // new
        HabitEntry remote3 = createEntry("entry-4"); // new
        remoteHabit.addEntry(remote1);
        remoteHabit.addEntry(remote2);
        remoteHabit.addEntry(remote3);

        invokeMergeToLocal(
                Collections.singletonList(localHabit), Collections.singletonList(remoteHabit), localUser);

        // Local should now have 4 entries: entry-1, entry-2, entry-3, entry-4
        assertEquals(4, localHabit.getEntries().size());
    }

    @Test
    void testMergeToLocalMultipleRemoteOnlyHabits() throws Exception {
        Habit remote1 = createHabit("Habit A", "Description A");
        Habit remote2 = createHabit("Habit B", "Description B");
        Habit remote3 = createHabit("Habit C", "Description C");

        SyncService.SyncResult result = invokeMergeToLocal(
                Collections.emptyList(), Arrays.asList(remote1, remote2, remote3), localUser);

        assertEquals(0, result.getPushed());
        assertEquals(3, result.getPulled());
        assertEquals(0, result.getMerged());
        assertEquals(3, repository.findAll().size());
    }

    @Test
    void testMergeToLocalMultipleLocalOnlyHabits() throws Exception {
        Habit local1 = createHabit("Habit X", "Desc X");
        local1.setUser(localUser);
        Habit local2 = createHabit("Habit Y", "Desc Y");
        local2.setUser(localUser);

        SyncService.SyncResult result = invokeMergeToLocal(
                Arrays.asList(local1, local2), Collections.emptyList(), localUser);

        assertEquals(2, result.getPushed());
        assertEquals(0, result.getPulled());
        assertEquals(0, result.getMerged());
    }

    @Test
    void testMergeToLocalMatchingHabitSavedToRepository() throws Exception {
        Habit localHabit = createHabit("Exercise", "Daily exercise");
        localHabit.setUser(localUser);
        repository.save(localHabit);

        Habit remoteHabit = createHabit("Exercise", "Daily exercise");
        HabitEntry newEntry = createEntry("new-1");
        remoteHabit.addEntry(newEntry);

        invokeMergeToLocal(
                Collections.singletonList(localHabit), Collections.singletonList(remoteHabit), localUser);

        // Verify the merged habit was saved back to repository
        Optional<Habit> saved = repository.findById(localHabit.getId());
        assertTrue(saved.isPresent());
        assertEquals(1, saved.get().getEntries().size());
    }

    @Test
    void testMergeToLocalMatchingHabitsNoNewEntriesNotSaved() throws Exception {
        Habit localHabit = createHabit("Exercise", "Daily exercise");
        localHabit.setUser(localUser);
        HabitEntry entry = createEntry("entry-1");
        localHabit.addEntry(entry);

        Habit remoteHabit = createHabit("Exercise", "Daily exercise");
        HabitEntry sameEntry = createEntry("entry-1");
        remoteHabit.addEntry(sameEntry);

        // Repository starts empty - mergeToLocal should not save if no new entries
        SyncService.SyncResult result = invokeMergeToLocal(
                Collections.singletonList(localHabit), Collections.singletonList(remoteHabit), localUser);

        assertEquals(0, result.getMerged());
    }

    @Test
    void testMergeToLocalPulledHabitWithMultipleEntries() throws Exception {
        Habit remoteHabit = createHabit("Stretching", "Morning stretching");
        HabitEntry e1 = createEntry("e1");
        e1.setCompletedAt(LocalDateTime.of(2025, 1, 1, 8, 0));
        e1.setValue(1);
        HabitEntry e2 = createEntry("e2");
        e2.setCompletedAt(LocalDateTime.of(2025, 1, 2, 8, 0));
        e2.setValue(2);
        HabitEntry e3 = createEntry("e3");
        e3.setCompletedAt(LocalDateTime.of(2025, 1, 3, 8, 0));
        e3.setNotes("note3");
        e3.setValue(3);
        remoteHabit.addEntry(e1);
        remoteHabit.addEntry(e2);
        remoteHabit.addEntry(e3);

        invokeMergeToLocal(
                Collections.emptyList(), Collections.singletonList(remoteHabit), localUser);

        Habit saved = repository.findAll().get(0);
        assertEquals(3, saved.getEntries().size());
    }

    @Test
    void testMergeToLocalDuplicateLocalHabitNamesFirstWins() throws Exception {
        // If two local habits have the same name, the Collectors.toMap merge function keeps the first
        Habit localHabit1 = createHabit("Exercise", "First");
        localHabit1.setUser(localUser);
        HabitEntry local1Entry = createEntry("local-1");
        localHabit1.addEntry(local1Entry);

        Habit localHabit2 = createHabit("Exercise", "Second");
        localHabit2.setUser(localUser);

        Habit remoteHabit = createHabit("Exercise", "Remote");
        HabitEntry remoteEntry = createEntry("remote-new-1");
        remoteHabit.addEntry(remoteEntry);

        SyncService.SyncResult result = invokeMergeToLocal(
                Arrays.asList(localHabit1, localHabit2), Collections.singletonList(remoteHabit), localUser);

        // The first local habit should be the one that gets merged
        assertEquals(2, localHabit1.getEntries().size());
        assertEquals(1, result.getMerged());
        // The second duplicate is not in processedNames since only one match per name
        // localHabit2 has name "Exercise" which IS in processedNames, so it's not counted as pushed
        assertEquals(0, result.getPushed());
    }

    // --- mergeEntries tests ---

    @Test
    void testMergeEntriesNoRemoteEntries() throws Exception {
        Habit local = createHabit("Test", "Test");
        HabitEntry localEntry = createEntry("e1");
        local.addEntry(localEntry);

        Habit remote = createHabit("Test", "Test");

        boolean result = invokeMergeEntries(local, remote);
        assertFalse(result);
        assertEquals(1, local.getEntries().size());
    }

    @Test
    void testMergeEntriesAllNew() throws Exception {
        Habit local = createHabit("Test", "Test");

        Habit remote = createHabit("Test", "Test");
        HabitEntry r1 = createEntry("r1");
        HabitEntry r2 = createEntry("r2");
        remote.addEntry(r1);
        remote.addEntry(r2);

        boolean result = invokeMergeEntries(local, remote);
        assertTrue(result);
        assertEquals(2, local.getEntries().size());
    }

    @Test
    void testMergeEntriesAllDuplicate() throws Exception {
        Habit local = createHabit("Test", "Test");
        HabitEntry e1 = createEntry("e1");
        HabitEntry e2 = createEntry("e2");
        local.addEntry(e1);
        local.addEntry(e2);

        Habit remote = createHabit("Test", "Test");
        HabitEntry re1 = createEntry("e1");
        HabitEntry re2 = createEntry("e2");
        remote.addEntry(re1);
        remote.addEntry(re2);

        boolean result = invokeMergeEntries(local, remote);
        assertFalse(result);
        assertEquals(2, local.getEntries().size());
    }

    @Test
    void testMergeEntriesPartialOverlap() throws Exception {
        Habit local = createHabit("Test", "Test");
        HabitEntry e1 = createEntry("e1");
        HabitEntry e2 = createEntry("e2");
        local.addEntry(e1);
        local.addEntry(e2);

        Habit remote = createHabit("Test", "Test");
        HabitEntry re2 = createEntry("e2");  // duplicate
        HabitEntry re3 = createEntry("e3");  // new
        remote.addEntry(re2);
        remote.addEntry(re3);

        boolean result = invokeMergeEntries(local, remote);
        assertTrue(result);
        assertEquals(3, local.getEntries().size());
    }

    @Test
    void testMergeEntriesCopiesFieldsCorrectly() throws Exception {
        Habit local = createHabit("Test", "Test");

        Habit remote = createHabit("Test", "Test");
        HabitEntry remoteEntry = createEntry("re1");
        remoteEntry.setCompletedAt(LocalDateTime.of(2025, 5, 20, 16, 45));
        remoteEntry.setNotes("important note");
        remoteEntry.setValue(42);
        remote.addEntry(remoteEntry);

        invokeMergeEntries(local, remote);

        assertEquals(1, local.getEntries().size());
        HabitEntry merged = local.getEntries().get(0);
        assertEquals("re1", merged.getId());
        assertEquals(LocalDateTime.of(2025, 5, 20, 16, 45), merged.getCompletedAt());
        assertEquals("important note", merged.getNotes());
        assertEquals(42, merged.getValue());
    }

    @Test
    void testMergeEntriesBothEmpty() throws Exception {
        Habit local = createHabit("Test", "Test");
        Habit remote = createHabit("Test", "Test");

        boolean result = invokeMergeEntries(local, remote);
        assertFalse(result);
        assertEquals(0, local.getEntries().size());
    }

    // --- copyHabitForLocal tests ---

    @Test
    void testCopyHabitForLocalBasicProperties() throws Exception {
        Habit source = createHabit("Meditation", "Morning meditation session");
        source.setFrequencyType(FrequencyType.WEEKLY);
        source.setTargetFrequency(5);
        source.setCategoryId("category-abc");
        source.setPositiveScoring(false);

        Habit copy = invokeCopyHabitForLocal(source, localUser);

        assertEquals("Meditation", copy.getName());
        assertEquals("Morning meditation session", copy.getDescription());
        assertEquals(FrequencyType.WEEKLY, copy.getFrequencyType());
        assertEquals(5, copy.getTargetFrequency());
        assertEquals("category-abc", copy.getCategoryId());
        assertFalse(copy.isPositiveScoring());
        assertEquals(localUser, copy.getUser());
    }

    @Test
    void testCopyHabitForLocalNewId() throws Exception {
        Habit source = createHabit("Test", "Test");
        String sourceId = source.getId();

        Habit copy = invokeCopyHabitForLocal(source, localUser);

        assertNotNull(copy.getId());
        assertNotEquals(sourceId, copy.getId());
    }

    @Test
    void testCopyHabitForLocalCopiesEntries() throws Exception {
        Habit source = createHabit("Test", "Test");
        HabitEntry e1 = createEntry("src-e1");
        e1.setCompletedAt(LocalDateTime.of(2025, 6, 1, 12, 0));
        e1.setNotes("note 1");
        e1.setValue(10);
        HabitEntry e2 = createEntry("src-e2");
        e2.setCompletedAt(LocalDateTime.of(2025, 6, 2, 13, 0));
        e2.setNotes("note 2");
        e2.setValue(20);
        source.addEntry(e1);
        source.addEntry(e2);

        Habit copy = invokeCopyHabitForLocal(source, localUser);

        assertEquals(2, copy.getEntries().size());
        HabitEntry ce1 = copy.getEntries().get(0);
        assertEquals(LocalDateTime.of(2025, 6, 1, 12, 0), ce1.getCompletedAt());
        assertEquals("note 1", ce1.getNotes());
        assertEquals(10, ce1.getValue());

        HabitEntry ce2 = copy.getEntries().get(1);
        assertEquals(LocalDateTime.of(2025, 6, 2, 13, 0), ce2.getCompletedAt());
        assertEquals("note 2", ce2.getNotes());
        assertEquals(20, ce2.getValue());
    }

    @Test
    void testCopyHabitForLocalNoEntries() throws Exception {
        Habit source = createHabit("Empty", "No entries");

        Habit copy = invokeCopyHabitForLocal(source, localUser);

        assertNotNull(copy.getEntries());
        assertTrue(copy.getEntries().isEmpty());
    }

    @Test
    void testCopyHabitForLocalDefaultPositiveScoring() throws Exception {
        Habit source = createHabit("Positive", "Positive habit");
        source.setPositiveScoring(true);

        Habit copy = invokeCopyHabitForLocal(source, localUser);

        assertTrue(copy.isPositiveScoring());
    }

    // --- Helper methods ---

    private Habit createHabit(String name, String description) {
        return new Habit(name, description);
    }

    private HabitEntry createEntry(String id) {
        HabitEntry entry = new HabitEntry();
        entry.setId(id);
        entry.setCompletedAt(LocalDateTime.of(2025, 1, 1, 12, 0));
        entry.setValue(1);
        return entry;
    }

    private SyncService.SyncResult invokeMergeToLocal(List<Habit> localHabits, List<Habit> remoteHabits, User user) throws Exception {
        Method method = SyncService.class.getDeclaredMethod("mergeToLocal", List.class, List.class, User.class);
        method.setAccessible(true);
        return (SyncService.SyncResult) method.invoke(syncService, localHabits, remoteHabits, user);
    }

    private boolean invokeMergeEntries(Habit local, Habit remote) throws Exception {
        Method method = SyncService.class.getDeclaredMethod("mergeEntries", Habit.class, Habit.class);
        method.setAccessible(true);
        return (boolean) method.invoke(syncService, local, remote);
    }

    private Habit invokeCopyHabitForLocal(Habit source, User user) throws Exception {
        Method method = SyncService.class.getDeclaredMethod("copyHabitForLocal", Habit.class, User.class);
        method.setAccessible(true);
        return (Habit) method.invoke(syncService, source, user);
    }

    // In-memory repository for testing
    private static class InMemoryHabitRepository implements HabitRepository {
        private final List<Habit> habits = new ArrayList<>();

        @Override
        public Habit save(Habit habit) {
            habits.removeIf(h -> h.getId().equals(habit.getId()));
            habits.add(habit);
            return habit;
        }

        @Override
        public Optional<Habit> findById(String id) {
            return habits.stream().filter(h -> h.getId().equals(id)).findFirst();
        }

        @Override
        public List<Habit> findAll() {
            return new ArrayList<>(habits);
        }

        @Override
        public void deleteById(String id) {
            habits.removeIf(h -> h.getId().equals(id));
        }

        @Override
        public boolean existsById(String id) {
            return habits.stream().anyMatch(h -> h.getId().equals(id));
        }

        @Override
        public List<Habit> findByUserId(String userId) {
            List<Habit> result = new ArrayList<>();
            for (Habit h : habits) {
                if (h.getUser() != null && h.getUser().getId().equals(userId)) {
                    result.add(h);
                }
            }
            return result;
        }
    }
}
