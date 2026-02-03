package de.idrinth.habitevaluator.webserver.config;

import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public HabitEvaluatorService habitEvaluatorService() {
        return new HabitEvaluatorService();
    }

    @Bean
    public HabitScoringService habitScoringService() {
        return new HabitScoringService();
    }

    @Bean
    public DiaryService diaryService() {
        return new DiaryService();
    }
}
