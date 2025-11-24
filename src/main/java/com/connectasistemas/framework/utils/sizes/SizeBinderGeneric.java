package com.connectasistemas.framework.utils.sizes;

import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.interfaces.SizeBinder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;

/**
 * Implementação genérica do SizeBinder
 * OBS: é usado para elementos mais génericos é que seguem padrões comuns
 */
public class SizeBinderGeneric implements SizeBinder {

    @Override
    public void applyAll(ScreenFieldSize s, Node node) {

        if (s == null || node == null) return;

        // ----- Width / Height -----
        if (node instanceof Region r) {

            double width = s.width();
            double height = s.height();

            if (s.maxWidth()) {
                r.setMaxWidth(Double.MAX_VALUE);
            } else if (width > 0) {
                r.setMinWidth(width);
                r.setPrefWidth(width);
            }

            if (s.maxHeight()) {
                r.setMaxHeight(Double.MAX_VALUE);
            } else if (height > 0) {
                r.setMinHeight(height);
                r.setPrefHeight(height);
            }
        }

        // ----- Padding -----
        if (node instanceof Region r) {
            int[] p = s.padding();
            if (p.length == 4) {
                r.setPadding(new Insets(p[0], p[1], p[2], p[3]));
            }
        }

        // ----- Margin -----
        int[] m = s.margin();
        if (m.length == 4) {
            Insets margin = new Insets(m[0], m[1], m[2], m[3]);

            if (node.getParent() instanceof VBox) VBox.setMargin(node, margin);
            if (node.getParent() instanceof BorderPane) BorderPane.setMargin(node, margin);
            if (node.getParent() instanceof HBox) javafx.scene.layout.HBox.setMargin(node, margin);
        }

        // ----- Grow -----
        if (s.vgrow() && node.getParent() instanceof VBox) {
            VBox.setVgrow(node, Priority.ALWAYS);
        }

        // ----- Spacing -----
        if (node instanceof Pane p) {
            double spacing = s.spacing();
            if (spacing > 0) {
                if (p instanceof VBox vbox) vbox.setSpacing(spacing);
                if (p instanceof HBox hbox) hbox.setSpacing(spacing);
            }
        }
    }
}
