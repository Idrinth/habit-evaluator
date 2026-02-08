package de.idrinth.habitevaluator.webserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestIdFilterTest {

    private RequestIdFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestIdFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void testGetRequestsAreNotFiltered() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/habits");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testAuthEndpointsAreNotFiltered() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testSharedEndpointsAreNotFiltered() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/shared/some-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testH2ConsoleEndpointsAreNotFiltered() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/h2-console/query");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testPostWithoutHeaderIsRejected() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/habits");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(400, response.getStatus());
    }

    @Test
    void testPutWithoutHeaderIsRejected() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/habits/123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(400, response.getStatus());
    }

    @Test
    void testDeleteWithoutHeaderIsRejected() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/habits/123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(400, response.getStatus());
    }

    @Test
    void testPatchWithoutHeaderIsRejected() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/habits/123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(400, response.getStatus());
    }

    @Test
    void testInvalidUuidIsRejected() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/habits");
        request.addHeader("X-Request-ID", "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(400, response.getStatus());
    }

    @Test
    void testNonV4UuidIsRejected() throws ServletException, IOException {
        // UUID v1
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/habits");
        request.addHeader("X-Request-ID", "6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(400, response.getStatus());
    }

    @Test
    void testValidUuidV4IsAccepted() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/habits");
        request.addHeader("X-Request-ID", UUID.randomUUID().toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testDuplicateUuidIsRejected() throws ServletException, IOException {
        String id = UUID.randomUUID().toString();

        MockHttpServletRequest request1 = new MockHttpServletRequest("POST", "/api/habits");
        request1.addHeader("X-Request-ID", id);
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        filter.doFilter(request1, response1, filterChain);
        assertEquals(200, response1.getStatus());

        MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/api/habits");
        request2.addHeader("X-Request-ID", id);
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request2, response2, filterChain);
        assertEquals(409, response2.getStatus());
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void testDuplicateUuidCaseInsensitive() throws ServletException, IOException {
        String id = UUID.randomUUID().toString();

        MockHttpServletRequest request1 = new MockHttpServletRequest("POST", "/api/habits");
        request1.addHeader("X-Request-ID", id.toLowerCase());
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        filter.doFilter(request1, response1, filterChain);
        assertEquals(200, response1.getStatus());

        MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/api/habits");
        request2.addHeader("X-Request-ID", id.toUpperCase());
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request2, response2, filterChain);
        assertEquals(409, response2.getStatus());
    }

    @Test
    void testDifferentUuidsAreAccepted() throws ServletException, IOException {
        MockHttpServletRequest request1 = new MockHttpServletRequest("POST", "/api/habits");
        request1.addHeader("X-Request-ID", UUID.randomUUID().toString());
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        filter.doFilter(request1, response1, filterChain);
        assertEquals(200, response1.getStatus());

        MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/api/habits");
        request2.addHeader("X-Request-ID", UUID.randomUUID().toString());
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(request2, response2, filterChain);
        assertEquals(200, response2.getStatus());

        verify(filterChain, times(2)).doFilter(any(), any());
    }

    @Test
    void testBlankHeaderIsRejected() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/habits");
        request.addHeader("X-Request-ID", "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertEquals(400, response.getStatus());
    }

    @Test
    void testBlacklistSizeGrowsAndEvicts() throws ServletException, IOException {
        assertEquals(0, filter.blacklistSize());

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/habits");
        request.addHeader("X-Request-ID", UUID.randomUUID().toString());
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, filterChain);

        assertEquals(1, filter.blacklistSize());
    }
}
