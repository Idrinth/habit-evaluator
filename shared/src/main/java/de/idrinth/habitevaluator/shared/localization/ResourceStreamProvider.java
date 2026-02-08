package de.idrinth.habitevaluator.shared.localization;

import java.io.InputStream;

/**
 * Provides classpath resource streams with path-traversal protection.
 *
 * <p>All paths are validated before loading to prevent directory traversal
 * and restrict access to the expected {@code localization/} prefix.
 */
public class ResourceStreamProvider {

    private static final String ALLOWED_PREFIX = "localization/";

    /**
     * Returns an input stream for the given classpath resource path.
     *
     * @param path the classpath resource path (must start with {@code localization/},
     *             contain no {@code ..} segments, no backslashes, and no null bytes)
     * @return the resource stream, or {@code null} if the resource does not exist
     * @throws IllegalArgumentException if the path fails validation
     */
    public InputStream getResourceStream(String path) {
        validate(path);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ResourceStreamProvider.class.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return classLoader.getResourceAsStream(path);
    }

    private static void validate(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Resource path must not be null or empty");
        }
        if (path.contains("..")) {
            throw new IllegalArgumentException("Resource path must not contain '..'");
        }
        if (path.contains("\\")) {
            throw new IllegalArgumentException("Resource path must not contain backslashes");
        }
        if (path.contains("\0")) {
            throw new IllegalArgumentException("Resource path must not contain null bytes");
        }
        if (!path.startsWith(ALLOWED_PREFIX)) {
            throw new IllegalArgumentException(
                    "Resource path must start with '" + ALLOWED_PREFIX + "'");
        }
    }
}
