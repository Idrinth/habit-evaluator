package de.idrinth.habitevaluator.webserver.repository;

import de.idrinth.habitevaluator.shared.model.HabitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaHabitCategoryRepository extends JpaRepository<HabitCategory, String> {
}
