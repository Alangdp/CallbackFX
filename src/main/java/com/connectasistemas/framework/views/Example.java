package com.connectasistemas.framework.views;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.views.components.ProjectSummaryCard;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * View para seleção de projetos.
 * OBS: Usado para criar novos projetos ou abrir projetos existentes.
 * OBS: Página inicial do aplicativo.
 */
@Screen(title = "Project Manager", height = 530, width = 800, region = VBox.class, callbacks = ExampleCallbacks.class)
@ScreenProperties(resizable = false)
public class Example {

    // --------------------------------------------
    // ? Containers
    // --------------------------------------------

    @ScreenField(acronym = "topContainer", order = 1, position = Position.CENTER)
    @ScreenFieldSize(vgrow = true, hgrow = true)
    public HBox topContainer;

    @ScreenField(acronym = "bottomContainer", order = 2)
    @ScreenFieldSize(height = 55)
    public BorderPane bottomContainer;

    // --------------------------------------------
    // ? Container superior
    // ? Seleção de projeto
    // --------------------------------------------

    @ScreenField(acronym = "selectProjectContainer", father = "topContainer", order = 1)
    VBox selectProjectContainer;

    @ScreenField(acronym = "selectProjectList", father = "selectProjectContainer", order = 1)
    public ListView<String> selectProjectList;

    @ScreenField(acronym = "selectProjectFilter", father = "selectProjectContainer", order = 2)
    public TextField selectProjectFilter;

    // --------------------------------------------
    // ? Criação de projeto
    // --------------------------------------------

    @ScreenField(acronym = "createProjectContainer", father = "topContainer", position = Position.CENTER_LEFT, order = 2)
    @ScreenFieldSize(padding = { 10, 10, 10, 10 }, spacing = 10, hgrow = true)
    public VBox createProjectContainer;

    // Exemplo de componente composto via @Screen
    @ScreenField(acronym = "projectSummaryCard", father = "topContainer", position = Position.RIGHT, order = 3)
    @ScreenFieldSize(padding = { 10, 10, 10, 10 }, hgrow = true, vgrow = true)
    public ProjectSummaryCard projectSummaryCard;

    @ScreenField(acronym = "projectTabs", father = "createProjectContainer", order = 4)
    @ScreenFieldSize(vgrow = true, hgrow = true)
    public TabPane projectTabs;

    @ScreenField(acronym = "noPagesTab", father = "projectTabs", literal = "No pages created", order = 5)
    public Tab noPagesTab;

    @ScreenField(acronym = "createProjectPathLabel", father = "createProjectContainer", literal = "Project name", order = 1)
    public Label createProjectPathLabel;

    @ScreenField(acronym = "createProjectName", father = "createProjectContainer", literal = "Project name", order = 2)
    public TextField createProjectName;

    @ScreenField(acronym = "completeProjectContainerLabel", father = "createProjectContainer", literal = "Complete project path", order = 3)
    @ScreenFieldSize(spacing = 10)
    public HBox completeProjectContainerLabel;

    @ScreenField(acronym = "completeProjectPath", father = "completeProjectContainerLabel", order = 1)
    @ScreenProperties(enabled = false)
    @ScreenFieldSize(hgrow = true)
    public TextField completeProjectPath;

    @ScreenField(acronym = "browseProjectPathButton", father = "completeProjectContainerLabel", literal = "Browse", order = 2)
    public Button browseProjectPathButton;


    // --------------------------------------------
    // ? Conteúdo inferior
    // --------------------------------------------

    // Alinha o container na direita
    @ScreenField(acronym = "buttomButtonContainer", father = "bottomContainer", position = Position.RIGHT)
    @ScreenFieldSize(padding = { 10, 10, 10, 10 }, spacing = 4, height = 35)
    public HBox buttomButtonContainer;

    @ScreenField(acronym = "closeButton", father = "buttomButtonContainer", literal = "Close")
    @ScreenFieldSize(width = 80, height = 35)
    public Button closeButton;

    @ScreenField(acronym = "advanceButton", father = "buttomButtonContainer", literal = "Create")
    @ScreenFieldSize(width = 80, height = 35)
    public Button advanceButton;
}
