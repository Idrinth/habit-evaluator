package de.idrinth.habitevaluator.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Base data for a medication: name, Wikipedia link, and provision type.
 * Used to make it easier to fill in medication log entries.
 */
@Entity
@Table(name = "medications")
public class Medication {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "wikipedia_link", length = 500)
    private String wikipediaLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "provision_type", nullable = false)
    private MedicationProvisionType provisionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Medication() {
        this.id = UUID.randomUUID().toString();
    }

    public Medication(String name, MedicationProvisionType provisionType) {
        this();
        this.name = name;
        this.provisionType = provisionType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWikipediaLink() {
        return wikipediaLink;
    }

    public void setWikipediaLink(String wikipediaLink) {
        this.wikipediaLink = wikipediaLink;
    }

    public MedicationProvisionType getProvisionType() {
        return provisionType;
    }

    public void setProvisionType(MedicationProvisionType provisionType) {
        this.provisionType = provisionType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the display unit for this medication's provision type.
     */
    public String getUnit() {
        if (provisionType == null) {
            return "";
        }
        switch (provisionType) {
            case PILL:
                return "mg";
            case LIQUID_DROPS:
                return "drops";
            case LIQUID_ML:
                return "ml";
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return name + " (" + getUnit() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medication that = (Medication) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
