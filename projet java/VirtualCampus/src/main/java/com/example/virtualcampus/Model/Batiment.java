package com.example.virtualcampus.Model;

public class Batiment {
    private int id;
    private String nom;
    private String type;
    private int capacite;
    private double wifiConsommation;
    private double electriciteConsommation;
    private double eauConsommation;
    private int impactSatisfaction;
    private boolean estOuvert;
    private String adresse;

    // Default constructor
    public Batiment() {
    }

    // Full constructor
    public Batiment(int id, String nom, String type, int capacite,
                    double wifiConsommation, double electriciteConsommation,
                    double eauConsommation, int impactSatisfaction) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.capacite = capacite;
        this.wifiConsommation = wifiConsommation;
        this.electriciteConsommation = electriciteConsommation;
        this.eauConsommation = eauConsommation;
        this.impactSatisfaction = impactSatisfaction;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public double getWifiConsommation() {
        return wifiConsommation;
    }

    public void setWifiConsommation(double wifiConsommation) {
        this.wifiConsommation = wifiConsommation;
    }

    public double getElectriciteConsommation() {
        return electriciteConsommation;
    }

    public void setElectriciteConsommation(double electriciteConsommation) {
        this.electriciteConsommation = electriciteConsommation;
    }

    public double getEauConsommation() {
        return eauConsommation;
    }

    public void setEauConsommation(double eauConsommation) {
        this.eauConsommation = eauConsommation;
    }

    public int getImpactSatisfaction() {
        return impactSatisfaction;
    }

    public void setImpactSatisfaction(int impactSatisfaction) {
        this.impactSatisfaction = impactSatisfaction;
    }

    public boolean isEstOuvert() {
        return estOuvert;
    }

    public void setEstOuvert(boolean estOuvert) {
        this.estOuvert = estOuvert;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return id + " - " + nom;
    }
}