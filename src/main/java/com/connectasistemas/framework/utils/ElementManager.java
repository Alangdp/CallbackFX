package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.utils.position.BorderPanePosition;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
    private static String literal = "";

    static {
        // Registro padrão
        registry.put(TextField.class, TextField::new);
        registry.put(Label.class, Label::new);
        registry.put(CheckBox.class, CheckBox::new);
        registry.put(PasswordField.class, PasswordField::new);
        registry.put(Button.class,  Button::new);

        // Registros personalizados
        registry.put(TextEntryLabel.class, () -> new TextEntryLabel(literal));

        registry.put(ImageView.class, () -> new ImageView("https://fastly.picsum.photos/id/335/200/300.jpg?hmac=G81PbTg8uAk00mCq0eZdiTJwpa_-_FvFZJVhEGcouXQ"));

        // Criação de Region
        registry.put(Region.class, Region::new);
        registry.put(BorderPane.class, BorderPane::new);
        registry.put(VBox.class, VBox::new);
        registry.put(HBox.class, HBox::new);
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
            Node node = creator.get();
            ElementManager.applyLiteral(node, ElementManager.literal);
            return node;
        }


        throw new RuntimeException("Tipo inválido: " + type);
    }

    public static void addChild(Region region, Node child, Position position) {
        if (region instanceof BorderPane borderPane) {
            borderPanePosition.apply(borderPane, child, position);
            return;
        }

        if (region instanceof Pane pane) {
            pane.getChildren().add(child);
            return;
        }

        throw new RuntimeException("Tipo não permitido para adicionar elementos: " + region);
    }

    public static void setLiteral(String literal) {
        ElementManager.literal = literal;
    }

    /**
     * Aplica o literal (texto) ao componente conforme seu tipo
     */
    private static void applyLiteral(Node node, String literal) {
        if (literal == null || literal.isEmpty()) return;

        if (node instanceof Labeled labeled) {
            labeled.setText(literal);
        } else if (node instanceof TextInputControl textInput) {
            textInput.setPromptText(literal);
        }

        ElementManager.literal = "";
    }
    
}