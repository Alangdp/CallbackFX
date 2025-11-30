package com.connectasistemas.framework.controller;

import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.view.LoginView;

public class LoginController {

    public void callbackConfigLoginView(LoginView screen) {
        screen.usernameField.setText("");
        screen.passwordField.setText("");
        Status.clearError();
        Status.VALIDA = false;
    }

    public void callbackAltcamLoginButton(LoginView screen) {
        String username = StringUtils.trim(screen.usernameField.getText());
        String password = screen.passwordField.getText();

        if (StringUtils.isBlank(username)) {
            MessageUtil.warn("Login obrigatório", "Informe o usuário.");
            Status.markError(screen.usernameField);
            screen.usernameField.requestFocus();
            return;
        }

        if (StringUtils.isBlank(password)) {
            MessageUtil.warn("Senha obrigatória", "Informe a senha.");
            Status.markError(screen.passwordField);
            screen.passwordField.requestFocus();
            return;
        }

        MessageUtil.info("Login efetuado", StringUtils.concat("Bem-vindo, ", username, "!"));
        screen.passwordField.setText("");
    }
}
