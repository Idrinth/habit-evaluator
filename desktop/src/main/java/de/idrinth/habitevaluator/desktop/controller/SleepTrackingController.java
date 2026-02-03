package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.SleepStats;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.SleepEvaluationService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SleepTrackingController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField fromTimeField;

    @FXML
    private TextField untilTimeField;

    @FXML
    private TextField notesField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label weeklyAvgLabel;

    @FXML
    private Label weeklyMinLabel;

    @FXML
    private Label weeklyMaxLabel;

    @FXML
    private Label weeklyCountLabel;

    @FXML
    private Label monthlyAvgLabel;

    @FXML
    private Label monthlyMinLabel;

    @FXML
    private Label monthlyMaxLabel;

    @FXML
    private Label monthlyCountLabel;

    @FXML
    private VBox entriesContainer;

    private SleepEntryRepository sleepEntryRepository;
    private User currentUser;
    private final SleepEvaluationService sleepEvaluationService = new SleepEvaluationService();
    private List<SleepEntry> entries = new ArrayList<>();

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        fromTimeField.setText("22:00");
        untilTimeField.setText("06:00");
    }

    public void setSleepEntryRepository(SleepEntryRepository repository) {
        this.sleepEntryRepository = repository;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadData();
    }

    private void loadData() {
        if (sleepEntryRepository == null || currentUser == null) {
            return;
        }
        entries = new ArrayList<>(sleepEntryRepository.findByUserId(currentUser.getId()));
        entries.sort(Comparator.comparing(SleepEntry::getDate)
                .thenComparing(SleepEntry::getFromTime).reversed());
        updateStats();
        updateEntryList();
    }

    private void updateStats() {
        SleepStats weekly = sleepEvaluationService.getCurrentWeekStats(entries);
        SleepStats monthly = sleepEvaluationService.getCurrentMonthStats(entries);

        if (weekly.getTotalEntries() > 0) {
            weeklyAvgLabel.setText(String.format("Avg: %.1f hrs", weekly.getAverageHours()));
            weeklyMinLabel.setText(String.format("Min: %.1f hrs", weekly.getMinHours()));
            weeklyMaxLabel.setText(String.format("Max: %.1f hrs", weekly.getMaxHours()));
            weeklyCountLabel.setText("Entries: " + weekly.getTotalEntries());
        } else {
            weeklyAvgLabel.setText("Avg: -");
            weeklyMinLabel.setText("Min: -");
            weeklyMaxLabel.setText("Max: -");
            weeklyCountLabel.setText("Entries: 0");
        }

        if (monthly.getTotalEntries() > 0) {
            monthlyAvgLabel.setText(String.format("Avg: %.1f hrs", monthly.getAverageHours()));
            monthlyMinLabel.setText(String.format("Min: %.1f hrs", monthly.getMinHours()));
            monthlyMaxLabel.setText(String.format("Max: %.1f hrs", monthly.getMaxHours()));
            monthlyCountLabel.setText("Entries: " + monthly.getTotalEntries());
        } else {
            monthlyAvgLabel.setText("Avg: -");
            monthlyMinLabel.setText("Min: -");
            monthlyMaxLabel.setText("Max: -");
            monthlyCountLabel.setText("Entries: 0");
        }
    }

    private void updateEntryList() {
        entriesContainer.getChildren().clear();
        if (entries.isEmpty()) {
            entriesContainer.getChildren().add(new Label("No sleep entries yet."));
            return;
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (SleepEntry entry : entries) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color: #444; -fx-border-radius: 4; -fx-padding: 6; -fx-background-color: #333; -fx-background-radius: 4;");

            Label dateLabel = new Label(entry.getDate().toString());
            dateLabel.setStyle("-fx-font-weight: bold;");

            Label timeLabel = new Label(entry.getFromTime().format(timeFormatter) + " - " + entry.getUntilTime().format(timeFormatter));
            timeLabel.setStyle("-fx-text-fill: #aaa;");

            Label hoursLabel = new Label(String.format("%.1f hrs", entry.getHours()));
            hoursLabel.setStyle("-fx-font-weight: bold;");

            HBox infoBox = new HBox(10, dateLabel, timeLabel, hoursLabel);
            infoBox.setAlignment(Pos.CENTER_LEFT);

            if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
                Label notesLabel = new Label(entry.getNotes());
                notesLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
                infoBox.getChildren().add(notesLabel);
            }

            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 11px;");
            deleteBtn.setOnAction(e -> {
                sleepEntryRepository.deleteById(entry.getId());
                loadData();
                messageLabel.setText("Entry deleted");
                messageLabel.setStyle("-fx-text-fill: green;");
            });

            row.getChildren().addAll(infoBox, deleteBtn);
            entriesContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleAddEntry() {
        messageLabel.setText("");
        messageLabel.setStyle("");

        LocalDate date = datePicker.getValue();
        if (date == null) {
            messageLabel.setText("Please select a date");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        LocalTime fromTime;
        LocalTime untilTime;
        try {
            fromTime = LocalTime.parse(fromTimeField.getText().trim());
            untilTime = LocalTime.parse(untilTimeField.getText().trim());
        } catch (DateTimeParseException e) {
            messageLabel.setText("Invalid time format. Use HH:mm");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (sleepEvaluationService.hasOverlap(entries, date, fromTime, untilTime)) {
            messageLabel.setText("Sleep entry overlaps with an existing entry");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        SleepEntry entry = new SleepEntry(fromTime, untilTime, date);
        String notes = notesField.getText();
        if (notes != null && !notes.trim().isEmpty()) {
            entry.setNotes(notes.trim());
        }
        entry.setUser(currentUser);

        sleepEntryRepository.save(entry);
        notesField.clear();
        messageLabel.setText("Sleep entry added");
        messageLabel.setStyle("-fx-text-fill: green;");
        loadData();
    }
}
