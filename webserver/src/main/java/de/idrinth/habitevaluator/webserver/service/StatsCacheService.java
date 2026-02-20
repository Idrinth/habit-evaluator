package de.idrinth.habitevaluator.webserver.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for expensive stats endpoint results.
 * Caches per-user responses with a configurable TTL and explicit invalidation.
 */
public class StatsCacheService {

    public static final String DASHBOARD = "dashboard";
    public static final String DAILY_TIMELINE = "dailyTimeline";
    public static final String CORRELATIONS = "correlations";
    public static final String EMOTION_SCATTER = "emotionScatter";
    public static final String FOOD_DISTRIBUTION = "foodDistribution";

    private final long ttlMillis;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public StatsCacheService(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCachedMap(String userId, String endpoint) {
        CacheEntry entry = cache.get(cacheKey(userId, endpoint));
        if (entry != null && !entry.isExpired(ttlMillis)) {
            return (Map<String, Object>) entry.value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCachedList(String userId, String endpoint) {
        CacheEntry entry = cache.get(cacheKey(userId, endpoint));
        if (entry != null && !entry.isExpired(ttlMillis)) {
            return (List<Map<String, Object>>) entry.value;
        }
        return null;
    }

    public void putMap(String userId, String endpoint, Map<String, Object> value) {
        cache.put(cacheKey(userId, endpoint), new CacheEntry(value));
    }

    public void putList(String userId, String endpoint, List<Map<String, Object>> value) {
        cache.put(cacheKey(userId, endpoint), new CacheEntry(value));
    }

    public void invalidateUser(String userId) {
        cache.keySet().removeIf(key -> key.startsWith(userId + ":"));
    }

    public int size() {
        return cache.size();
    }

    private String cacheKey(String userId, String endpoint) {
        return userId + ":" + endpoint;
    }

    private static class CacheEntry {
        final Object value;
        final Instant createdAt;

        CacheEntry(Object value) {
            this.value = value;
            this.createdAt = Instant.now();
        }

        boolean isExpired(long ttlMillis) {
            return Instant.now().toEpochMilli() - createdAt.toEpochMilli() > ttlMillis;
        }
    }
}
