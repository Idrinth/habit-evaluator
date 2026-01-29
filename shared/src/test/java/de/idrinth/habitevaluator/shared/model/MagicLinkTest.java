package de.idrinth.habitevaluator.shared.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MagicLinkTest {

    @Test
    void testDefaultConstructorSetsDefaults() {
        MagicLink link = new MagicLink();
        assertNotNull(link.getId());
        assertNotNull(link.getToken());
        assertNotNull(link.getCreatedAt());
        assertNull(link.getUser());
        assertNull(link.getCategoryId());
        assertNull(link.getFilterStart());
        assertNull(link.getFilterEnd());
        assertNull(link.getExpiresAt());
    }

    @Test
    void testIdAndTokenAreDifferent() {
        MagicLink link = new MagicLink();
        assertNotEquals(link.getId(), link.getToken());
    }

    @Test
    void testSetId() {
        MagicLink link = new MagicLink();
        link.setId("custom-id");
        assertEquals("custom-id", link.getId());
    }

    @Test
    void testSetToken() {
        MagicLink link = new MagicLink();
        link.setToken("custom-token");
        assertEquals("custom-token", link.getToken());
    }

    @Test
    void testSetUser() {
        MagicLink link = new MagicLink();
        User user = new User("testuser", "password");
        link.setUser(user);
        assertSame(user, link.getUser());
    }

    @Test
    void testSetCategoryId() {
        MagicLink link = new MagicLink();
        link.setCategoryId("cat-1");
        assertEquals("cat-1", link.getCategoryId());
    }

    @Test
    void testSetFilterStart() {
        MagicLink link = new MagicLink();
        LocalDate start = LocalDate.of(2025, 1, 1);
        link.setFilterStart(start);
        assertEquals(start, link.getFilterStart());
    }

    @Test
    void testSetFilterEnd() {
        MagicLink link = new MagicLink();
        LocalDate end = LocalDate.of(2025, 12, 31);
        link.setFilterEnd(end);
        assertEquals(end, link.getFilterEnd());
    }

    @Test
    void testSetExpiresAt() {
        MagicLink link = new MagicLink();
        LocalDateTime expires = LocalDateTime.of(2026, 6, 1, 0, 0);
        link.setExpiresAt(expires);
        assertEquals(expires, link.getExpiresAt());
    }

    @Test
    void testSetCreatedAt() {
        MagicLink link = new MagicLink();
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 12, 0);
        link.setCreatedAt(created);
        assertEquals(created, link.getCreatedAt());
    }

    @Test
    void testIsExpiredReturnsFalseWhenNoExpiry() {
        MagicLink link = new MagicLink();
        assertFalse(link.isExpired());
    }

    @Test
    void testIsExpiredReturnsFalseWhenFutureExpiry() {
        MagicLink link = new MagicLink();
        link.setExpiresAt(LocalDateTime.now().plusDays(1));
        assertFalse(link.isExpired());
    }

    @Test
    void testIsExpiredReturnsTrueWhenPastExpiry() {
        MagicLink link = new MagicLink();
        link.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertTrue(link.isExpired());
    }

    @Test
    void testEqualsSameId() {
        MagicLink link1 = new MagicLink();
        MagicLink link2 = new MagicLink();
        link2.setId(link1.getId());
        assertEquals(link1, link2);
    }

    @Test
    void testEqualsDifferentId() {
        MagicLink link1 = new MagicLink();
        MagicLink link2 = new MagicLink();
        assertNotEquals(link1, link2);
    }

    @Test
    void testEqualsSameObject() {
        MagicLink link = new MagicLink();
        assertEquals(link, link);
    }

    @Test
    void testEqualsNull() {
        MagicLink link = new MagicLink();
        assertNotEquals(null, link);
    }

    @Test
    void testEqualsDifferentType() {
        MagicLink link = new MagicLink();
        assertNotEquals("not a magic link", link);
    }

    @Test
    void testHashCodeConsistentWithEquals() {
        MagicLink link1 = new MagicLink();
        MagicLink link2 = new MagicLink();
        link2.setId(link1.getId());
        assertEquals(link1.hashCode(), link2.hashCode());
    }

    @Test
    void testUniqueTokensGenerated() {
        MagicLink link1 = new MagicLink();
        MagicLink link2 = new MagicLink();
        assertNotEquals(link1.getToken(), link2.getToken());
    }
}
