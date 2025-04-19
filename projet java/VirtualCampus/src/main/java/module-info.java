module com.example.virtualcampus {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;       // For JDBC

    opens com.example.virtualcampus to javafx.fxml;
    exports com.example.virtualcampus;
    exports com.example.virtualcampus.Model;
    opens com.example.virtualcampus.Model to javafx.fxml;
    exports com.example.virtualcampus.Controllers;
    opens com.example.virtualcampus.Controllers to javafx.fxml;
    exports com.example.virtualcampus.database;
    opens com.example.virtualcampus.database to javafx.fxml;
}