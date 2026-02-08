package de.idrinth.habitevaluator.shared.localization;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceStreamProviderTest {

    private final ResourceStreamProvider provider = new ResourceStreamProvider();

    @Test
    void returnsStreamForValidPath() {
        InputStream stream = provider.getResourceStream("localization/en.yml");
        assertNotNull(stream);
    }

    @Test
    void returnsNullForNonexistentResource() {
        InputStream stream = provider.getResourceStream("localization/nonexistent.yml");
        assertNull(stream);
    }

    @Test
    void rejectsNullPath() {
        assertThrows(IllegalArgumentException.class, () -> provider.getResourceStream(null));
    }

    @Test
    void rejectsEmptyPath() {
        assertThrows(IllegalArgumentException.class, () -> provider.getResourceStream(""));
    }

    @Test
    void rejectsPathTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> provider.getResourceStream("localization/../../etc/passwd"));
    }

    @Test
    void rejectsPathWithBackslashes() {
        assertThrows(IllegalArgumentException.class,
                () -> provider.getResourceStream("localization\\en.yml"));
    }

    @Test
    void rejectsPathWithNullBytes() {
        assertThrows(IllegalArgumentException.class,
                () -> provider.getResourceStream("localization/en.yml\0.txt"));
    }

    @Test
    void rejectsPathWithWrongPrefix() {
        assertThrows(IllegalArgumentException.class,
                () -> provider.getResourceStream("other/en.yml"));
    }

    @Test
    void rejectsAbsolutePath() {
        assertThrows(IllegalArgumentException.class,
                () -> provider.getResourceStream("/etc/passwd"));
    }
}
