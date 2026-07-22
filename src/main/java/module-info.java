module com.example.farmersin2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.farmersin2 to javafx.fxml;
    exports com.example.farmersin2;
}