// Gerencia criação de Regions/LayoutPanes do JavaFX
package com.connectasistemas.framework.utils;

import javafx.scene.layout.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RegionManager {
    // Registro de tipos suportados
    private static final Map<Class<?>, Supplier<Region>> registry = new HashMap<>();

    static {
        // Registro padrão
        registry.put(Pane.class, Pane::new);
        registry.put(AnchorPane.class, AnchorPane::new);
        registry.put(BorderPane.class, BorderPane::new);
        registry.put(FlowPane.class, FlowPane::new);
        registry.put(GridPane.class, GridPane::new);
        registry.put(HBox.class, HBox::new);
        registry.put(VBox.class, VBox::new);
        registry.put(StackPane.class, StackPane::new);
        registry.put(TilePane.class, TilePane::new);
        registry.put(ScrollPane.class, ScrollPane::new);
        registry.put(SplitPane.class, SplitPane::new);
        registry.put(TextFlow.class, TextFlow::new);
    }

    /**
     * Cria um Region do tipo recebido
     *
     * @param type Classe do layout/region
     * @return Instância do layout
     */
    public static Region createRegion(Class<?> type) {
        Supplier<Region> creator = registry.get(type);

        if (creator != null) {
            return creator.get();
        }

        throw new RuntimeException("Region inválida: " + type);
    }
}
