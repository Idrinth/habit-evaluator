package de.idrinth.habitevaluator.android.persistence

import kotlinx.coroutines.flow.Flow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Reflection-based tests for all Room DAO interfaces in Daos.kt.
 * Verifies interface declarations, method signatures, parameter counts, and return types.
 * Note: Room annotations (@Dao, @Query, @Transaction, @Insert) have CLASS retention
 * and are not available at runtime, so annotation-level tests are not possible.
 * Actual query correctness is tested indirectly via Room*Repository tests.
 */
class DaosTest {

    // --- Helper functions ---

    private fun assertIsInterface(daoClass: Class<*>) {
        assertTrue(daoClass.isInterface, "${daoClass.simpleName} must be an interface")
    }

    private fun assertMethodExists(daoClass: Class<*>, methodName: String) {
        val methods = daoClass.declaredMethods.map { it.name }
        assertTrue(
            methods.contains(methodName),
            "${daoClass.simpleName} should declare method '$methodName', found: $methods"
        )
    }

    private fun getMethod(daoClass: Class<*>, methodName: String) =
        daoClass.declaredMethods.first { it.name == methodName }

    private fun assertMethodParameterCount(daoClass: Class<*>, methodName: String, expectedCount: Int) {
        val method = getMethod(daoClass, methodName)
        assertEquals(
            expectedCount,
            method.parameterCount,
            "${daoClass.simpleName}.$methodName should have $expectedCount parameter(s)"
        )
    }

    @Nested
    inner class HabitDaoTest {
        private val dao = HabitDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertMethodExists() = assertMethodExists(dao, "insert")

        @Test
        fun testInsertEntryMethodExists() = assertMethodExists(dao, "insertEntry")

        @Test
        fun testInsertNameTranslationMethodExists() = assertMethodExists(dao, "insertNameTranslation")

        @Test
        fun testInsertDescTranslationMethodExists() = assertMethodExists(dao, "insertDescTranslation")

        @Test
        fun testFindByIdMethodExists() = assertMethodExists(dao, "findById")

        @Test
        fun testFindAllMethodExists() = assertMethodExists(dao, "findAll")

        @Test
        fun testFindByUserIdMethodExists() = assertMethodExists(dao, "findByUserId")

        @Test
        fun testObserveByUserIdMethodExists() = assertMethodExists(dao, "observeByUserId")

        @Test
        fun testObserveByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testFindEntriesByHabitIdMethodExists() = assertMethodExists(dao, "findEntriesByHabitId")

        @Test
        fun testFindNameTranslationsMethodExists() = assertMethodExists(dao, "findNameTranslations")

        @Test
        fun testFindDescTranslationsMethodExists() = assertMethodExists(dao, "findDescTranslations")

        @Test
        fun testDeleteByIdMethodExists() = assertMethodExists(dao, "deleteById")

        @Test
        fun testDeleteEntriesByHabitIdMethodExists() = assertMethodExists(dao, "deleteEntriesByHabitId")

        @Test
        fun testDeleteNameTranslationsMethodExists() = assertMethodExists(dao, "deleteNameTranslations")

        @Test
        fun testDeleteDescTranslationsMethodExists() = assertMethodExists(dao, "deleteDescTranslations")

        @Test
        fun testExistsByIdMethodExists() = assertMethodExists(dao, "existsById")

        @Test
        fun testSaveWithDetailsMethodExists() = assertMethodExists(dao, "saveWithDetails")

        @Test
        fun testDeleteWithDetailsMethodExists() = assertMethodExists(dao, "deleteWithDetails")

        @Test
        fun testInsertTakesOneParameter() = assertMethodParameterCount(dao, "insert", 2) // entity + continuation

        @Test
        fun testFindByIdTakesIdParameter() = assertMethodParameterCount(dao, "findById", 2) // id + continuation

        @Test
        fun testDeleteByIdTakesIdParameter() = assertMethodParameterCount(dao, "deleteById", 2)

        @Test
        fun testFindAllTakesNoBusinessParameters() = assertMethodParameterCount(dao, "findAll", 1) // only continuation

        @Test
        fun testObserveByUserIdTakesUserIdParameter() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(1, method.parameterCount, "Non-suspend Flow method takes one parameter")
        }

