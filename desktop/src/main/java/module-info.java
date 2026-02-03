module de.idrinth.habitevaluator.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.idrinth.habitevaluator.shared;
    requires com.google.gson;

    // Database persistence
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires com.h2database;
    requires com.github.librepdf.openpdf;
    requires java.desktop;

    opens de.idrinth.habitevaluator.desktop to javafx.fxml;
    opens de.idrinth.habitevaluator.desktop.controller to javafx.fxml;
    opens de.idrinth.habitevaluator.desktop.persistence to org.hibernate.orm.core;

    exports de.idrinth.habitevaluator.desktop;
    exports de.idrinth.habitevaluator.desktop.controller;
    exports de.idrinth.habitevaluator.desktop.persistence;
}
