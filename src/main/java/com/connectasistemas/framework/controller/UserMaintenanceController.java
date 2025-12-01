package com.connectasistemas.framework.controller;

import com.connectasistemas.framework.dao.BorrowHistoryDao;
import com.connectasistemas.framework.dao.DaoFactory;
import com.connectasistemas.framework.dao.UserDao;
import com.connectasistemas.framework.enums.AcceptMode;
import com.connectasistemas.framework.enums.WindowFunction;
import com.connectasistemas.framework.models.User;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.NumberUtils;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.utils.UserData;
import com.connectasistemas.framework.utils.PasswordAuthentication;
import com.connectasistemas.framework.view.UserMaintenanceView;

/**
 * Controller da janela de manutenção de usuários
 */
public class UserMaintenanceController {
    private static WindowFunction function;
    private static AcceptMode status;
    private static Integer initialUserId;
    private static boolean returnAfterAction;

    private UserDao userDao;
    private BorrowHistoryDao borrowHistoryDao;
    private final PasswordAuthentication passwordAuthentication = new PasswordAuthentication();
    private User maintenanceUser = new User();

    public static void setFunction(WindowFunction function) {
        UserMaintenanceController.function = function;

        switch (function) {
            case REGISTER -> UserMaintenanceController.status = AcceptMode.DATA;
            case UPDATE, CONSULT, DELETE -> UserMaintenanceController.status = AcceptMode.KEY;
            default -> UserMaintenanceController.status = AcceptMode.DATA;
        }
    }

    public static void setInitialUserId(Integer userId) {
        UserMaintenanceController.initialUserId = userId;
    }

    public static void setReturnAfterAction(boolean shouldReturn) {
        UserMaintenanceController.returnAfterAction = shouldReturn;
    }

    public void callbackConfigUserMaintenanceView(UserMaintenanceView screen) {
        userDao = DaoFactory.dao(UserDao.class);
        borrowHistoryDao = DaoFactory.dao(BorrowHistoryDao.class);

        switch (UserMaintenanceController.status) {
            case KEY -> acceptKey(screen);
            case DATA -> acceptData(screen);
        }

        switch (UserMaintenanceController.function) {
            case REGISTER -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Cadastro de usuário");
            case UPDATE -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Atualização de usuário");
            case DELETE -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Exclusão de usuário");
            case CONSULT -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Consulta de usuário");
        }

        screen.userCreatedAt.getTextField().setEditable(false);
        preloadUserIfNeeded(screen);
    }

    public void acceptKey(UserMaintenanceView screen) {
        ScreenManager.disableWindow(UserMaintenanceView.class);

        ScreenManager.enableNode(screen.advanceButton);
        ScreenManager.enableNode(screen.cancelButton);
        ScreenManager.enableNode(screen.userCode);
    }

    public void acceptData(UserMaintenanceView screen) {
        Integer nextId = userDao.nextId();
        if (nextId == null) {
            nextId = 1;
        }
        screen.userCode.setValue(String.valueOf(nextId));

        ScreenManager.disableNode(screen.userCode);
        screen.userCreatedAt.setValue(maintenanceUser.getCreatedAt());
    }

    private void preloadUserIfNeeded(UserMaintenanceView screen) {
        if (initialUserId == null) {
            return;
        }

        screen.userCode.setValue(initialUserId.toString());

        if (UserMaintenanceController.function != WindowFunction.REGISTER) {
            User user = userDao.find(String.valueOf(initialUserId));
            if (user == null) {
                MessageUtil.warn("Usuário não encontrado", "Não foi possível localizar o usuário informado.");
            } else {
                maintenanceUser = user;
                moveDad(screen);
                adjustAccessForFunction(screen);
            }
        }

        initialUserId = null;
    }

    private void adjustAccessForFunction(UserMaintenanceView screen) {
        screen.userCode.getTextField().setDisable(false);
        screen.userCode.getTextField().setEditable(false);

        switch (UserMaintenanceController.function) {
            case UPDATE -> {
                ScreenManager.enableAll(screen.contentContainer);
                ScreenManager.enableNode(screen.advanceButton);
                ScreenManager.enableNode(screen.cancelButton);
                screen.userCreatedAt.getTextField().setEditable(false);
            }
            case DELETE, CONSULT -> {
                ScreenManager.disableAll(screen.contentContainer);
                ScreenManager.enableNode(screen.userCode);
                ScreenManager.enableNode(screen.advanceButton);
                ScreenManager.enableNode(screen.cancelButton);
            }
            default -> {
            }
        }
    }

    public void callbackSaicamUserCode(UserMaintenanceView screen) {
        if (!Status.VALIDA) {
            return;
        }

        int userCode = NumberUtils.toInt(screen.userCode.getValue());
        if (userCode <= 0) {
            return;
        }

        User user = userDao.find(String.valueOf(userCode));
        if (user == null) {
            MessageUtil.warn("Atenção", "Usuário não encontrado.");
            Status.markError(screen.userCode.getTextField());
            return;
        }

        maintenanceUser = user;
        moveDad(screen);
    }

