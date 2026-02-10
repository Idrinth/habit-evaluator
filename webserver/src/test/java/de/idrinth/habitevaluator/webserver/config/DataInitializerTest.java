package de.idrinth.habitevaluator.webserver.config;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    private UserRepository userRepository;
    private HabitCategoryRepository categoryRepository;
    private HabitRepository habitRepository;
    private PasswordEncoder passwordEncoder;
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        categoryRepository = mock(HabitCategoryRepository.class);
        habitRepository = mock(HabitRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        dataInitializer = new DataInitializer();
    }

    @Test
    void testDefaultConstants() {
        assertEquals("demo", DataInitializer.DEFAULT_USERNAME);
        assertEquals("demo123", DataInitializer.DEFAULT_PASSWORD);
    }

    @Test
    void testInitializeDataCreatesUserWhenNotExists() throws Exception {
        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(passwordEncoder.encode("demo123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        CommandLineRunner runner = dataInitializer.initializeData(
                userRepository, categoryRepository, habitRepository, passwordEncoder);
        runner.run();

        verify(userRepository).existsByUsername("demo");
        verify(passwordEncoder).encode("demo123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testInitializeDataSkipsWhenUserExists() throws Exception {
        when(userRepository.existsByUsername("demo")).thenReturn(true);

        CommandLineRunner runner = dataInitializer.initializeData(
                userRepository, categoryRepository, habitRepository, passwordEncoder);
        runner.run();

        verify(userRepository).existsByUsername("demo");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
