package de.idrinth.habitevaluator.webserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Filter that requires a unique X-Request-ID header (UUID v4) on all
 * state-changing HTTP methods (POST, PUT, DELETE, PATCH).
 *
 * <p>This serves two purposes:
 * <ul>
 *   <li>CSRF protection — browsers will not send custom headers cross-origin
 *       without a CORS preflight, so a forged form submission cannot include
 *       this header.</li>
 *   <li>Replay / spam prevention — each UUID is blacklisted for one minute
 *       after first use, rejecting duplicate submissions.</li>
 * </ul>
 *
 * <p>Public endpoints ({@code /api/auth/**}, {@code /api/shared/**},
 * {@code /h2-console/**}) are exempt.
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    static final String HEADER_NAME = "X-Request-ID";
    private static final long BLACKLIST_DURATION_MS = 60_000; // 1 minute
    private static final Set<String> STATE_CHANGING_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final ConcurrentMap<String, Long> usedIds = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/api/shared/") || path.startsWith("/h2-console")) {
            return true;
        }
        return !STATE_CHANGING_METHODS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = request.getHeader(HEADER_NAME);

        if (requestId == null || requestId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing " + HEADER_NAME + " header");
            return;
        }

        if (!isValidUuidV4(requestId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + HEADER_NAME + " — must be a UUID v4");
            return;
        }

        String normalized = requestId.toLowerCase();
        long now = System.currentTimeMillis();

        evictExpired(now);

        Long previous = usedIds.putIfAbsent(normalized, now);
        if (previous != null) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Duplicate " + HEADER_NAME + " — this request ID has already been used");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isValidUuidV4(String value) {
        try {
            UUID uuid = UUID.fromString(value);
            return uuid.version() == 4;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void evictExpired(long now) {
        usedIds.entrySet().removeIf(entry -> now - entry.getValue() > BLACKLIST_DURATION_MS);
    }

    // visible for testing
    int blacklistSize() {
        evictExpired(System.currentTimeMillis());
        return usedIds.size();
    }
}
