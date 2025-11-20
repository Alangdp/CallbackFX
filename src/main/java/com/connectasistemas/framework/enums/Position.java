package com.connectasistemas.framework.enums;

/**
 * Enum de posições genéricas
 * OBS: É usado tanto por BorderPanes, quanto para GridPanes...
 * ...A validações é executada na aplicação via PositionElement
 */
public enum Position {
    // Posições básicas
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    CENTER,

    // Posições Espécificas
    TOP_LEFT,
    TOP_RIGHT,
    TOP_CENTER,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
    CENTER_LEFT,
    CENTER_RIGHT,
    CENTER_CENTER,
}
