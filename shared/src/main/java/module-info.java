module de.idrinth.habitevaluator.shared {
    requires com.google.gson;
    requires org.slf4j;
    requires org.yaml.snakeyaml;
    requires static jakarta.persistence;
    requires static com.fasterxml.jackson.annotation;

    exports de.idrinth.habitevaluator.shared.api;
    exports de.idrinth.habitevaluator.shared.backup;
    exports de.idrinth.habitevaluator.shared.gson;
    exports de.idrinth.habitevaluator.shared.localization;
    exports de.idrinth.habitevaluator.shared.model;
    exports de.idrinth.habitevaluator.shared.persistence;
    exports de.idrinth.habitevaluator.shared.service;
    exports de.idrinth.habitevaluator.shared.repository;
    exports de.idrinth.habitevaluator.shared.util;

    opens de.idrinth.habitevaluator.shared.model to org.hibernate.orm.core;
    opens de.idrinth.habitevaluator.shared.backup to com.google.gson;
}
