package com.connectasistemas.framework.views;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.views.components.ProjectSummaryCard;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

    @ScreenField(acronym = "tabEventStatus", father = "createProjectContainer", literal = "Selecione uma aba para ver os callbacks", order = 5)
    public Label tabEventStatus;

    @ScreenField(acronym = "noPagesTab", father = "projectTabs", literal = "No pages created", order = 5)
    public Tab noPagesTab;

    @ScreenField(acronym = "addPageTab", father = "projectTabs", literal = "Nova página", order = 6)
    @ScreenProperties(visible = false)
    public Tab addPageTab;

    @ScreenField(acronym = "addPageTabContent", father = "addPageTab", literal = "Use esta aba para criar uma nova página", order = 1)
    public Label addPageTabContent;

    @ScreenField(acronym = "createProjectPathLabel", father = "createProjectContainer", literal = "Project name", order = 1)
    public Label createProjectPathLabel;

    @ScreenField(acronym = "createProjectName", father = "createProjectContainer", literal = "Project name", order = 2)
    @ScreenValidation(required = true, minLength = 3, maxLength = 50, showMessage = true, validateOn = "advanceButton")
    public TextField createProjectName;

    @ScreenField(acronym = "completeProjectContainerLabel", father = "createProjectContainer", literal = "Complete project path", order = 3)
    @ScreenFieldSize(spacing = 10)
    public HBox completeProjectContainerLabel;

    @ScreenField(acronym = "completeProjectPath", father = "completeProjectContainerLabel", order = 1)
    @ScreenProperties(enabled = false)
    @ScreenFieldSize(hgrow = true)
    @ScreenValidation(required = true, minLength = 5, maxLength = 120, showMessage = true, validateOn = "advanceButton")
    public TextField completeProjectPath;

    @ScreenField(acronym = "browseProjectPathButton", father = "completeProjectContainerLabel", literal = "Browse", order = 2)
    public Button browseProjectPathButton;


    // --------------------------------------------
    // ? Estrutura de projeto (TreeView)
    // --------------------------------------------

    @ScreenField(acronym = "projectStructureContainer", father = "createProjectContainer", order = 6)
    @ScreenFieldSize(spacing = 6, vgrow = true)
    public VBox projectStructureContainer;

    @ScreenField(acronym = "projectStructureTitle", father = "projectStructureContainer", literal = "Estrutura do projeto", order = 1)
    public Label projectStructureTitle;

    @ScreenField(acronym = "projectTreeStatus", father = "projectStructureContainer", literal = "Aguardando interação", order = 2)
    public Label projectTreeStatus;

    @ScreenField(acronym = "projectStructureTree", father = "projectStructureContainer", order = 3)
    @ScreenFieldSize(vgrow = true, hgrow = true)
    @ScreenProperties(showRoot = false, focusTraversable = true)
    public TreeView<String> projectStructureTree;

    @ScreenField(acronym = "projectTreeRoot", father = "projectStructureTree", literal = "Projeto CallbackFX", order = 1)
    @ScreenProperties(expanded = true)
    public TreeItem<String> projectTreeRoot;

    @ScreenField(acronym = "projectTreeFolderSrc", father = "projectTreeRoot", literal = "src", order = 1)
    @ScreenProperties(expanded = true)
    public TreeItem<String> projectTreeFolderSrc;

    @ScreenField(acronym = "projectTreeFolderMain", father = "projectTreeFolderSrc", literal = "main", order = 1)
    @ScreenProperties(expanded = true)
    public TreeItem<String> projectTreeFolderMain;

    @ScreenField(acronym = "projectTreeFolderJava", father = "projectTreeFolderMain", literal = "java", order = 1)
    public TreeItem<String> projectTreeFolderJava;

    @ScreenField(acronym = "projectTreeFolderResources", father = "projectTreeFolderMain", literal = "resources", order = 2)
    public TreeItem<String> projectTreeFolderResources;

    @ScreenField(acronym = "projectTreeFolderTest", father = "projectTreeRoot", literal = "test", order = 2)
    public TreeItem<String> projectTreeFolderTest;


    // --------------------------------------------
    // ? Conteúdo inferior
    // --------------------------------------------

    // Alinha o container na direita
    @ScreenField(acronym = "buttomButtonContainer", father = "bottomContainer", position = Position.RIGHT)
    @ScreenFieldSize(padding = { 10, 10, 10, 10 }, spacing = 4, height = 35)
    public HBox buttomButtonContainer;

    @ScreenField(acronym = "closeButton", father = "buttomButtonContainer", literal = "Close", order = 1)
    @ScreenFieldSize(width = 80, height = 35)
    public Button closeButton;

    @ScreenField(acronym = "advanceButton", father = "buttomButtonContainer", literal = "Create", order = 2)
    @ScreenFieldSize(width = 80, height = 35)
    public Button advanceButton;

    @ScreenField(acronym = "testChildCallbacksButton", father = "buttomButtonContainer", literal = "Test Child Events", order = 3)
    @ScreenFieldSize(width = 160, height = 35)
    public Button testChildCallbacksButton;

    @ScreenField(acronym = "showAddPageButton", father = "buttomButtonContainer", literal = "Mostrar aba extra", order = 4)
    @ScreenFieldSize(width = 160, height = 35)
    public Button showAddPageButton;

    @ScreenField(acronym = "openTreeEditorButton", father = "buttomButtonContainer", literal = "Gerenciar árvore", order = 5)
    @ScreenFieldSize(width = 160, height = 35)
    public Button openTreeEditorButton;
}
