package com.connectasistemas.framework.internal.interfaces;

import com.connectasistemas.framework.annotation.ScreenProperties;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Interface para aplicação genérica das propriedades declaradas em {@link ScreenProperties}.
 */
public interface PropertiesBinder {
    boolean applyAll(ScreenProperties properties, Node node);
    void applyToStage(ScreenProperties properties, Stage stage);
}
