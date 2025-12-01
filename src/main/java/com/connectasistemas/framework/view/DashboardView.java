package com.connectasistemas.framework.view;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.controller.DashboardController;
import com.connectasistemas.framework.enums.Position;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.connectasistemas.framework.models.Book;
import com.connectasistemas.framework.models.BorrowHistory;
import com.connectasistemas.framework.models.User;

/**
 * DashboardView representa a tela principal do sistema, exibindo informações administrativas e do usuário.
 * OBS: Se o usuário logado for um administrador, ele verá as seções administrativas.
 * Se for um usuário comum, ele verá suas informações de empréstimos.
 */
@Screen(title = "Dashboard", width = 1080, height = 960, callbacks = DashboardController.class, region = BorderPane.class)
public class DashboardView {

    @ScreenField(acronym = "contentContainer", order = 1, position = Position.CENTER)
    @ScreenFieldPosition(alignment = Position.LEFT)
    @ScreenFieldSize(maxWidth = true, padding = {35, 30, 20, 30}, spacing = 18)
    public HBox contentContainer;

    @ScreenField(acronym = "adminContent", father = "contentContainer", order = 1)
    @ScreenFieldSize(spacing = 16, hgrow = true, vgrow = true)
    public HBox adminContent;

    @ScreenField(acronym = "adminUsersBox", father = "adminContent", order = 1)
    @ScreenFieldSize(spacing = 8, hgrow = true, vgrow = true)
    public VBox adminUsersBox;

    @ScreenField(acronym = "adminUsersTitle", father = "adminUsersBox", order = 1, literal = "Usuários cadastrados")
    public Label adminUsersTitle;

    @ScreenField(acronym = "adminUsersTable", father = "adminUsersBox", order = 2)
    @ScreenFieldSize(hgrow = true, vgrow = true, maxWidth = true, maxHeight = true)
    public TableView<User> adminUsersTable;

    @ScreenField(acronym = "adminUsersButtons", father = "adminUsersBox", order = 3)
    @ScreenFieldSize(spacing = 8)
    public HBox adminUsersButtons;

    @ScreenField(acronym = "adminUserIncludeButton", father = "adminUsersButtons", order = 1, literal = "Incluir")
    @ScreenFieldSize(width = 100)
    public Button adminUserIncludeButton;

    @ScreenField(acronym = "adminUserUpdateButton", father = "adminUsersButtons", order = 2, literal = "Alterar")
    @ScreenFieldSize(width = 100)
    public Button adminUserUpdateButton;

    @ScreenField(acronym = "adminUserDeleteButton", father = "adminUsersButtons", order = 3, literal = "Excluir")
    @ScreenFieldSize(width = 100)
    public Button adminUserDeleteButton;

    @ScreenField(acronym = "adminUserConsultButton", father = "adminUsersButtons", order = 4, literal = "Consultar")
    @ScreenFieldSize(width = 100)
    public Button adminUserConsultButton;

    @ScreenField(acronym = "adminBooksBox", father = "adminContent", order = 2)
    @ScreenFieldSize(spacing = 8, hgrow = true, vgrow = true)
    public VBox adminBooksBox;

    @ScreenField(acronym = "adminBooksTitle", father = "adminBooksBox", order = 1, literal = "Livros cadastrados")
    public Label adminBooksTitle;

    @ScreenField(acronym = "adminBooksTable", father = "adminBooksBox", order = 2)
    @ScreenFieldSize(hgrow = true, vgrow = true, maxWidth = true, maxHeight = true)
    public TableView<Book> adminBooksTable;

    @ScreenField(acronym = "adminBooksButtons", father = "adminBooksBox", order = 3)
    @ScreenFieldSize(spacing = 8)
    public HBox adminBooksButtons;

    @ScreenField(acronym = "adminBookIncludeButton", father = "adminBooksButtons", order = 1, literal = "Incluir")
    @ScreenFieldSize(width = 100)
    public Button adminBookIncludeButton;

    @ScreenField(acronym = "adminBookUpdateButton", father = "adminBooksButtons", order = 2, literal = "Alterar")
    @ScreenFieldSize(width = 100)
    public Button adminBookUpdateButton;

    @ScreenField(acronym = "adminBookDeleteButton", father = "adminBooksButtons", order = 3, literal = "Excluir")
    @ScreenFieldSize(width = 100)
    public Button adminBookDeleteButton;

    @ScreenField(acronym = "adminBookConsultButton", father = "adminBooksButtons", order = 4, literal = "Consultar")
    @ScreenFieldSize(width = 100)
    public Button adminBookConsultButton;

    @ScreenField(acronym = "userContent", father = "contentContainer", order = 2)
    @ScreenFieldSize(spacing = 12, hgrow = true, vgrow = true)
    public VBox userContent;

    @ScreenField(acronym = "userSectionTitle", father = "userContent", order = 1, literal = "Meus empréstimos")
    public Label userSectionTitle;

    @ScreenField(acronym = "userBorrowSummary", father = "userContent", order = 2, literal = "")
    public Label userBorrowSummary;

    @ScreenField(acronym = "userBorrowTable", father = "userContent", order = 3)
    @ScreenFieldSize(hgrow = true, vgrow = true, maxWidth = true, maxHeight = true)
    public TableView<BorrowHistory> userBorrowTable;

    @ScreenField(acronym = "bottomContainer", order = 2, position = Position.BOTTOM)
    @ScreenFieldPosition(alignment = Position.BOTTOM)
    @ScreenFieldSize(maxWidth = true, height = 80, padding = {20, 30, 20, 30}, spacing = 10, vgrow = true)
    public BorderPane bottomContainer;

    @ScreenField(acronym = "adminSide", father = "bottomContainer", order = 2, position = Position.RIGHT)
    @ScreenFieldPosition(alignment = Position.CENTER_RIGHT)
    @ScreenFieldSize(spacing = 8)
    public HBox rightSide;

    @ScreenField(acronym = "usersButton", father = "adminSide", order = 1, literal = "Usuários")
    @ScreenFieldSize(width = 140)
    public Button usersButton;

    @ScreenField(acronym = "backButton", father = "adminSide", order = 3, literal = "Voltar")
    @ScreenFieldSize(width = 140)
    public Button backButton;

    @ScreenField(acronym = "booksButton", father = "adminSide", order = 2, literal = "Livros")
    @ScreenFieldSize(width = 140)
    public Button booksButton;

    @ScreenField(acronym = "userSide", father = "bottomContainer", order = 1, position = Position.LEFT)
    @ScreenFieldPosition(alignment = Position.CENTER_LEFT)
    @ScreenFieldSize(spacing = 8)
    public HBox leftSide;

    @ScreenField(acronym = "historyButton", father = "userSide", order = 1, literal = "Histórico")
    @ScreenFieldSize(width = 160)
    public Button historyButton;

    @ScreenField(acronym = "borrowedBooksButton", father = "userSide", order = 2, literal = "Livros retirados")
    @ScreenFieldSize(width = 160)
    public Button borrowedBooksButton;
}
