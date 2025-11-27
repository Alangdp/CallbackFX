package com.connectasistemas.framework.fxelements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TextEntryLabel extends HBox {

    private final Label label;
    private final TextField textField;

    public TextEntryLabel(String labelText) {
        this.label = new Label(labelText);
        this.textField = new TextField();

        VBox.setVgrow(label, Priority.NEVER);
        setAlignment(Pos.CENTER_LEFT);

        setSpacing(8);
        getChildren().addAll(label, textField);
    }

    // Getter simples
    public String getValue() {
        return textField.getText();
    }

    public void setValue(String value) {
        textField.setText(value);
    }

    public TextField getTextField() {
        return textField;
    }

    public Label getLabel() {
        return label;
    }
}
