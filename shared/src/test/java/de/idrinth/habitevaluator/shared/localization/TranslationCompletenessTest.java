package de.idrinth.habitevaluator.shared.localization;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TranslationCompletenessTest {

    @SuppressWarnings("unchecked")
    private Set<String> extractKeys(String language) {
        String path = "localization/" + language + ".yml";
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        assertNotNull(stream, "Missing localization file: " + path);
        Yaml yaml = new Yaml();
        Map<String, Object> raw = yaml.load(stream);
        assertNotNull(raw, "Empty localization file: " + path);
        Set<String> keys = new TreeSet<>();
        for (Map.Entry<String, Object> module : raw.entrySet()) {
            if (module.getValue() instanceof Map) {
                for (String key : ((Map<String, Object>) module.getValue()).keySet()) {
                    keys.add(module.getKey() + "." + key);
                }
            }
        }
        return keys;
    }

    @ParameterizedTest(name = "{0} has same keys as en")
    @ValueSource(strings = {"de", "es", "fr"})
    void languageHasSameKeysAsEnglish(String language) {
        Set<String> englishKeys = extractKeys("en");
        Set<String> otherKeys = extractKeys(language);

        Set<String> missingInOther = new TreeSet<>(englishKeys);
        missingInOther.removeAll(otherKeys);

        Set<String> extraInOther = new TreeSet<>(otherKeys);
        extraInOther.removeAll(englishKeys);

        assertEquals(
                Set.of(),
                missingInOther,
                language + " is missing keys: " + missingInOther
        );
        assertEquals(
                Set.of(),
                extraInOther,
                language + " has extra keys: " + extraInOther
        );
    }
}
