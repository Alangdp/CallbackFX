package com.connectasistemas.framework.controller;


import com.connectasistemas.framework.dao.DaoFactory;
import com.connectasistemas.framework.dao.UserDao;
import com.connectasistemas.framework.models.User;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.PasswordAuthentication;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.utils.UserData;
import com.connectasistemas.framework.view.LoginView;

/**
 * Controller da janela de login
 */
public class LoginController {
    private UserDao userDao;
    private User maintenanceUser = new User();

    public void callbackConfigLoginView(LoginView screen) {
        userDao = DaoFactory.dao(UserDao.class);

        screen.studentCodeField.setText("");
        screen.passwordField.setText("");
        Status.clearError();
        Status.VALIDA = false;
    }

    public void callbackAltcamLoginButton(LoginView screen) {
        String studentId = StringUtils.trim(screen.studentCodeField.getText());
        String password = screen.passwordField.getText();

        if (StringUtils.isBlank(studentId)) {
            MessageUtil.warn("Login obrigatório", "Informe o usuário.");
            Status.markError(screen.studentCodeField);
            screen.studentCodeField.requestFocus();
            return;
        }

        if (StringUtils.isBlank(password)) {
            MessageUtil.warn("Senha obrigatória", "Informe a senha.");
            Status.markError(screen.passwordField);
            screen.passwordField.requestFocus();
            return;
        }

        maintenanceUser = userDao.findByStudentId(studentId);
        if (maintenanceUser == null) {
            MessageUtil.error("Erro de acesso", "Usuário ou senha incorretos.");
            Status.markError(screen.studentCodeField);
            screen.studentCodeField.requestFocus();
            return;
        }

        PasswordAuthentication pa = new PasswordAuthentication();
        boolean passwordMatch = pa.authenticate(password.toCharArray(), maintenanceUser.getPasswordHash());
        if (!passwordMatch) {
            MessageUtil.error("Erro de acesso", "Usuário ou senha incorretos.");
            Status.markError(screen.passwordField);
            screen.passwordField.requestFocus();
            return;
        }

        UserData.LOGGED_USER = maintenanceUser;

        MessageUtil.info("Login efetuado", StringUtils.concat("Bem-vindo, ", maintenanceUser.getName(), "!"));
        ScreenManager.goBack();
    }
}
