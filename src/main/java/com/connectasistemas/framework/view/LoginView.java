package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.controller.HomeController;
import com.connectasistemas.framework.controller.LoginController;
import com.connectasistemas.framework.enums.Position;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

@Screen(title = "Login", width = 460, height = 400, callbacks = LoginController.class, region = VBox.class)
public class LoginView {

    @ScreenField(acronym = "root_container")
    @ScreenFieldPosition(alignment = Position.CENTER)
    @ScreenFieldSize(maxWidth = true, padding = {50, 20, 20, 20}, spacing = 20)
    private VBox root_container;

    // Título da tela
    @ScreenField(acronym = "title_container", father = "root_container", order = 1)
    @ScreenFieldPosition(alignment = Position.CENTER)
    private VBox title_container;

    @ScreenField(acronym = "title_label", father = "title_container", literal = "Tela de login")
    private Label title_label;

    // Campos de dados
    @ScreenField(acronym = "data_container", father = "root_container", order = 2)
    @ScreenFieldSize(spacing = 5)
    private VBox data_container;

    // Campos de usuário
    @ScreenField(acronym = "username_container", father = "data_container", order = 1)
    @ScreenFieldSize(spacing = 10)
    private VBox username_container;

    @ScreenField(acronym = "username_label", father = "username_container", literal = "Usuário:", order = 1)
    private Label username_label;

    @ScreenField(acronym = "username_field", father = "username_container", literal = "Usuário:", order = 2)
    private TextField username_field;

    // Campos de senha
    @ScreenField(acronym = "password_container", father = "data_container", order = 2)
    @ScreenFieldSize(spacing = 10)
    private VBox password_container;

    @ScreenField(acronym = "password_label", father = "password_container", literal = "Senha:", order = 1)
    private Label password_label;

    @ScreenField(acronym = "password_field", father = "password_container", literal = "Senha:", order = 2)
    private PasswordField password_field;

    @ScreenField(acronym = "login_button", father = "root_container", literal = "Entrar", order = 3)
    @ScreenFieldSize(maxWidth = true)
    private Button login_button;
}
