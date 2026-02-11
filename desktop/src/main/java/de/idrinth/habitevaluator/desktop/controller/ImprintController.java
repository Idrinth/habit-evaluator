package de.idrinth.habitevaluator.desktop.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class ImprintController {

    @FXML
    private Hyperlink emailLink;

    @FXML
    private Label versionLabel;

    @FXML
    private void initialize() {
        String version = "unknown";
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                version = props.getProperty("version", "unknown");
            }
        } catch (Exception e) {
            // ignore - version will show as unknown
        }
        versionLabel.setText("Version " + version);
    }

    @FXML
    private void handleEmailLink() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().mail(new URI("mailto:self@idrinth.de"));
            }
        } catch (Exception e) {
            // ignore - link text is still visible for manual use
        }
    }

    @FXML
    private void handleLicenseLink(ActionEvent event) {
        try {
            if (event.getSource() instanceof Hyperlink link && Desktop.isDesktopSupported()) {
                String url = link.getAccessibleText();
                if (url != null && !url.isEmpty()) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            }
        } catch (Exception e) {
            // ignore - license info is still visible as text
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) emailLink.getScene().getWindow();
        stage.close();
    }
}
