module com.example.farmersin2 {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports frontend;
    exports database;
    exports model;
    exports dao;
    exports services;

    opens frontend to javafx.graphics, javafx.fxml;
    opens model to javafx.base;
}