    public void callbackSaicamUserAge(UserMaintenanceView screen) {
        if (!Status.VALIDA) {
            return;
        }

        int age = NumberUtils.toInt(screen.userAge.getValue());
        if (age <= 0 || age > 120) {
            MessageUtil.warn("Idade inválida", "Informe uma idade entre 1 e 120.");
            screen.userAge.setValue("");
            Status.markError(screen.userAge.getTextField());
        }
    }

    public void callbackSaicamUserConfirmPassword(UserMaintenanceView screen) {
        if (!Status.VALIDA) {
            return;
        }

        String password = StringUtils.trim(screen.userPassword.getValue());
        String confirm = StringUtils.trim(screen.userConfirmPassword.getValue());

        if (StringUtils.isBlank(password) && StringUtils.isBlank(confirm)) {
            return;
        }

        if (!StringUtils.isBlank(password) && !password.equals(confirm)) {
            MessageUtil.warn("Senhas não conferem", "Repita a senha corretamente.");
            screen.userConfirmPassword.setValue("");
            Status.markError(screen.userConfirmPassword.getTextField());
        }
    }

    public void callbackAltcamAdvanceButton(UserMaintenanceView screen) {
        switch (UserMaintenanceController.function) {
            case REGISTER -> handleRegister(screen);
            case UPDATE -> handleUpdate(screen);
            case CONSULT -> handleConsult(screen);
            case DELETE -> handleDelete(screen);
        }
    }

    private void handleRegister(UserMaintenanceView screen) {
        if (!MessageUtil.confirm("Confirmação", "Deseja cadastrar o usuário informado?")) {
            return;
        }

        maintenanceUser = new User();
        screen.userCreatedAt.setValue(maintenanceUser.getCreatedAt());
        if (!collectFormData(screen, true)) {
            return;
        }

        if (studentIdInUse(null)) {
            MessageUtil.warn("Matrícula duplicada", "Já existe um usuário com esta matrícula.");
            Status.markError(screen.userStudentId.getTextField());
            return;
        }

        try {
            userDao.insert(maintenanceUser);
            MessageUtil.info("Sucesso", "Usuário cadastrado com sucesso.");
            resetScreen(screen);
        } catch (Exception e) {
            MessageUtil.error("Erro", "Não foi possível cadastrar o usuário.");
            e.printStackTrace();
        }
    }

    private void handleUpdate(UserMaintenanceView screen) {
        int userId = NumberUtils.toInt(screen.userCode.getValue());
        if (userId <= 0) {
            MessageUtil.warn("Código inválido", "Informe um código válido para atualizar.");
            Status.markError(screen.userCode.getTextField());
            return;
        }

        User persisted = userDao.find(String.valueOf(userId));
        if (persisted == null) {
            MessageUtil.warn("Usuário não encontrado", "Não foi possível localizar o usuário informado.");
            Status.markError(screen.userCode.getTextField());
            return;
        }

        maintenanceUser = persisted;

        if (!collectFormData(screen, false)) {
            return;
        }

        if (studentIdInUse(userId)) {
            MessageUtil.warn("Matrícula duplicada", "Já existe outro usuário com esta matrícula.");
            Status.markError(screen.userStudentId.getTextField());
            return;
        }

        if (!MessageUtil.confirm("Confirmação", "Deseja atualizar o usuário selecionado?")) {
            return;
        }

        try {
            maintenanceUser.setId(userId);
            userDao.update(maintenanceUser);
            MessageUtil.info("Sucesso", "Usuário atualizado com sucesso.");
            resetScreen(screen);
        } catch (Exception e) {
            MessageUtil.error("Erro", "Não foi possível atualizar o usuário.");
            e.printStackTrace();
        }
    }

    private void handleConsult(UserMaintenanceView screen) {
        int userId = NumberUtils.toInt(screen.userCode.getValue());
        if (userId <= 0) {
            MessageUtil.warn("Código inválido", "Informe um código válido para consultar.");
            Status.markError(screen.userCode.getTextField());
            return;
        }

        User persisted = userDao.find(String.valueOf(userId));
        if (persisted == null) {
            MessageUtil.warn("Usuário não encontrado", "Não foi possível localizar o usuário informado.");
            Status.markError(screen.userCode.getTextField());
            return;
        }

        maintenanceUser = persisted;
        moveDad(screen);
        ScreenManager.disableNode(screen.advanceButton);
    }

    private void handleDelete(UserMaintenanceView screen) {
        int userId = NumberUtils.toInt(screen.userCode.getValue());
        if (userId <= 0) {
            MessageUtil.warn("Código inválido", "Informe um código válido para excluir.");
            Status.markError(screen.userCode.getTextField());
            return;
        }

        User persisted = userDao.find(String.valueOf(userId));
        if (persisted == null) {
            MessageUtil.warn("Usuário não encontrado", "Não foi possível localizar o usuário informado.");
            Status.markError(screen.userCode.getTextField());
            return;
        }

        if (UserData.LOGGED_USER != null && persisted.getId() != null
                && persisted.getId().equals(UserData.LOGGED_USER.getId())) {
            MessageUtil.warn("Operação bloqueada", "Não é possível excluir o usuário atualmente autenticado.");
            return;
        }

        maintenanceUser = persisted;
        moveDad(screen);

        if (!MessageUtil.confirm("Confirmação", "Deseja excluir o usuário selecionado?")) {
            return;
        }

        try {
            borrowHistoryDao.deleteByUser(String.valueOf(userId));
            userDao.delete(String.valueOf(userId));
            MessageUtil.info("Sucesso", "Usuário excluído com sucesso.");
            resetScreen(screen);
        } catch (Exception e) {
            MessageUtil.error("Erro", "Não foi possível excluir o usuário.");
            e.printStackTrace();
        }
    }

