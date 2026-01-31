module de.idrinth.habitevaluator.shared {
    requires com.google.gson;
    requires org.slf4j;
    requires org.yaml.snakeyaml;
    requires static jakarta.persistence;
    requires static com.fasterxml.jackson.annotation;

    exports de.idrinth.habitevaluator.shared.api;
    exports de.idrinth.habitevaluator.shared.localization;
    exports de.idrinth.habitevaluator.shared.model;
    exports de.idrinth.habitevaluator.shared.service;
    exports de.idrinth.habitevaluator.shared.repository;

    // Open model package for JPA/Hibernate reflection access
    opens de.idrinth.habitevaluator.shared.model to org.hibernate.orm.core;
}
