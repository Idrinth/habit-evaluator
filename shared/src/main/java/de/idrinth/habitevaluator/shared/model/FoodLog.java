package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a food log entry tracking nutritional intake.
 * Each entry records carbohydrates, calories, a timestamp, and a list of foodstuff consumed.
 * Food items are stored both as a comma-separated string (legacy) and as normalized FoodTag
 * entities via an n:m join table for case-insensitive correlation analysis.
 */
@Entity
@Table(name = "food_logs")
public class FoodLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column
    private Double carbohydrates;

    @Column
    private Integer kcal;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "food_items", nullable = false, length = 2000)
    private String foodItems;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "food_log_tags",
            joinColumns = @JoinColumn(name = "food_log_id"),
            inverseJoinColumns = @JoinColumn(name = "food_tag_id")
    )
    private Set<FoodTag> tags = new HashSet<>();

    public FoodLog() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.dateTime = LocalDateTime.now();
        this.foodItems = "";
    }

    public FoodLog(Double carbohydrates, Integer kcal, LocalDateTime dateTime, String foodItems) {
        this();
        this.carbohydrates = carbohydrates;
        this.kcal = kcal;
        this.dateTime = dateTime;
        this.foodItems = foodItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(Double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public Integer getKcal() {
        return kcal;
    }

    public void setKcal(Integer kcal) {
        this.kcal = kcal;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(String foodItems) {
        this.foodItems = foodItems;
    }

    /**
     * Returns the food items as a parsed list of individual item names.
     */
    @Transient
    public List<String> getFoodItemList() {
        if (foodItems == null || foodItems.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(foodItems.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Sets the food items from a list of individual item names.
     */
    public void setFoodItemList(List<String> items) {
        if (items == null || items.isEmpty()) {
            this.foodItems = "";
        } else {
            this.foodItems = items.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(", "));
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<FoodTag> getTags() {
        return tags;
    }

    public void setTags(Set<FoodTag> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodLog that = (FoodLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
