package de.idrinth.habitevaluator.shared.service;

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
 */
public class DefaultDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDataInitializer.class);

    private final HabitCategoryRepository categoryRepository;
    private final HabitRepository habitRepository;

    public DefaultDataInitializer(HabitCategoryRepository categoryRepository, HabitRepository habitRepository) {
        this.categoryRepository = categoryRepository;
        this.habitRepository = habitRepository;
    }

    /**
     * Builds the default category-to-habits mapping based on early warning symptom tracking.
     */
    private static Map<HabitCategory, List<HabitDefinition>> buildDefaults() {
        Map<HabitCategory, List<HabitDefinition>> defaults = new LinkedHashMap<>();

        defaults.put(
            new HabitCategory("Hobbies", "Tracking engagement with personal hobbies and leisure activities", "#4CAF50"),
            List.of(
                new HabitDefinition("Days with computer games", "Days spent playing computer games", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Days watching movies", "Days spent watching movies", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Days reading", "Days spent reading", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Modding days", "Days spent modding", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Soapstone carving", "Days spent on soapstone carving", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Work", "Tracking work habits and productivity", "#2196F3"),
            List.of(
                new HabitDefinition("Open-source days", "Days contributed to open-source projects", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Days started punctually", "Days where work was started on time", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Hours of work per week", "Total hours worked in the week", FrequencyType.WEEKLY, 40, false, new int[]{9, 10, 11, 12}),
                new HabitDefinition("Emails processed", "Number of emails processed", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Emotions", "Monitoring emotional states and warning signs", "#F44336"),
            List.of(
                new HabitDefinition("Irritability", "Days experiencing irritability", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Listlessness", "Days experiencing listlessness or apathy", FrequencyType.WEEKLY, 7, false, null),
                new HabitDefinition("Tiredness of life", "Days experiencing tiredness of life", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Sleep", "Tracking sleep patterns and caffeine consumption", "#9C27B0"),
            List.of(
                new HabitDefinition("Days falling asleep after midnight", "Days where sleep started after 0:00", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Days waking up before 5:00", "Days where waking up occurred before 5:00", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Liters of cola/coffee/tea", "Liters of caffeinated beverages consumed", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Awake at 6 on time", "Days awake punctually at 6:00", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Napped during the day", "Days with daytime naps", FrequencyType.WEEKLY, 7, false, new int[]{1, 2, 3, 4})
            )
        );

        defaults.put(
            new HabitCategory("Sport", "Tracking physical exercise and relaxation activities", "#FF9800"),
            List.of(
                new HabitDefinition("Walking", "Days with a walk", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Fencing practice", "Days with fencing practice", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Muscle relaxation (Jacobsen)", "Days performing progressive muscle relaxation", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Food", "Tracking eating habits and meal preparation", "#795548"),
            List.of(
                new HabitDefinition("Meals cooked", "Number of self-cooked meals", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Meals ordered", "Number of ordered meals", FrequencyType.WEEKLY, 7, false, null),
                new HabitDefinition("Breakfasts eaten", "Number of breakfasts eaten", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Motivation", "Tracking daily drive and initiative", "#607D8B"),
            List.of(
                new HabitDefinition("Left the house", "Days where the house was left", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Got mail", "Days where mail was collected", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Showered in the morning", "Days with a morning shower", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Sexuality", "Days with sexual activity", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Hygiene", "Tracking personal hygiene routines", "#00BCD4"),
            List.of(
                new HabitDefinition("Shaved daily", "Days with daily shaving", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Showered daily", "Days with a daily shower", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Brushed teeth twice daily", "Days with twice-daily tooth brushing", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Relaxation baths", "Number of relaxation baths taken", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Pet Care", "Tracking pet care responsibilities", "#8BC34A"),
            List.of(
                new HabitDefinition("Cage cleaning", "Days where cage was cleaned", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Fed pets", "Days where pets were fed", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Food stocked", "Days where pet food supply was checked/stocked", FrequencyType.WEEKLY, 7)
            )
        );

        defaults.put(
            new HabitCategory("Social", "Tracking social interactions and connections", "#E91E63"),
            List.of(
                new HabitDefinition("Met someone in person", "Days where someone was met in real life", FrequencyType.WEEKLY, 7),
                new HabitDefinition("Days called on Discord", "Days with voice/video calls on Discord", FrequencyType.WEEKLY, 7)
            )
        );

        return defaults;
    }

    /**
     * Initializes default categories and habits for a user.
     * Idempotent: reuses existing categories by name and skips habits
     * that already exist for the user with the same name and category.
     *
     * @param user the user to create default habits for
     */
    public void initializeDefaults(User user) {
        logger.info("Initializing default categories and habits for user: {}", user.getUsername());
        Map<HabitCategory, List<HabitDefinition>> defaults = buildDefaults();

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
