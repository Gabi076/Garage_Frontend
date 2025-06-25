package com.example.garage_app.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Maintenance implements Serializable {
    private Long id;
    private MaintenanceType title;
    private String description;
    private LocalDate date;
    private Double cost;
    private Integer mileage;
    private Long carId;

    public Maintenance(Long id, MaintenanceType title, String description, LocalDate date, Double cost, Integer mileage, Long carId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.cost = cost;
        this.mileage = mileage;
        this.carId = carId;
    }

    public Long getId() {
        return id;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MaintenanceType getTitle() {
        return title;
    }

    public void setTitle(MaintenanceType title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
}

