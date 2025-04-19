package com.example.virtualcampus.Model;

public class Professeur extends Personne {
    private String matiereEnseignee;
    private boolean disponible;
    private boolean enGreve;
    private int heuresEnseignement;

    // Constructeurs
    public Professeur() {
        super();
        setType(TypePersonne.PROFESSEUR);
    }

    // Getters et Setters
    public String getMatiereEnseignee() {
        return matiereEnseignee;
    }

    public void setMatiereEnseignee(String matiereEnseignee) {
        this.matiereEnseignee = matiereEnseignee;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isEnGreve() {
        return enGreve;
    }

    public void setEnGreve(boolean enGreve) {
        this.enGreve = enGreve;
    }

    public int getHeuresEnseignement() {
        return heuresEnseignement;
    }

    public void setHeuresEnseignement(int heuresEnseignement) {
        this.heuresEnseignement = heuresEnseignement;
    }
}
