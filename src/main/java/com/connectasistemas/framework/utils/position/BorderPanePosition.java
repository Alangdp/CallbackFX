package com.connectasistemas.framework.utils.position;

import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.interfaces.PositionElement;
import com.connectasistemas.framework.utils.StringUtils;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.util.EnumSet;

/**
 * Classe de posição especifíca
 */
public class BorderPanePosition implements PositionElement {

    // Lista de posições válidas
    private static final EnumSet<Position> VALID_POSITION =
            EnumSet.of(Position.TOP, Position.BOTTOM, Position.LEFT, Position.RIGHT, Position.CENTER);

    /**
     * Válida se a posição recebida é válida para o tipo de elemento
     * @param position Posição a ser validada
     */
    @Override
    public boolean validate(Position position) {
        return VALID_POSITION.contains(position);
    }

    /**
     * Aplica um elemento em uma posição
     * @param target Elemento a ser adicionado
     * @param position Posição a ser adicionada
     */
    @Override
    public void apply(Node target, Position position) {
        if (!validate(position)) {
            throw new RuntimeException(StringUtils.concat("Posição: ", position, " inválida para o elemento BorderPane"));
        }
    }

    /**
     * Função genérica para retornar a Classe do tipo do elemento (Fixo)
     */
    @Override
    public Class<?> getElementType() {
        return BorderPane.class;
    }
}