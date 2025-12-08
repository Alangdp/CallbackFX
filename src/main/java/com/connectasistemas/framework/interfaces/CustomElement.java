package com.connectasistemas.framework.interfaces;

import javafx.scene.layout.Region;

/**
 * Marca um componente customizado criado pelo usuário.
 * A classe que implementa esta interface deve estender {@link Region}
 * para que o framework consiga aplicar propriedades e posicionamento
 * como se fosse qualquer outro elemento padrão.
 */
public interface CustomElement {

    /**
     * Classe base que representa o tipo "oficial" do componente.
     * Ex.: uma barra customizada que estende {@code HBox} deve retornar {@code HBox.class}.
     */
    Class<? extends Region> getType();

    /**
     * Gancho simples para permitir configuração adicional após a criação do componente.
     * Chamado automaticamente pelo {@code ElementManager} logo depois da instanciação.
     */
    default void onElementCreated(Region instance) {
        // default no-op
    }
}