        @Test
        fun testSaveWithDetailsTakesFourBusinessParameters() {
            // habit, entries, nameTranslations, descTranslations + continuation
            assertMethodParameterCount(dao, "saveWithDetails", 5)
        }

        @Test
        fun testDeleteWithDetailsTakesIdParameter() = assertMethodParameterCount(dao, "deleteWithDetails", 2)
    }

    @Nested
    inner class HabitCategoryDaoTest {
        private val dao = HabitCategoryDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertMethodExists() = assertMethodExists(dao, "insert")

        @Test
        fun testInsertNameTranslationMethodExists() = assertMethodExists(dao, "insertNameTranslation")

        @Test
        fun testInsertDescTranslationMethodExists() = assertMethodExists(dao, "insertDescTranslation")

        @Test
        fun testFindByIdMethodExists() = assertMethodExists(dao, "findById")

        @Test
        fun testFindAllMethodExists() = assertMethodExists(dao, "findAll")

        @Test
        fun testFindByUserIdMethodExists() = assertMethodExists(dao, "findByUserId")

        @Test
        fun testObserveByUserIdMethodExists() = assertMethodExists(dao, "observeByUserId")

        @Test
        fun testObserveByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testFindNameTranslationsMethodExists() = assertMethodExists(dao, "findNameTranslations")

        @Test
        fun testFindDescTranslationsMethodExists() = assertMethodExists(dao, "findDescTranslations")

        @Test
        fun testDeleteByIdMethodExists() = assertMethodExists(dao, "deleteById")

        @Test
        fun testDeleteNameTranslationsMethodExists() = assertMethodExists(dao, "deleteNameTranslations")

        @Test
        fun testDeleteDescTranslationsMethodExists() = assertMethodExists(dao, "deleteDescTranslations")

        @Test
        fun testExistsByIdMethodExists() = assertMethodExists(dao, "existsById")

        @Test
        fun testSaveWithDetailsMethodExists() = assertMethodExists(dao, "saveWithDetails")

        @Test
        fun testDeleteWithDetailsMethodExists() = assertMethodExists(dao, "deleteWithDetails")

        @Test
        fun testSaveWithDetailsTakesThreeBusinessParameters() {
            // category, nameTranslations, descTranslations + continuation
            assertMethodParameterCount(dao, "saveWithDetails", 4)
        }
    }

    @Nested
    inner class DiaryDaoTest {
        private val dao = DiaryDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertReferenceMethodExists() = assertMethodExists(dao, "insertReference")

        @Test
        fun testFindReferenceByIdMethodExists() = assertMethodExists(dao, "findReferenceById")

        @Test
        fun testFindReferencesByUserIdMethodExists() = assertMethodExists(dao, "findReferencesByUserId")

        @Test
        fun testFindReferenceByUserIdAndDescLowerMethodExists() =
            assertMethodExists(dao, "findReferenceByUserIdAndDescLower")

        @Test
        fun testFindReferenceByUserIdAndDescLowerTakesTwoBusinessParameters() {
            // userId, descLower + continuation
            assertMethodParameterCount(dao, "findReferenceByUserIdAndDescLower", 3)
        }

        @Test
        fun testDeleteReferenceByIdMethodExists() = assertMethodExists(dao, "deleteReferenceById")

        @Test
        fun testFindDistinctDescriptionsMethodExists() = assertMethodExists(dao, "findDistinctDescriptions")

        @Test
        fun testInsertEntryMethodExists() = assertMethodExists(dao, "insertEntry")

        @Test
        fun testFindEntryByIdMethodExists() = assertMethodExists(dao, "findEntryById")

        @Test
        fun testFindAllEntriesMethodExists() = assertMethodExists(dao, "findAllEntries")

        @Test
        fun testFindEntriesByUserIdMethodExists() = assertMethodExists(dao, "findEntriesByUserId")

        @Test
        fun testObserveEntriesByUserIdMethodExists() = assertMethodExists(dao, "observeEntriesByUserId")

        @Test
        fun testObserveEntriesByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeEntriesByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteEntryByIdMethodExists() = assertMethodExists(dao, "deleteEntryById")

        @Test
        fun testFindEntriesNeedingMigrationMethodExists() =
            assertMethodExists(dao, "findEntriesNeedingMigration")
    }

    @Nested
    inner class SleepEntryDaoTest {
        private val dao = SleepEntryDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertMethodExists() = assertMethodExists(dao, "insert")

        @Test
        fun testFindByIdMethodExists() = assertMethodExists(dao, "findById")

        @Test
        fun testFindAllMethodExists() = assertMethodExists(dao, "findAll")

        @Test
        fun testFindByUserIdMethodExists() = assertMethodExists(dao, "findByUserId")

        @Test
        fun testObserveByUserIdMethodExists() = assertMethodExists(dao, "observeByUserId")

        @Test
        fun testObserveByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteByIdMethodExists() = assertMethodExists(dao, "deleteById")

        @Test
        fun testExistsByIdMethodExists() = assertMethodExists(dao, "existsById")
    }

    @Nested
    inner class EmotionDaoTest {
        private val dao = EmotionDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertPairMethodExists() = assertMethodExists(dao, "insertPair")

        @Test
        fun testFindPairByIdMethodExists() = assertMethodExists(dao, "findPairById")

        @Test
        fun testFindAllPairsMethodExists() = assertMethodExists(dao, "findAllPairs")

        @Test
        fun testFindPairsByUserIdMethodExists() = assertMethodExists(dao, "findPairsByUserId")

        @Test
        fun testObservePairsByUserIdMethodExists() = assertMethodExists(dao, "observePairsByUserId")

        @Test
        fun testObservePairsByUserIdReturnsFlow() {
            val method = getMethod(dao, "observePairsByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeletePairByIdMethodExists() = assertMethodExists(dao, "deletePairById")

        @Test
        fun testInsertEntryMethodExists() = assertMethodExists(dao, "insertEntry")

        @Test
        fun testFindEntryByIdMethodExists() = assertMethodExists(dao, "findEntryById")

        @Test
        fun testFindAllEntriesMethodExists() = assertMethodExists(dao, "findAllEntries")

        @Test
        fun testFindEntriesByUserIdMethodExists() = assertMethodExists(dao, "findEntriesByUserId")

        @Test
        fun testObserveEntriesByUserIdMethodExists() = assertMethodExists(dao, "observeEntriesByUserId")

        @Test
        fun testObserveEntriesByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeEntriesByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteEntryByIdMethodExists() = assertMethodExists(dao, "deleteEntryById")
    }

    @Nested
    inner class FoodLogDaoTest {
        private val dao = FoodLogDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertMethodExists() = assertMethodExists(dao, "insert")

        @Test
        fun testFindByIdMethodExists() = assertMethodExists(dao, "findById")

        @Test
        fun testFindAllMethodExists() = assertMethodExists(dao, "findAll")

        @Test
        fun testFindByUserIdMethodExists() = assertMethodExists(dao, "findByUserId")

        @Test
        fun testObserveByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteByIdMethodExists() = assertMethodExists(dao, "deleteById")

        @Test
        fun testExistsByIdMethodExists() = assertMethodExists(dao, "existsById")

        @Test
        fun testInsertTagMethodExists() = assertMethodExists(dao, "insertTag")

        @Test
        fun testFindTagByIdMethodExists() = assertMethodExists(dao, "findTagById")

        @Test
        fun testFindTagsByUserIdMethodExists() = assertMethodExists(dao, "findTagsByUserId")

        @Test
        fun testFindTagByNameLowerAndUserIdMethodExists() =
            assertMethodExists(dao, "findTagByNameLowerAndUserId")

        @Test
        fun testFindTagByNameLowerAndUserIdTakesTwoBusinessParameters() {
            // nameLower, userId + continuation
            assertMethodParameterCount(dao, "findTagByNameLowerAndUserId", 3)
        }

        @Test
        fun testDeleteTagByIdMethodExists() = assertMethodExists(dao, "deleteTagById")

        @Test
        fun testDeleteEmptyTagsMethodExists() = assertMethodExists(dao, "deleteEmptyTags")

        @Test
        fun testLinkTagToFoodLogMethodExists() = assertMethodExists(dao, "linkTagToFoodLog")

        @Test
        fun testUnlinkAllTagsFromFoodLogMethodExists() = assertMethodExists(dao, "unlinkAllTagsFromFoodLog")

        @Test
        fun testFindTagsByFoodLogIdMethodExists() = assertMethodExists(dao, "findTagsByFoodLogId")
    }

    @Nested
    inner class SportLogDaoTest {
        private val dao = SportLogDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertMethodExists() = assertMethodExists(dao, "insert")

        @Test
        fun testFindByIdMethodExists() = assertMethodExists(dao, "findById")

        @Test
        fun testFindAllMethodExists() = assertMethodExists(dao, "findAll")

        @Test
        fun testFindByUserIdMethodExists() = assertMethodExists(dao, "findByUserId")

        @Test
        fun testObserveByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteByIdMethodExists() = assertMethodExists(dao, "deleteById")

        @Test
        fun testExistsByIdMethodExists() = assertMethodExists(dao, "existsById")

        @Test
        fun testFindDistinctNamesMethodExists() = assertMethodExists(dao, "findDistinctNames")

        @Test
        fun testFindDistinctMeasurementUnitsMethodExists() =
            assertMethodExists(dao, "findDistinctMeasurementUnits")
    }

    @Nested
    inner class MedicationDaoTest {
        private val dao = MedicationDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertMethodExists() = assertMethodExists(dao, "insert")

        @Test
        fun testFindByIdMethodExists() = assertMethodExists(dao, "findById")

        @Test
        fun testFindAllMethodExists() = assertMethodExists(dao, "findAll")

        @Test
        fun testFindByUserIdMethodExists() = assertMethodExists(dao, "findByUserId")

        @Test
        fun testObserveByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteByIdMethodExists() = assertMethodExists(dao, "deleteById")

        @Test
        fun testInsertLogMethodExists() = assertMethodExists(dao, "insertLog")

        @Test
        fun testFindLogByIdMethodExists() = assertMethodExists(dao, "findLogById")

        @Test
        fun testFindAllLogsMethodExists() = assertMethodExists(dao, "findAllLogs")

        @Test
        fun testFindLogsByUserIdMethodExists() = assertMethodExists(dao, "findLogsByUserId")

        @Test
        fun testObserveLogsByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeLogsByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteLogByIdMethodExists() = assertMethodExists(dao, "deleteLogById")

        @Test
        fun testFindLogsByUserIdPagedMethodExists() = assertMethodExists(dao, "findLogsByUserIdPaged")

        @Test
        fun testCountLogsByUserIdMethodExists() = assertMethodExists(dao, "countLogsByUserId")

        @Test
        fun testFindLogsByUserIdPagedTakesThreeBusinessParameters() {
            // userId, limit, offset + continuation
            assertMethodParameterCount(dao, "findLogsByUserIdPaged", 4)
        }
    }

    @Nested
    inner class EmergencyPlanDaoTest {
        private val dao = EmergencyPlanDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertStepMethodExists() = assertMethodExists(dao, "insertStep")

        @Test
        fun testFindStepByIdMethodExists() = assertMethodExists(dao, "findStepById")

        @Test
        fun testFindAllStepsMethodExists() = assertMethodExists(dao, "findAllSteps")

        @Test
        fun testFindStepsByUserIdMethodExists() = assertMethodExists(dao, "findStepsByUserId")

        @Test
        fun testObserveStepsByUserIdMethodExists() = assertMethodExists(dao, "observeStepsByUserId")

        @Test
        fun testObserveStepsByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeStepsByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteStepByIdMethodExists() = assertMethodExists(dao, "deleteStepById")

        @Test
        fun testInsertActionMethodExists() = assertMethodExists(dao, "insertAction")

        @Test
        fun testFindActionByIdMethodExists() = assertMethodExists(dao, "findActionById")

        @Test
        fun testFindActionsByStepIdMethodExists() = assertMethodExists(dao, "findActionsByStepId")

        @Test
        fun testDeleteActionByIdMethodExists() = assertMethodExists(dao, "deleteActionById")

        @Test
        fun testDeleteActionsByStepIdMethodExists() = assertMethodExists(dao, "deleteActionsByStepId")

        @Test
        fun testSaveStepWithActionsMethodExists() = assertMethodExists(dao, "saveStepWithActions")

        @Test
        fun testDeleteStepWithActionsMethodExists() = assertMethodExists(dao, "deleteStepWithActions")

        @Test
        fun testSaveStepWithActionsTakesTwoBusinessParameters() {
            // step, actions + continuation
            assertMethodParameterCount(dao, "saveStepWithActions", 3)
        }

        @Test
        fun testDeleteStepWithActionsTakesIdParameter() {
            assertMethodParameterCount(dao, "deleteStepWithActions", 2) // id + continuation
        }
    }

    @Nested
    inner class ActivityLogDaoTest {
        private val dao = ActivityLogDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertMethodExists() = assertMethodExists(dao, "insert")

        @Test
        fun testFindByIdMethodExists() = assertMethodExists(dao, "findById")

        @Test
        fun testFindAllMethodExists() = assertMethodExists(dao, "findAll")

        @Test
        fun testFindByUserIdMethodExists() = assertMethodExists(dao, "findByUserId")

        @Test
        fun testObserveByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteByIdMethodExists() = assertMethodExists(dao, "deleteById")

        @Test
        fun testExistsByIdMethodExists() = assertMethodExists(dao, "existsById")

        @Test
        fun testFindDistinctLocationsMethodExists() = assertMethodExists(dao, "findDistinctLocations")

        @Test
        fun testFindDistinctActivitiesMethodExists() = assertMethodExists(dao, "findDistinctActivities")

        @Test
        fun testInsertGroupMethodExists() = assertMethodExists(dao, "insertGroup")

        @Test
        fun testFindGroupByIdMethodExists() = assertMethodExists(dao, "findGroupById")

        @Test
        fun testFindAllGroupsMethodExists() = assertMethodExists(dao, "findAllGroups")

        @Test
        fun testFindGroupsByUserIdMethodExists() = assertMethodExists(dao, "findGroupsByUserId")

        @Test
        fun testObserveGroupsByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeGroupsByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteGroupByIdMethodExists() = assertMethodExists(dao, "deleteGroupById")

        @Test
        fun testGroupExistsByIdMethodExists() = assertMethodExists(dao, "groupExistsById")

        @Test
        fun testInsertLogGroupLinkMethodExists() = assertMethodExists(dao, "insertLogGroupLink")

        @Test
        fun testDeleteLinksForActivityLogMethodExists() = assertMethodExists(dao, "deleteLinksForActivityLog")

        @Test
        fun testDeleteLinksForGroupMethodExists() = assertMethodExists(dao, "deleteLinksForGroup")

        @Test
        fun testFindGroupIdsForActivityLogMethodExists() = assertMethodExists(dao, "findGroupIdsForActivityLog")

        @Test
        fun testFindGroupsByActivityLogIdMethodExists() = assertMethodExists(dao, "findGroupsByActivityLogId")

        @Test
        fun testSaveActivityLogWithLinksMethodExists() = assertMethodExists(dao, "saveActivityLogWithLinks")

        @Test
        fun testDeleteActivityLogWithLinksMethodExists() = assertMethodExists(dao, "deleteActivityLogWithLinks")

        @Test
        fun testDeleteGroupWithLinksMethodExists() = assertMethodExists(dao, "deleteGroupWithLinks")

        @Test
        fun testSaveActivityLogWithLinksTakesTwoBusinessParameters() {
            // entry, groupIds + continuation
            assertMethodParameterCount(dao, "saveActivityLogWithLinks", 3)
        }
    }

    @Nested
    inner class DayPlannerDaoTest {
        private val dao = DayPlannerDao::class.java

        @Test
        fun testIsInterface() = assertIsInterface(dao)

        @Test
        fun testInsertActivityMethodExists() = assertMethodExists(dao, "insertActivity")

        @Test
        fun testFindActivityByIdMethodExists() = assertMethodExists(dao, "findActivityById")

        @Test
        fun testFindAllActivitiesMethodExists() = assertMethodExists(dao, "findAllActivities")

        @Test
        fun testFindActivitiesByUserIdMethodExists() = assertMethodExists(dao, "findActivitiesByUserId")

        @Test
        fun testObserveActivitiesByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeActivitiesByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteActivityByIdMethodExists() = assertMethodExists(dao, "deleteActivityById")

        @Test
        fun testActivityExistsByIdMethodExists() = assertMethodExists(dao, "activityExistsById")

        @Test
        fun testInsertActivityGroupLinkMethodExists() = assertMethodExists(dao, "insertActivityGroupLink")

        @Test
        fun testDeleteLinksForActivityMethodExists() = assertMethodExists(dao, "deleteLinksForActivity")

        @Test
        fun testFindGroupIdsForActivityMethodExists() = assertMethodExists(dao, "findGroupIdsForActivity")

        @Test
        fun testFindActivitiesByGroupIdMethodExists() = assertMethodExists(dao, "findActivitiesByGroupId")

        @Test
        fun testSaveActivityWithLinksMethodExists() = assertMethodExists(dao, "saveActivityWithLinks")

        @Test
        fun testDeleteActivityWithLinksMethodExists() = assertMethodExists(dao, "deleteActivityWithLinks")

        @Test
        fun testSaveActivityWithLinksTakesTwoBusinessParameters() {
            // activity, groupIds + continuation
            assertMethodParameterCount(dao, "saveActivityWithLinks", 3)
        }

        @Test
        fun testInsertGroupMethodExists() = assertMethodExists(dao, "insertGroup")

        @Test
        fun testFindGroupByIdMethodExists() = assertMethodExists(dao, "findGroupById")

        @Test
        fun testFindAllGroupsMethodExists() = assertMethodExists(dao, "findAllGroups")

        @Test
        fun testFindGroupsByUserIdMethodExists() = assertMethodExists(dao, "findGroupsByUserId")

        @Test
        fun testObserveGroupsByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeGroupsByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testDeleteGroupByIdMethodExists() = assertMethodExists(dao, "deleteGroupById")

        @Test
        fun testGroupExistsByIdMethodExists() = assertMethodExists(dao, "groupExistsById")

        @Test
        fun testDeleteGroupWithLinksMethodExists() = assertMethodExists(dao, "deleteGroupWithLinks")

        @Test
        fun testInsertSlotMethodExists() = assertMethodExists(dao, "insertSlot")

        @Test
        fun testFindSlotByIdMethodExists() = assertMethodExists(dao, "findSlotById")

        @Test
        fun testFindAllSlotsMethodExists() = assertMethodExists(dao, "findAllSlots")

        @Test
        fun testFindSlotsByUserIdMethodExists() = assertMethodExists(dao, "findSlotsByUserId")

        @Test
        fun testObserveSlotsByUserIdReturnsFlow() {
            val method = getMethod(dao, "observeSlotsByUserId")
            assertEquals(Flow::class.java, method.returnType)
        }

        @Test
        fun testFindSlotsByUserIdAndDayOfWeekMethodExists() =
            assertMethodExists(dao, "findSlotsByUserIdAndDayOfWeek")

        @Test
        fun testFindSlotsByUserIdAndDayOfWeekTakesTwoBusinessParameters() {
            // userId, dayOfWeek + continuation
            assertMethodParameterCount(dao, "findSlotsByUserIdAndDayOfWeek", 3)
        }

        @Test
        fun testDeleteSlotByIdMethodExists() = assertMethodExists(dao, "deleteSlotById")

        @Test
        fun testSlotExistsByIdMethodExists() = assertMethodExists(dao, "slotExistsById")

        @Test
        fun testInsertSlotGroupLinkMethodExists() = assertMethodExists(dao, "insertSlotGroupLink")

        @Test
        fun testDeleteSlotGroupLinksForSlotMethodExists() = assertMethodExists(dao, "deleteSlotGroupLinksForSlot")

        @Test
        fun testDeleteSlotGroupLinksForGroupMethodExists() = assertMethodExists(dao, "deleteSlotGroupLinksForGroup")

        @Test
        fun testFindGroupIdsForSlotMethodExists() = assertMethodExists(dao, "findGroupIdsForSlot")

        @Test
        fun testSaveSlotWithGroupsMethodExists() = assertMethodExists(dao, "saveSlotWithGroups")

        @Test
        fun testSaveSlotWithGroupsTakesTwoBusinessParameters() {
            // slot, groupIds + continuation
            assertMethodParameterCount(dao, "saveSlotWithGroups", 3)
        }

        @Test
        fun testDeleteSlotWithLinksMethodExists() = assertMethodExists(dao, "deleteSlotWithLinks")

        @Test
        fun testInsertConfirmationMethodExists() = assertMethodExists(dao, "insertConfirmation")

        @Test
        fun testFindConfirmationByIdMethodExists() = assertMethodExists(dao, "findConfirmationById")

        @Test
        fun testFindAllConfirmationsMethodExists() = assertMethodExists(dao, "findAllConfirmations")

        @Test
        fun testFindConfirmationsByUserIdMethodExists() = assertMethodExists(dao, "findConfirmationsByUserId")

        @Test
        fun testDeleteConfirmationByIdMethodExists() = assertMethodExists(dao, "deleteConfirmationById")

        @Test
        fun testConfirmationExistsByIdMethodExists() = assertMethodExists(dao, "confirmationExistsById")

        @Test
        fun testDeleteLinksForGroupMethodExists() = assertMethodExists(dao, "deleteLinksForGroup")
    }

    // --- Cross-cutting DAO interface tests ---

    @Test
    fun testAllElevenDaoInterfacesExist() {
        val daoClasses = listOf(
            HabitDao::class.java,
            HabitCategoryDao::class.java,
            DiaryDao::class.java,
            SleepEntryDao::class.java,
            EmotionDao::class.java,
            FoodLogDao::class.java,
            SportLogDao::class.java,
            MedicationDao::class.java,
            EmergencyPlanDao::class.java,
            ActivityLogDao::class.java,
            DayPlannerDao::class.java
        )
        assertEquals(11, daoClasses.size)
        daoClasses.forEach { assertTrue(it.isInterface, "${it.simpleName} should be an interface") }
    }

    @Test
    fun testAllDaosAreInPersistencePackage() {
        val daoClasses = listOf(
            HabitDao::class.java, HabitCategoryDao::class.java, DiaryDao::class.java,
            SleepEntryDao::class.java, EmotionDao::class.java, FoodLogDao::class.java,
            SportLogDao::class.java, MedicationDao::class.java, EmergencyPlanDao::class.java,
            ActivityLogDao::class.java, DayPlannerDao::class.java
        )
        daoClasses.forEach {
            assertEquals(
                "de.idrinth.habitevaluator.android.persistence",
                it.packageName,
                "${it.simpleName} should be in the persistence package"
            )
        }
    }

    @Test
    fun testDaosWithFlowObserveMethods() {
        val daosWithFlows = listOf(
            HabitDao::class.java to "observeByUserId",
            HabitCategoryDao::class.java to "observeByUserId",
            SleepEntryDao::class.java to "observeByUserId",
            FoodLogDao::class.java to "observeByUserId",
            SportLogDao::class.java to "observeByUserId",
            MedicationDao::class.java to "observeByUserId",
            ActivityLogDao::class.java to "observeByUserId",
        )
        daosWithFlows.forEach { (daoClass, methodName) ->
            val method = daoClass.declaredMethods.first { it.name == methodName }
            assertEquals(
                Flow::class.java,
                method.returnType,
                "${daoClass.simpleName}.$methodName should return Flow"
            )
        }
    }

    @Test
    fun testTransactionMethodsExistInExpectedDaos() {
        val daosWithTransactions = mapOf(
            HabitDao::class.java to listOf("saveWithDetails", "deleteWithDetails"),
            HabitCategoryDao::class.java to listOf("saveWithDetails", "deleteWithDetails"),
            EmergencyPlanDao::class.java to listOf("saveStepWithActions", "deleteStepWithActions"),
            ActivityLogDao::class.java to listOf("saveActivityLogWithLinks", "deleteActivityLogWithLinks", "deleteGroupWithLinks"),
            DayPlannerDao::class.java to listOf("saveActivityWithLinks", "deleteActivityWithLinks", "deleteGroupWithLinks", "saveSlotWithGroups", "deleteSlotWithLinks")
        )
        daosWithTransactions.forEach { (daoClass, methods) ->
            methods.forEach { methodName ->
                assertMethodExists(daoClass, methodName)
            }
        }
    }
}
