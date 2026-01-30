package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.desktop.persistence.H2HabitRepository;
import de.idrinth.habitevaluator.desktop.persistence.H2UserRepository;
import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.repository.UserRepository;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final String PLACEHOLDER_USERNAME = "desktop_user";

    @FXML
    private ListView<Habit> habitListView;

    @FXML
    private TextField habitNameField;

    @FXML
    private TextArea habitDescriptionArea;

    @FXML
    private Label streakLabel;

    @FXML
    private Label completionRateLabel;

    @FXML
    private ProgressBar completionProgressBar;

    @FXML
    private VBox trackHabitsContainer;

    @FXML
    private Label trackMessage;

    private final Map<String, CheckBox> trackCheckBoxes = new HashMap<>();
    private final ObservableList<Habit> habits = FXCollections.observableArrayList();
    private final HabitEvaluatorService evaluatorService = new HabitEvaluatorService();
    private final HabitRepository habitRepository = new H2HabitRepository();
    private final UserRepository userRepository = new H2UserRepository();
    private User currentUser;

    @FXML
    public void initialize() {
        // Initialize placeholder user
        currentUser = userRepository.findByUsername(PLACEHOLDER_USERNAME)
                .orElseGet(() -> {
                    User user = new User(PLACEHOLDER_USERNAME, "placeholder");
                    return userRepository.save(user);
                });

        // Load habits for the current user
        habits.addAll(habitRepository.findByUserId(currentUser.getId()));

        habitListView.setItems(habits);
        habitListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Habit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        habitListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displayHabitDetails(newValue);
                    }
                });

        refreshTrackHabits();
        habits.addListener((javafx.collections.ListChangeListener<Habit>) change -> refreshTrackHabits());
    }

    private void refreshTrackHabits() {
        trackHabitsContainer.getChildren().clear();
        trackCheckBoxes.clear();
        trackMessage.setText("");
        if (habits.isEmpty()) {
            trackHabitsContainer.getChildren().add(new Label("No habits found. Add a habit first."));
            return;
        }
        for (Habit habit : habits) {
            CheckBox checkBox = new CheckBox(habit.getName());
            if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
                checkBox.setTooltip(new Tooltip(habit.getDescription()));
            }
            trackCheckBoxes.put(habit.getId(), checkBox);
            trackHabitsContainer.getChildren().add(checkBox);
        }
    }

    @FXML
    private void handleAddHabit() {
        String name = habitNameField.getText().trim();
        String description = habitDescriptionArea.getText().trim();

        if (!name.isEmpty()) {
            Habit habit = new Habit(name, description);
            habit.setUser(currentUser);
            habitRepository.save(habit);
            habits.add(habit);
            clearInputFields();
        }
    }

    @FXML
    private void handleCompleteHabit() {
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            HabitEntry entry = new HabitEntry(selectedHabit.getId());
            selectedHabit.addEntry(entry);
            habitRepository.save(selectedHabit);
            displayHabitDetails(selectedHabit);
        }
    }

    @FXML
    private void handleDeleteHabit() {
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            habitRepository.deleteById(selectedHabit.getId());
            habits.remove(selectedHabit);
            clearEvaluationDisplay();
        }
    }

    @FXML
    private void handleTrackHabits() {
        List<Habit> checkedHabits = new ArrayList<>();
        for (Habit habit : habits) {
            CheckBox checkBox = trackCheckBoxes.get(habit.getId());
            if (checkBox != null && checkBox.isSelected()) {
                checkedHabits.add(habit);
            }
        }
        if (checkedHabits.isEmpty()) {
            trackMessage.setText("No habits were selected");
            trackMessage.setStyle("-fx-text-fill: red;");
            return;
        }
        int count = 0;
        for (Habit habit : checkedHabits) {
            HabitEntry entry = new HabitEntry(habit.getId());
            habit.addEntry(entry);
            habitRepository.save(habit);
            count++;
        }
        trackMessage.setText(count + " habit(s) tracked successfully");
        trackMessage.setStyle("-fx-text-fill: green;");

        // Uncheck all boxes
        for (CheckBox checkBox : trackCheckBoxes.values()) {
            checkBox.setSelected(false);
        }
    }

    private void displayHabitDetails(Habit habit) {
        Evaluation evaluation = evaluatorService.evaluate(
                habit,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        streakLabel.setText("Current Streak: " + evaluation.getCurrentStreak() + " days");
        completionRateLabel.setText(String.format("Completion Rate: %.1f%%", evaluation.getCompletionRate() * 100));
        completionProgressBar.setProgress(evaluation.getCompletionRate());
    }

    private void clearInputFields() {
        habitNameField.clear();
        habitDescriptionArea.clear();
    }

    private void clearEvaluationDisplay() {
        streakLabel.setText("Current Streak: -");
        completionRateLabel.setText("Completion Rate: -");
        completionProgressBar.setProgress(0);
    }
}
