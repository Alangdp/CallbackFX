package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.controller.HomeController;
import com.connectasistemas.framework.enums.Position;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

// Tela de teste com todos os lados do BorderPane
@Screen(
        title = "Dummy BorderPane",
        callbacks = HomeController.class,
        region = VBox.class
)
public class HomeView {

    // BorderPane 1
    @ScreenField(acronym = "pane1")
    private BorderPane pane1;

    // BorderPane 2
    @ScreenField(acronym = "pane2")
    private BorderPane pane2;


    // ============================================================
    // PRIMEIRO BORERPANE (DENTRO DE UMA VBOX)
    // ============================================================

    // CENTER
    @ScreenField(acronym = "p1_center", father = "pane1", position = Position.CENTER)
    private TextField p1_center;

    // TOP
    @ScreenField(acronym = "p1_top", father = "pane1", position = Position.TOP)
    private TextField p1_top;

    // BOTTOM
    @ScreenField(acronym = "p1_bottom", father = "pane1", position = Position.BOTTOM)
    private TextField p1_bottom;

    // LEFT
    @ScreenField(acronym = "p1_left", father = "pane1", position = Position.LEFT)
    private TextField p1_left;

    // RIGHT
    @ScreenField(acronym = "p1_right", father = "pane1", position = Position.RIGHT)
    private TextField p1_right;


    // ============================================================
    // SEGUNDO BORERPANE (DENTRO DE UMA VBOX)
    // ============================================================

    // CENTER
    @ScreenField(acronym = "p2_center", father = "pane2", position = Position.CENTER)
    private TextField p2_center;

    // TOP
    @ScreenField(acronym = "p2_top", father = "pane2", position = Position.TOP)
    private TextField p2_top;

    // BOTTOM
    @ScreenField(acronym = "p2_bottom", father = "pane2", position = Position.BOTTOM)
    private TextField p2_bottom;

    // LEFT
    @ScreenField(acronym = "p2_left", father = "pane2", position = Position.LEFT)
    private TextField p2_left;

    // RIGHT
    @ScreenField(acronym = "p2_right", father = "pane2", position = Position.RIGHT)
    private TextField p2_right;
}

