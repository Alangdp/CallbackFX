package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.controller.HomeController;
import javafx.scene.control.TextField;

@Screen(title = "Home", callbacks = HomeController.class)
public class HomeView {

    @ScreenField(acronym = "nome")
    private TextField nome;

    @ScreenField(acronym = "cend")
    private TextField endereco;

}
