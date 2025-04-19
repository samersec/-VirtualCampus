package com.example.virtualcampus.Model;

public class Etudiant extends Personne {
    private String filiere;
    private int niveau;
    private int heuresCours;
    private int satisfaction;

    // Constructors
    public Etudiant() {
        super();
        setType(TypePersonne.ETUDIANT);
    }

    // Getters and Setters
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }

    public int getNiveau() { return niveau; }
    public void setNiveau(int niveau) { this.niveau = niveau; }

    public int getHeuresCours() { return heuresCours; }
    public void setHeuresCours(int heuresCours) { this.heuresCours = heuresCours; }

    @Override
    public int getSatisfaction() { return satisfaction; }
    @Override
    public void setSatisfaction(int satisfaction) { this.satisfaction = satisfaction; }
}