package com.connectasistemas.framework.interfaces;

import com.connectasistemas.framework.enums.Position;
import javafx.scene.Node;

/**
 * Declaração de um elemento que pode aplicar posições em seus filhos
 */
public interface PositionElement {
    boolean validate(Position position);
    void apply(Node root, Node child, Position position);
    Class<?> getElementType();
}
