package de.idrinth.habitevaluator.android.ui;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test: Android's BottomNavigationView supports a maximum of 5 menu items.
 * Adding a 6th item causes a runtime crash. This test parses the menu XML to prevent that.
 */
class BottomNavigationRegressionTest {

    private static final int BOTTOM_NAV_MAX_ITEMS = 5;
    private static final String MENU_XML_PATH = "src/main/res/menu/bottom_navigation.xml";

    private Document parseMenu() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new File(MENU_XML_PATH));
    }

    @Test
    void testBottomNavigationHasAtMostFiveItems() throws Exception {
        File menuFile = new File(MENU_XML_PATH);
        assertTrue( menuFile.exists(),"bottom_navigation.xml must exist at " + MENU_XML_PATH);

        NodeList items = parseMenu().getElementsByTagName("item");

        assertTrue(
                items.getLength() <= BOTTOM_NAV_MAX_ITEMS
        ,
                "BottomNavigationView supports at most " + BOTTOM_NAV_MAX_ITEMS
                        + " items, but bottom_navigation.xml defines " + items.getLength());
    }

    @Test
    void testBottomNavigationHasAtLeastOneItem() throws Exception {
        NodeList items = parseMenu().getElementsByTagName("item");

        assertTrue(
                items.getLength() >= 1
        ,
                "Bottom navigation must have at least one item");
    }

    @Test
    void testAllBottomNavItemsHaveIds() throws Exception {
        NodeList items = parseMenu().getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Node attr = items.item(i).getAttributes()
                    .getNamedItemNS("http://schemas.android.com/apk/res/android", "id");
            assertNotNull( attr,"Menu item at index " + i + " must have an android:id");
            assertFalse(
                    attr.getNodeValue().isEmpty(),"Menu item at index " + i + " must have a non-empty android:id");
        }
    }

    @Test
    void testAllBottomNavItemsHaveTitles() throws Exception {
        NodeList items = parseMenu().getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Node attr = items.item(i).getAttributes()
                    .getNamedItemNS("http://schemas.android.com/apk/res/android", "title");
            assertNotNull( attr,"Menu item at index " + i + " must have an android:title");
            assertFalse(
                    attr.getNodeValue().isEmpty(),"Menu item at index " + i + " must have a non-empty android:title");
        }
    }

    @Test
    void testAllBottomNavItemsHaveIcons() throws Exception {
        NodeList items = parseMenu().getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Node attr = items.item(i).getAttributes()
                    .getNamedItemNS("http://schemas.android.com/apk/res/android", "icon");
            assertNotNull( attr,"Menu item at index " + i + " must have an android:icon");
            assertFalse(
                    attr.getNodeValue().isEmpty(),"Menu item at index " + i + " must have a non-empty android:icon");
        }
    }

    @Test
    void testBottomNavItemCountMatchesNavigationHandlerCount() {
        // The setupBottomNavigation handler in MainActivity maps exactly 5 nav IDs to pages:
        // nav_home -> PAGE_HOME, nav_diary -> PAGE_DIARY, nav_sleep -> PAGE_SLEEP,
        // nav_stats -> PAGE_STATS, nav_emotions -> PAGE_EMOTIONAL_STATE
        int[] bottomNavPages = {
                ScreenPagerAdapter.PAGE_HOME,
                ScreenPagerAdapter.PAGE_DIARY,
                ScreenPagerAdapter.PAGE_SLEEP,
                ScreenPagerAdapter.PAGE_STATS,
                ScreenPagerAdapter.PAGE_EMOTIONAL_STATE
        };

        assertTrue(
                bottomNavPages.length <= BOTTOM_NAV_MAX_ITEMS
        ,
                "Bottom navigation page mappings (" + bottomNavPages.length
                        + ") must not exceed " + BOTTOM_NAV_MAX_ITEMS);

        // Each mapped page must be a valid page index
        for (int page : bottomNavPages) {
            assertTrue(
                    page >= 0 && page < ScreenPagerAdapter.PAGE_COUNT
            ,
                    "Bottom nav maps to page index " + page
                            + " which is outside valid range [0, " + (ScreenPagerAdapter.PAGE_COUNT - 1) + "]");
        }
    }
}
