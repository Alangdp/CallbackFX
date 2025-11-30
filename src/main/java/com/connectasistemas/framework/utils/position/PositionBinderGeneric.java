package com.connectasistemas.framework.utils.position;

import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.interfaces.PositionBinder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

/**
 * Implementação genérica do PositionBinder
 * OBS: é usado para elementos mais génericos é que seguem padrões comuns
 */
public class PositionBinderGeneric implements PositionBinder {
    @Override
    public void applyAll(ScreenFieldPosition p, Node node) {

        if (p == null || node == null) return;

        // Só funciona se o nó for um pane alinhável (VBox, HBox, etc.)
        if (node instanceof Pane pane) {

            Position align = p.alignment();

            switch (align) {
                case CENTER ->
                        setAlignment(pane, Pos.CENTER);
                case LEFT ->
                        setAlignment(pane, Pos.CENTER_LEFT);
                case RIGHT ->
                        setAlignment(pane, Pos.CENTER_RIGHT);
                case TOP ->
                        setAlignment(pane, Pos.TOP_CENTER);
                case BOTTOM ->
                        setAlignment(pane, Pos.BOTTOM_CENTER);
                default -> {}
            }
        }
    }

    private void setAlignment(Pane pane, Pos pos) {
        // VBox
        if (pane instanceof VBox v) {
            v.setAlignment(pos);
            return;
        }

        // HBox
        if (pane instanceof HBox h) {
            h.setAlignment(pos);
            return;
        }

        // StackPane centraliza sem esforço
        if (pane instanceof StackPane sp) {
            sp.setAlignment(pos);
            return;
        }

        // FlowPane
        if (pane instanceof FlowPane fp) {
            fp.setAlignment(pos);
            return;
        }

        // TilePane
        if (pane instanceof TilePane tp) {
            tp.setAlignment(pos);
            return;
        }
    }
}
