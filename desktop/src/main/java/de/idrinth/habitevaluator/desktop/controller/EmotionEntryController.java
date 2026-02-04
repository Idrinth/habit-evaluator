package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EmotionStrengthFormatter;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EmotionEntryController {

    @FXML
    private ComboBox<EmotionPair> emotionPairComboBox;

    @FXML
    private Slider strengthSlider;

    @FXML
    private Label strengthLabel;

    @FXML
    private Label negativeEndLabel;

    @FXML
    private Label positiveEndLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Spinner<Integer> hourSpinner;

    @FXML
    private Spinner<Integer> minuteSpinner;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox entriesContainer;

    private EmotionPairRepository emotionPairRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private User currentUser;
    private List<EmotionEntry> entries = new ArrayList<>();

    public void setEmotionPairRepository(EmotionPairRepository emotionPairRepository) {
        this.emotionPairRepository = emotionPairRepository;
    }

    public void setEmotionEntryRepository(EmotionEntryRepository emotionEntryRepository) {
        this.emotionEntryRepository = emotionEntryRepository;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        LocalTime now = LocalTime.now();
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, now.getHour()));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, now.getMinute()));

        strengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int val = newVal.intValue();
            EmotionPair selected = emotionPairComboBox.getSelectionModel().getSelectedItem();
            strengthLabel.setText(EmotionStrengthFormatter.format(val, selected));
        });

        emotionPairComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(EmotionPair item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        emotionPairComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(EmotionPair item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        emotionPairComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        negativeEndLabel.setText(newVal.getNegativeLabel());
                        positiveEndLabel.setText(newVal.getPositiveLabel());
                    } else {
                        negativeEndLabel.setText("-10");
                        positiveEndLabel.setText("+10");
                    }
                });
    }

    public void loadData() {
        List<EmotionPair> pairs = new ArrayList<>();
        if (emotionPairRepository != null && currentUser != null) {
            pairs = emotionPairRepository.findByUserId(currentUser.getId());
        }
        emotionPairComboBox.setItems(FXCollections.observableArrayList(pairs));
        if (!pairs.isEmpty()) {
            emotionPairComboBox.getSelectionModel().selectFirst();
        }

        if (emotionEntryRepository != null && currentUser != null) {
            entries = new ArrayList<>(emotionEntryRepository.findByUserId(currentUser.getId()));
            entries.sort(Comparator.comparing(EmotionEntry::getRecordedAt).reversed());
        }
        refreshEntriesList();
    }

    @FXML
    private void handleRecordEmotion() {
        EmotionPair selectedPair = emotionPairComboBox.getSelectionModel().getSelectedItem();
        if (selectedPair == null) {
            statusLabel.setText("Select an emotion pair first.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null) {
            date = LocalDate.now();
        }

        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        LocalDateTime recordedAt = LocalDateTime.of(date, LocalTime.of(hour, minute));

        int strength = (int) strengthSlider.getValue();
        String notes = notesArea.getText();
        if (notes != null && notes.trim().isEmpty()) {
            notes = null;
        }

        EmotionEntry entry = new EmotionEntry(selectedPair, strength, recordedAt, notes);
        entry.setUser(currentUser);
        emotionEntryRepository.save(entry);
        entries.add(0, entry);

        strengthSlider.setValue(0);
        notesArea.clear();
        statusLabel.setText("Emotion recorded.");
        statusLabel.setStyle("-fx-text-fill: green;");

        refreshEntriesList();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private void refreshEntriesList() {
        entriesContainer.getChildren().clear();
        if (entries.isEmpty()) {
            entriesContainer.getChildren().add(new Label("No emotion entries recorded yet."));
            return;
        }

        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int shown = Math.min(entries.size(), 50);
        for (int i = 0; i < shown; i++) {
            EmotionEntry entry = entries.get(i);
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color: #444; -fx-border-radius: 4; -fx-padding: 6; -fx-background-color: #333; -fx-background-radius: 4;");

            Label dateLabel = new Label(entry.getRecordedAt().format(dtFmt));
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
            dateLabel.setPrefWidth(110);

            String pairText = entry.getEmotionPair() != null ? entry.getEmotionPair().toString() : "?";
            Label pairLabel = new Label(pairText);
            pairLabel.setStyle("-fx-font-size: 11px;");
            pairLabel.setPrefWidth(140);

            Label strengthLbl = new Label(EmotionStrengthFormatter.format(entry.getStrength(), entry.getEmotionPair()));
            strengthLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            strengthLbl.setPrefWidth(120);

            Label notesLabel = new Label(entry.getNotes() != null ? entry.getNotes() : "");
            notesLabel.setStyle("-fx-font-size: 11px;");
            HBox.setHgrow(notesLabel, javafx.scene.layout.Priority.ALWAYS);

            Button deleteBtn = new Button("X");
            deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
            deleteBtn.setOnAction(e -> {
                emotionEntryRepository.deleteById(entry.getId());
                entries.remove(entry);
                refreshEntriesList();
            });

            row.getChildren().addAll(dateLabel, pairLabel, strengthLbl, notesLabel, deleteBtn);
            entriesContainer.getChildren().add(row);
        }
    }
}
