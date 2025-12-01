package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.controller.WithdrawController;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.enums.ValidationDataType;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Tela de retirada rápida de livros, onde o usuário pode inserir o código do livro para efetuar a retirada.
 */
@Screen(title = "Retirada rápida", width = 420, height = 220, callbacks = WithdrawController.class, region = BorderPane.class)
public class WithdrawView {

	@ScreenField(acronym = "contentContainer", order = 1, position = Position.CENTER)
	@ScreenFieldPosition(alignment = Position.CENTER)
	@ScreenFieldSize(padding = {30, 30, 30, 30}, spacing = 18, maxWidth = true)
	public VBox contentContainer;

	@ScreenField(acronym = "bookCode", father = "contentContainer", literal = "Código do livro", order = 1)
	@ScreenFieldSize(labelWidth = 120, maxWidth = true)
	@ScreenValidation(maxLength = 14, minLength = 1, dataType = ValidationDataType.INTEGER, allowLetters = false, allowSymbols = false)
	public TextEntryLabel bookCode;

	@ScreenField(acronym = "bottomContainer", order = 2, position = Position.BOTTOM)
	@ScreenFieldPosition(alignment = Position.BOTTOM)
	@ScreenFieldSize(maxWidth = true, height = 60, padding = {10, 20, 10, 20})
	public BorderPane bottomContainer;

	@ScreenField(acronym = "bottomSpacer", father = "bottomContainer", position = Position.LEFT, order = 1)
	@ScreenFieldSize(hgrow = true)
	public Region bottomSpacer;

	@ScreenField(acronym = "accountButton", father = "bottomContainer", literal = "Conta", position = Position.RIGHT, order = 2)
	@ScreenFieldSize(width = 140)
	public Button accountButton;
}
