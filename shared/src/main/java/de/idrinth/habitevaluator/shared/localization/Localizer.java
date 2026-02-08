package de.idrinth.habitevaluator.shared.localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides localized string lookup from YAML resource files.
 *
 * <p>YAML files are expected at {@code localization/{lang}.yml} on the classpath,
 * structured as:
 * <pre>
 * general:
 *   shared_key: "Shared translation"
 * module_name:
 *   key: "Translation"
 * </pre>
 *
 * <p>Resolution order for {@code translate(module, key, language)}:
 * <ol>
 *   <li>{@code module.key} in the requested language</li>
 *   <li>{@code module.key} in English</li>
 *   <li>{@code general.key} in the requested language</li>
 *   <li>{@code general.key} in English</li>
 *   <li>The literal string {@code module.key}</li>
 * </ol>
 */
public class Localizer {

    private static final Logger LOG = LoggerFactory.getLogger(Localizer.class);
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String GENERAL_MODULE = "general";

    private final ConcurrentHashMap<String, Map<String, Map<String, String>>> languages = new ConcurrentHashMap<>();
    private final ResourceStreamProvider resourceStreamProvider;

    public Localizer() {
        this(new ResourceStreamProvider());
    }

    public Localizer(ResourceStreamProvider resourceStreamProvider) {
        this.resourceStreamProvider = resourceStreamProvider;
        load(DEFAULT_LANGUAGE);
    }

    /**
     * Translates a key within a module for the given language, following the
     * fallback chain documented above.
     *
     * @param module   the module (top-level YAML key) to look in first
     * @param key      the translation key
     * @param language the desired language code (e.g. "en", "de")
     * @return the resolved translation, or {@code module.key} if nothing matched
     */
    public String translate(String module, String key, String language) {
        load(language);

        // 1. module.key in requested language
        String value = lookup(language, module, key);
        if (value != null) {
            return value;
        }

        // 2. module.key in English
        if (!DEFAULT_LANGUAGE.equals(language)) {
            value = lookup(DEFAULT_LANGUAGE, module, key);
            if (value != null) {
                return value;
            }
        }

        // 3. general.key in requested language
        value = lookup(language, GENERAL_MODULE, key);
        if (value != null) {
            return value;
        }

        // 4. general.key in English
        if (!DEFAULT_LANGUAGE.equals(language)) {
            value = lookup(DEFAULT_LANGUAGE, GENERAL_MODULE, key);
            if (value != null) {
                return value;
            }
        }

        // 5. fallback to module.key literal
        return module + "." + key;
    }

    /**
     * Translates a key within a module using the default language (English).
     */
    public String translate(String module, String key) {
        return translate(module, key, DEFAULT_LANGUAGE);
    }

    private String lookup(String language, String module, String key) {
        Map<String, Map<String, String>> lang = languages.get(language);
        if (lang == null) {
            return null;
        }
        Map<String, String> mod = lang.get(module);
        if (mod == null) {
            return null;
        }
        return mod.get(key);
    }

    @SuppressWarnings("unchecked")
    private void load(String language) {
        if (languages.containsKey(language)) {
            return;
        }
        String path = "localization/" + language + ".yml";
        try (InputStream stream = resourceStreamProvider.getResourceStream(path)) {
            if (stream == null) {
                LOG.debug("No localization file found for language: {}", language);
                languages.put(language, Map.of());
                return;
            }
            Yaml yaml = new Yaml();
            Map<String, Object> raw = yaml.load(stream);
            if (raw == null) {
                languages.put(language, Map.of());
                return;
            }
            Map<String, Map<String, String>> parsed = new ConcurrentHashMap<>();
            for (Map.Entry<String, Object> moduleEntry : raw.entrySet()) {
                if (moduleEntry.getValue() instanceof Map) {
                    Map<String, String> keys = new ConcurrentHashMap<>();
                    for (Map.Entry<String, Object> keyEntry : ((Map<String, Object>) moduleEntry.getValue()).entrySet()) {
                        if (keyEntry.getValue() != null) {
                            keys.put(keyEntry.getKey(), String.valueOf(keyEntry.getValue()));
                        }
                    }
                    parsed.put(moduleEntry.getKey(), keys);
                }
            }
            languages.put(language, parsed);
        } catch (Exception e) {
            LOG.warn("Failed to load localization file: {}", path, e);
            languages.put(language, Map.of());
        }
    }

}
