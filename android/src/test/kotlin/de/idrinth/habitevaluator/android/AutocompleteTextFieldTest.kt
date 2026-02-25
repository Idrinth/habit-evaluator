package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for autocomplete filtering logic used in AutocompleteTextField.
 */
class AutocompleteTextFieldTest {

    companion object {
        /**
         * Replication of the single-value filtering logic from AutocompleteTextField.
         * Filters suggestions based on current input, excluding exact matches.
         */
        fun filterSuggestions(value: String, suggestions: List<String>): List<String> {
            return if (value.isBlank()) {
                suggestions
            } else {
                suggestions.filter {
                    it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true)
                }
            }
        }

        /**
         * Replication of comma-separated filtering logic used in FoodLogScreen and ActivityLogScreen.
         * Filters suggestions for the last item in a comma-separated string,
         * excluding items already entered.
         */
        fun filterCommaSeparatedSuggestions(
            currentValue: String,
            suggestions: List<String>
        ): List<String> {
            val parts = currentValue.split(",").map { it.trim().lowercase() }
            val alreadyEntered = if (parts.size > 1) parts.dropLast(1).toSet() else emptySet()
            val currentInput = parts.lastOrNull()?.trim() ?: ""
            return suggestions.filter { suggestion ->
                suggestion.lowercase() !in alreadyEntered &&
                    (currentInput.isEmpty() || suggestion.contains(currentInput, ignoreCase = true))
            }.map { suggestion ->
                if (parts.size > 1) {
                    val prefix = currentValue.substringBeforeLast(",") + ", "
                    prefix + suggestion
                } else {
                    suggestion
                }
            }
        }
    }

    // --- Single-value filtering tests ---

    @Test
    fun testFilterSuggestionsEmptyInput() {
        val result = filterSuggestions("", listOf("apple", "banana", "cherry"))
        assertEquals(listOf("apple", "banana", "cherry"), result)
    }

    @Test
    fun testFilterSuggestionsBlankInput() {
        val result = filterSuggestions("   ", listOf("apple", "banana", "cherry"))
        assertEquals(listOf("apple", "banana", "cherry"), result)
    }

    @Test
    fun testFilterSuggestionsPartialMatch() {
        val result = filterSuggestions("app", listOf("apple", "banana", "pineapple"))
        assertEquals(listOf("apple", "pineapple"), result)
    }

    @Test
    fun testFilterSuggestionsCaseInsensitive() {
        val result = filterSuggestions("APP", listOf("apple", "banana", "pineapple"))
        assertEquals(listOf("apple", "pineapple"), result)
    }

    @Test
    fun testFilterSuggestionsExactMatchExcluded() {
        val result = filterSuggestions("apple", listOf("apple", "pineapple"))
        assertEquals(listOf("pineapple"), result)
    }

    @Test
    fun testFilterSuggestionsExactMatchCaseInsensitiveExcluded() {
        val result = filterSuggestions("Apple", listOf("apple", "pineapple"))
        assertEquals(listOf("pineapple"), result)
    }

    @Test
    fun testFilterSuggestionsNoMatch() {
        val result = filterSuggestions("xyz", listOf("apple", "banana", "cherry"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterSuggestionsEmptyList() {
        val result = filterSuggestions("app", emptyList())
        assertTrue(result.isEmpty())
    }

    // --- Comma-separated filtering tests ---

    @Test
    fun testCommaSeparatedFirstItem() {
        val result = filterCommaSeparatedSuggestions("app", listOf("apple", "banana", "pineapple"))
        assertEquals(listOf("apple", "pineapple"), result)
    }

    @Test
    fun testCommaSeparatedSecondItem() {
        val result = filterCommaSeparatedSuggestions("apple, ban", listOf("apple", "banana", "cherry"))
        assertEquals(listOf("apple, banana"), result)
    }

    @Test
    fun testCommaSeparatedExcludesAlreadyEntered() {
        val result = filterCommaSeparatedSuggestions("apple, ", listOf("apple", "banana", "cherry"))
        assertEquals(listOf("apple, banana", "apple, cherry"), result)
    }

    @Test
    fun testCommaSeparatedMultipleAlreadyEntered() {
        val result = filterCommaSeparatedSuggestions(
            "apple, banana, ",
            listOf("apple", "banana", "cherry", "date")
        )
        assertEquals(listOf("apple, banana, cherry", "apple, banana, date"), result)
    }

    @Test
    fun testCommaSeparatedEmptyInput() {
        val result = filterCommaSeparatedSuggestions("", listOf("apple", "banana"))
        assertEquals(listOf("apple", "banana"), result)
    }

    @Test
    fun testCommaSeparatedEmptySuggestions() {
        val result = filterCommaSeparatedSuggestions("app", emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCommaSeparatedNoMatchForCurrentItem() {
        val result = filterCommaSeparatedSuggestions("apple, xyz", listOf("apple", "banana", "cherry"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCommaSeparatedCaseInsensitiveExclusion() {
        val result = filterCommaSeparatedSuggestions("Apple, ", listOf("apple", "banana"))
        assertEquals(listOf("Apple, banana"), result)
    }
}
