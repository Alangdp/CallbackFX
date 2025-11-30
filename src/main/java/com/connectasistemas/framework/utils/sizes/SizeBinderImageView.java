package com.connectasistemas.framework.utils.sizes;

import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.interfaces.SizeBinder;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

/**
 * SizeBinder específico para ImageView
 */
public class SizeBinderImageView implements SizeBinder {

    @Override
    public boolean applyAll(ScreenFieldSize s, Node node) {

        if (s == null || node == null)
            return false;
        if (!(node instanceof ImageView iv))
            return false;

        // ----- Largura -----
        double width = s.width();
        if (s.maxWidth()) {
            // permite que o layout controle o crescimento da largura
            iv.setFitWidth(0);
        } else if (width > 0) {
            // aplica largura fixa
            iv.setFitWidth(width);
        }

        // ----- Altura -----
        double height = s.height();
        if (s.maxHeight()) {
            // permite que o layout controle o crescimento da altura
            iv.setFitHeight(0);
        } else if (height > 0) {
            // aplica altura fixa
            iv.setFitHeight(height);
        }

        // habilita preservação de proporção
        iv.setPreserveRatio(true);

        // melhora renderização
        iv.setSmooth(true);

        return false;
    }
}
