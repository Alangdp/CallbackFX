package com.connectasistemas.framework.fxelements;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Componente que une um Label e um CheckBox
 * OBS: Padroniza a forma de trabalhar com um texto horinzontal a um checkbox
 * OBS: Eventos se aplicam ao HBox pai
 */
public class CheckEntryLabel extends HBox {

    private final Label label;
    private final CheckBox checkBox;

    public CheckEntryLabel(String labelText) {
        this.label = new Label(labelText);
        this.checkBox = new CheckBox();

        VBox.setVgrow(label, Priority.NEVER);
        setAlignment(Pos.CENTER_LEFT);

        setSpacing(8);
        getChildren().addAll(label, checkBox);
    }

    public String getValue() {
        return Boolean.toString(checkBox.isSelected());
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setValue(Boolean value) {
        checkBox.setSelected(Boolean.TRUE.equals(value));
    }

    public void setValue(boolean value) {
        checkBox.setSelected(value);
    }

    public void setValue(String value) {
        if (value == null || value.isBlank()) {
            checkBox.setSelected(false);
            return;
        }

        checkBox.setSelected(Boolean.parseBoolean(value));
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public Label getLabel() {
        return label;
    }
}
