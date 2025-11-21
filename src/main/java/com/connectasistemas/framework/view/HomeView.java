package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.controller.HomeController;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.utils.position.BorderPanePosition;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

@Screen(title = "Home", callbacks = HomeController.class)
public class HomeView {


    @ScreenField(acronym = "nome", father = "root", position = Position.CENTER)
    private TextField nome;

    @ScreenField(acronym = "nome1")
    private TextField endereco;

}
