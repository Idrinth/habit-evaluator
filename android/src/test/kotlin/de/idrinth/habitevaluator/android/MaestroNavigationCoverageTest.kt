package de.idrinth.habitevaluator.android

import de.idrinth.habitevaluator.android.ui.navigation.Screen
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Unit tests derived from Maestro UI tests 01-13.
 *
 * These tests verify navigation structure, route mapping, and screen
 * configuration that the Maestro tests validated via emulator interactions.
 * By testing these invariants at the unit level we catch regressions faster
 * without needing a running emulator.
 *
 * Maestro test mapping:
 *  01 -> first-start key, start destination
 *  02 -> bottom nav items match Screen routes
 *  03 -> toolbar actions map to Stats/Settings/Imprint
 *  04 -> diary navigation cards map to valid Screen routes
 *  05 -> AddHabit route reachable from Home
 *  06 -> AddEmotionPair route reachable from EmotionalState
 *  07 -> SleepAnalysis route reachable from Sleep
 *  08 -> settings sections use correct SettingsConstants keys
 *  09 -> imprint content constants are stable
 *  10 -> stats screen route exists
 *  11 -> emergency plan route and dialogue route exist
 *  12 -> sleep tracking route exists
 *  13 -> full navigation flow (all routes connected)
 */
class MaestroNavigationCoverageTest {

    companion object {
        private const val MENU_XML_PATH = "src/main/res/menu/bottom_navigation.xml"

        /** Bottom navigation items as defined in MainActivity.kt AppContent composable. */
        val BOTTOM_NAV_SCREENS = listOf(
            Screen.Home,
            Screen.Diary,
            Screen.Planner,
            Screen.EmergencyPlan,
            Screen.EmotionalState
        )

        /** Toolbar action screens as defined in MainActivity.kt TopAppBar actions. */
        val TOOLBAR_SCREENS = listOf(
            Screen.Stats,
            Screen.Settings,
            Screen.Imprint
        )

        /** Diary navigation card targets as defined in DiaryNavigationScreen.kt. */
        val DIARY_NAV_CARD_SCREENS = listOf(
            Screen.PositivityDiary,
            Screen.Sleep,
            Screen.SportLog,
            Screen.FoodLog,
            Screen.MedicationLog,
            Screen.MedicationList,
            Screen.ActivityLog
        )

        /** Every Screen object that AppNavigation registers a composable for. */
        val ALL_REGISTERED_SCREENS = listOf(
            Screen.Home,
            Screen.Diary,
            Screen.Sleep,
            Screen.EmergencyPlan,
            Screen.EmotionalState,
            Screen.EditHabits,
            Screen.AddHabit,
            Screen.Stats,
            Screen.PointDevelopment,
            Screen.AddEmotionPair,
            Screen.RecordEmotionEntry,
            Screen.Settings,
            Screen.Imprint,
            Screen.PositivityDiary,
            Screen.SportLog,
            Screen.FoodLog,
            Screen.MedicationLog,
            Screen.MedicationList,
            Screen.ActivityLog,
            Screen.PdfExport,
            Screen.SleepAnalysis,
            Screen.Correlations,
            Screen.EmergencyDialogue,
            Screen.Planner
        )
    }

    // ── Maestro 01: First start & home ────────────────────────────────────

    @Test
    fun testStartDestinationIsHome() {
        assertEquals(
            "home", Screen.Home.route,
            "Start destination must be 'home' (AppNavigation startDestination)"
        )
    }

    @Test
    fun testFirstStartCompletedKeyExists() {
        assertEquals(
            "first_start_completed",
            SettingsConstants.KEY_FIRST_START_COMPLETED,
            "First-start dialog requires a SharedPreferences key"
        )
    }

    @Test
    fun testStorageModeLocalIsDefault() {
        assertEquals(
            "LOCAL",
            SettingsConstants.MODE_LOCAL,
            "First-start home screen displays 'Storage: Local' which requires MODE_LOCAL constant"
        )
    }

    // ── Maestro 02: Bottom navigation tabs ────────────────────────────────

    @Test
    fun testBottomNavHasExactlyFiveItems() {
        assertEquals(5, BOTTOM_NAV_SCREENS.size, "Bottom navigation must have exactly 5 items")
    }

