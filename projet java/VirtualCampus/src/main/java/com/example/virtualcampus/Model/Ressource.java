package com.example.virtualcampus.Model;

import java.time.LocalDateTime;

public class Ressource {
    private int id;
    private String name;
    private ResourceType type;
    private double currentConsumption;
    private double capacity;
    private String unit;
    private LocalDateTime lastUpdated;
    private ResourceStatus status;

    public enum ResourceType {
        ELECTRICITY, WATER, WIFI, SPACE
    }

    public enum ResourceStatus {
        NORMAL, CRITICAL, OPTIMIZED
    }

    // Constructors
    public Ressource() {}

    public Ressource(int id, String name, ResourceType type, double currentConsumption,
                    double capacity, String unit, LocalDateTime lastUpdated, ResourceStatus status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.currentConsumption = currentConsumption;
        this.capacity = capacity;
        this.unit = unit;
        this.lastUpdated = lastUpdated;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public double getCurrentConsumption() { return currentConsumption; }
    public void setCurrentConsumption(double currentConsumption) {
        this.currentConsumption = currentConsumption;
    }

    public double getCapacity() { return capacity; }
    public void setCapacity(double capacity) { this.capacity = capacity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }

    private String getResourceStatus(double percentage) {
        if (percentage >= 0.7 || percentage < 0.1) {
            return "🔴 CRITICAL";
        } else if (percentage < 0.4) {
            return "🟢 OPTIMIZED";
        } else {
            return "✅ NORMAL";
        }
    }
}