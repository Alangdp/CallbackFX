package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.controller.LoginController;
import com.connectasistemas.framework.enums.Position;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

@Screen(title = "Login", width = 460, height = 400, callbacks = LoginController.class, region = VBox.class)
public class LoginView {

    @ScreenField(acronym = "rootContainer")
    @ScreenFieldPosition(alignment = Position.CENTER)
    @ScreenFieldSize(maxWidth = true, padding = {50, 20, 20, 20}, spacing = 20)
    public VBox rootContainer;

    // Título da tela
    @ScreenField(acronym = "titleContainer", father = "rootContainer", order = 1)
    @ScreenFieldPosition(alignment = Position.CENTER)
    public VBox titleContainer;

    @ScreenField(acronym = "titleLabel", father = "titleContainer", literal = "Tela de login")
    public Label titleLabel;

    // Campos de dados
    @ScreenField(acronym = "dataContainer", father = "rootContainer", order = 2)
    @ScreenFieldSize(spacing = 5)
    public VBox dataContainer;

    // Campos de usuário
    @ScreenField(acronym = "usernameContainer", father = "dataContainer", order = 1)
    @ScreenFieldSize(spacing = 10)
    public VBox usernameContainer;

    @ScreenField(acronym = "usernameLabel", father = "usernameContainer", literal = "Usuário:", order = 1)
    public Label usernameLabel;

    @ScreenField(acronym = "usernameField", father = "usernameContainer", literal = "Usuário:", order = 2)
    @ScreenValidation(required = true, minLength = 3, maxLength = 40, allowWhitespace = false, allowSymbols = false)
    public TextField usernameField;

    // Campos de senha
    @ScreenField(acronym = "passwordContainer", father = "dataContainer", order = 2)
    @ScreenFieldSize(spacing = 10)
    public VBox passwordContainer;

    @ScreenField(acronym = "passwordLabel", father = "passwordContainer", literal = "Senha:", order = 1)
    public Label passwordLabel;

    @ScreenField(acronym = "passwordField", father = "passwordContainer", literal = "Senha:", order = 2)
    @ScreenValidation(required = true, minLength = 6, maxLength = 64, allowWhitespace = false)
    public PasswordField passwordField;

    @ScreenField(acronym = "loginButton", father = "rootContainer", literal = "Entrar", order = 3)
    @ScreenFieldSize(maxWidth = true)
    public Button loginButton;
}
