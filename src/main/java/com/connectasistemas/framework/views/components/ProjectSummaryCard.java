package com.connectasistemas.framework.views.components;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenProperties;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Cartão reutilizável que exibe um resumo simples de projeto utilizando o
 * pipeline de composição de @Screen para servir como componente embutido.
 */
@Screen(title = "Resumo do Projeto", width = 300, height = 180, region = VBox.class)
@ScreenProperties(focusTraversable = false)
public class ProjectSummaryCard extends VBox {

    @ScreenField(acronym = "cardContainer", order = 1)
    @ScreenFieldSize(padding = { 12, 12, 12, 12 }, spacing = 8, hgrow = true)
    public VBox cardContainer;

    @ScreenField(acronym = "cardTitle", father = "cardContainer", literal = "Projeto selecionado", order = 1)
    public Label cardTitle;

    @ScreenField(acronym = "projectNameLabel", father = "cardContainer", literal = "Nenhum projeto selecionado", order = 2)
    public Label projectNameLabel;

    @ScreenField(acronym = "openProjectButton", father = "cardContainer", literal = "Abrir projeto", order = 3)
    @ScreenFieldSize(width = 140, height = 30)
    public Button openProjectButton;
}
