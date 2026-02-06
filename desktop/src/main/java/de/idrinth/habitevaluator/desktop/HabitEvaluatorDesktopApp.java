package de.idrinth.habitevaluator.desktop;

import de.idrinth.habitevaluator.desktop.controller.MainController;
import de.idrinth.habitevaluator.desktop.persistence.PersistenceManager;
import de.idrinth.habitevaluator.shared.api.StorageConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HabitEvaluatorDesktopApp extends Application {

    private static final String CONFIG_FILE = System.getProperty("user.home") + "/.habit-evaluator/storage.properties";
    private MainController mainController;
    private ReminderService reminderService;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        if (shouldUseDarkMode()) {
            scene.getStylesheets().add(getClass().getResource("/css/dark.css").toExternalForm());
        }

        InputStream iconStream = getClass().getResourceAsStream("/logo.svg");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }

        primaryStage.setTitle("Habit Evaluator");
        primaryStage.setScene(scene);
        primaryStage.show();

        StorageConfig reminderConfig = new StorageConfig(new File(CONFIG_FILE));
        reminderService = new ReminderService(reminderConfig);
        reminderService.reschedule();
    }

    private boolean shouldUseDarkMode() {
        StorageConfig config = new StorageConfig(new File(CONFIG_FILE));
        StorageConfig.ThemeMode themeMode = config.getThemeMode();
        if (themeMode == StorageConfig.ThemeMode.DARK) {
            return true;
        }
        if (themeMode == StorageConfig.ThemeMode.LIGHT) {
            return false;
        }
        return isSystemDarkMode();
    }

    private boolean isSystemDarkMode() {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("mac")) {
                Process process = Runtime.getRuntime().exec(new String[]{"defaults", "read", "-g", "AppleInterfaceStyle"});
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String result = reader.readLine();
                    return result != null && result.trim().equalsIgnoreCase("Dark");
                }
            } else if (os.contains("win")) {
                Process process = Runtime.getRuntime().exec(new String[]{
                    "reg", "query",
                    "HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme"
                });
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("AppsUseLightTheme") && line.contains("0x0")) {
                            return true;
                        }
                    }
                }
            } else {
                // Linux: check common environment variables and gsettings
                String gtkTheme = System.getenv("GTK_THEME");
                if (gtkTheme != null && gtkTheme.toLowerCase().contains("dark")) {
                    return true;
                }
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{
                        "gsettings", "get", "org.gnome.desktop.interface", "color-scheme"
                    });
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String result = reader.readLine();
                        if (result != null && result.contains("dark")) {
                            return true;
                        }
                    }
                } catch (IOException ignored) {
                    // gsettings not available
                }
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{
                        "gsettings", "get", "org.gnome.desktop.interface", "gtk-theme"
                    });
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String result = reader.readLine();
                        return result != null && result.toLowerCase().contains("dark");
                    }
                } catch (IOException ignored) {
                    // gsettings not available
                }
            }
        } catch (IOException ignored) {
            // Unable to detect, default to light mode
        }
        return false;
    }

    @Override
    public void stop() {
        // Stop reminder scheduler
        if (reminderService != null) {
            reminderService.stop();
        }
        // Sync remote data and save local backup before shutdown
        if (mainController != null) {
            mainController.shutdown();
        }
        // Close database connection on application shutdown
        PersistenceManager.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
