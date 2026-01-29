module de.idrinth.habitevaluator.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.idrinth.habitevaluator.shared;

    opens de.idrinth.habitevaluator.desktop to javafx.fxml;
    opens de.idrinth.habitevaluator.desktop.controller to javafx.fxml;

    exports de.idrinth.habitevaluator.desktop;
    exports de.idrinth.habitevaluator.desktop.controller;
}
