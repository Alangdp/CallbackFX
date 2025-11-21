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
    public void apply(Node root, Node target, Position position) {
        // valida posição
        if (!validate(position)) {
            throw new RuntimeException(StringUtils.concat("Posição: ", position, " inválida para o elemento BorderPane"));
        }

        // valida tipo
        if (!(root instanceof BorderPane)) {
            throw new RuntimeException(StringUtils.concat("Usado rotina de BorderPanePosition passando um: ", root));
        }

        BorderPane borderPane = (BorderPane) root;

        switch (position) {
            case TOP:
                // já existe um elemento no TOP
                if (borderPane.getTop() != null) {
                    throw new RuntimeException(StringUtils.concat("Elemento já adicionado no topo: ", borderPane.getTop()));
                }
                borderPane.setTop(target);
                break;

            case BOTTOM:
                // já existe um elemento no BOTTOM
                if (borderPane.getBottom() != null) {
                    throw new RuntimeException(StringUtils.concat("Elemento já adicionado embaixo: ", borderPane.getBottom()));
                }
                borderPane.setBottom(target);
                break;

            case LEFT:
                // já existe um elemento no LEFT
                if (borderPane.getLeft() != null) {
                    throw new RuntimeException(StringUtils.concat("Elemento já adicionado à esquerda: ", borderPane.getLeft()));
                }
                borderPane.setLeft(target);
                break;

            case RIGHT:
                // já existe um elemento no RIGHT
                if (borderPane.getRight() != null) {
                    throw new RuntimeException(StringUtils.concat("Elemento já adicionado à direita: ", borderPane.getRight()));
                }
                borderPane.setRight(target);
                break;

            case CENTER:
                // já existe um elemento no CENTER
                if (borderPane.getCenter() != null) {
                    throw new RuntimeException(StringUtils.concat("Elemento já adicionado no centro: ", borderPane.getCenter()));
                }
                borderPane.setCenter(target);
                break;

            default:
                throw new RuntimeException(StringUtils.concat("Posição não suportada para BorderPane: ", position));
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