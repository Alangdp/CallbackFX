package com.connectasistemas.framework.interfaces;

import com.connectasistemas.framework.enums.Position;
import javafx.scene.Node;

/**
 * Declaração
 */
public interface PositionElement {
    boolean validate(Position position);
    void apply(Node root, Position position);
    Class<?> getElementType();
}
