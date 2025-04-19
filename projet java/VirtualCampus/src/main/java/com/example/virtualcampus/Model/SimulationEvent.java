package com.example.virtualcampus.Model;

import java.io.Serializable;

public class SimulationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventType implements Serializable {
        GREVE_PROFS("Grève des professeurs", "Les professeurs sont en grève ! Satisfaction générale en baisse.", -10),
        COUPURE_WIFI("Coupure Wi-Fi", "Le réseau Wi-Fi est en panne ! Les cours en ligne sont perturbés.", -15),
        CAFETERIA_PROBLEME("Problème à la cafétéria", "La cafétéria est temporairement fermée pour des raisons sanitaires.", -10),
        EXAMENS_STRESS("Période d'examens", "Les examens approchent ! Le niveau de stress augmente.", -5),
        PORTES_OUVERTES("Journée portes ouvertes", "Journée portes ouvertes ! Augmentation du nombre de visiteurs.", +5);

        private final String title;
        private final String description;
        private final int satisfactionImpact;

        EventType(String title, String description, int satisfactionImpact) {
            this.title = title;
            this.description = description;
            this.satisfactionImpact = satisfactionImpact;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getSatisfactionImpact() { return satisfactionImpact; }
    }

    private final EventType type;
    private final long timestamp;

    public SimulationEvent(EventType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public EventType getType() { return type; }
    public long getTimestamp() { return timestamp; }
}