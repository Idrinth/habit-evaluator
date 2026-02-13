package de.idrinth.habitevaluator.shared.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import de.idrinth.habitevaluator.shared.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized GSON serializers and deserializers for date/time types and common entities.
 * Used by FileSystem*Repository classes to avoid code duplication.
 */
public final class GsonSerializers {

    private GsonSerializers() {
        // Utility class
    }

    /**
     * Serializer for LocalDateTime using ISO format.
     */
    public static final JsonSerializer<LocalDateTime> LOCAL_DATE_TIME_SERIALIZER =
            (src, typeOfSrc, context) -> {
                if (src == null) {
                    return JsonNull.INSTANCE;
                }
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            };

    /**
     * Deserializer for LocalDateTime using ISO format.
     */
    public static final JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_DESERIALIZER =
            (json, typeOfT, context) -> {
                if (json.isJsonNull()) {
                    return null;
                }
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            };

    /**
     * Serializer for LocalDate using ISO format.
     */
    public static final JsonSerializer<LocalDate> LOCAL_DATE_SERIALIZER =
            (src, typeOfSrc, context) -> {
                if (src == null) {
                    return JsonNull.INSTANCE;
                }
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
            };

    /**
     * Deserializer for LocalDate using ISO format.
     */
    public static final JsonDeserializer<LocalDate> LOCAL_DATE_DESERIALIZER =
            (json, typeOfT, context) -> {
                if (json.isJsonNull()) {
                    return null;
                }
                return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
            };

    /**
     * Serializer for LocalTime using HH:mm format.
     */
    public static final JsonSerializer<LocalTime> LOCAL_TIME_SERIALIZER =
            (src, typeOfSrc, context) -> {
                if (src == null) {
                    return JsonNull.INSTANCE;
                }
                return new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("HH:mm")));
            };

    /**
     * Deserializer for LocalTime using HH:mm format.
     */
    public static final JsonDeserializer<LocalTime> LOCAL_TIME_DESERIALIZER =
            (json, typeOfT, context) -> {
                if (json.isJsonNull()) {
                    return null;
                }
                return LocalTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("HH:mm"));
            };

    /**
     * Creates a Gson instance with all date/time type adapters registered.
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER)
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER)
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_SERIALIZER)
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_DESERIALIZER)
                .registerTypeAdapter(LocalTime.class, LOCAL_TIME_SERIALIZER)
                .registerTypeAdapter(LocalTime.class, LOCAL_TIME_DESERIALIZER)
                .create();
    }

    /**
     * Creates a Gson instance with only LocalDateTime adapters registered.
     */
    public static Gson createGsonWithDateTime() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER)
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER)
                .create();
    }

    /**
     * Creates a Gson instance with LocalDateTime and LocalDate adapters registered.
     */
    public static Gson createGsonWithDateTimeAndDate() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER)
                .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER)
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_SERIALIZER)
                .registerTypeAdapter(LocalDate.class, LOCAL_DATE_DESERIALIZER)
                .create();
    }

    /**
     * Safely extracts a String value from a JsonObject, returning null if the key
     * doesn't exist or the value is null.
     */
    public static String getStringOrNull(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    /**
     * Safely extracts an int value from a JsonObject, returning the default value
     * if the key doesn't exist or the value is null.
     */
    public static int getIntOrDefault(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return defaultValue;
    }

    /**
     * Safely extracts a boolean value from a JsonObject, returning the default value
     * if the key doesn't exist or the value is null.
     */
    public static boolean getBooleanOrDefault(JsonObject obj, String key, boolean defaultValue) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    /**
     * Serializes a User entity to a JsonObject containing only id and username.
     */
    public static JsonObject serializeUser(User user) {
        if (user == null) {
            return null;
        }
        JsonObject userObj = new JsonObject();
        userObj.addProperty("id", user.getId());
        userObj.addProperty("username", user.getUsername());
        return userObj;
    }

    /**
     * Deserializes a User entity from a JsonObject.
     * Sets a placeholder password since passwords are not serialized.
     */
    public static User deserializeUser(JsonObject userObj) {
        if (userObj == null) {
            return null;
        }
        User user = new User();
        user.setId(userObj.get("id").getAsString());
        user.setUsername(getStringOrNull(userObj, "username"));
        user.setPassword("placeholder");
        return user;
    }

    /**
     * Checks if a JsonObject has a non-null value for the given key.
     */
    public static boolean hasNonNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull();
    }
}
