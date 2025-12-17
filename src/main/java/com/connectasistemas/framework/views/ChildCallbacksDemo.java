package com.connectasistemas.framework.views;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.fxelements.TextEntryLabel;

/**
 * Tela auxiliar para demonstrar callbacks em child windows.
 */
@Screen(title = "Child Callback Tester", width = 420, height = 520, region = BorderPane.class, callbacks = ChildCallbacksDemoCallbacks.class)
@ScreenProperties(resizable = false)
public class ChildCallbacksDemo {

    // --------------------------------------------
    // ? Conteúdo principal
    // --------------------------------------------

    @ScreenField(acronym = "formContainer", position = Position.CENTER, order = 1)
    @ScreenFieldSize(padding = { 16, 16, 16, 16 }, spacing = 12)
    public VBox formContainer;

    @ScreenField(acronym = "childNameInput", father = "formContainer", literal = "Nome da página", order = 1)
    public TextEntryLabel childNameInput;

    @ScreenField(acronym = "childContentInput", father = "formContainer", literal = "Conteúdo", order = 2)
    public TextEntryLabel childContentInput;

    @ScreenField(acronym = "childNotesInput", father = "formContainer", literal = "Notas", order = 3)
    public TextEntryLabel childNotesInput;

    @ScreenField(acronym = "statusLabel", father = "formContainer", order = 4)
    public Label statusLabel;

    // --------------------------------------------
    // ? Casos de validação
    // --------------------------------------------

    @ScreenField(acronym = "centerContainer", father = "formContainer", order = 5)
    @ScreenFieldSize(spacing = 8, vgrow = true)
    public VBox centerContainer;

    @ScreenField(acronym = "pageTitleInput", father = "centerContainer", literal = "Page Title", order = 1)
    @ScreenFieldSize(labelWidth = 80)
    @ScreenValidation(required = true, minLength = 3, maxLength = 50)
    public TextEntryLabel pageTitleInput;

    @ScreenField(acronym = "pageNameInput", father = "centerContainer", literal = "Page Name", order = 2)
    @ScreenFieldSize(labelWidth = 80)
    @ScreenValidation(required = true, minLength = 3, maxLength = 50)
    public TextEntryLabel pageNameInput;

    @ScreenField(acronym = "validationExamplesLabel", father = "centerContainer", literal = "Casos de exemplo", order = 3)
    public Label validationExamplesLabel;

    @ScreenField(acronym = "validationExamplesList", father = "centerContainer", order = 4)
    @ScreenFieldSize(height = 140, vgrow = true)
    public ListView<String> validationExamplesList;

    // --------------------------------------------
    // ? Barra inferior
    // --------------------------------------------

    @ScreenField(acronym = "actionsBar", position = Position.BOTTOM, order = 2)
    @ScreenFieldSize(padding = { 8, 16, 8, 16 }, spacing = 10)
    public HBox actionsBar;

    @ScreenField(acronym = "closeChildButton", father = "actionsBar", literal = "Fechar", order = 1)
    @ScreenFieldSize(width = 90, height = 32)
    public Button closeChildButton;

    @ScreenField(acronym = "confirmChildButton", father = "actionsBar", literal = "Confirmar", order = 2)
    @ScreenFieldSize(width = 110, height = 32)
    public Button confirmChildButton;

    @ScreenField(acronym = "goBackButton", father = "actionsBar", literal = "Voltar tela", order = 3)
    @ScreenFieldSize(width = 110, height = 32)
    public Button goBackButton;
}
