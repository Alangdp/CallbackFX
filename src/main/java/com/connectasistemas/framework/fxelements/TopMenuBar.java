package com.connectasistemas.framework.fxelements;

import com.connectasistemas.framework.interfaces.CustomElement;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Exemplo simples de elemento customizado.
 */
public class TopMenuBar extends HBox implements CustomElement {

    public TopMenuBar() {
        setSpacing(12);
        setPadding(new Insets(12, 16, 12, 16));
        getStyleClass().add("top-menu-bar");

        getChildren().addAll(
                createButton("Projects"),
                createButton("Templates"),
                createButton("Settings"));
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("top-menu-bar__button");
        return button;
    }

    @Override
    public Class<? extends Region> getType() {
        return HBox.class;
    }

    @Override
    public void onElementCreated(Region instance) {
        instance.setMinHeight(40);
    }
}
