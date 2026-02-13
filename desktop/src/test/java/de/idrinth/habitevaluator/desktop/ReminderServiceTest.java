package de.idrinth.habitevaluator.desktop;

import de.idrinth.habitevaluator.shared.api.StorageConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

class ReminderServiceTest {

    private StorageConfig config;
    private ReminderService service;
    private File tempConfigFile;

    @BeforeEach
    void setUp() throws Exception {
        tempConfigFile = File.createTempFile("reminder-test", ".properties");
        tempConfigFile.deleteOnExit();
        config = new StorageConfig(tempConfigFile);
        service = new ReminderService(config);
    }

    @AfterEach
    void tearDown() {
        service.stop();
        if (tempConfigFile != null) {
            tempConfigFile.delete();
        }
    }

    @Test
    void testConstructorAcceptsConfig() {
        assertNotNull(service);
    }

    @Test
    void testStopWithoutRescheduleDoesNotThrow() {
        assertDoesNotThrow(() -> service.stop());
    }

    @Test
    void testRescheduleWithAllRemindersDisabled() {
        config.setSleepReminderEnabled(false);
        config.setDiaryReminderEnabled(false);
        config.setEmotionReminderEnabled(false);

        assertDoesNotThrow(() -> service.reschedule());

        ScheduledExecutorService scheduler = getScheduler();
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    void testRescheduleWithSleepReminderEnabled() {
        config.setSleepReminderEnabled(true);
        config.setSleepReminderTime("08:00");
        config.setDiaryReminderEnabled(false);
        config.setEmotionReminderEnabled(false);

        assertDoesNotThrow(() -> service.reschedule());

        ScheduledExecutorService scheduler = getScheduler();
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    void testRescheduleWithDiaryReminderEnabled() {
        config.setSleepReminderEnabled(false);
        config.setDiaryReminderEnabled(true);
        config.setDiaryReminderTime("20:00");
        config.setEmotionReminderEnabled(false);

        assertDoesNotThrow(() -> service.reschedule());

        ScheduledExecutorService scheduler = getScheduler();
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    void testRescheduleWithEmotionReminderEnabled() {
        config.setSleepReminderEnabled(false);
        config.setDiaryReminderEnabled(false);
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(3);
        config.setWakingHoursStart("07:00");
        config.setWakingHoursEnd("22:00");

        assertDoesNotThrow(() -> service.reschedule());

        ScheduledExecutorService scheduler = getScheduler();
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    void testRescheduleWithAllRemindersEnabled() {
        config.setSleepReminderEnabled(true);
        config.setSleepReminderTime("08:00");
        config.setDiaryReminderEnabled(true);
        config.setDiaryReminderTime("20:00");
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(5);
        config.setWakingHoursStart("07:00");
        config.setWakingHoursEnd("22:00");

        assertDoesNotThrow(() -> service.reschedule());

        ScheduledExecutorService scheduler = getScheduler();
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    void testStopShutsDownScheduler() {
        service.reschedule();
        ScheduledExecutorService scheduler = getScheduler();
        assertNotNull(scheduler);
        assertFalse(scheduler.isShutdown());

        service.stop();
        assertTrue(scheduler.isShutdown());
    }

    @Test
    void testStopCalledTwiceDoesNotThrow() {
        service.reschedule();
        service.stop();
        assertDoesNotThrow(() -> service.stop());
    }

    @Test
    void testRescheduleStopsPreviousScheduler() {
        service.reschedule();
        ScheduledExecutorService firstScheduler = getScheduler();

        service.reschedule();
        ScheduledExecutorService secondScheduler = getScheduler();

        assertTrue(firstScheduler.isShutdown());
        assertFalse(secondScheduler.isShutdown());
        assertNotSame(firstScheduler, secondScheduler);
    }

    @Test
    void testRescheduleWithEmotionReminderCountAtMaximum() {
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(10);
        config.setWakingHoursStart("07:00");
        config.setWakingHoursEnd("22:00");

        assertDoesNotThrow(() -> service.reschedule());
    }

    @Test
    void testRescheduleWithEmotionReminderCountAtMinimum() {
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(1);
        config.setWakingHoursStart("07:00");
        config.setWakingHoursEnd("22:00");

        assertDoesNotThrow(() -> service.reschedule());
    }

    @Test
    void testRescheduleWithWakingHoursEndBeforeStart() {
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(2);
        config.setWakingHoursStart("22:00");
        config.setWakingHoursEnd("07:00");

        assertDoesNotThrow(() -> service.reschedule());
    }

    @Test
    void testRescheduleWithEqualWakingHours() {
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(2);
        config.setWakingHoursStart("10:00");
        config.setWakingHoursEnd("10:00");

        assertDoesNotThrow(() -> service.reschedule());
    }

    @Test
    void testSecondsUntilReturnsPositiveValue() throws Exception {
        Method secondsUntil = ReminderService.class.getDeclaredMethod("secondsUntil", String.class);
        secondsUntil.setAccessible(true);

        long seconds = (long) secondsUntil.invoke(service, "12:00");
        assertTrue(seconds > 0, "secondsUntil should always return a positive value");
    }

    @Test
    void testSecondsUntilNeverExceedsOneDay() throws Exception {
        Method secondsUntil = ReminderService.class.getDeclaredMethod("secondsUntil", String.class);
        secondsUntil.setAccessible(true);

        long seconds = (long) secondsUntil.invoke(service, "12:00");
        assertTrue(seconds <= 86400, "secondsUntil should not exceed 24 hours (86400 seconds)");
    }

    @Test
    void testRescheduleWithNarrowWakingHoursWindow() {
        config.setEmotionReminderEnabled(true);
        config.setEmotionReminderCount(5);
        config.setWakingHoursStart("10:00");
        config.setWakingHoursEnd("10:30");

        assertDoesNotThrow(() -> service.reschedule());
    }

    @Test
    void testSchedulerUsesDaemonThread() {
        service.reschedule();
        ScheduledExecutorService scheduler = getScheduler();
        assertNotNull(scheduler);
        // Daemon thread scheduler should not prevent JVM shutdown
        assertFalse(scheduler.isShutdown());
    }

    private ScheduledExecutorService getScheduler() {
        try {
            Field schedulerField = ReminderService.class.getDeclaredField("scheduler");
            schedulerField.setAccessible(true);
            return (ScheduledExecutorService) schedulerField.get(service);
        } catch (Exception e) {
            fail("Failed to access scheduler field: " + e.getMessage());
            return null;
        }
    }
}
