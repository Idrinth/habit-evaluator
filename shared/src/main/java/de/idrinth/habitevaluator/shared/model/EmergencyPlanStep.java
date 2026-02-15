package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single step in a user's emergency plan.
 * Each step contains a yes/no question and an action to take if the answer is yes.
 * Steps are ordered by position and may include a phone number for easy calling/copying.
 */
@Entity
@Table(name = "emergency_plan_steps")
public class EmergencyPlanStep {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 500)
    private String action;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public EmergencyPlanStep() {
        this.id = UUID.randomUUID().toString();
    }

    public EmergencyPlanStep(String question, String action, int stepOrder) {
        this();
        this.question = question;
        this.action = action;
        this.stepOrder = stepOrder;
    }

    public EmergencyPlanStep(String question, String action, String phoneNumber, int stepOrder) {
        this(question, action, stepOrder);
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmergencyPlanStep that = (EmergencyPlanStep) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
