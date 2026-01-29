package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.Evaluation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitEntry;
import de.idrinth.habitevaluator.shared.service.HabitEvaluatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class MainController {

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

    private final ObservableList<Habit> habits = FXCollections.observableArrayList();
    private final HabitEvaluatorService evaluatorService = new HabitEvaluatorService();

    @FXML
    public void initialize() {
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
    }

    @FXML
    private void handleAddHabit() {
        String name = habitNameField.getText().trim();
        String description = habitDescriptionArea.getText().trim();

        if (!name.isEmpty()) {
            Habit habit = new Habit(name, description);
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
            displayHabitDetails(selectedHabit);
        }
    }

    @FXML
    private void handleDeleteHabit() {
        Habit selectedHabit = habitListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            habits.remove(selectedHabit);
            clearEvaluationDisplay();
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
