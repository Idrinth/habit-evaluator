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
 * Represents a single action within an emergency plan step.
 * Each step can have multiple actions, each with optional phone number.
 */
@Entity
@Table(name = "emergency_plan_actions")
public class EmergencyPlanAction {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "action_text", nullable = false, length = 500)
    private String actionText;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "action_order", nullable = false)
    private int actionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private EmergencyPlanStep step;

    public EmergencyPlanAction() {
        this.id = UUID.randomUUID().toString();
    }

    public EmergencyPlanAction(String actionText, int actionOrder) {
        this();
        this.actionText = actionText;
        this.actionOrder = actionOrder;
    }

    public EmergencyPlanAction(String actionText, String phoneNumber, int actionOrder) {
        this(actionText, actionOrder);
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getActionOrder() {
        return actionOrder;
    }

    public void setActionOrder(int actionOrder) {
        this.actionOrder = actionOrder;
    }

    public EmergencyPlanStep getStep() {
        return step;
    }

    public void setStep(EmergencyPlanStep step) {
        this.step = step;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmergencyPlanAction that = (EmergencyPlanAction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
