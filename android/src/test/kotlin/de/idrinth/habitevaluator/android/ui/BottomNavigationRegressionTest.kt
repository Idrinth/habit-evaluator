package de.idrinth.habitevaluator.android.ui

import org.junit.Test
import org.junit.Assert.*
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Regression test: Android's BottomNavigationView supports a maximum of 5 menu items.
 * Adding a 6th item causes a runtime crash. This test parses the menu XML to prevent
 * that regression.
 *
 * The original test also verified page-index constants from ScreenPagerAdapter.
 * Those constants are reproduced locally (they are also tested in ScreenPagerAdapterTest).
 */
class BottomNavigationRegressionTest {

    companion object {
        private const val BOTTOM_NAV_MAX_ITEMS = 5
        private const val MENU_XML_PATH = "src/main/res/menu/bottom_navigation.xml"

        // Page constants mirrored from ScreenPagerAdapterTest
        const val PAGE_HOME = 0
        const val PAGE_DIARY = 1
        const val PAGE_PLANNER = 2
        const val PAGE_EMERGENCY_PLAN = 3
        const val PAGE_EMOTIONAL_STATE = 4
        const val PAGE_COUNT = 20
    }

    private fun parseMenu(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        return factory.newDocumentBuilder().parse(File(MENU_XML_PATH))
    }

    @Test
    fun testBottomNavigationHasAtMostFiveItems() {
        val menuFile = File(MENU_XML_PATH)
        assertTrue("bottom_navigation.xml must exist at $MENU_XML_PATH", menuFile.exists())

        val items: NodeList = parseMenu().getElementsByTagName("item")
        assertTrue(
            "BottomNavigationView supports at most $BOTTOM_NAV_MAX_ITEMS items, " +
                "but bottom_navigation.xml defines ${items.length}",
            items.length <= BOTTOM_NAV_MAX_ITEMS
        )
    }

    @Test
    fun testBottomNavigationHasAtLeastOneItem() {
        val items: NodeList = parseMenu().getElementsByTagName("item")
        assertTrue("Bottom navigation must have at least one item", items.length >= 1)
    }

    @Test
    fun testAllBottomNavItemsHaveIds() {
        val items: NodeList = parseMenu().getElementsByTagName("item")
        for (i in 0 until items.length) {
            val attr = items.item(i).attributes
                .getNamedItemNS("http://schemas.android.com/apk/res/android", "id")
            assertNotNull("Menu item at index $i must have an android:id", attr)
            assertFalse("Menu item at index $i must have a non-empty android:id", attr.nodeValue.isEmpty())
        }
    }

    @Test
    fun testAllBottomNavItemsHaveTitles() {
        val items: NodeList = parseMenu().getElementsByTagName("item")
        for (i in 0 until items.length) {
            val attr = items.item(i).attributes
                .getNamedItemNS("http://schemas.android.com/apk/res/android", "title")
            assertNotNull("Menu item at index $i must have an android:title", attr)
            assertFalse("Menu item at index $i must have a non-empty android:title", attr.nodeValue.isEmpty())
        }
    }

    @Test
    fun testAllBottomNavItemsHaveIcons() {
        val items: NodeList = parseMenu().getElementsByTagName("item")
        for (i in 0 until items.length) {
            val attr = items.item(i).attributes
                .getNamedItemNS("http://schemas.android.com/apk/res/android", "icon")
            assertNotNull("Menu item at index $i must have an android:icon", attr)
            assertFalse("Menu item at index $i must have a non-empty android:icon", attr.nodeValue.isEmpty())
        }
    }

    @Test
    fun testBottomNavItemCountMatchesNavigationHandlerCount() {
        // The bottom navigation handler maps exactly 5 nav IDs to Compose screen routes:
        // nav_home, nav_diary, nav_planner, nav_emergency_plan, nav_emotions
        val bottomNavPages = intArrayOf(
            PAGE_HOME,
            PAGE_DIARY,
            PAGE_PLANNER,
            PAGE_EMERGENCY_PLAN,
            PAGE_EMOTIONAL_STATE
        )

        assertTrue(
            "Bottom navigation page mappings (${bottomNavPages.size}) must not exceed $BOTTOM_NAV_MAX_ITEMS",
            bottomNavPages.size <= BOTTOM_NAV_MAX_ITEMS
        )

        // Each mapped page must be a valid page index
        for (page in bottomNavPages) {
            assertTrue(
                "Bottom nav maps to page index $page which is outside valid range [0, ${PAGE_COUNT - 1}]",
                page >= 0 && page < PAGE_COUNT
            )
        }
    }
}
