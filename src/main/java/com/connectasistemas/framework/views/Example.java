package com.connectasistemas.framework.views;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.enums.Position;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * View para seleção de projetos.
 * OBS: Usado para criar novos projetos ou abrir projetos existentes.
 * OBS: Página inicial do aplicativo.
 */
@Screen(title = "Project Manager", height = 600, width = 800, region = VBox.class)
public class Example {

    // --------------------------------------------
    // ? Containers
    // --------------------------------------------

    @ScreenField(acronym = "topContainer", order = 1, position = Position.CENTER)
    @ScreenFieldSize(vgrow = true)
    @ScreenProperties(resizable = false)
    public SplitPane topContainer;

    @ScreenField(acronym = "bottomContainer", order = 2)
    @ScreenFieldSize(height = 55)
    public BorderPane bottomContainer;

    // --------------------------------------------
    // ? Container superior
    // --------------------------------------------

    @ScreenField(acronym = "selectProjectContainer", father = "topContainer")
    public SplitPane selectProjectContainer;

    // --------------------------------------------
    // ? Seleção de projeto
    // --------------------------------------------

    @ScreenField(acronym = "selectProjectList", father = "selectProjectContainer", order = 1)
    public ListView<String> selectProjectList;

    // --------------------------------------------
    // ? Criação de projeto
    // --------------------------------------------

    @ScreenField(acronym = "createProjectContainer", father = "selectProjectContainer", position = Position.CENTER_LEFT, order = 2)
    @ScreenFieldSize(padding = { 10, 10, 10, 10 }, spacing = 10)
    public VBox createProjectContainer;

    @ScreenField(acronym = "createProjectPathLabel", father = "createProjectContainer", literal = "Project name", order = 1)
    public Label createProjectPathLabel;

    @ScreenField(acronym = "createProjectPathInput", father = "createProjectContainer", literal = "Project name", order = 2)
    public TextField createProjectPathInput;

    @ScreenField(acronym = "completeProjectContainerLabel", father = "createProjectContainer", literal = "Complete project path", order = 3)
    @ScreenFieldSize(spacing = 10)
    public HBox completeProjectContainerLabel;

    @ScreenField(acronym = "completeProjectPath", father = "completeProjectContainerLabel", order = 1)
    @ScreenProperties(enabled = false)
    public TextField completeProjectPath;

    @ScreenField(acronym = "browseProjectPath", father = "completeProjectContainerLabel", literal = "Browse", order = 2)
    public Button browsePathButton;


    // --------------------------------------------
    // ? Conteúdo inferior
    // --------------------------------------------

    // Alinha o container na direita
    @ScreenField(acronym = "buttomButtonContainer", father = "bottomContainer", position = Position.RIGHT)
    @ScreenFieldSize(padding = { 10, 10, 10, 10 })
    public HBox buttomButtonContainer;

    @ScreenField(acronym = "closeButton", father = "buttomButtonContainer", literal = "Close")
    @ScreenFieldSize(width = 80, height = 30)
    public Button closeButton;
}
