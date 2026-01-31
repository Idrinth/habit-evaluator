package de.idrinth.habitevaluator.webserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("de.idrinth.habitevaluator.shared.model")
public class HabitEvaluatorWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitEvaluatorWebApplication.class, args);
    }
}
