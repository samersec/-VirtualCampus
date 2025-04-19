package com.example.virtualcampus.Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SimulationState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isRunning;
    private int globalSatisfaction;
    private boolean wifiWorking;
    private boolean cafeteriaOpen;
    private int visitorCount;
    private final List<SimulationEvent> eventHistory;
    private LocalDateTime lastSaveTime;

    public SimulationState() {
        this.isRunning = false;
        this.globalSatisfaction = 100;
        this.wifiWorking = true;
        this.cafeteriaOpen = true;
        this.visitorCount = 0;
        this.eventHistory = new ArrayList<>();
        this.lastSaveTime = LocalDateTime.now();
    }

    // Reset state to default values
    public void reset() {
        this.isRunning = false;
        this.globalSatisfaction = 100;
        this.wifiWorking = true;
        this.cafeteriaOpen = true;
        this.visitorCount = 0;
        this.eventHistory.clear();
        this.lastSaveTime = LocalDateTime.now();
    }

    public boolean isRunning() { return isRunning; }
    public void setRunning(boolean running) { isRunning = running; }

    public int getGlobalSatisfaction() { return globalSatisfaction; }

    public void adjustSatisfaction(int delta) {
        globalSatisfaction = Math.max(0, Math.min(100, globalSatisfaction + delta));
    }

    public boolean isWifiWorking() { return wifiWorking; }
    public void setWifiWorking(boolean wifiWorking) { this.wifiWorking = wifiWorking; }

    public boolean isCafeteriaOpen() { return cafeteriaOpen; }
    public void setCafeteriaOpen(boolean cafeteriaOpen) { this.cafeteriaOpen = cafeteriaOpen; }

    public int getVisitorCount() { return visitorCount; }
    public void setVisitorCount(int visitorCount) { this.visitorCount = visitorCount; }

    public List<SimulationEvent> getEventHistory() {
        return eventHistory;
    }

    public void addEvent(SimulationEvent event) {
        eventHistory.add(event);
    }

    public LocalDateTime getLastSaveTime() {
        return lastSaveTime;
    }

    public void updateSaveTime() {
        this.lastSaveTime = LocalDateTime.now();
    }
}