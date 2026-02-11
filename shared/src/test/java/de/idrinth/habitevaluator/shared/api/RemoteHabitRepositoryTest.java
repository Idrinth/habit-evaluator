package de.idrinth.habitevaluator.shared.api;

import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RemoteHabitRepositoryTest {

    private StubApiClient stubApiClient;
    private RemoteHabitRepository repository;

    @BeforeEach
    void setUp() {
        stubApiClient = new StubApiClient();
        repository = new RemoteHabitRepository(stubApiClient);
    }

    @Test
    void testFindByIdReturnsHabitWhenFound() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        stubApiClient.setGetResponse(habit);

        Optional<Habit> found = repository.findById(habit.getId());
        assertTrue(found.isPresent());
        assertEquals("Exercise", found.get().getName());
    }

    @Test
    void testFindByIdReturnsEmptyOnIOException() {
        stubApiClient.setGetException(new IOException("Network error"));

        Optional<Habit> found = repository.findById("some-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdReturnsEmptyWhenNull() {
        stubApiClient.setGetResponse(null);

        Optional<Habit> found = repository.findById("some-id");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllReturnsHabits() {
        List<Habit> habits = new ArrayList<>();
        habits.add(new Habit("Exercise", "Daily exercise"));
        habits.add(new Habit("Reading", "Read books"));
        stubApiClient.setGetListResponse(habits);

        List<Habit> result = repository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllReturnsEmptyListOnIOException() {
        stubApiClient.setGetListException(new IOException("Network error"));

        List<Habit> result = repository.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveNewHabitCallsPost() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        Habit savedHabit = new Habit("Exercise", "Daily exercise");
        savedHabit.setId(habit.getId());

        // findById returns empty (doesn't exist) -> save uses POST
        stubApiClient.setGetException(new IOException("Not found"));
        stubApiClient.setPostResponse(savedHabit);

        Habit result = repository.save(habit);
        assertNotNull(result);
        assertEquals("Exercise", result.getName());
        assertTrue(stubApiClient.wasPostCalled());
    }

    @Test
    void testSaveExistingHabitCallsPut() {
        Habit habit = new Habit("Exercise", "Daily exercise");

        // findById returns the habit (exists) -> save uses PUT
        stubApiClient.setGetResponse(habit);
        stubApiClient.setPutResponse(habit);

        Habit result = repository.save(habit);
        assertNotNull(result);
        assertTrue(stubApiClient.wasPutCalled());
    }

    @Test
    void testSaveThrowsRuntimeExceptionOnIOException() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        stubApiClient.setGetException(new IOException("Not found"));
        stubApiClient.setPostException(new IOException("Save failed"));

        assertThrows(RuntimeException.class, () -> repository.save(habit));
    }

    @Test
    void testDeleteByIdSucceeds() {
        stubApiClient.setDeleteSuccess(true);

        assertDoesNotThrow(() -> repository.deleteById("some-id"));
        assertTrue(stubApiClient.wasDeleteCalled());
    }

    @Test
    void testDeleteByIdThrowsRuntimeExceptionOnIOException() {
        stubApiClient.setDeleteException(new IOException("Delete failed"));

        assertThrows(RuntimeException.class, () -> repository.deleteById("some-id"));
    }

    @Test
    void testExistsByIdReturnsTrueWhenFound() {
        Habit habit = new Habit("Exercise", "Daily exercise");
        stubApiClient.setGetResponse(habit);

        assertTrue(repository.existsById(habit.getId()));
    }

    @Test
    void testExistsByIdReturnsFalseWhenNotFound() {
        stubApiClient.setGetException(new IOException("Not found"));

        assertFalse(repository.existsById("nonexistent-id"));
    }

    @Test
    void testFindByUserIdDelegatesToFindAll() {
        List<Habit> habits = new ArrayList<>();
        habits.add(new Habit("Exercise", "Daily exercise"));
        stubApiClient.setGetListResponse(habits);

        List<Habit> result = repository.findByUserId("any-user-id");
        assertEquals(1, result.size());
    }

    @Test
    void testAddEntrySucceeds() {
        HabitEntry entry = new HabitEntry();
        entry.setCompletedAt(LocalDateTime.now());
        entry.setValue(1);
        stubApiClient.setPostResponse(entry);

        HabitEntry result = repository.addEntry("habit-id", entry);
        assertNotNull(result);
    }

    @Test
    void testAddEntryThrowsRuntimeExceptionOnIOException() {
        HabitEntry entry = new HabitEntry();
        stubApiClient.setPostException(new IOException("Network error"));

        assertThrows(RuntimeException.class, () -> repository.addEntry("habit-id", entry));
    }

    /**
     * Stub ApiClient that avoids HTTP connections for testing.
     */
    static class StubApiClient extends ApiClient {

        private Object getResponse;
        private IOException getException;
        private Object getListResponse;
        private IOException getListException;
        private Object postResponse;
        private IOException postException;
        private Object putResponse;
        private IOException putException;
        private boolean deleteSuccess;
        private IOException deleteException;

        private boolean postCalled;
        private boolean putCalled;
        private boolean deleteCalled;

        StubApiClient() {
            super("http://localhost:8080");
        }

        void setGetResponse(Object response) {
            this.getResponse = response;
            this.getException = null;
        }

        void setGetException(IOException exception) {
            this.getException = exception;
            this.getResponse = null;
        }

        void setGetListResponse(Object response) {
            this.getListResponse = response;
            this.getListException = null;
        }

        void setGetListException(IOException exception) {
            this.getListException = exception;
            this.getListResponse = null;
        }

        void setPostResponse(Object response) {
            this.postResponse = response;
            this.postException = null;
        }

        void setPostException(IOException exception) {
            this.postException = exception;
            this.postResponse = null;
        }

        void setPutResponse(Object response) {
            this.putResponse = response;
            this.putException = null;
        }

        void setPutException(IOException exception) {
            this.putException = exception;
            this.putResponse = null;
        }

        void setDeleteSuccess(boolean success) {
            this.deleteSuccess = success;
            this.deleteException = null;
        }

        void setDeleteException(IOException exception) {
            this.deleteException = exception;
        }

        boolean wasPostCalled() {
            return postCalled;
        }

        boolean wasPutCalled() {
            return putCalled;
        }

        boolean wasDeleteCalled() {
            return deleteCalled;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(String path, Type responseType) throws IOException {
            if (getException != null) {
                throw getException;
            }
            return (T) getResponse;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> getList(String path, Type listType) throws IOException {
            if (getListException != null) {
                throw getListException;
            }
            return (List<T>) getListResponse;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T post(String path, Object requestBody, Type responseType) throws IOException {
            postCalled = true;
            if (postException != null) {
                throw postException;
            }
            return (T) postResponse;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T put(String path, Object requestBody, Type responseType) throws IOException {
            putCalled = true;
            if (putException != null) {
                throw putException;
            }
            return (T) putResponse;
        }

        @Override
        public void delete(String path) throws IOException {
            deleteCalled = true;
            if (deleteException != null) {
                throw deleteException;
            }
        }
    }
}
