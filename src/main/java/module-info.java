module com.connectasistemas.framework {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.connectasistemas.framework to javafx.fxml;
    exports com.connectasistemas.framework;
}