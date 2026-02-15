package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single step in a user's emergency plan.
 * Each step contains a yes/no question and one or more actions to take if the answer is yes.
 * Steps are ordered by position and actions may include phone numbers for easy calling/copying.
 */
@Entity
@Table(name = "emergency_plan_steps")
public class EmergencyPlanStep {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "step", fetch = FetchType.LAZY)
    private List<EmergencyPlanAction> actions = new ArrayList<>();

    public EmergencyPlanStep() {
        this.id = UUID.randomUUID().toString();
    }

    public EmergencyPlanStep(String question, int stepOrder) {
        this();
        this.question = question;
        this.stepOrder = stepOrder;
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

    public List<EmergencyPlanAction> getActions() {
        return actions;
    }

    public void setActions(List<EmergencyPlanAction> actions) {
        this.actions = actions;
    }

    public void addAction(EmergencyPlanAction action) {
        actions.add(action);
        action.setStep(this);
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
