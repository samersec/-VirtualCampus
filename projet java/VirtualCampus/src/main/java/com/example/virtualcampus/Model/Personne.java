package com.example.virtualcampus.Model;

import java.time.LocalDate;

public class Personne {
    private String id;
    private TypePersonne type;
    private String nom;
    private String prenom;
    private String email;
    private int satisfaction;
    private LocalDate dateInscription;
    private Integer batimentId;

    public enum TypePersonne {
        ETUDIANT, PROFESSEUR
    }

    // Constructors
    public Personne() {}

    public Personne(String id, TypePersonne type, String nom, String prenom, String email,
                    int satisfaction, LocalDate dateInscription, Integer batimentId) {
        this.id = id;
        this.type = type;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.satisfaction = satisfaction;
        this.dateInscription = dateInscription;
        this.batimentId = batimentId;
    }

    // Getters and Setters
    public Integer getIdBatiment() { return batimentId; }
    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public TypePersonne getType() { return type; }
    public void setType(TypePersonne type) { this.type = type; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getSatisfaction() { return satisfaction; }
    public void setSatisfaction(int satisfaction) { this.satisfaction = satisfaction; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public int getBatimentId() { return batimentId; }
    public void setBatimentId(Integer batimentId) { this.batimentId = batimentId; }
}