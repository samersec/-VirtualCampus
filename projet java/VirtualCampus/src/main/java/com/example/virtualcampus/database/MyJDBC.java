package com.example.virtualcampus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class MyJDBC {
    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/virtualcampus",
                    "root",
                    "root"
            );

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM USER");

            while(resultSet.next()) {
                System.out.println(resultSet.getString("username"));
                System.out.println(resultSet.getString("password"));
            }

//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery("SELECT * FROM batiments");
//
//            // 3. Parcours des résultats
//            while (resultSet.next()) {
//                int id = resultSet.getInt("id");
//                String nom = resultSet.getString("nom");
//                String type = resultSet.getString("type");
//                int capacite = resultSet.getInt("capacite");
//                double consoWifi = resultSet.getDouble("conso_wifi");
//                double consoElec = resultSet.getDouble("conso_elec");
//                double consoEau = resultSet.getDouble("conso_eau");
//                double satisfaction = resultSet.getDouble("impact_satisfaction");
//
//                // 4. Affichage
//                System.out.println("ID: " + id);
//                System.out.println("Nom: " + nom);
//                System.out.println("Type: " + type);
//                System.out.println("Capacité: " + capacite);
//                System.out.println("Conso Wifi: " + consoWifi);
//                System.out.println("Conso Elec: " + consoElec);
//                System.out.println("Conso Eau: " + consoEau);
//                System.out.println("Satisfaction: " + satisfaction);
//                System.out.println("------------------------");
//            }
//
//            // Close resources
//            resultSet.close();
//            statement.close();
//            connection.close();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}