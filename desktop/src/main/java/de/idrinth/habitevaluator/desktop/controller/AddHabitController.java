package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.FrequencyType;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.ScoringRule;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddHabitController {

    private static final String NEW_CATEGORY = "New category";

    @FXML
    private TextField habitNameField;

    @FXML
    private TextArea habitDescriptionArea;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> frequencyTypeComboBox;

    @FXML
    private TextField targetFrequencyField;

    @FXML
    private TextField maxEntriesPerDayField;

    @FXML
    private CheckBox positiveScoringCheckBox;

    @FXML
    private TextField threshold1Field;

    @FXML
    private TextField threshold2Field;

    @FXML
    private TextField threshold4Field;

    @FXML
    private TextField threshold8Field;

    @FXML
    private VBox translationsContainer;

    private static final String[] TRANSLATION_LANGUAGES = {"en", "de", "es", "fr"};
    private static final String[] TRANSLATION_LANGUAGE_LABELS = {"English", "Deutsch", "Español", "Français"};

    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private ApiClient apiClient;
    private User currentUser;
    private StorageConfig storageConfig;
    private List<HabitCategory> categoryList = new ArrayList<>();
    private final Map<String, String> categoryNameToId = new LinkedHashMap<>();
    private final Map<String, TextField> nameTranslationFields = new HashMap<>();
    private final Map<String, TextField> descTranslationFields = new HashMap<>();
    private Habit addedHabit;

    public void setHabitRepository(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public void setCategoryRepository(HabitCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        buildTranslationFields();
    }

    public void setCategoryList(List<HabitCategory> categoryList) {
        this.categoryList = new ArrayList<>(categoryList);
        populateCategoryComboBox();
    }

    public Habit getAddedHabit() {
        return addedHabit;
    }

    @FXML
    public void initialize() {
        ObservableList<String> frequencyTypes = FXCollections.observableArrayList();
        for (FrequencyType ft : FrequencyType.values()) {
            frequencyTypes.add(ft.name().substring(0, 1) + ft.name().substring(1).toLowerCase());
        }
        frequencyTypeComboBox.setItems(frequencyTypes);
        frequencyTypeComboBox.getSelectionModel().selectFirst();
    }

    private void populateCategoryComboBox() {
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        categoryNames.add(NEW_CATEGORY);
        categoryNameToId.clear();
        for (HabitCategory cat : categoryList) {
            categoryNames.add(cat.getName());
            categoryNameToId.put(cat.getName(), cat.getId());
        }
        categoryComboBox.setItems(categoryNames);
        if (!categoryList.isEmpty()) {
            categoryComboBox.getSelectionModel().select(1);
        } else {
            categoryComboBox.getSelectionModel().selectFirst();
        }
        categoryComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (NEW_CATEGORY.equals(newValue)) {
                        showNewCategoryDialog();
                    }
                });
    }

    private void showNewCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Category");
        dialog.setHeaderText(null);
        dialog.setContentText("Category name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            createCategory(result.get().trim());
        } else {
            if (!categoryList.isEmpty()) {
                categoryComboBox.getSelectionModel().select(1);
            }
        }
    }

    private void createCategory(String name) {
        HabitCategory category = new HabitCategory(name);
        category.setUser(currentUser);
        if (categoryRepository != null) {
            categoryRepository.save(category);
            categoryList.add(category);
            populateCategoryComboBox();
            categoryComboBox.getSelectionModel().select(name);
        } else if (apiClient != null) {
            new Thread(() -> {
                try {
                    Map<String, String> body = new LinkedHashMap<>();
                    body.put("name", name);
                    HabitCategory created = apiClient.post("/api/categories", body, HabitCategory.class);
                    Platform.runLater(() -> {
                        categoryList.add(created);
                        populateCategoryComboBox();
                        categoryComboBox.getSelectionModel().select(created.getName());
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to create category: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void buildTranslationFields() {
        if (translationsContainer == null) {
            return;
        }
        translationsContainer.getChildren().clear();
        nameTranslationFields.clear();
        descTranslationFields.clear();
        if (storageConfig == null || !storageConfig.isCustomTranslationsEnabled()) {
            translationsContainer.setVisible(false);
            translationsContainer.setManaged(false);
            return;
        }
        translationsContainer.setVisible(true);
        translationsContainer.setManaged(true);

        Label title = new Label("Translations");
        title.setStyle("-fx-font-weight: bold;");
        translationsContainer.getChildren().add(title);

        for (int i = 0; i < TRANSLATION_LANGUAGES.length; i++) {
            String lang = TRANSLATION_LANGUAGES[i];
            String label = TRANSLATION_LANGUAGE_LABELS[i];

            TextField nameField = new TextField();
            nameField.setPromptText("Name (" + label + ")");
            nameField.setPrefWidth(150);

            TextField descField = new TextField();
            descField.setPromptText("Description (" + label + ")");
            descField.setPrefWidth(200);

            HBox row = new HBox(6, new Label(label + ":"), nameField, descField);
            row.setAlignment(Pos.CENTER_LEFT);
            translationsContainer.getChildren().add(row);

            nameTranslationFields.put(lang, nameField);
            descTranslationFields.put(lang, descField);
        }
    }

    @FXML
    private void handleAddHabit() {
        String name = habitNameField.getText().trim();
        String description = habitDescriptionArea.getText().trim();

        if (name.isEmpty()) {
            showAlert("Validation Error", "Please enter a habit name.");
            return;
        }

        String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory == null || NEW_CATEGORY.equals(selectedCategory)) {
            showAlert("Category Required", "Please select or create a category before adding a habit.");
            return;
        }

        Habit habit = new Habit(name, description);
        habit.setUser(currentUser);

        String catId = categoryNameToId.get(selectedCategory);
        if (catId != null) {
            habit.setCategoryId(catId);
        }

        int freqIdx = frequencyTypeComboBox.getSelectionModel().getSelectedIndex();
        if (freqIdx >= 0 && freqIdx < FrequencyType.values().length) {
            habit.setFrequencyType(FrequencyType.values()[freqIdx]);
        }

        try {
            int targetFreq = Integer.parseInt(targetFrequencyField.getText().trim());
            if (targetFreq > 0) {
                habit.setTargetFrequency(targetFreq);
            }
        } catch (NumberFormatException e) {
            // keep default
        }

        try {
            int maxEntries = Integer.parseInt(maxEntriesPerDayField.getText().trim());
            if (maxEntries > 0) {
                habit.setMaxEntriesPerDay(maxEntries);
            }
        } catch (NumberFormatException e) {
            // keep default
        }

        habit.setPositiveScoring(positiveScoringCheckBox.isSelected());

        try {
            int t1 = Integer.parseInt(threshold1Field.getText().trim());
            int t2 = Integer.parseInt(threshold2Field.getText().trim());
            int t4 = Integer.parseInt(threshold4Field.getText().trim());
            int t8 = Integer.parseInt(threshold8Field.getText().trim());
            if (t1 >= 0 && t2 >= t1 && t4 >= t2 && t8 >= t4) {
                ScoringRule rule = new ScoringRule("custom", t1, t2, t4, t8);
                habit.setScoringRule(rule);
            }
        } catch (NumberFormatException e) {
            // keep default scoring rule
        }

        if (!nameTranslationFields.isEmpty()) {
            Map<String, String> nameTranslations = new HashMap<>();
            Map<String, String> descTranslations = new HashMap<>();
            for (String lang : TRANSLATION_LANGUAGES) {
                TextField nf = nameTranslationFields.get(lang);
                if (nf != null && !nf.getText().trim().isEmpty()) {
                    nameTranslations.put(lang, nf.getText().trim());
                }
                TextField df = descTranslationFields.get(lang);
                if (df != null && !df.getText().trim().isEmpty()) {
                    descTranslations.put(lang, df.getText().trim());
                }
            }
            habit.setNameTranslations(nameTranslations);
            habit.setDescriptionTranslations(descTranslations);
        }

        habitRepository.save(habit);
        addedHabit = habit;

        Stage stage = (Stage) habitNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
