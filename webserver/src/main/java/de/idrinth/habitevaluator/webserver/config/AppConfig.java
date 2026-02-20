package de.idrinth.habitevaluator.webserver.config;

import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import de.idrinth.habitevaluator.shared.service.SportLogService;
import de.idrinth.habitevaluator.webserver.service.StatsCacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private static final long STATS_CACHE_TTL_MILLIS = 5 * 60 * 1000L;

    @Bean
    public StatsCacheService statsCacheService() {
        return new StatsCacheService(STATS_CACHE_TTL_MILLIS);
    }

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
