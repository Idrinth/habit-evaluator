package de.idrinth.habitevaluator.webserver.config;

import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import de.idrinth.habitevaluator.shared.service.SportLogService;
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

    @Bean
    public SleepEvaluationService sleepEvaluationService() {
        return new SleepEvaluationService();
    }

    @Bean
    public EventCorrelationService eventCorrelationService() {
        return new EventCorrelationService();
    }

    @Bean
    public SportLogService sportLogService() {
        return new SportLogService();
    }
}
