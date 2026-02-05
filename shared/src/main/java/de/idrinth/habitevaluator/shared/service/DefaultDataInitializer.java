package de.idrinth.habitevaluator.shared.service;

import de.idrinth.habitevaluator.shared.localization.Localizer;
import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Initializes default categories and habits when a user's database is first set up.
 * Categories are global; habits are created per user.
 * Supports localization via the Localizer service.
 */
public class DefaultDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDataInitializer.class);
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String MODULE = "default_data";

    private final HabitCategoryRepository categoryRepository;
    private final HabitRepository habitRepository;
    private final Localizer localizer;

    public DefaultDataInitializer(HabitCategoryRepository categoryRepository, HabitRepository habitRepository) {
        this(categoryRepository, habitRepository, new Localizer());
    }

    public DefaultDataInitializer(HabitCategoryRepository categoryRepository, HabitRepository habitRepository, Localizer localizer) {
        this.categoryRepository = categoryRepository;
        this.habitRepository = habitRepository;
        this.localizer = localizer;
    }

    /**
     * Builds the default category-to-habits mapping based on early warning symptom tracking.
     * Uses the localizer to translate category and habit names/descriptions.
     *
     * @param language the language code to use for translations (e.g., "en", "de", "es", "fr")
     */
    private Map<HabitCategory, List<HabitDefinition>> buildDefaults(String language) {
        Map<HabitCategory, List<HabitDefinition>> defaults = new LinkedHashMap<>();

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_hobbies_name", language),
                localizer.translate(MODULE, "category_hobbies_description", language),
                "#4CAF50"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_computer_games_name", language),
                    localizer.translate(MODULE, "habit_computer_games_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_watching_movies_name", language),
                    localizer.translate(MODULE, "habit_watching_movies_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_reading_name", language),
                    localizer.translate(MODULE, "habit_reading_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_modding_name", language),
                    localizer.translate(MODULE, "habit_modding_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_soapstone_name", language),
                    localizer.translate(MODULE, "habit_soapstone_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_work_name", language),
                localizer.translate(MODULE, "category_work_description", language),
                "#2196F3"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_opensource_name", language),
                    localizer.translate(MODULE, "habit_opensource_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_punctual_name", language),
                    localizer.translate(MODULE, "habit_punctual_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_work_hours_name", language),
                    localizer.translate(MODULE, "habit_work_hours_description", language),
                    FrequencyType.WEEKLY, 40, false, new int[]{9, 10, 11, 12}
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_emails_name", language),
                    localizer.translate(MODULE, "habit_emails_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_emotions_name", language),
                localizer.translate(MODULE, "category_emotions_description", language),
                "#F44336"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_irritability_name", language),
                    localizer.translate(MODULE, "habit_irritability_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_listlessness_name", language),
                    localizer.translate(MODULE, "habit_listlessness_description", language),
                    FrequencyType.WEEKLY, 7, false, null
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_tiredness_of_life_name", language),
                    localizer.translate(MODULE, "habit_tiredness_of_life_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_sleep_name", language),
                localizer.translate(MODULE, "category_sleep_description", language),
                "#9C27B0"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_late_sleep_name", language),
                    localizer.translate(MODULE, "habit_late_sleep_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_early_wake_name", language),
                    localizer.translate(MODULE, "habit_early_wake_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_caffeine_name", language),
                    localizer.translate(MODULE, "habit_caffeine_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_awake_on_time_name", language),
                    localizer.translate(MODULE, "habit_awake_on_time_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_napped_name", language),
                    localizer.translate(MODULE, "habit_napped_description", language),
                    FrequencyType.WEEKLY, 7, false, new int[]{1, 2, 3, 4}
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_sport_name", language),
                localizer.translate(MODULE, "category_sport_description", language),
                "#FF9800"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_walking_name", language),
                    localizer.translate(MODULE, "habit_walking_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_fencing_name", language),
                    localizer.translate(MODULE, "habit_fencing_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_muscle_relaxation_name", language),
                    localizer.translate(MODULE, "habit_muscle_relaxation_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_food_name", language),
                localizer.translate(MODULE, "category_food_description", language),
                "#795548"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_meals_cooked_name", language),
                    localizer.translate(MODULE, "habit_meals_cooked_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_meals_ordered_name", language),
                    localizer.translate(MODULE, "habit_meals_ordered_description", language),
                    FrequencyType.WEEKLY, 7, false, null
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_breakfasts_name", language),
                    localizer.translate(MODULE, "habit_breakfasts_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_motivation_name", language),
                localizer.translate(MODULE, "category_motivation_description", language),
                "#607D8B"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_left_house_name", language),
                    localizer.translate(MODULE, "habit_left_house_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_got_mail_name", language),
                    localizer.translate(MODULE, "habit_got_mail_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_morning_shower_name", language),
                    localizer.translate(MODULE, "habit_morning_shower_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_sexuality_name", language),
                    localizer.translate(MODULE, "habit_sexuality_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_hygiene_name", language),
                localizer.translate(MODULE, "category_hygiene_description", language),
                "#00BCD4"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_shaved_name", language),
                    localizer.translate(MODULE, "habit_shaved_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_showered_name", language),
                    localizer.translate(MODULE, "habit_showered_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_brushed_teeth_name", language),
                    localizer.translate(MODULE, "habit_brushed_teeth_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_relaxation_baths_name", language),
                    localizer.translate(MODULE, "habit_relaxation_baths_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_pet_care_name", language),
                localizer.translate(MODULE, "category_pet_care_description", language),
                "#8BC34A"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_cage_cleaning_name", language),
                    localizer.translate(MODULE, "habit_cage_cleaning_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_fed_pets_name", language),
                    localizer.translate(MODULE, "habit_fed_pets_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_food_stocked_name", language),
                    localizer.translate(MODULE, "habit_food_stocked_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        defaults.put(
            new HabitCategory(
                localizer.translate(MODULE, "category_social_name", language),
                localizer.translate(MODULE, "category_social_description", language),
                "#E91E63"
            ),
            List.of(
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_met_someone_name", language),
                    localizer.translate(MODULE, "habit_met_someone_description", language),
                    FrequencyType.WEEKLY, 7
                ),
                new HabitDefinition(
                    localizer.translate(MODULE, "habit_discord_calls_name", language),
                    localizer.translate(MODULE, "habit_discord_calls_description", language),
                    FrequencyType.WEEKLY, 7
                )
            )
        );

        return defaults;
    }

    /**
     * Initializes default categories and habits for a user using the default language (English).
     * Idempotent: reuses existing categories by name and skips habits
     * that already exist for the user with the same name and category.
     *
     * @param user the user to create default habits for
     */
    public void initializeDefaults(User user) {
        initializeDefaults(user, DEFAULT_LANGUAGE);
    }

    /**
     * Initializes default categories and habits for a user using the specified language.
     * Idempotent: reuses existing categories by name and skips habits
     * that already exist for the user with the same name and category.
     *
     * @param user the user to create default habits for
     * @param language the language code for translations (e.g., "en", "de", "es", "fr")
     */
    public void initializeDefaults(User user, String language) {
        logger.info("Initializing default categories and habits for user: {} in language: {}", user.getUsername(), language);
        Map<HabitCategory, List<HabitDefinition>> defaults = buildDefaults(language);

        // Build lookup of existing categories by name for this user
        Map<String, HabitCategory> existingCategoriesByName = new HashMap<>();
        for (HabitCategory cat : categoryRepository.findByUserId(user.getId())) {
            existingCategoriesByName.put(cat.getName(), cat);
        }

        // Build lookup of existing habits for this user by (name, categoryId)
        Set<String> existingHabitKeys = habitRepository.findByUserId(user.getId()).stream()
                .map(h -> h.getName() + "\0" + h.getCategoryId())
                .collect(Collectors.toSet());

        for (Map.Entry<HabitCategory, List<HabitDefinition>> entry : defaults.entrySet()) {
            HabitCategory defaultCategory = entry.getKey();
            HabitCategory category = existingCategoriesByName.get(defaultCategory.getName());
            if (category == null) {
                try {
                    defaultCategory.setUser(user);
                    category = categoryRepository.save(defaultCategory);
                    logger.info("Created default category: {}", category.getName());
                } catch (RuntimeException e) {
                    logger.info("Category already exists (concurrent insert), reloading: {}", defaultCategory.getName());
                    // Reload categories for this user after concurrent insert
                    for (HabitCategory cat : categoryRepository.findByUserId(user.getId())) {
                        existingCategoriesByName.put(cat.getName(), cat);
                    }
                    category = existingCategoriesByName.get(defaultCategory.getName());
                    if (category == null) {
                        logger.warn("Failed to create or find category: {}", defaultCategory.getName());
                        continue;
                    }
                }
            } else {
                logger.info("Reusing existing category: {}", category.getName());
            }

            for (HabitDefinition def : entry.getValue()) {
                String habitKey = def.name + "\0" + category.getId();
                if (existingHabitKeys.contains(habitKey)) {
                    logger.info("Habit already exists, skipping: {} in category: {}", def.name, category.getName());
                    continue;
                }
                try {
                    Habit habit = new Habit(def.name, def.description);
                    habit.setCategoryId(category.getId());
                    habit.setFrequencyType(def.frequencyType);
                    habit.setTargetFrequency(def.targetFrequency);
                    habit.setPositiveScoring(def.positiveScoring);
                    if (def.scoringThresholds != null) {
                        habit.setScoringRule(new ScoringRule(
                            def.name,
                            def.scoringThresholds[0],
                            def.scoringThresholds[1],
                            def.scoringThresholds[2],
                            def.scoringThresholds[3]
                        ));
                    }
                    habit.setUser(user);
                    habitRepository.save(habit);
                    logger.info("Created default habit: {} in category: {}", def.name, category.getName());
                } catch (RuntimeException e) {
                    logger.info("Habit already exists (concurrent insert), skipping: {} in category: {}",
                            def.name, category.getName());
                }
            }
        }

        logger.info("Default data initialization complete");
    }

    private static class HabitDefinition {
        final String name;
        final String description;
        final FrequencyType frequencyType;
        final int targetFrequency;
        final boolean positiveScoring;
        final int[] scoringThresholds;

        HabitDefinition(String name, String description, FrequencyType frequencyType, int targetFrequency) {
            this(name, description, frequencyType, targetFrequency, true, null);
        }

        HabitDefinition(String name, String description, FrequencyType frequencyType, int targetFrequency,
                         boolean positiveScoring, int[] scoringThresholds) {
            this.name = name;
            this.description = description;
            this.frequencyType = frequencyType;
            this.targetFrequency = targetFrequency;
            this.positiveScoring = positiveScoring;
            this.scoringThresholds = scoringThresholds;
        }
    }
}
