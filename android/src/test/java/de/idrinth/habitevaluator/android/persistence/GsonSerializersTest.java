package de.idrinth.habitevaluator.android.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.idrinth.habitevaluator.shared.model.User;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class GsonSerializersTest {

    // --- LocalDateTime serialization ---

    @Test
    public void testLocalDateTimeSerializerProducesISOFormat() {
        LocalDateTime dt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        Gson gson = GsonSerializers.createGsonWithDateTime();

        String json = gson.toJson(dt);
        assertEquals("\"2025-01-15T10:30:00\"", json);
    }

    @Test
    public void testLocalDateTimeDeserializerParsesISOFormat() {
        Gson gson = GsonSerializers.createGsonWithDateTime();

        LocalDateTime result = gson.fromJson("\"2025-01-15T10:30:00\"", LocalDateTime.class);
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 30, 0), result);
    }

    @Test
    public void testLocalDateTimeSerializerHandlesNull() {
        var result = GsonSerializers.LOCAL_DATE_TIME_SERIALIZER.serialize(null, null, null);
        assertTrue(result.isJsonNull());
    }

    @Test
    public void testLocalDateTimeRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        Gson gson = GsonSerializers.createGsonWithDateTime();

        String json = gson.toJson(original);
        LocalDateTime deserialized = gson.fromJson(json, LocalDateTime.class);
        assertEquals(original, deserialized);
    }

    // --- LocalDate serialization ---

    @Test
    public void testLocalDateSerializerProducesISOFormat() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        Gson gson = GsonSerializers.createGsonWithDateTimeAndDate();

        String json = gson.toJson(date);
        assertEquals("\"2025-06-15\"", json);
    }

    @Test
    public void testLocalDateDeserializerParsesISOFormat() {
        Gson gson = GsonSerializers.createGsonWithDateTimeAndDate();

        LocalDate result = gson.fromJson("\"2025-06-15\"", LocalDate.class);
        assertEquals(LocalDate.of(2025, 6, 15), result);
    }

    @Test
    public void testLocalDateSerializerHandlesNull() {
        var result = GsonSerializers.LOCAL_DATE_SERIALIZER.serialize(null, null, null);
        assertTrue(result.isJsonNull());
    }

    @Test
    public void testLocalDateRoundTrip() {
        LocalDate original = LocalDate.of(2024, 2, 29); // Leap year
        Gson gson = GsonSerializers.createGsonWithDateTimeAndDate();

        String json = gson.toJson(original);
        LocalDate deserialized = gson.fromJson(json, LocalDate.class);
        assertEquals(original, deserialized);
    }

    // --- LocalTime serialization ---

    @Test
    public void testLocalTimeSerializerProducesHHmmFormat() {
        LocalTime time = LocalTime.of(14, 30);
        Gson gson = GsonSerializers.createGson();

        String json = gson.toJson(time);
        assertEquals("\"14:30\"", json);
    }

    @Test
    public void testLocalTimeDeserializerParsesHHmmFormat() {
        Gson gson = GsonSerializers.createGson();

        LocalTime result = gson.fromJson("\"14:30\"", LocalTime.class);
        assertEquals(LocalTime.of(14, 30), result);
    }

    @Test
    public void testLocalTimeSerializerHandlesNull() {
        var result = GsonSerializers.LOCAL_TIME_SERIALIZER.serialize(null, null, null);
        assertTrue(result.isJsonNull());
    }

    @Test
    public void testLocalTimeRoundTrip() {
        LocalTime original = LocalTime.of(23, 59);
        Gson gson = GsonSerializers.createGson();

        String json = gson.toJson(original);
        LocalTime deserialized = gson.fromJson(json, LocalTime.class);
        assertEquals(original, deserialized);
    }

    @Test
    public void testLocalTimeMidnight() {
        LocalTime midnight = LocalTime.of(0, 0);
        Gson gson = GsonSerializers.createGson();

        String json = gson.toJson(midnight);
        assertEquals("\"00:00\"", json);

        LocalTime deserialized = gson.fromJson(json, LocalTime.class);
        assertEquals(midnight, deserialized);
    }

    // --- Gson factory methods ---

    @Test
    public void testCreateGsonNotNull() {
        assertNotNull(GsonSerializers.createGson());
    }

    @Test
    public void testCreateGsonWithDateTimeNotNull() {
        assertNotNull(GsonSerializers.createGsonWithDateTime());
    }

    @Test
    public void testCreateGsonWithDateTimeAndDateNotNull() {
        assertNotNull(GsonSerializers.createGsonWithDateTimeAndDate());
    }

    // --- JSON helper methods ---

    @Test
    public void testGetStringOrNullReturnsValueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        assertEquals("value", GsonSerializers.getStringOrNull(obj, "key"));
    }

    @Test
    public void testGetStringOrNullReturnsNullWhenMissing() {
        JsonObject obj = new JsonObject();
        assertNull(GsonSerializers.getStringOrNull(obj, "missing"));
    }

    @Test
    public void testGetStringOrNullReturnsNullWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("key", JsonNull.INSTANCE);
        assertNull(GsonSerializers.getStringOrNull(obj, "key"));
    }

    @Test
    public void testGetIntOrDefaultReturnsValueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("count", 42);
        assertEquals(42, GsonSerializers.getIntOrDefault(obj, "count", 0));
    }

    @Test
    public void testGetIntOrDefaultReturnsDefaultWhenMissing() {
        JsonObject obj = new JsonObject();
        assertEquals(99, GsonSerializers.getIntOrDefault(obj, "missing", 99));
    }

    @Test
    public void testGetIntOrDefaultReturnsDefaultWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("count", JsonNull.INSTANCE);
        assertEquals(99, GsonSerializers.getIntOrDefault(obj, "count", 99));
    }

    @Test
    public void testGetBooleanOrDefaultReturnsValueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", true);
        assertTrue(GsonSerializers.getBooleanOrDefault(obj, "enabled", false));
    }

    @Test
    public void testGetBooleanOrDefaultReturnsDefaultWhenMissing() {
        JsonObject obj = new JsonObject();
        assertTrue(GsonSerializers.getBooleanOrDefault(obj, "missing", true));
    }

    @Test
    public void testGetBooleanOrDefaultReturnsDefaultWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("enabled", JsonNull.INSTANCE);
        assertFalse(GsonSerializers.getBooleanOrDefault(obj, "enabled", false));
    }

    // --- hasNonNull ---

    @Test
    public void testHasNonNullReturnsTrueWhenPresent() {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        assertTrue(GsonSerializers.hasNonNull(obj, "key"));
    }

    @Test
    public void testHasNonNullReturnsFalseWhenMissing() {
        JsonObject obj = new JsonObject();
        assertFalse(GsonSerializers.hasNonNull(obj, "missing"));
    }

    @Test
    public void testHasNonNullReturnsFalseWhenJsonNull() {
        JsonObject obj = new JsonObject();
        obj.add("key", JsonNull.INSTANCE);
        assertFalse(GsonSerializers.hasNonNull(obj, "key"));
    }

    // --- User serialization ---

    @Test
    public void testSerializeUserProducesIdAndUsername() {
        User user = new User("testuser", "password");
        JsonObject result = GsonSerializers.serializeUser(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.get("id").getAsString());
        assertEquals("testuser", result.get("username").getAsString());
    }

    @Test
    public void testSerializeUserReturnsNullForNullUser() {
        assertNull(GsonSerializers.serializeUser(null));
    }

    @Test
    public void testDeserializeUserProducesUserWithPlaceholderPassword() {
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
    public void testDeserializeUserReturnsNullForNullObject() {
        assertNull(GsonSerializers.deserializeUser(null));
    }

    @Test
    public void testSerializeDeserializeUserRoundTrip() {
        User original = new User("roundtrip", "secret");
        JsonObject serialized = GsonSerializers.serializeUser(original);
        User deserialized = GsonSerializers.deserializeUser(serialized);

        assertNotNull(deserialized);
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getUsername(), deserialized.getUsername());
        assertEquals("placeholder", deserialized.getPassword());
    }
}
