package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EmotionPairController {

    @FXML
    private TextField negativeLabelField;

    @FXML
    private TextField positiveLabelField;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox pairsContainer;

    private EmotionPairRepository emotionPairRepository;
    private User currentUser;
    private List<EmotionPair> pairs = new ArrayList<>();

    public void setEmotionPairRepository(EmotionPairRepository emotionPairRepository) {
        this.emotionPairRepository = emotionPairRepository;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void loadData() {
        if (emotionPairRepository != null && currentUser != null) {
            pairs = new ArrayList<>(emotionPairRepository.findByUserId(currentUser.getId()));
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
            label.setStyle("-fx-font-size: 12px;");
            HBox.setHgrow(label, javafx.scene.layout.Priority.ALWAYS);

            Button deleteBtn = new Button("X");
            deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6;");
            deleteBtn.setOnAction(e -> {
                emotionPairRepository.deleteById(pair.getId());
                pairs.remove(pair);
                refreshPairsList();
            });

            row.getChildren().addAll(label, deleteBtn);
            pairsContainer.getChildren().add(row);
        }
    }
}
