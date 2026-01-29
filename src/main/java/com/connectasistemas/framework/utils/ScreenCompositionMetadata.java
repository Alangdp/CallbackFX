package com.connectasistemas.framework.utils;

import javafx.scene.layout.Region;

/**
 * Contrato minimo exposto para acompanhar telas em processo de composicao.
 * Mantem apenas os dados necessarios para aplicacao do menu superior sem
 * revelar a implementacao concreta de {@code ScreenMetadata} aos consumers.
 */
public interface ScreenCompositionMetadata {

    /**
     * Retorna o root construido para a tela em foco.
     *
     * @return instancia do {@link Region} principal
     */
    Region root();
}
