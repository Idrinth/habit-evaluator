package de.idrinth.habitevaluator.webserver.config;

import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes default data on application startup.
 */
@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    public static final String DEFAULT_USERNAME = "demo";
    public static final String DEFAULT_PASSWORD = "demo123";

    @Bean
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            HabitCategoryRepository categoryRepository,
            HabitRepository habitRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            User defaultUser;
            if (!userRepository.existsByUsername(DEFAULT_USERNAME)) {
                defaultUser = new User(
                    DEFAULT_USERNAME,
                    passwordEncoder.encode(DEFAULT_PASSWORD),
                    "demo@example.com"
                );
                userRepository.save(defaultUser);
                logger.info("Created default user: {} (password: {})", DEFAULT_USERNAME, DEFAULT_PASSWORD);

                DefaultDataInitializer dataInitializer = new DefaultDataInitializer(categoryRepository, habitRepository);
                dataInitializer.initializeDefaults(defaultUser);
            } else {
                logger.info("Default user already exists");
            }
        };
    }
}
