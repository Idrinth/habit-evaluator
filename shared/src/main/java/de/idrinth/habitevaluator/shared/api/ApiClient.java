package de.idrinth.habitevaluator.shared.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP client for communicating with the habit evaluator webserver API.
 * Uses java.net.HttpURLConnection for compatibility with both desktop and Android.
 */
public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;
    private static final long CIRCUIT_BREAKER_COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes

    private final String baseUrl;
    private final Gson gson;
    private final CircuitBreaker circuitBreaker;
    private String sessionCookie;
    private boolean authenticated;

    public ApiClient(String baseUrl) {
        this(baseUrl, new CircuitBreaker(CIRCUIT_BREAKER_COOLDOWN_MS));
    }

    ApiClient(String baseUrl, CircuitBreaker circuitBreaker) {
        if (!StorageConfig.isUrlSecure(baseUrl)) {
            throw new IllegalArgumentException(
                    "Remote server URL must use HTTPS. Only localhost is allowed over plain HTTP.");
        }
        this.circuitBreaker = circuitBreaker;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                        (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
                        (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                        (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .create();
    }

    /**
     * Authenticates with the remote server using username and password.
     *
     * @param username the username
     * @param password the password
     * @return true if login succeeded
     */
    public boolean login(String username, String password) throws IOException {
        circuitBreaker.checkState();
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        String json = gson.toJson(credentials);
        try {
            HttpURLConnection conn = createConnection("/api/auth/login", "POST");
            writeBody(conn, json);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                extractSessionCookie(conn);
                String body = readResponse(conn);
                Map<String, Object> response = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
                authenticated = Boolean.TRUE.equals(response.get("success"));
                conn.disconnect();
                circuitBreaker.recordSuccess();
                return authenticated;
            }
            conn.disconnect();
            authenticated = false;
            circuitBreaker.recordFailure();
            return false;
        } catch (IOException e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Performs a GET request and deserializes the response.
     */
    public <T> T get(String path, Type responseType) throws IOException {
        circuitBreaker.checkState();
        try {
            HttpURLConnection conn = createConnection(path, "GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String body = readResponse(conn);
                conn.disconnect();
                circuitBreaker.recordSuccess();
                return gson.fromJson(body, responseType);
            }
            conn.disconnect();
            circuitBreaker.recordFailure();
            throw new IOException("GET " + path + " failed with status " + responseCode);
        } catch (IOException e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    /**
     * Performs a GET request and returns the list response.
     */
    public <T> List<T> getList(String path, Type listType) throws IOException {
        return get(path, listType);
    }

    /**
     * Performs a POST request with a JSON body and deserializes the response.
     */
    public <T> T post(String path, Object requestBody, Type responseType) throws IOException {
        circuitBreaker.checkState();
        try {
            HttpURLConnection conn = createConnection(path, "POST");
            writeBody(conn, gson.toJson(requestBody));
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String body = readResponse(conn);
                conn.disconnect();
                circuitBreaker.recordSuccess();
                return gson.fromJson(body, responseType);
            }
            conn.disconnect();
            circuitBreaker.recordFailure();
            throw new IOException("POST " + path + " failed with status " + responseCode);
        } catch (IOException e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    /**
     * Performs a PUT request with a JSON body and deserializes the response.
     */
    public <T> T put(String path, Object requestBody, Type responseType) throws IOException {
        circuitBreaker.checkState();
        try {
            HttpURLConnection conn = createConnection(path, "PUT");
            writeBody(conn, gson.toJson(requestBody));
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String body = readResponse(conn);
                conn.disconnect();
                circuitBreaker.recordSuccess();
                return gson.fromJson(body, responseType);
            }
            conn.disconnect();
            circuitBreaker.recordFailure();
            throw new IOException("PUT " + path + " failed with status " + responseCode);
        } catch (IOException e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    /**
     * Performs a DELETE request.
     */
    public void delete(String path) throws IOException {
        circuitBreaker.checkState();
        try {
            HttpURLConnection conn = createConnection(path, "DELETE");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            if (responseCode != 200 && responseCode != 204) {
                circuitBreaker.recordFailure();
                throw new IOException("DELETE " + path + " failed with status " + responseCode);
            }
            circuitBreaker.recordSuccess();
        } catch (IOException e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    private static final java.util.Set<String> STATE_CHANGING_METHODS =
            java.util.Set.of("POST", "PUT", "DELETE", "PATCH");

    private HttpURLConnection createConnection(String path, String method) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        if (STATE_CHANGING_METHODS.contains(method)) {
            conn.setRequestProperty("X-Request-ID", UUID.randomUUID().toString());
        }
        if (sessionCookie != null) {
            conn.setRequestProperty("Cookie", sessionCookie);
        }
        return conn;
    }

    private void writeBody(HttpURLConnection conn, String json) throws IOException {
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private void extractSessionCookie(HttpURLConnection conn) {
        List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith("JSESSIONID")) {
                    sessionCookie = cookie.split(";")[0];
                    return;
                }
            }
        }
    }

    /**
     * Fetches the server API version from the public version endpoint.
     * Does not require authentication.
     *
     * @return the masked server version string (e.g. "0.1.x")
     * @throws IOException if the request fails or the response is invalid
     */
    public String fetchVersion() throws IOException {
        circuitBreaker.checkState();
        try {
            HttpURLConnection conn = createConnection("/api/version", "GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String body = readResponse(conn);
                conn.disconnect();
                circuitBreaker.recordSuccess();
                Map<String, String> response = gson.fromJson(body, new TypeToken<Map<String, String>>() {}.getType());
                return response != null ? response.get("version") : null;
            }
            conn.disconnect();
            circuitBreaker.recordFailure();
            throw new IOException("GET /api/version failed with status " + responseCode);
        } catch (IOException e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    /**
     * Masks the bugfix portion of a version string, keeping only major and minor parts.
     * For example, "1.2.3" becomes "1.2.x" and "0.1.0-SNAPSHOT" becomes "0.1.x".
     * Versions with fewer than two dots are returned as-is.
     *
     * @param fullVersion the full version string
     * @return the masked version with bugfix replaced by "x"
     */
    public static String maskBugfixVersion(String fullVersion) {
        if (fullVersion == null) {
            return null;
        }
        int firstDot = fullVersion.indexOf('.');
        if (firstDot < 0) {
            return fullVersion;
        }
        int secondDot = fullVersion.indexOf('.', firstDot + 1);
        if (secondDot < 0) {
            return fullVersion;
        }
        return fullVersion.substring(0, secondDot) + ".x";
    }

    public Gson getGson() {
        return gson;
    }
}
