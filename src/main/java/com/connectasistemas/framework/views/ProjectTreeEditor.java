package com.connectasistemas.framework.views;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.enums.Position;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Janela auxiliar para adicionar pastas na árvore da tela principal.
 */
@Screen(title = "Tree Editor", width = 360, height = 180, region = VBox.class, callbacks = ProjectTreeEditorCallbacks.class)
@ScreenProperties(resizable = false)
public class ProjectTreeEditor {

    @ScreenField(acronym = "root", order = 1, position = Position.CENTER)
    @ScreenFieldSize(padding = { 16, 16, 16, 16 }, spacing = 12)
    public VBox root;

    @ScreenField(acronym = "infoLabel", father = "root", literal = "Escolha o nome da pasta a ser criada", order = 1)
    public Label infoLabel;

    @ScreenField(acronym = "folderNameInput", father = "root", literal = "Nome da pasta", order = 2)
    @ScreenFieldSize(width = 280)
    public TextField folderNameInput;

    @ScreenField(acronym = "statusLabel", father = "root", literal = "", order = 3)
    public Label statusLabel;

    @ScreenField(acronym = "actions", father = "root", order = 4)
    @ScreenFieldSize(spacing = 8)
    public VBox actions;

    @ScreenField(acronym = "confirmButton", father = "actions", literal = "Adicionar", order = 1)
    @ScreenFieldSize(width = 120)
    public Button confirmButton;

    @ScreenField(acronym = "cancelButton", father = "actions", literal = "Cancelar", order = 2)
    @ScreenFieldSize(width = 120)
    public Button cancelButton;
}
