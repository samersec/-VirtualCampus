package com.example.virtualcampus.dao;

import com.example.virtualcampus.Model.Personne;
import com.example.virtualcampus.Model.Etudiant;
import com.example.virtualcampus.Model.Professeur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneDAO {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/virtualcampus";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public int getMaxIdNumber(String prefix) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(id, 4) AS UNSIGNED)) as max_num FROM personne WHERE id LIKE ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int maxNum = rs.getInt("max_num");
                return maxNum > 0 ? maxNum : 0;
            }
            return 0;
        }
    }

    public List<Personne> findAll() throws SQLException {
        List<Personne> personnes = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "e.niveau as etudiant_niveau, e.filiere as etudiant_filiere, e.heures_cours as etudiant_heures, " +
                "pr.matiere_enseignee, pr.disponible, pr.en_greve, pr.heures_enseignement " +
                "FROM personne p " +
                "LEFT JOIN etudiant e ON p.id = e.personne_id " +
                "LEFT JOIN professeur pr ON p.id = pr.personne_id";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Personne personne = createPersonneFromResultSet(rs);
                personnes.add(personne);
            }
        }
        return personnes;
    }

    public List<Personne> findByType(Personne.TypePersonne type) throws SQLException {
        List<Personne> personnes = new ArrayList<>();
        String sql = "SELECT p.*, " +
                "e.niveau as etudiant_niveau, e.filiere as etudiant_filiere, e.heures_cours as etudiant_heures, " +
                "pr.matiere_enseignee, pr.disponible, pr.en_greve, pr.heures_enseignement " +
                "FROM personne p " +
                "LEFT JOIN etudiant e ON p.id = e.personne_id " +
                "LEFT JOIN professeur pr ON p.id = pr.personne_id " +
                "WHERE p.type = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, type.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Personne personne = createPersonneFromResultSet(rs);
                personnes.add(personne);
            }
        }
        return personnes;
    }

    private Personne createPersonneFromResultSet(ResultSet rs) throws SQLException {
        Personne personne;
        Personne.TypePersonne type = Personne.TypePersonne.valueOf(rs.getString("type"));

        switch (type) {
            case ETUDIANT:
                Etudiant etudiant = new Etudiant();
                etudiant.setNiveau(rs.getInt("etudiant_niveau"));
                etudiant.setFiliere(rs.getString("etudiant_filiere"));
                etudiant.setHeuresCours(rs.getInt("etudiant_heures"));
                personne = etudiant;
                break;

            case PROFESSEUR:
                Professeur professeur = new Professeur();
                professeur.setMatiereEnseignee(rs.getString("matiere_enseignee"));
                professeur.setDisponible(rs.getBoolean("disponible"));
                professeur.setEnGreve(rs.getBoolean("en_greve"));
                professeur.setHeuresEnseignement(rs.getInt("heures_enseignement"));
                personne = professeur;
                break;

            default:
                personne = new Personne();
        }

        // Set common fields
        personne.setId(rs.getString("id"));
        personne.setType(type);
        personne.setNom(rs.getString("nom"));
        personne.setPrenom(rs.getString("prenom"));
        personne.setEmail(rs.getString("email"));
        personne.setSatisfaction(rs.getInt("satisfaction"));
        personne.setDateInscription(rs.getDate("date_inscription").toLocalDate());
        personne.setBatimentId(rs.getInt("batiment_id"));

        return personne;
    }

    public void create(Personne personne) throws SQLException {
        // Generate ID if not provided
        if (personne.getId() == null || personne.getId().isEmpty()) {
            String prefix = personne.getType() == Personne.TypePersonne.ETUDIANT ? "ETD" : "PRO";
            int nextId = getMaxIdNumber(prefix) + 1;
            personne.setId(String.format("%s%03d", prefix, nextId));
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);

            // Insert into personne table
            String sql = "INSERT INTO personne (id, type, nom, prenom, email, satisfaction, date_inscription, batiment_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, personne.getId());
                pstmt.setString(2, personne.getType().toString());
                pstmt.setString(3, personne.getNom());
                pstmt.setString(4, personne.getPrenom());
                pstmt.setString(5, personne.getEmail());
                pstmt.setInt(6, personne.getSatisfaction());
                pstmt.setDate(7, Date.valueOf(personne.getDateInscription()));
                pstmt.setInt(8, personne.getBatimentId());
                pstmt.executeUpdate();
            }

            // Insert specific details
            if (personne instanceof Etudiant) {
                Etudiant etudiant = (Etudiant) personne;
                sql = "INSERT INTO etudiant (personne_id, niveau, filiere, heures_cours) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, etudiant.getId());
                    pstmt.setInt(2, etudiant.getNiveau());
                    pstmt.setString(3, etudiant.getFiliere());
                    pstmt.setInt(4, etudiant.getHeuresCours());
                    pstmt.executeUpdate();
                }
            } else if (personne instanceof Professeur) {
                Professeur professeur = (Professeur) personne;
                sql = "INSERT INTO professeur (personne_id, matiere_enseignee, disponible, en_greve, heures_enseignement) " +
                        "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, professeur.getId());
                    pstmt.setString(2, professeur.getMatiereEnseignee());
                    pstmt.setBoolean(3, professeur.isDisponible());
                    pstmt.setBoolean(4, professeur.isEnGreve());
                    pstmt.setInt(5, professeur.getHeuresEnseignement());
                    pstmt.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public void update(Personne personne) throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);

            // Update personne table
            String sql = "UPDATE personne SET nom = ?, prenom = ?, email = ?, " +
                    "satisfaction = ?, date_inscription = ?, batiment_id = ? WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, personne.getNom());
                pstmt.setString(2, personne.getPrenom());
                pstmt.setString(3, personne.getEmail());
                pstmt.setInt(4, personne.getSatisfaction());
                pstmt.setDate(5, Date.valueOf(personne.getDateInscription()));
                pstmt.setInt(6, personne.getBatimentId());
                pstmt.setString(7, personne.getId());
                pstmt.executeUpdate();
            }

            // Update specific details
            if (personne instanceof Etudiant) {
                Etudiant etudiant = (Etudiant) personne;
                sql = "UPDATE etudiant SET niveau = ?, filiere = ?, heures_cours = ? WHERE personne_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, etudiant.getNiveau());
                    pstmt.setString(2, etudiant.getFiliere());
                    pstmt.setInt(3, etudiant.getHeuresCours());
                    pstmt.setString(4, etudiant.getId());
                    pstmt.executeUpdate();
                }
            } else if (personne instanceof Professeur) {
                Professeur professeur = (Professeur) personne;
                sql = "UPDATE professeur SET matiere_enseignee = ?, disponible = ?, " +
                        "en_greve = ?, heures_enseignement = ? WHERE personne_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, professeur.getMatiereEnseignee());
                    pstmt.setBoolean(2, professeur.isDisponible());
                    pstmt.setBoolean(3, professeur.isEnGreve());
                    pstmt.setInt(4, professeur.getHeuresEnseignement());
                    pstmt.setString(5, professeur.getId());
                    pstmt.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public void delete(String id) throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);

            // Delete from specific tables first
            String sql = "DELETE FROM etudiant WHERE personne_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }

            sql = "DELETE FROM professeur WHERE personne_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }

            // Then delete from personne table
            sql = "DELETE FROM personne WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public void updateAllProfesseursGreveStatus(boolean enGreve) throws SQLException {
        String sql = "UPDATE professeur SET en_greve = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, enGreve);
            pstmt.executeUpdate();
        }
    }

    public void updateGlobalSatisfaction(int satisfaction) throws SQLException {
        // First validate the input range
        if (satisfaction < -100 || satisfaction > 100) {
            throw new IllegalArgumentException("Satisfaction adjustment must be between -100 and 100");
        }

        // Use a bounded update to prevent constraint violations
        String sql = "UPDATE personne SET satisfaction = GREATEST(0, LEAST(100, satisfaction + ?))";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, satisfaction);
            int updatedRows = pstmt.executeUpdate();
            System.out.println("Updated " + updatedRows + " rows with satisfaction adjustment: " + satisfaction);
        }
    }
    public double getAverageSatisfaction() throws SQLException {
        String sql = "SELECT AVG(satisfaction) as avg_satisfaction FROM personne";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("avg_satisfaction");
            }
            return 0;
        }
    }

    public void updateTypeSatisfaction(Personne.TypePersonne typePersonne, int satisfactionImpact) {
    }
}