package com.connectasistemas.framework.utils.sizes;

import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.interfaces.SizeBinder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;

/**
 * Implementação genérica do SizeBinder
 * OBS: É usado para elementos mais génericos é que seguem padrões comuns
 */
public class SizeBinderGeneric implements SizeBinder {

    @Override
    public boolean applyAll(ScreenFieldSize s, Node node) {
        // Tenta aplicar o size para imageView
        // OBS: Caso não seja um imageView cai fora
        if (applyImageView(s, node)) return true;

        if (s == null || node == null) return false;

        // ----- Width / Height -----
        if (node instanceof Region r) {
            double width = s.width();
            double height = s.height();

            if (s.maxWidth()) {
                r.setMaxWidth(Double.MAX_VALUE);
            } else if (width > 0) {
                r.setMinWidth(width);
                r.setPrefWidth(width);
                r.setMaxWidth(width);
            }

            if (s.maxHeight()) {
                r.setMaxHeight(Double.MAX_VALUE);
            } else if (height > 0) {
                r.setMinHeight(height);
                r.setPrefHeight(height);
                r.setMaxHeight(height);
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
            if (node.getParent() instanceof HBox) HBox.setMargin(node, margin);
        }

        // ----- Grow -----
        if (s.vgrow() && node.getParent() instanceof VBox) {
            VBox.setVgrow(node, Priority.ALWAYS);
        }

        if (s.hgrow() && node.getParent() instanceof HBox) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        // ----- Spacing -----
        if (node instanceof Pane p) {
            double spacing = s.spacing();
            if (spacing > 0) {
                if (p instanceof VBox vbox) vbox.setSpacing(spacing);
                if (p instanceof HBox hbox) hbox.setSpacing(spacing);
            }
        }

        // ----- Largura label -----
        if (s.labelWidth() > 0) {
            if (node instanceof TextEntryLabel tl) {
                tl.getLabel().setMinWidth(s.labelWidth());
                tl.getLabel().setPrefWidth(s.labelWidth());
                tl.getLabel().setMaxWidth(s.labelWidth());
            } else if (node instanceof CheckEntryLabel cl) {
                cl.getLabel().setMinWidth(s.labelWidth());
                cl.getLabel().setPrefWidth(s.labelWidth());
                cl.getLabel().setMaxWidth(s.labelWidth());
            }
        }

        // ----- Altura label -----
        if (s.labelHeight() > 0) {
            if (node instanceof TextEntryLabel tl) {
                tl.getLabel().setMinHeight(s.labelHeight());
                tl.getLabel().setPrefHeight(s.labelHeight());
                tl.getLabel().setMaxHeight(s.labelHeight());
            } else if (node instanceof CheckEntryLabel cl) {
                cl.getLabel().setMinHeight(s.labelHeight());
                cl.getLabel().setPrefHeight(s.labelHeight());
                cl.getLabel().setMaxHeight(s.labelHeight());
            }
        }

        return true;
    }

    /**
     * Aplica o binder de eventos para ImageView
     */
    private boolean applyImageView(ScreenFieldSize s, Node node) {
        SizeBinderImageView sizeBinderImageView = new SizeBinderImageView();
        return sizeBinderImageView.applyAll(s, node);
    }
}
