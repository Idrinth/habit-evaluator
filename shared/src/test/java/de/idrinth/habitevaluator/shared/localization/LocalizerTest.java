package de.idrinth.habitevaluator.shared.localization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalizerTest {

    private Localizer localizer;

    @BeforeEach
    void setUp() {
        localizer = new Localizer() {
            @Override
            java.io.InputStream getResourceStream(String path) {
                return super.getResourceStream(path.replace("localization/", "test-localization/"));
            }
        };
    }

    @Test
    void returnsModuleKeyInRequestedLanguage() {
        assertEquals("Gewohnheit hinzufügen", localizer.translate("habits", "add_button", "de"));
    }

    @Test
    void fallsBackToEnglishModuleKey() {
        // "track_heading" exists in en but not in de
        assertEquals("Track Habits", localizer.translate("habits", "track_heading", "de"));
    }

    @Test
    void fallsBackToGeneralKeyInRequestedLanguage() {
        // "shared_key" does not exist in module "habits" but exists in general for de
        assertEquals("Allgemein Deutsch", localizer.translate("habits", "shared_key", "de"));
    }

    @Test
    void fallsBackToGeneralKeyInEnglish() {
        // "shared_key" does not exist in module "scoring" and no "fr" file exists
        assertEquals("General English", localizer.translate("scoring", "shared_key", "fr"));
    }

    @Test
    void returnsModuleDotKeyWhenNothingMatches() {
        assertEquals("unknown.missing_key", localizer.translate("unknown", "missing_key", "de"));
    }

    @Test
    void translateWithDefaultLanguageUsesEnglish() {
        assertEquals("Add Habit", localizer.translate("habits", "add_button"));
    }

    @Test
    void returnsEnglishModuleKeyWhenLanguageIsEnglish() {
        assertEquals("Add Habit", localizer.translate("habits", "add_button", "en"));
    }

    @Test
    void returnsGeneralKeyInEnglishWhenLanguageIsEnglish() {
        assertEquals("General English", localizer.translate("scoring", "shared_key", "en"));
    }

    @Test
    void returnsFallbackForMissingLanguageFile() {
        assertEquals("Add Habit", localizer.translate("habits", "add_button", "xx"));
    }

    @Test
    void returnsFallbackKeyForCompletelyMissingKey() {
        assertEquals("habits.nonexistent", localizer.translate("habits", "nonexistent", "en"));
    }
}
