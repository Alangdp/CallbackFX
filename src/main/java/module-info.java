module com.connectasistemas.framework {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.jdbi.v3.sqlobject;
    requires org.jdbi.v3.core;
    requires java.sql;
    requires atlantafx.base;

    opens com.connectasistemas.framework to javafx.fxml;
    opens com.connectasistemas.framework.models to org.jdbi.v3.core;

    exports com.connectasistemas.framework;
    exports com.connectasistemas.framework.models;
}
