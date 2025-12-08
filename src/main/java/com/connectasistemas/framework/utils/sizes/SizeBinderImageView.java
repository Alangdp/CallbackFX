package com.connectasistemas.framework.utils.sizes;

import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.interfaces.SizeBinder;
import com.connectasistemas.framework.utils.ScreenManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

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

        Stage stage = ScreenManager.getMainStage();
        double stageWidth = SizeBinderGeneric.stageDimension(stage, true);
        double stageHeight = SizeBinderGeneric.stageDimension(stage, false);
        Rectangle2D screenBounds = SizeBinderGeneric.determineScreenBounds(stage);

        // ----- Largura -----
        if (s.unlimitedWidth()) {
            // permite que o layout controle o crescimento da largura
            iv.setFitWidth(0);
        } else {
            double width = SizeBinderGeneric.resolveSize(s.width(), stageWidth, true, screenBounds);
            if (width > 0) {
                // aplica largura fixa
                iv.setFitWidth(width);
            }
        }

        // ----- Altura -----
        if (s.unlimitedHeight()) {
            // permite que o layout controle o crescimento da altura
            iv.setFitHeight(0);
        } else {
            double height = SizeBinderGeneric.resolveSize(s.height(), stageHeight, false, screenBounds);
            if (height > 0) {
                // aplica altura fixa
                iv.setFitHeight(height);
            }
        }

        // habilita preservação de proporção
        iv.setPreserveRatio(true);

        // melhora renderização
        iv.setSmooth(true);

        return false;
    }
}
