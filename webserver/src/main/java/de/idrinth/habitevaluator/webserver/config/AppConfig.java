package de.idrinth.habitevaluator.webserver.config;

import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public HabitEvaluatorService habitEvaluatorService() {
        return new HabitEvaluatorService();
    }
}
