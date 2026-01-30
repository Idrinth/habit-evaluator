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
import java.util.List;
import java.util.Map;

/**
 * HTTP client for communicating with the habit evaluator webserver API.
 * Uses java.net.HttpURLConnection for compatibility with both desktop and Android.
 */
public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;

    private final String baseUrl;
    private final Gson gson;
    private String sessionCookie;
    private boolean authenticated;

    public ApiClient(String baseUrl) {
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
        String json = gson.toJson(Map.of("username", username, "password", password));
        HttpURLConnection conn = createConnection("/api/auth/login", "POST");
        writeBody(conn, json);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            extractSessionCookie(conn);
            String body = readResponse(conn);
            Map<String, Object> response = gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
            authenticated = Boolean.TRUE.equals(response.get("success"));
            conn.disconnect();
            return authenticated;
        }
        conn.disconnect();
        authenticated = false;
        return false;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Performs a GET request and deserializes the response.
     */
    public <T> T get(String path, Type responseType) throws IOException {
        HttpURLConnection conn = createConnection(path, "GET");
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String body = readResponse(conn);
            conn.disconnect();
            return gson.fromJson(body, responseType);
        }
        conn.disconnect();
        throw new IOException("GET " + path + " failed with status " + responseCode);
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
        HttpURLConnection conn = createConnection(path, "POST");
        writeBody(conn, gson.toJson(requestBody));
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String body = readResponse(conn);
            conn.disconnect();
            return gson.fromJson(body, responseType);
        }
        conn.disconnect();
        throw new IOException("POST " + path + " failed with status " + responseCode);
    }

    /**
     * Performs a PUT request with a JSON body and deserializes the response.
     */
    public <T> T put(String path, Object requestBody, Type responseType) throws IOException {
        HttpURLConnection conn = createConnection(path, "PUT");
        writeBody(conn, gson.toJson(requestBody));
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String body = readResponse(conn);
            conn.disconnect();
            return gson.fromJson(body, responseType);
        }
        conn.disconnect();
        throw new IOException("PUT " + path + " failed with status " + responseCode);
    }

    /**
     * Performs a DELETE request.
     */
    public void delete(String path) throws IOException {
        HttpURLConnection conn = createConnection(path, "DELETE");
        int responseCode = conn.getResponseCode();
        conn.disconnect();
        if (responseCode != 200 && responseCode != 204) {
            throw new IOException("DELETE " + path + " failed with status " + responseCode);
        }
    }

    private HttpURLConnection createConnection(String path, String method) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
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

    public Gson getGson() {
        return gson;
    }
}
