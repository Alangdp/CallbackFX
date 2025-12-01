package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.controller.UserMaintenanceController;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.enums.ValidationDataType;
import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * É a tela de manutenção de usuários, utilizada para incluir ou alterar usuários no sistema.
 */
@Screen(title = "Manutenção de usuários", width = 520, height = 420, callbacks = UserMaintenanceController.class, region = BorderPane.class)
public class UserMaintenanceView {

    @ScreenField(acronym = "contentContainer", order = 1, position = Position.CENTER)
    @ScreenFieldPosition(alignment = Position.LEFT)
    @ScreenFieldSize(maxWidth = true, padding = {35, 30, 20, 30}, spacing = 18)
    public HBox contentContainer;

    @ScreenField(acronym = "bottomContainer", order = 2, position = Position.BOTTOM)
    @ScreenFieldPosition(alignment = Position.BOTTOM)
    @ScreenFieldSize(maxWidth = true, height = 45, padding = {40, 30, 30, 30}, spacing = 10, vgrow = true)
    public HBox bottomContainer;

    @ScreenField(acronym = "bottomSpacer", father = "bottomContainer", order = 1)
    @ScreenFieldSize(hgrow = true)
    public Region bottomSpacer;

    @ScreenField(acronym = "cancelButton", father = "bottomContainer", literal = "Cancelar", order = 2)
    @ScreenFieldSize(width = 110)
    public Button cancelButton;

    @ScreenField(acronym = "advanceButton", father = "bottomContainer", literal = "Avançar", order = 3)
    @ScreenFieldSize(width = 110)
    public Button advanceButton;

    @ScreenField(acronym = "leftVBox", father = "contentContainer")
    @ScreenFieldSize(spacing = 10, hgrow = true, vgrow = true)
    public VBox leftVBox;

    @ScreenField(acronym = "rightVBox", father = "contentContainer")
    @ScreenFieldPosition(alignment = Position.TOP_CENTER)
    @ScreenFieldSize(spacing = 10, hgrow = true, vgrow = true)
    public VBox rightVBox;

    @ScreenField(acronym = "userCode", father = "leftVBox", literal = "Código", order = 1)
    @ScreenFieldSize(labelWidth = 70, maxWidth = true)
    @ScreenValidation(maxLength = 4, allowSymbols = false, allowLetters = false)
    public TextEntryLabel userCode;

    @ScreenField(acronym = "userName", father = "leftVBox", literal = "Nome", order = 2)
    @ScreenFieldSize(labelWidth = 70, maxWidth = true)
    @ScreenValidation(maxLength = 120, minLength = 3)
    public TextEntryLabel userName;

    @ScreenField(acronym = "userStudentId", father = "leftVBox", literal = "Matrícula", order = 3)
    @ScreenFieldSize(labelWidth = 70, maxWidth = true)
    @ScreenValidation(maxLength = 30, minLength = 3)
    public TextEntryLabel userStudentId;

    @ScreenField(acronym = "userAge", father = "leftVBox", literal = "Idade", order = 4)
    @ScreenFieldSize(labelWidth = 70, maxWidth = true)
    @ScreenValidation(dataType = ValidationDataType.INTEGER, minValue = 1, maxValue = 120, maxLength = 3, allowLetters = false, allowSymbols = false)
    public TextEntryLabel userAge;

    @ScreenField(acronym = "userPassword", father = "rightVBox", literal = "Senha", order = 1)
    @ScreenFieldSize(labelWidth = 70, maxWidth = true)
    @ScreenValidation(maxLength = 32, minLength = 4)
    public TextEntryLabel userPassword;

    @ScreenField(acronym = "userConfirmPassword", father = "rightVBox", literal = "Confirmar", order = 2)
    @ScreenFieldSize(labelWidth = 70, maxWidth = true)
    @ScreenValidation(maxLength = 32, minLength = 4)
    public TextEntryLabel userConfirmPassword;

    @ScreenField(acronym = "userAdmin", father = "rightVBox", literal = "Administrador", order = 3)
    public CheckEntryLabel userAdmin;

    @ScreenField(acronym = "userCreatedAt", father = "rightVBox", literal = "Criado em", order = 4)
    @ScreenFieldSize(labelWidth = 80, maxWidth = true)
    public TextEntryLabel userCreatedAt;
}
