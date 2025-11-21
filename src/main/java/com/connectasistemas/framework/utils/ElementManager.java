package com.connectasistemas.framework.utils;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Gerencia a criação e modificação de elementos do tipo Node em tela
 */
public class ElementManager {

    // Registro de tipos suportados
    private static final Map<Class<?>, Supplier<Node>> registry = new HashMap<>();

    static {
        // Registro padrão
        registry.put(TextField.class, TextField::new);
        registry.put(Label.class, Label::new);
        registry.put(CheckBox.class, CheckBox::new);
        registry.put(BorderPane.class, BorderPane::new);
    }

    // Cria o Node baseado apenas no tipo do objeto recebido

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
}