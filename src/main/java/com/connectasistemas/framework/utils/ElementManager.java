package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.utils.position.BorderPanePosition;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Gerencia a criação e modificação de elementos do tipo Node em tela
 */
public class ElementManager {

    // Registro de tipos suportados
    private static final Map<Class<?>, Supplier<Node>> registry = new HashMap<>();

    private static final BorderPanePosition borderPanePosition = new BorderPanePosition();

    static {
        // Registro padrão
        registry.put(TextField.class, TextField::new);
        registry.put(Label.class, Label::new);
        registry.put(CheckBox.class, CheckBox::new);
        registry.put(BorderPane.class, BorderPane::new);
    }

    /**
     * Cria um elemento vazio do tipo recebido
     *
     * @param type Classe do tipo que deseja criar o elemento
     * @return Node do tipo recebido
     */
    public static Node createElement(Class<?> type) {
        Supplier<Node> creator = registry.get(type);

        if (creator != null) {
            return creator.get();
        }

        throw new RuntimeException("Tipo inválido: " + type);
    }

    public static void addChild(Region region, Node child, Position position) {
        if (region.getClass() == Pane.class) {
            Pane pane = (Pane) region;
            pane.getChildren().add(child);
            return;
        }

        if (region.getClass() == HBox.class) {
            HBox hBox = (HBox) region;
            hBox.getChildren().add(child);
            return;
        }

        if (region.getClass() == VBox.class) {
            VBox vBox = (VBox) region;
            vBox.getChildren().add(child);
            return;
        }

        if (region.getClass() == BorderPane.class) {
            BorderPane borderPane = (BorderPane) region;
            borderPanePosition.apply(borderPane, child, position);
            return;
        }

        throw new RuntimeException("Tipo não permitido para adicionar elementos: " + region);
    }
}