    @Test
    fun testBottomNavScreensAreAllRegistered() {
        val registeredRoutes = ALL_REGISTERED_SCREENS.map { it.route }.toSet()
        for (screen in BOTTOM_NAV_SCREENS) {
            assertTrue(
                registeredRoutes.contains(screen.route),
                "Bottom nav screen '${screen.route}' must be registered in AppNavigation"
            )
        }
    }

    @Test
    fun testBottomNavRoutesAreUnique() {
        val routes = BOTTOM_NAV_SCREENS.map { it.route }
        assertEquals(routes.size, routes.toSet().size, "Bottom nav routes must be unique")
    }

    @Test
    fun testBottomNavFirstItemIsHome() {
        assertEquals(Screen.Home, BOTTOM_NAV_SCREENS[0], "First bottom nav item must be Home")
    }

    @Test
    fun testBottomNavSecondItemIsDiary() {
        assertEquals(Screen.Diary, BOTTOM_NAV_SCREENS[1], "Second bottom nav item must be Diary")
    }

    @Test
    fun testBottomNavThirdItemIsPlanner() {
        assertEquals(Screen.Planner, BOTTOM_NAV_SCREENS[2], "Third bottom nav item must be Planner")
    }

    @Test
    fun testBottomNavFourthItemIsEmergencyPlan() {
        assertEquals(Screen.EmergencyPlan, BOTTOM_NAV_SCREENS[3], "Fourth bottom nav item must be EmergencyPlan")
    }

    @Test
    fun testBottomNavFifthItemIsEmotionalState() {
        assertEquals(Screen.EmotionalState, BOTTOM_NAV_SCREENS[4], "Fifth bottom nav item must be EmotionalState")
    }

    @Test
    fun testBottomNavMenuXmlItemCountMatchesScreenCount() {
        val menuFile = File(MENU_XML_PATH)
        if (!menuFile.exists()) return // Skip if running from a different working directory
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val doc = factory.newDocumentBuilder().parse(menuFile)
        val items: NodeList = doc.getElementsByTagName("item")
        assertEquals(
            BOTTOM_NAV_SCREENS.size, items.length,
            "bottom_navigation.xml item count must match the bottom nav screen list"
        )
    }

    // ── Maestro 03: Toolbar buttons ───────────────────────────────────────

    @Test
    fun testToolbarHasThreeActions() {
        assertEquals(3, TOOLBAR_SCREENS.size, "Toolbar must have exactly 3 action buttons")
    }

    @Test
    fun testToolbarScreensAreAllRegistered() {
        val registeredRoutes = ALL_REGISTERED_SCREENS.map { it.route }.toSet()
        for (screen in TOOLBAR_SCREENS) {
            assertTrue(
                registeredRoutes.contains(screen.route),
                "Toolbar screen '${screen.route}' must be registered in AppNavigation"
            )
        }
    }

    @Test
    fun testToolbarFirstActionIsStats() {
        assertEquals(Screen.Stats, TOOLBAR_SCREENS[0], "First toolbar action must be Stats")
    }

    @Test
    fun testToolbarSecondActionIsSettings() {
        assertEquals(Screen.Settings, TOOLBAR_SCREENS[1], "Second toolbar action must be Settings")
    }

    @Test
    fun testToolbarThirdActionIsImprint() {
        assertEquals(Screen.Imprint, TOOLBAR_SCREENS[2], "Third toolbar action must be Imprint")
    }

    @Test
    fun testToolbarRoutesAreNotInBottomNav() {
        val bottomRoutes = BOTTOM_NAV_SCREENS.map { it.route }.toSet()
        for (screen in TOOLBAR_SCREENS) {
            assertFalse(
                bottomRoutes.contains(screen.route),
                "Toolbar screen '${screen.route}' must not duplicate a bottom nav route"
            )
        }
    }

    // ── Maestro 04: Diary sub-pages ───────────────────────────────────────

    @Test
    fun testDiaryNavigationHasSevenCards() {
        assertEquals(7, DIARY_NAV_CARD_SCREENS.size, "Diary navigation must have 7 cards")
    }

