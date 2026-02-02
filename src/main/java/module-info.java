module com.connectasistemas.framework {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    exports com.connectasistemas.framework.annotation;
    exports com.connectasistemas.framework.enums;
    exports com.connectasistemas.framework.fxelements;
    exports com.connectasistemas.framework.interfaces;
    exports com.connectasistemas.framework.internal.processor;
    exports com.connectasistemas.framework.utils;
    exports com.connectasistemas.framework.utils.delimiter;

    exports com.connectasistemas.framework to javafx.graphics;
}
