package de.idrinth.habitevaluator.webserver.config;

import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import de.idrinth.habitevaluator.shared.service.SportLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig();
    }

    @Test
    void testHabitEvaluatorServiceBeanCreated() {
        HabitEvaluatorService service = appConfig.habitEvaluatorService();

        assertNotNull(service);
        assertInstanceOf(HabitEvaluatorService.class, service);
    }

    @Test
    void testHabitScoringServiceBeanCreated() {
        HabitScoringService service = appConfig.habitScoringService();

        assertNotNull(service);
        assertInstanceOf(HabitScoringService.class, service);
    }

    @Test
    void testDiaryServiceBeanCreated() {
        DiaryService service = appConfig.diaryService();

        assertNotNull(service);
        assertInstanceOf(DiaryService.class, service);
    }

    @Test
    void testSleepEvaluationServiceBeanCreated() {
        SleepEvaluationService service = appConfig.sleepEvaluationService();

        assertNotNull(service);
        assertInstanceOf(SleepEvaluationService.class, service);
    }

    @Test
    void testEventCorrelationServiceBeanCreated() {
        EventCorrelationService service = appConfig.eventCorrelationService();

        assertNotNull(service);
        assertInstanceOf(EventCorrelationService.class, service);
    }

    @Test
    void testSportLogServiceBeanCreated() {
        SportLogService service = appConfig.sportLogService();

        assertNotNull(service);
        assertInstanceOf(SportLogService.class, service);
    }

    @Test
    void testEachCallReturnsNewInstance() {
        HabitEvaluatorService service1 = appConfig.habitEvaluatorService();
        HabitEvaluatorService service2 = appConfig.habitEvaluatorService();

        assertNotSame(service1, service2);
    }
}
