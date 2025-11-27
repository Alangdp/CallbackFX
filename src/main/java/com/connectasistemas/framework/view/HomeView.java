package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.controller.HomeController;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.enums.ValidationDataType;

import com.connectasistemas.framework.fxelements.TextEntryLabel;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Screen(title = "Manutenção de livros", width = 540, height = 480, callbacks = HomeController.class, region = BorderPane.class)
public class HomeView {
    
    @ScreenField(acronym = "contentContainer", order = 1, position = Position.CENTER)
    @ScreenFieldPosition(alignment = Position.LEFT)
    @ScreenFieldSize(maxWidth = true, padding = {35, 30, 20, 30}, spacing = 18)
    public HBox contentContainer;

    @ScreenField(acronym = "bottomContainer", order = 2, position = Position.BOTTOM)
    @ScreenFieldPosition(alignment = Position.BOTTOM)
    @ScreenFieldSize(maxWidth = true, height = 45, padding = {30, 30, 30, 30}, spacing = 10, vgrow = true)
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

    // -------------------------------
    // LADO ESQUERDO
    // -------------------------------

    @ScreenField(acronym = "bookCode", father = "leftVBox", literal = "Código", order = 1)
    @ScreenFieldSize(labelWidth = 60, maxWidth = true)
    @ScreenValidation(maxLength = 8, allowSymbols = false, allowLetters = false)
    public TextEntryLabel bookCode;

    @ScreenField(acronym = "bookTitle", father = "leftVBox", literal = "Nome", order = 2)
    @ScreenFieldSize(labelWidth = 60, maxWidth = true)
    @ScreenValidation(maxLength = 120)
    public TextEntryLabel bookTitle;

    @ScreenField(acronym = "bookAuthor", father = "leftVBox", literal = "Autor", order = 3)
    @ScreenFieldSize(labelWidth = 60, maxWidth = true)
    @ScreenValidation(maxLength = 80)
    public TextEntryLabel bookAuthor;

    @ScreenField(acronym = "bookPublisher", father = "leftVBox", literal = "Editora", order = 4)
    @ScreenFieldSize(labelWidth = 60, maxWidth = true)
    @ScreenValidation(maxLength = 80)
    public TextEntryLabel bookPublisher;

    @ScreenField(acronym = "bookYear", father = "leftVBox", literal = "Ano", order = 4)
    @ScreenFieldSize(labelWidth = 60, maxWidth = true)
    @ScreenValidation(dataType = ValidationDataType.INTEGER, maxLength = 4, minValue = 0, maxValue = 2100, allowLetters = false, allowSymbols = false)
    public TextEntryLabel bookYear;

    @ScreenField(acronym = "bookIsbn", father = "leftVBox", literal = "ISBN", order = 6)
    @ScreenFieldSize(labelWidth = 60, maxWidth = true)
    @ScreenValidation(maxLength = 17, allowLetters = false, allowSymbols = false)
    public TextEntryLabel bookIsbn;

    // -------------------------------
    // LADO DIREITO
    // -------------------------------

    // Capa (primeiro elemento)
    @ScreenField(acronym = "bookCover", father = "rightVBox", literal = "Capa", order = 1)
    @ScreenFieldSize(height = 220, width = 180)
    public ImageView bookCover;

    // Páginas
    @ScreenField(acronym = "bookPages", father = "rightVBox", literal = "Páginas", order = 2)
    @ScreenFieldSize(labelWidth = 65, maxWidth = true)
    @ScreenValidation(dataType = ValidationDataType.INTEGER, maxLength = 4, minValue = 1, maxValue = 10000, allowLetters = false, allowSymbols = false)
    public TextEntryLabel bookPages;

    // Caminho da capa (string)
    @ScreenField(acronym = "bookCoverPath", father = "rightVBox", literal = "Arquivo", order = 3)
    @ScreenFieldSize(labelWidth = 65, maxWidth = true)
    @ScreenValidation(maxLength = 255)
    public TextEntryLabel bookCoverPath;

    // Disponível (true/false)
    @ScreenField(acronym = "bookAvailable", father = "rightVBox", literal = "Disp.", order = 4)
    @ScreenFieldSize(labelWidth = 65, maxWidth = true)
    @ScreenValidation(maxLength = 5, allowSymbols = false)
    public TextEntryLabel bookAvailable;

    // Data de criação
    @ScreenField(acronym = "bookCreatedAt", father = "rightVBox", literal = "Criado", order = 5)
    @ScreenFieldSize(labelWidth = 65, maxWidth = true)
    @ScreenValidation(dataType = ValidationDataType.DATE, datePattern = "yyyy-MM-dd", minDate = "1900-01-01")
    public TextEntryLabel bookCreatedAt;
}
