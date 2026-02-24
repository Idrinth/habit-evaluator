package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.PlannerActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabasePlannerActivityRepositoryTest {

    private JpaPlannerActivityRepository jpaRepository;
    private DatabasePlannerActivityRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaPlannerActivityRepository.class);
        repository = new DatabasePlannerActivityRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        PlannerActivity activity = new PlannerActivity("Walk");
        when(jpaRepository.save(activity)).thenReturn(activity);

        PlannerActivity result = repository.save(activity);

        assertSame(activity, result);
        verify(jpaRepository).save(activity);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        PlannerActivity activity = new PlannerActivity("Walk");
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(activity));

        Optional<PlannerActivity> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(activity, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<PlannerActivity> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<PlannerActivity> activities = List.of(new PlannerActivity("Walk"), new PlannerActivity("Run"));
        when(jpaRepository.findAll()).thenReturn(activities);

        List<PlannerActivity> result = repository.findAll();

        assertEquals(2, result.size());
        verify(jpaRepository).findAll();
    }

    @Test
    void testDeleteByIdDelegatesToJpa() {
        repository.deleteById("id-1");

        verify(jpaRepository).deleteById("id-1");
    }

    @Test
    void testExistsByIdDelegatesToJpa() {
        when(jpaRepository.existsById("id-1")).thenReturn(true);
        when(jpaRepository.existsById("id-2")).thenReturn(false);

        assertTrue(repository.existsById("id-1"));
        assertFalse(repository.existsById("id-2"));
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<PlannerActivity> activities = List.of(new PlannerActivity("Walk"));
        when(jpaRepository.findByUserId("user-1")).thenReturn(activities);

        List<PlannerActivity> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindByGroupIdDelegatesToJpa() {
        List<PlannerActivity> activities = List.of(new PlannerActivity("Run"));
        when(jpaRepository.findByGroupId("group-1")).thenReturn(activities);

        List<PlannerActivity> result = repository.findByGroupId("group-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByGroupId("group-1");
    }
}