    public void callbackAltcamCancelButton(UserMaintenanceView screen) {
        ScreenManager.goBack();
    }

    private boolean collectFormData(UserMaintenanceView screen, boolean requirePassword) {
        String name = StringUtils.trim(screen.userName.getValue());
        if (StringUtils.isBlank(name)) {
            MessageUtil.warn("Nome obrigatório", "Informe o nome do usuário.");
            Status.markError(screen.userName.getTextField());
            screen.userName.getTextField().requestFocus();
            return false;
        }

        String studentId = StringUtils.trim(screen.userStudentId.getValue());
        if (StringUtils.isBlank(studentId)) {
            MessageUtil.warn("Matrícula obrigatória", "Informe a matrícula do usuário.");
            Status.markError(screen.userStudentId.getTextField());
            screen.userStudentId.getTextField().requestFocus();
            return false;
        }

        int age = NumberUtils.toInt(screen.userAge.getValue());
        if (age <= 0 || age > 120) {
            MessageUtil.warn("Idade inválida", "Informe uma idade entre 1 e 120.");
            Status.markError(screen.userAge.getTextField());
            screen.userAge.getTextField().requestFocus();
            return false;
        }

        String password = StringUtils.trim(screen.userPassword.getValue());
        String confirm = StringUtils.trim(screen.userConfirmPassword.getValue());
        boolean passwordFilled = !StringUtils.isBlank(password);

        if (requirePassword && StringUtils.isBlank(password)) {
            MessageUtil.warn("Senha obrigatória", "Informe a senha do usuário.");
            Status.markError(screen.userPassword.getTextField());
            screen.userPassword.getTextField().requestFocus();
            return false;
        }

        if (!StringUtils.isBlank(password) && !password.equals(confirm)) {
            MessageUtil.warn("Senhas não conferem", "Repita a senha corretamente.");
            Status.markError(screen.userConfirmPassword.getTextField());
            screen.userConfirmPassword.getTextField().requestFocus();
            return false;
        }

        maintenanceUser.setName(name);
        maintenanceUser.setStudentId(studentId);
        maintenanceUser.setAge(age);
        maintenanceUser.setAdmin(screen.userAdmin.isSelected());
        maintenanceUser.setCreatedAt(screen.userCreatedAt.getValue());

        if (passwordFilled) {
            maintenanceUser.setPasswordHash(passwordAuthentication.hash(password.toCharArray()));
        }

        return true;
    }

    private boolean studentIdInUse(Integer currentId) {
        User existing = userDao.findByStudentId(maintenanceUser.getStudentId());
        if (existing == null) {
            return false;
        }

        if (currentId == null) {
            return true;
        }

        return existing.getId() != null && !existing.getId().equals(currentId);
    }

    private void resetScreen(UserMaintenanceView screen) {
        clearForm(screen);
        maintenanceUser = new User();
        Status.clearError();
        Status.clearExitReason();
        Status.CONFIRMED_SELECTION = false;

        if (UserMaintenanceController.status == AcceptMode.KEY) {
            screen.userCode.setValue("");
            acceptKey(screen);
        } else {
            acceptData(screen);
            ScreenManager.enableNode(screen.advanceButton);
            ScreenManager.enableNode(screen.cancelButton);
        }
    }

    private void clearForm(UserMaintenanceView screen) {
        screen.userName.setValue("");
        screen.userStudentId.setValue("");
        screen.userAge.setValue("");
        screen.userPassword.setValue("");
        screen.userConfirmPassword.setValue("");
        screen.userAdmin.setValue(false);
        screen.userCreatedAt.setValue("");

        screen.userCode.getTextField().setEditable(true);
        screen.userCode.getTextField().setDisable(false);
        screen.userCreatedAt.getTextField().setEditable(false);

        if (returnAfterAction) {
            returnAfterAction = false;
            ScreenManager.goBack();
        }
    }

    private void moveDad(UserMaintenanceView screen) {
        screen.userCode.setValue(maintenanceUser.getId() == null ? "" : maintenanceUser.getId().toString());
        screen.userName.setValue(maintenanceUser.getName());
        screen.userStudentId.setValue(maintenanceUser.getStudentId());
        screen.userAge.setValue(maintenanceUser.getAge() == null ? "" : maintenanceUser.getAge().toString());
        screen.userPassword.setValue("");
        screen.userConfirmPassword.setValue("");
        screen.userAdmin.setValue(maintenanceUser.isAdmin());
        screen.userCreatedAt.setValue(maintenanceUser.getCreatedAt());
    }
}
