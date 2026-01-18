module com.connectasistemas.framework {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    exports com.connectasistemas.framework.annotation;
    exports com.connectasistemas.framework.enums;
    exports com.connectasistemas.framework.fxelements;
    exports com.connectasistemas.framework.interfaces;
    exports com.connectasistemas.framework.processor;
    exports com.connectasistemas.framework.utils;
    exports com.connectasistemas.framework.utils.events;
    exports com.connectasistemas.framework.utils.position;
    exports com.connectasistemas.framework.utils.sizes;
    exports com.connectasistemas.framework.utils.validation;
    exports com.connectasistemas.framework;
    exports com.connectasistemas.framework.views;

    opens com.connectasistemas.framework.examples to javafx.graphics;
}