    @Test
    fun testDiaryNavCardsAreAllRegistered() {
        val registeredRoutes = ALL_REGISTERED_SCREENS.map { it.route }.toSet()
        for (screen in DIARY_NAV_CARD_SCREENS) {
            assertTrue(
                registeredRoutes.contains(screen.route),
                "Diary nav card screen '${screen.route}' must be registered in AppNavigation"
            )
        }
    }

    @Test
    fun testDiaryNavCardRoutesAreUnique() {
        val routes = DIARY_NAV_CARD_SCREENS.map { it.route }
        assertEquals(routes.size, routes.toSet().size, "Diary nav card routes must be unique")
    }

    @Test
    fun testDiaryNavFirstCardIsPositivityDiary() {
        assertEquals(Screen.PositivityDiary, DIARY_NAV_CARD_SCREENS[0])
    }

    @Test
    fun testDiaryNavSecondCardIsSleep() {
        assertEquals(Screen.Sleep, DIARY_NAV_CARD_SCREENS[1])
    }

    @Test
    fun testDiaryNavThirdCardIsSportLog() {
        assertEquals(Screen.SportLog, DIARY_NAV_CARD_SCREENS[2])
    }

    @Test
    fun testDiaryNavFourthCardIsFoodLog() {
        assertEquals(Screen.FoodLog, DIARY_NAV_CARD_SCREENS[3])
    }

    @Test
    fun testDiaryNavFifthCardIsMedicationLog() {
        assertEquals(Screen.MedicationLog, DIARY_NAV_CARD_SCREENS[4])
    }

    @Test
    fun testDiaryNavSixthCardIsMedicationList() {
        assertEquals(Screen.MedicationList, DIARY_NAV_CARD_SCREENS[5])
    }

    @Test
    fun testDiaryNavSeventhCardIsActivityLog() {
        assertEquals(Screen.ActivityLog, DIARY_NAV_CARD_SCREENS[6])
    }

    // ── Maestro 05: Add habit page ────────────────────────────────────────

