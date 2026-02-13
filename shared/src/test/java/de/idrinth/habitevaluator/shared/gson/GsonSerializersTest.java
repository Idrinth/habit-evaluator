package de.idrinth.habitevaluator.shared.gson;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import de.idrinth.habitevaluator.shared.model.User;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class GsonSerializersTest {

    @Test
    void testLocalDateTimeSerializerProducesISOFormat() {
        LocalDateTime dt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        Gson gson = GsonSerializers.createGsonWithDateTime();
        String json = gson.toJson(dt);
        assertEquals("\"2025-01-15T10:30:00\"", json);
    }

    @Test
    void testLocalDateTimeDeserializerParsesISOFormat() {
        Gson gson = GsonSerializers.createGsonWithDateTime();
        LocalDateTime result = gson.fromJson("\"2025-01-15T10:30:00\"", LocalDateTime.class);
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 30, 0), result);
    }

    @Test
    void testLocalDateTimeSerializerHandlesNull() {
        var result = GsonSerializers.LOCAL_DATE_TIME_SERIALIZER.serialize(null, null, null);
        assertTrue(result.isJsonNull());
    }

    @Test
    void testLocalDateTimeRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        Gson gson = GsonSerializers.createGsonWithDateTime();
        String json = gson.toJson(original);
        LocalDateTime deserialized = gson.fromJson(json, LocalDateTime.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testLocalDateSerializerProducesISOFormat() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        Gson gson = GsonSerializers.createGsonWithDateTimeAndDate();
        String json = gson.toJson(date);
        assertEquals("\"2025-06-15\"", json);
    }

    @Test
    void testLocalDateDeserializerParsesISOFormat() {
        Gson gson = GsonSerializers.createGsonWithDateTimeAndDate();
        LocalDate result = gson.fromJson("\"2025-06-15\"", LocalDate.class);
        assertEquals(LocalDate.of(2025, 6, 15), result);
    }

    @Test
    void testLocalDateSerializerHandlesNull() {
        var result = GsonSerializers.LOCAL_DATE_SERIALIZER.serialize(null, null, null);
        assertTrue(result.isJsonNull());
    }

    @Test
    void testLocalDateRoundTrip() {
        LocalDate original = LocalDate.of(2024, 2, 29);
        Gson gson = GsonSerializers.createGsonWithDateTimeAndDate();
        String json = gson.toJson(original);
        LocalDate deserialized = gson.fromJson(json, LocalDate.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testLocalTimeSerializerProducesHHmmFormat() {
        LocalTime time = LocalTime.of(14, 30);
        Gson gson = GsonSerializers.createGson();
        String json = gson.toJson(time);
        assertEquals("\"14:30\"", json);
    }

    @Test
    void testLocalTimeDeserializerParsesHHmmFormat() {
        Gson gson = GsonSerializers.createGson();
        LocalTime result = gson.fromJson("\"14:30\"", LocalTime.class);
        assertEquals(LocalTime.of(14, 30), result);
    }

    @Test
    void testLocalTimeSerializerHandlesNull() {
        var result = GsonSerializers.LOCAL_TIME_SERIALIZER.serialize(null, null, null);
        assertTrue(result.isJsonNull());
    }

    @Test
    void testLocalTimeRoundTrip() {
        LocalTime original = LocalTime.of(23, 59);
        Gson gson = GsonSerializers.createGson();
        String json = gson.toJson(original);
        LocalTime deserialized = gson.fromJson(json, LocalTime.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testLocalTimeMidnight() {
        LocalTime midnight = LocalTime.of(0, 0);
        Gson gson = GsonSerializers.createGson();
        String json = gson.toJson(midnight);
        assertEquals("\"00:00\"", json);
        LocalTime deserialized = gson.fromJson(json, LocalTime.class);
        assertEquals(midnight, deserialized);
    }

    @Test
    void testCreateGsonNotNull() {
        assertNotNull(GsonSerializers.createGson());
    }

    @Test
    void testCreateGsonWithDateTimeNotNull() {
        assertNotNull(GsonSerializers.createGsonWithDateTime());
    }

    @Test
    void testCreateGsonWithDateTimeAndDateNotNull() {
        assertNotNull(GsonSerializers.createGsonWithDateTimeAndDate());
    }

    @Test
    void testGetStringOrNullReturnsValueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        assertEquals("value", GsonSerializers.getStringOrNull(obj, "key"));
    }

    @Test
    void testGetStringOrNullReturnsNullWhenMissing() {
        JsonObject obj = new JsonObject();
        assertNull(GsonSerializers.getStringOrNull(obj, "missing"));
    }

    @Test
    void testGetStringOrNullReturnsNullWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("key", JsonNull.INSTANCE);
        assertNull(GsonSerializers.getStringOrNull(obj, "key"));
    }

    @Test
    void testGetIntOrDefaultReturnsValueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("count", 42);
        assertEquals(42, GsonSerializers.getIntOrDefault(obj, "count", 0));
    }

    @Test
    void testGetIntOrDefaultReturnsDefaultWhenMissing() {
        JsonObject obj = new JsonObject();
        assertEquals(99, GsonSerializers.getIntOrDefault(obj, "missing", 99));
    }

    @Test
    void testGetIntOrDefaultReturnsDefaultWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("count", JsonNull.INSTANCE);
        assertEquals(99, GsonSerializers.getIntOrDefault(obj, "count", 99));
    }

    @Test
    void testGetBooleanOrDefaultReturnsValueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", true);
        assertTrue(GsonSerializers.getBooleanOrDefault(obj, "enabled", false));
    }

    @Test
    void testGetBooleanOrDefaultReturnsDefaultWhenMissing() {
        JsonObject obj = new JsonObject();
        assertTrue(GsonSerializers.getBooleanOrDefault(obj, "missing", true));
    }

    @Test
    void testGetBooleanOrDefaultReturnsDefaultWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("enabled", JsonNull.INSTANCE);
        assertFalse(GsonSerializers.getBooleanOrDefault(obj, "enabled", false));
    }

    @Test
    void testHasNonNullReturnsTrueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        assertTrue(GsonSerializers.hasNonNull(obj, "key"));
    }

    @Test
    void testHasNonNullReturnsFalseWhenMissing() {
        JsonObject obj = new JsonObject();
        assertFalse(GsonSerializers.hasNonNull(obj, "missing"));
    }

    @Test
    void testHasNonNullReturnsFalseWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("key", JsonNull.INSTANCE);
        assertFalse(GsonSerializers.hasNonNull(obj, "key"));
    }

    @Test
    void testSerializeUserProducesIdAndUsername() {
        User user = new User("testuser", "password");
        JsonObject result = GsonSerializers.serializeUser(user);
        assertNotNull(result);
        assertEquals(user.getId(), result.get("id").getAsString());
        assertEquals("testuser", result.get("username").getAsString());
    }

    @Test
    void testSerializeUserReturnsNullForNullUser() {
        assertNull(GsonSerializers.serializeUser(null));
    }

    @Test
    void testDeserializeUserProducesUserWithPlaceholderPassword() {
        JsonObject userObj = new JsonObject();
        userObj.addProperty("id", "user-123");
        userObj.addProperty("username", "testuser");
        User user = GsonSerializers.deserializeUser(userObj);
        assertNotNull(user);
        assertEquals("user-123", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("placeholder", user.getPassword());
    }

    @Test
    void testDeserializeUserReturnsNullForNullObject() {
        assertNull(GsonSerializers.deserializeUser(null));
    }

    @Test
    void testSerializeDeserializeUserRoundTrip() {
        User original = new User("roundtrip", "secret");
        JsonObject serialized = GsonSerializers.serializeUser(original);
        User deserialized = GsonSerializers.deserializeUser(serialized);
        assertNotNull(deserialized);
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getUsername(), deserialized.getUsername());
        assertEquals("placeholder", deserialized.getPassword());
    }
}
