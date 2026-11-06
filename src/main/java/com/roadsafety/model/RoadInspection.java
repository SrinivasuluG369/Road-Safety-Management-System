
package com.roadsafety.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import jakarta.persistence.Column;

@Entity
@Table(name = "road_inspections")
public class RoadInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageName;

    @Column(name = "road_condition")
    private String condition;

    private double damagePercentage;

    private LocalDateTime inspectionDate;

    private String status;

    // Default Constructor
    public RoadInspection() {
    }

    // Parameterized Constructor
    public RoadInspection(String imageName,
                          String condition,
                          double damagePercentage,
                          LocalDateTime inspectionDate,
                          String status) {

        this.imageName = imageName;
        this.condition = condition;
        this.damagePercentage = damagePercentage;
        this.inspectionDate = inspectionDate;
        this.status = status;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getDamagePercentage() {
        return damagePercentage;
    }

    public void setDamagePercentage(double damagePercentage) {
        this.damagePercentage = damagePercentage;
    }

    public LocalDateTime getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDateTime inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
