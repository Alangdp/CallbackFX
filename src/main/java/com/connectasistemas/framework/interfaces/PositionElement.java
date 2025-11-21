package com.connectasistemas.framework.interfaces;

import com.connectasistemas.framework.enums.Position;
import javafx.scene.Node;

/**
 * Declaração
 */
public interface PositionElement {
    boolean validate(Position position);
    void apply(Node root, Node child, Position position);
    Class<?> getElementType();
}
