package de.idrinth.habitevaluator.shared.api;

import java.io.IOException;

/**
 * Thrown when the client version does not match the server's API version.
 * Only the major and minor (feature) version parts are compared;
 * bugfix versions are ignored.
 */
public class VersionMismatchException extends IOException {

    private final String clientVersion;
    private final String serverVersion;

    public VersionMismatchException(String clientVersion, String serverVersion) {
        super("Version mismatch: client " + clientVersion + " does not match server " + serverVersion);
        this.clientVersion = clientVersion;
        this.serverVersion = serverVersion;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }
}
