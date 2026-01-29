package com.connectasistemas.framework.internal.interfaces;

import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import javafx.scene.Node;

/**
 * Interface para binders de posição
 */
public interface PositionBinder {
    void applyAll(ScreenFieldPosition p, Node node);
}
