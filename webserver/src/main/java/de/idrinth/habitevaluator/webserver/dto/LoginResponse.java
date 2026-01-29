package de.idrinth.habitevaluator.webserver.dto;

/**
 * DTO for login responses.
 */
public class LoginResponse {

    private String userId;
    private String username;
    private String message;
    private boolean success;

    public LoginResponse() {
    }

    public LoginResponse(String userId, String username, String message, boolean success) {
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.success = success;
    }

    public static LoginResponse success(String userId, String username) {
        return new LoginResponse(userId, username, "Login successful", true);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(null, null, message, false);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
