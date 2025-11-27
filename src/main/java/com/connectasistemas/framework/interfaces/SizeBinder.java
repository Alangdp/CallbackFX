package com.connectasistemas.framework.interfaces;

import com.connectasistemas.framework.annotation.ScreenFieldSize;
import javafx.scene.Node;

/**
 * Interface para binders de tamanho
 */
public interface SizeBinder {
    boolean applyAll(ScreenFieldSize s, Node node);
}
