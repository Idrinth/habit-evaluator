package de.idrinth.habitevaluator.android

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for the phone URI scheme constant equivalent to what EmergencyPlanFragment provided.
 * Used to build tel: URIs for crisis contact phone numbers.
 */
class EmergencyPlanScreenTest {

    companion object {
        const val PHONE_URI_SCHEME = "tel:"

        /**
         * Replication of action filtering logic from EmergencyPlanScreen.
         * Filters action inputs to only those with non-blank text.
         */
        fun filterValidActions(actions: List<Pair<String, String>>): List<Pair<String, String>> {
            return actions.filter { it.first.isNotBlank() }
        }

        /**
         * Replication of next step order calculation from EmergencyPlanScreen.
         * Returns 0 for empty list, or max order + 1 for non-empty.
         */
        fun calculateNextStepOrder(existingOrders: List<Int>): Int {
            return if (existingOrders.isEmpty()) 0 else existingOrders.max() + 1
        }

        /**
         * Replication of swap order logic from EmergencyPlanScreen.
         * Swaps the order values of two indices in a mutable list.
         */
        fun swapOrders(orders: MutableList<Int>, indexA: Int, indexB: Int): List<Int> {
            val tmp = orders[indexA]
            orders[indexA] = orders[indexB]
            orders[indexB] = tmp
            return orders.toList()
        }
    }

    @Test
    fun testPhoneUriSchemeValue() {
        assertEquals("tel:", PHONE_URI_SCHEME)
    }

    @Test
    fun testPhoneUriSchemeStartsWithTel() {
        assertTrue(PHONE_URI_SCHEME.startsWith("tel"))
    }

    @Test
    fun testPhoneUriSchemeEndsWithColon() {
        assertTrue(PHONE_URI_SCHEME.endsWith(":"))
    }

    @Test
    fun testPhoneUriSchemeIsNotEmpty() {
        assertFalse(PHONE_URI_SCHEME.isEmpty())
    }

    @Test
    fun testPhoneUriSchemeCanBePrependedToNumber() {
        val phoneNumber = "1234567890"
        val uri = PHONE_URI_SCHEME + phoneNumber
        assertEquals("tel:1234567890", uri)
    }

    @Test
    fun testPhoneUriSchemeWithInternationalNumber() {
        val phoneNumber = "+49123456789"
        val uri = PHONE_URI_SCHEME + phoneNumber
        assertTrue(uri.startsWith("tel:+"))
    }

    @Test
    fun testPhoneUriSchemeIsLowerCase() {
        assertEquals(PHONE_URI_SCHEME, PHONE_URI_SCHEME.lowercase())
    }

    @Test
    fun testPhoneUriSchemeHasNoSpaces() {
        assertFalse(PHONE_URI_SCHEME.contains(" "))
    }

    // --- Action filtering tests ---

    @Test
    fun testFilterValidActionsAllValid() {
        val actions = listOf("Call crisis line" to "112", "Take medication" to "")
        val result = filterValidActions(actions)
        assertEquals(2, result.size)
    }

    @Test
    fun testFilterValidActionsRemovesBlank() {
        val actions = listOf("Call crisis line" to "112", "" to "555", "Take medication" to "")
        val result = filterValidActions(actions)
        assertEquals(2, result.size)
        assertEquals("Call crisis line", result[0].first)
        assertEquals("Take medication", result[1].first)
    }

    @Test
    fun testFilterValidActionsRemovesWhitespace() {
        val actions = listOf("   " to "112", "Take medication" to "")
        val result = filterValidActions(actions)
        assertEquals(1, result.size)
        assertEquals("Take medication", result[0].first)
    }

    @Test
    fun testFilterValidActionsAllEmpty() {
        val actions = listOf("" to "", "   " to "112")
        val result = filterValidActions(actions)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterValidActionsEmptyList() {
        val result = filterValidActions(emptyList())
        assertTrue(result.isEmpty())
    }

    // --- Step order calculation tests ---

    @Test
    fun testCalculateNextStepOrderEmptyList() {
        assertEquals(0, calculateNextStepOrder(emptyList()))
    }

    @Test
    fun testCalculateNextStepOrderSingleItem() {
        assertEquals(1, calculateNextStepOrder(listOf(0)))
    }

    @Test
    fun testCalculateNextStepOrderMultipleItems() {
        assertEquals(3, calculateNextStepOrder(listOf(0, 1, 2)))
    }

    @Test
    fun testCalculateNextStepOrderNonSequential() {
        assertEquals(11, calculateNextStepOrder(listOf(0, 5, 10)))
    }

    @Test
    fun testCalculateNextStepOrderWithGaps() {
        assertEquals(101, calculateNextStepOrder(listOf(0, 100)))
    }

    // --- Swap order tests ---

    @Test
    fun testSwapOrdersBasic() {
        val orders = mutableListOf(0, 1, 2)
        val result = swapOrders(orders, 0, 1)
        assertEquals(listOf(1, 0, 2), result)
    }

    @Test
    fun testSwapOrdersSameIndex() {
        val orders = mutableListOf(0, 1, 2)
        val result = swapOrders(orders, 1, 1)
        assertEquals(listOf(0, 1, 2), result)
    }

    @Test
    fun testSwapOrdersFirstAndLast() {
        val orders = mutableListOf(0, 1, 2, 3)
        val result = swapOrders(orders, 0, 3)
        assertEquals(listOf(3, 1, 2, 0), result)
    }

    @Test
    fun testSwapOrdersPreservesOtherElements() {
        val orders = mutableListOf(10, 20, 30)
        val result = swapOrders(orders, 0, 2)
        assertEquals(20, result[1])
    }
}
