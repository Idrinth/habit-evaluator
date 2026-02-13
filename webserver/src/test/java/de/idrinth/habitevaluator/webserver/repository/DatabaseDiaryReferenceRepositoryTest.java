package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.DiaryReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseDiaryReferenceRepositoryTest {

    private JpaDiaryReferenceRepository jpaRepository;
    private DatabaseDiaryReferenceRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaDiaryReferenceRepository.class);
        repository = new DatabaseDiaryReferenceRepository(jpaRepository);
    }

    @Test
    void testSaveDelegatesToJpa() {
        DiaryReference reference = new DiaryReference("Work meeting");
        when(jpaRepository.save(reference)).thenReturn(reference);

        DiaryReference result = repository.save(reference);

        assertSame(reference, result);
        verify(jpaRepository).save(reference);
    }

    @Test
    void testFindByIdDelegatesToJpa() {
        DiaryReference reference = new DiaryReference("Work meeting");
        when(jpaRepository.findById("id-1")).thenReturn(Optional.of(reference));

        Optional<DiaryReference> result = repository.findById("id-1");

        assertTrue(result.isPresent());
        assertSame(reference, result.get());
        verify(jpaRepository).findById("id-1");
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<DiaryReference> result = repository.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllDelegatesToJpa() {
        List<DiaryReference> references = List.of(new DiaryReference("A"), new DiaryReference("B"));
        when(jpaRepository.findAll()).thenReturn(references);

        List<DiaryReference> result = repository.findAll();

        assertEquals(2, result.size());
        verify(jpaRepository).findAll();
    }

    @Test
    void testDeleteByIdDelegatesToJpa() {
        repository.deleteById("id-1");

        verify(jpaRepository).deleteById("id-1");
    }

    @Test
    void testFindByUserIdDelegatesToJpa() {
        List<DiaryReference> references = List.of(new DiaryReference("Meeting"));
        when(jpaRepository.findByUserId("user-1")).thenReturn(references);

        List<DiaryReference> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        verify(jpaRepository).findByUserId("user-1");
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseDelegatesToJpa() {
        DiaryReference reference = new DiaryReference("Work meeting");
        when(jpaRepository.findByUserIdAndDescriptionIgnoreCase("user-1", "work meeting"))
                .thenReturn(Optional.of(reference));

        Optional<DiaryReference> result = repository.findByUserIdAndDescriptionIgnoreCase("user-1", "work meeting");

        assertTrue(result.isPresent());
        assertSame(reference, result.get());
        verify(jpaRepository).findByUserIdAndDescriptionIgnoreCase("user-1", "work meeting");
    }

    @Test
    void testFindByUserIdAndDescriptionIgnoreCaseReturnsEmptyWhenNotFound() {
        when(jpaRepository.findByUserIdAndDescriptionIgnoreCase("user-1", "unknown"))
                .thenReturn(Optional.empty());

        Optional<DiaryReference> result = repository.findByUserIdAndDescriptionIgnoreCase("user-1", "unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindDistinctDescriptionsByUserIdDelegatesToJpa() {
        List<String> descriptions = List.of("Work", "Exercise");
        when(jpaRepository.findDistinctDescriptionsByUserId("user-1")).thenReturn(descriptions);

        List<String> result = repository.findDistinctDescriptionsByUserId("user-1");

        assertEquals(2, result.size());
        verify(jpaRepository).findDistinctDescriptionsByUserId("user-1");
    }
}
