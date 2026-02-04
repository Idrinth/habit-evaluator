package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.EmotionStrengthFormatter;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EmotionPairController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TextField negativeLabelField;

    @FXML
    private TextField positiveLabelField;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox pairsContainer;

    private EmotionPairRepository emotionPairRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private User currentUser;
    private List<EmotionPair> pairs = new ArrayList<>();
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

    public void loadData() {
        if (emotionPairRepository != null && currentUser != null) {
            pairs = new ArrayList<>(emotionPairRepository.findByUserId(currentUser.getId()));
        }
        if (emotionEntryRepository != null && currentUser != null) {
            entries = new ArrayList<>(emotionEntryRepository.findByUserId(currentUser.getId()));
            entries.sort(Comparator.comparing(EmotionEntry::getRecordedAt).reversed());
        }
        refreshPairsList();
    }

    @FXML
    private void handleAddPair() {
        String negative = negativeLabelField.getText();
        String positive = positiveLabelField.getText();

        if (negative == null || negative.trim().isEmpty()
                || positive == null || positive.trim().isEmpty()) {
            statusLabel.setText("Both labels are required.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        EmotionPair pair = new EmotionPair(negative.trim(), positive.trim());
        pair.setUser(currentUser);
        emotionPairRepository.save(pair);
        pairs.add(pair);

        negativeLabelField.clear();
        positiveLabelField.clear();
        statusLabel.setText("Emotion pair added.");
        statusLabel.setStyle("-fx-text-fill: green;");

        refreshPairsList();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private void refreshPairsList() {
        pairsContainer.getChildren().clear();
        if (pairs.isEmpty()) {
            pairsContainer.getChildren().add(new Label("No emotion pairs defined yet."));
            return;
        }
        for (EmotionPair pair : pairs) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color: #444; -fx-border-radius: 4; -fx-padding: 6; -fx-background-color: #333; -fx-background-radius: 4;");

            Label label = new Label(pair.getNegativeLabel() + "  \u2014  " + pair.getPositiveLabel());
            label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            HBox.setHgrow(label, javafx.scene.layout.Priority.ALWAYS);

            Button deleteBtn = new Button("X");
            deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
            deleteBtn.setOnAction(e -> {
                emotionPairRepository.deleteById(pair.getId());
                pairs.remove(pair);
                entries.removeIf(entry -> entry.getEmotionPair() != null
                        && entry.getEmotionPair().getId().equals(pair.getId()));
                refreshPairsList();
            });

            row.getChildren().addAll(label, deleteBtn);
            pairsContainer.getChildren().add(row);

            List<EmotionEntry> pairEntries = new ArrayList<>();
            for (EmotionEntry entry : entries) {
                if (entry.getEmotionPair() != null && entry.getEmotionPair().getId().equals(pair.getId())) {
                    pairEntries.add(entry);
                }
            }

            for (EmotionEntry entry : pairEntries) {
                HBox entryRow = new HBox(10);
                entryRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                entryRow.setStyle("-fx-padding: 4 6 4 20;");

                Label dateLabel = new Label(entry.getRecordedAt().format(DATE_FORMAT));
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
                dateLabel.setPrefWidth(110);

                Label strengthLbl = new Label(EmotionStrengthFormatter.format(entry.getStrength(), pair));
                strengthLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                strengthLbl.setPrefWidth(120);

                Label notesLabel = new Label(entry.getNotes() != null ? entry.getNotes() : "");
                notesLabel.setStyle("-fx-font-size: 11px;");
                HBox.setHgrow(notesLabel, javafx.scene.layout.Priority.ALWAYS);

                Button deleteEntryBtn = new Button("X");
                deleteEntryBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
                deleteEntryBtn.setOnAction(e -> {
                    emotionEntryRepository.deleteById(entry.getId());
                    entries.remove(entry);
                    refreshPairsList();
                });

                entryRow.getChildren().addAll(dateLabel, strengthLbl, notesLabel, deleteEntryBtn);
                pairsContainer.getChildren().add(entryRow);
            }
        }
    }
}
