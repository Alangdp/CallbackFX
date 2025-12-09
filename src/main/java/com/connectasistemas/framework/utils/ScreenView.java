package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.annotation.ScreenProperties;
import javafx.scene.layout.Region;

/**
 * Representa o resultado da composição de uma tela anotada com {@code @Screen}.
 */
public record ScreenView(
        Class<?> screenClass,
        Object screenInstance,
        ScreenMetadata metadata,
        ScreenProperties screenProperties) {

    /**
     * Retorna o nó raiz associado à tela.
     */
    public Region root() {
        return metadata.root();
    }
}
