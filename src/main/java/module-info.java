module com.appregistros.proyectocrud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.appregistros.proyectocrud to javafx.fxml;
    opens com.appregistros.proyectocrud.controller to javafx.fxml;
    exports com.appregistros.proyectocrud;
    exports com.appregistros.proyectocrud.controller;
    exports com.appregistros.proyectocrud.model;
    exports connection;
}