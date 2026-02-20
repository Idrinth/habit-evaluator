package de.idrinth.habitevaluator.webserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatsCacheServiceTest {

    private StatsCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new StatsCacheService(60_000L);
    }

    @Test
    void testPutAndGetMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("labels", List.of("2026-01-01"));
        data.put("value", 42);

        cacheService.putMap("user1", StatsCacheService.DASHBOARD, data);

        Map<String, Object> cached = cacheService.getCachedMap("user1", StatsCacheService.DASHBOARD);
        assertNotNull(cached);
        assertEquals(data, cached);
    }

    @Test
    void testPutAndGetList() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("eventA", "Exercise");
        entry.put("eventB", "Sleep");
        entry.put("correlation", 0.75);
        data.add(entry);

        cacheService.putList("user1", StatsCacheService.CORRELATIONS, data);

        List<Map<String, Object>> cached = cacheService.getCachedList("user1", StatsCacheService.CORRELATIONS);
        assertNotNull(cached);
        assertEquals(1, cached.size());
        assertEquals("Exercise", cached.get(0).get("eventA"));
    }

    @Test
    void testCacheMissReturnsNull() {
        assertNull(cacheService.getCachedMap("user1", StatsCacheService.DASHBOARD));
        assertNull(cacheService.getCachedList("user1", StatsCacheService.CORRELATIONS));
    }

    @Test
    void testDifferentUsersHaveSeparateCaches() {
        Map<String, Object> data1 = Map.of("value", 1);
        Map<String, Object> data2 = Map.of("value", 2);

        cacheService.putMap("user1", StatsCacheService.DASHBOARD, data1);
        cacheService.putMap("user2", StatsCacheService.DASHBOARD, data2);

        assertEquals(1, cacheService.getCachedMap("user1", StatsCacheService.DASHBOARD).get("value"));
        assertEquals(2, cacheService.getCachedMap("user2", StatsCacheService.DASHBOARD).get("value"));
    }

    @Test
    void testDifferentEndpointsHaveSeparateCaches() {
        Map<String, Object> dashboardData = Map.of("type", "dashboard");
        Map<String, Object> timelineData = Map.of("type", "timeline");

        cacheService.putMap("user1", StatsCacheService.DASHBOARD, dashboardData);
        cacheService.putMap("user1", StatsCacheService.DAILY_TIMELINE, timelineData);

        assertEquals("dashboard", cacheService.getCachedMap("user1", StatsCacheService.DASHBOARD).get("type"));
        assertEquals("timeline", cacheService.getCachedMap("user1", StatsCacheService.DAILY_TIMELINE).get("type"));
    }

    @Test
    void testInvalidateUserClearsAllEndpoints() {
        cacheService.putMap("user1", StatsCacheService.DASHBOARD, Map.of("v", 1));
        cacheService.putMap("user1", StatsCacheService.DAILY_TIMELINE, Map.of("v", 2));
        cacheService.putList("user1", StatsCacheService.CORRELATIONS, List.of(Map.of("v", 3)));
        cacheService.putMap("user1", StatsCacheService.EMOTION_SCATTER, Map.of("v", 4));
        cacheService.putMap("user1", StatsCacheService.FOOD_DISTRIBUTION, Map.of("v", 5));

        cacheService.invalidateUser("user1");

        assertNull(cacheService.getCachedMap("user1", StatsCacheService.DASHBOARD));
        assertNull(cacheService.getCachedMap("user1", StatsCacheService.DAILY_TIMELINE));
        assertNull(cacheService.getCachedList("user1", StatsCacheService.CORRELATIONS));
        assertNull(cacheService.getCachedMap("user1", StatsCacheService.EMOTION_SCATTER));
        assertNull(cacheService.getCachedMap("user1", StatsCacheService.FOOD_DISTRIBUTION));
    }

    @Test
    void testInvalidateUserDoesNotAffectOtherUsers() {
        cacheService.putMap("user1", StatsCacheService.DASHBOARD, Map.of("v", 1));
        cacheService.putMap("user2", StatsCacheService.DASHBOARD, Map.of("v", 2));

        cacheService.invalidateUser("user1");

        assertNull(cacheService.getCachedMap("user1", StatsCacheService.DASHBOARD));
        assertNotNull(cacheService.getCachedMap("user2", StatsCacheService.DASHBOARD));
    }

    @Test
    void testExpiredCacheReturnsNull() {
        StatsCacheService shortTtl = new StatsCacheService(1L);
        shortTtl.putMap("user1", StatsCacheService.DASHBOARD, Map.of("v", 1));

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertNull(shortTtl.getCachedMap("user1", StatsCacheService.DASHBOARD));
    }

    @Test
    void testSize() {
        assertEquals(0, cacheService.size());

        cacheService.putMap("user1", StatsCacheService.DASHBOARD, Map.of("v", 1));
        assertEquals(1, cacheService.size());

        cacheService.putMap("user1", StatsCacheService.DAILY_TIMELINE, Map.of("v", 2));
        assertEquals(2, cacheService.size());

        cacheService.invalidateUser("user1");
        assertEquals(0, cacheService.size());
    }

    @Test
    void testOverwriteExistingEntry() {
        cacheService.putMap("user1", StatsCacheService.DASHBOARD, Map.of("v", 1));
        cacheService.putMap("user1", StatsCacheService.DASHBOARD, Map.of("v", 2));

        assertEquals(2, cacheService.getCachedMap("user1", StatsCacheService.DASHBOARD).get("v"));
        assertEquals(1, cacheService.size());
    }
}
