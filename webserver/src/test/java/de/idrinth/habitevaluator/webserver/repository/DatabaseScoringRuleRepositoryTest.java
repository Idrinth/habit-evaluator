package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.ScoringRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseScoringRuleRepositoryTest {

    private JpaScoringRuleRepository jpaRepository;
    private DatabaseScoringRuleRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaScoringRuleRepository.class);
        repository = new DatabaseScoringRuleRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        ScoringRule rule = new ScoringRule();
        when(jpaRepository.save(rule)).thenReturn(rule);

        ScoringRule result = repository.save(rule);

        assertSame(rule, result);
        verify(jpaRepository).save(rule);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        ScoringRule rule = new ScoringRule();
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(rule));

        Optional<ScoringRule> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(rule, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<ScoringRule> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<ScoringRule> rules = List.of(new ScoringRule(), new ScoringRule());
        when(jpaRepository.findAll()).thenReturn(rules);

        List<ScoringRule> result = repository.findAll();

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
        List<ScoringRule> rules = List.of(new ScoringRule());
        when(jpaRepository.findByUserId("user-1")).thenReturn(rules);

        List<ScoringRule> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }
}
