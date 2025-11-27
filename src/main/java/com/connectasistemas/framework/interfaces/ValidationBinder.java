package com.connectasistemas.framework.interfaces;

import com.connectasistemas.framework.annotation.ScreenValidation;
import javafx.scene.Node;

/**
 * Interface para binders de validação.
 */
public interface ValidationBinder {
    boolean applyAll(ScreenValidation validation,
                     Node node,
                     String acronym,
                     Object screenInstance,
                     Object callbacksInstance);
}