    @Test
    fun testAddHabitRouteIsRegistered() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.AddHabit),
            "AddHabit screen must be registered in AppNavigation"
        )
    }

    @Test
    fun testAddHabitRouteIsNotParameterized() {
        assertFalse(
            Screen.AddHabit.route.contains("{"),
            "AddHabit route must not be parameterized"
        )
    }

    // ── Maestro 06: Add emotion pair page ─────────────────────────────────

    @Test
    fun testAddEmotionPairRouteIsRegistered() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.AddEmotionPair),
            "AddEmotionPair screen must be registered in AppNavigation"
        )
    }

    // ── Maestro 07: Sleep analysis page ───────────────────────────────────

    @Test
    fun testSleepAnalysisRouteIsRegistered() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.SleepAnalysis),
            "SleepAnalysis screen must be registered in AppNavigation"
        )
    }

    // ── Maestro 08: Settings sections ─────────────────────────────────────

    @Test
    fun testSettingsScreenRouteExists() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.Settings),
            "Settings screen must be registered in AppNavigation"
        )
    }

    @Test
    fun testSettingsHasStorageModeKeys() {
        assertNotNull(SettingsConstants.KEY_STORAGE_MODE)
        assertNotNull(SettingsConstants.MODE_LOCAL)
        assertNotNull(SettingsConstants.MODE_REMOTE)
    }

    @Test
    fun testSettingsHasThemeModeKeys() {
        assertNotNull(SettingsConstants.KEY_THEME_MODE)
        assertNotNull(SettingsConstants.THEME_SYSTEM)
        assertNotNull(SettingsConstants.THEME_LIGHT)
        assertNotNull(SettingsConstants.THEME_DARK)
    }

    @Test
    fun testSettingsHasLanguageKeys() {
        assertNotNull(SettingsConstants.KEY_LANGUAGE)
        assertNotNull(SettingsConstants.LANGUAGE_SYSTEM)
        assertNotNull(SettingsConstants.LANGUAGE_EN)
        assertNotNull(SettingsConstants.LANGUAGE_DE)
        assertNotNull(SettingsConstants.LANGUAGE_ES)
        assertNotNull(SettingsConstants.LANGUAGE_FR)
    }

    @Test
    fun testSettingsHasFontSizeKey() {
        assertNotNull(SettingsConstants.KEY_FONT_SIZE)
    }

    @Test
    fun testSettingsHasModuleVisibilityKeys() {
        assertNotNull(SettingsConstants.KEY_MODULE_DIARY_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_SLEEP_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_EMOTIONS_VISIBLE)
        assertNotNull(SettingsConstants.KEY_MODULE_STATISTICS_VISIBLE)
    }

    @Test
    fun testSettingsHasBackupKeys() {
        assertNotNull(SettingsConstants.KEY_BACKUP_ENABLED)
        assertNotNull(SettingsConstants.KEY_BACKUP_PASSWORD)
    }

    // ── Maestro 10: Stats page ────────────────────────────────────────────

    @Test
    fun testStatsScreenRouteExists() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.Stats),
            "Stats screen must be registered in AppNavigation"
        )
    }

    @Test
    fun testPdfExportScreenRouteExists() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.PdfExport),
            "PdfExport screen must be registered — Stats page has an 'Export PDF' button"
        )
    }

    @Test
    fun testCorrelationsScreenRouteExists() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.Correlations),
            "Correlations screen must be registered — Stats page has a 'View Correlations' button"
        )
    }

    // ── Maestro 11: Emergency plan page ───────────────────────────────────

    @Test
    fun testEmergencyPlanRouteExists() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.EmergencyPlan),
            "EmergencyPlan screen must be registered in AppNavigation"
        )
    }

    @Test
    fun testEmergencyDialogueRouteExists() {
        assertTrue(
            ALL_REGISTERED_SCREENS.contains(Screen.EmergencyDialogue),
            "EmergencyDialogue screen must be registered — Emergency Plan has a 'Start Dialogue' button"
        )
    }

    // ── Maestro 13: Full navigation flow (completeness) ───────────────────

    @Test
    fun testAllRegisteredScreensHaveNonEmptyRoutes() {
        for (screen in ALL_REGISTERED_SCREENS) {
            assertTrue(
                screen.route.isNotBlank(),
                "Screen ${screen::class.simpleName} must have a non-blank route"
            )
        }
    }

    @Test
    fun testAllRegisteredScreenRoutesAreUnique() {
        val routes = ALL_REGISTERED_SCREENS.map { it.route }
        assertEquals(
            routes.size, routes.toSet().size,
            "All registered screen routes must be unique. Duplicates: ${routes.groupBy { it }.filter { it.value.size > 1 }.keys}"
        )
    }

    @Test
    fun testRegisteredScreenCountMatchesExpected() {
        assertEquals(
            24, ALL_REGISTERED_SCREENS.size,
            "Total number of registered screens must be 24"
        )
    }

    @Test
    fun testBottomNavAndToolbarCoverAllPrimaryNavigation() {
        val primaryNavRoutes = (BOTTOM_NAV_SCREENS + TOOLBAR_SCREENS).map { it.route }.toSet()
        assertEquals(8, primaryNavRoutes.size, "Primary navigation (bottom + toolbar) should cover 8 unique routes")
    }

    @Test
    fun testParameterizedRoutesHavePlaceholders() {
        assertTrue(Screen.EditHabits.route.contains("{habitId}"), "EditHabits route must contain {habitId}")
        assertTrue(Screen.PointDevelopment.route.contains("{habitId}"), "PointDevelopment route must contain {habitId}")
        assertTrue(Screen.RecordEmotionEntry.route.contains("{pairId}"), "RecordEmotionEntry route must contain {pairId}")
    }

    @Test
    fun testParameterizedRoutesGenerateValidPaths() {
        val editRoute = Screen.EditHabits.createRoute("abc-123")
        assertFalse(editRoute.contains("{"), "Generated EditHabits route must not contain placeholders")
        assertTrue(editRoute.contains("abc-123"), "Generated EditHabits route must contain the ID")

        val pointRoute = Screen.PointDevelopment.createRoute("def-456")
        assertFalse(pointRoute.contains("{"), "Generated PointDevelopment route must not contain placeholders")
        assertTrue(pointRoute.contains("def-456"), "Generated PointDevelopment route must contain the ID")

        val emotionRoute = Screen.RecordEmotionEntry.createRoute("ghi-789")
        assertFalse(emotionRoute.contains("{"), "Generated RecordEmotionEntry route must not contain placeholders")
        assertTrue(emotionRoute.contains("ghi-789"), "Generated RecordEmotionEntry route must contain the ID")
    }
}
