package com.connectasistemas.framework.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.connectasistemas.framework.dao.BookDao;
import com.connectasistemas.framework.dao.BorrowHistoryDao;
import com.connectasistemas.framework.dao.DaoFactory;
import com.connectasistemas.framework.dao.UserDao;
import com.connectasistemas.framework.enums.WindowFunction;
import com.connectasistemas.framework.models.Book;
import com.connectasistemas.framework.models.BorrowHistory;
import com.connectasistemas.framework.models.User;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.utils.UserData;
import com.connectasistemas.framework.view.BookMaintenanceView;
import com.connectasistemas.framework.view.DashboardView;
import com.connectasistemas.framework.view.LoginView;
import com.connectasistemas.framework.view.UserMaintenanceView;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller da janela de dashboard
 */
public class DashboardController {

    private static final DateTimeFormatter DB_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_BORROW_DAYS = 14;

    private final Map<Integer, Book> bookCache = new HashMap<>();

    private UserDao userDao;
    private BookDao bookDao;
    private BorrowHistoryDao borrowHistoryDao;

    /**
     * Callback de config da janela
     */
    public void callbackConfigDashboardView(DashboardView screen) {
        // Se não há usuário logado, altera para tela de login
        if (UserData.LOGGED_USER == null) {
            ScreenManager.changeTo(LoginView.class);
            return;
        }

        ensureDaos();
        configurePlaceholders(screen);

        boolean admin = UserData.LOGGED_USER.isAdmin();

        ScreenManager.setNodeVisibility(screen.adminContent, admin);
        ScreenManager.setNodeVisibility(screen.userContent, !admin);

        ScreenManager.setNodeVisibility(screen.booksButton, false);
        ScreenManager.setNodeVisibility(screen.usersButton, false);
        ScreenManager.setNodeVisibility(screen.leftSide, !admin);
        ScreenManager.setNodeVisibility(screen.historyButton, false);
        ScreenManager.setNodeVisibility(screen.borrowedBooksButton, false);
        ScreenManager.setNodeVisibility(screen.backButton, true);

        if (admin) {
            setupAdminTables(screen);
            loadAdminData(screen);
            configureAdminActions(screen);
        } else {
            setupUserTable(screen);
            loadUserData(screen);
        }
    }

    public void callbackAltcamBackButton(DashboardView screen) {
        ScreenManager.goBack();
    }

    private void ensureDaos() {
        if (userDao == null) {
            userDao = DaoFactory.dao(UserDao.class);
        }
        if (bookDao == null) {
            bookDao = DaoFactory.dao(BookDao.class);
        }
        if (borrowHistoryDao == null) {
            borrowHistoryDao = DaoFactory.dao(BorrowHistoryDao.class);
        }
    }

    private void configurePlaceholders(DashboardView screen) {
        screen.adminUsersTable.setPlaceholder(new Label("Nenhum usuário encontrado."));
        screen.adminBooksTable.setPlaceholder(new Label("Nenhum livro cadastrado."));
        screen.userBorrowTable.setPlaceholder(new Label("Nenhum empréstimo registrado."));
    }

    private void setupAdminTables(DashboardView screen) {
        if (screen.adminUsersTable.getColumns().isEmpty()) {
            TableColumn<User, String> nameCol = new TableColumn<>("Nome");
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(nullSafe(data.getValue().getName())));

            TableColumn<User, String> studentIdCol = new TableColumn<>("Matrícula");
            studentIdCol.setCellValueFactory(data -> new SimpleStringProperty(nullSafe(data.getValue().getStudentId())));

            TableColumn<User, String> adminCol = new TableColumn<>("Administrador");
            adminCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isAdmin() ? "Sim" : "Não"));

            screen.adminUsersTable.getColumns().addAll(nameCol, studentIdCol, adminCol);
            screen.adminUsersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        if (screen.adminBooksTable.getColumns().isEmpty()) {
            TableColumn<Book, String> idCol = new TableColumn<>("Código");
            idCol.setCellValueFactory(data -> {
                Integer groupCode = data.getValue().getGroupCode();
                if (groupCode == null) {
                    groupCode = data.getValue().getId();
                }
                return new SimpleStringProperty(groupCode == null ? "" : groupCode.toString());
            });

            TableColumn<Book, String> titleCol = new TableColumn<>("Título");
            titleCol.setCellValueFactory(data -> new SimpleStringProperty(nullSafe(data.getValue().getTitle())));

            TableColumn<Book, String> availableCol = new TableColumn<>("Disponível");
            availableCol.setCellValueFactory(data -> new SimpleStringProperty(Boolean.TRUE.equals(data.getValue().getAvailable()) ? "Sim" : "Não"));

            screen.adminBooksTable.getColumns().addAll(idCol, titleCol, availableCol);
            screen.adminBooksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }

    private void loadAdminData(DashboardView screen) {
        List<User> users = userDao.findAll();
        if (users == null) {
            users = List.of();
        }
        screen.adminUsersTable.setItems(FXCollections.observableArrayList(users));

        List<Book> books = bookDao.findAll();
        if (books == null) {
            books = List.of();
        }

        Map<Integer, Book> grouped = new LinkedHashMap<>();
        for (Book book : books) {
            Integer groupCode = book.getGroupCode() != null ? book.getGroupCode() : book.getId();
            if (groupCode == null) {
                continue;
            }

            Book existing = grouped.get(groupCode);
            if (existing == null) {
                grouped.put(groupCode, book);
                continue;
            }

            int existingSeq = existing.getSequence() == null ? Integer.MAX_VALUE : existing.getSequence();
            int candidateSeq = book.getSequence() == null ? Integer.MAX_VALUE : book.getSequence();
            if (candidateSeq < existingSeq) {
                grouped.put(groupCode, book);
            }
        }

        List<Book> groupedBooks = new ArrayList<>(grouped.values());
        groupedBooks.sort(Comparator.comparing(book -> {
            Integer group = book.getGroupCode() != null ? book.getGroupCode() : book.getId();
            return group == null ? Integer.MAX_VALUE : group;
        }));

        screen.adminBooksTable.setItems(FXCollections.observableArrayList(groupedBooks));

        bookCache.clear();
        books.stream()
            .filter(book -> book.getGroupCode() != null)
            .forEach(book -> bookCache.put(book.getGroupCode(), book));
    }

    private void configureAdminActions(DashboardView screen) {
        screen.adminUserIncludeButton.setOnAction(event -> openUserMaintenance(WindowFunction.REGISTER, null));
        screen.adminUserUpdateButton.setOnAction(event -> openUserMaintenanceWithSelection(screen, WindowFunction.UPDATE));
        screen.adminUserDeleteButton.setOnAction(event -> openUserMaintenanceWithSelection(screen, WindowFunction.DELETE));
        screen.adminUserConsultButton.setOnAction(event -> openUserMaintenanceWithSelection(screen, WindowFunction.CONSULT));

        screen.adminBookIncludeButton.setOnAction(event -> openBookMaintenance(WindowFunction.REGISTER, null));
        screen.adminBookUpdateButton.setOnAction(event -> openBookMaintenanceWithSelection(screen, WindowFunction.UPDATE));
        screen.adminBookDeleteButton.setOnAction(event -> openBookMaintenanceWithSelection(screen, WindowFunction.DELETE));
        screen.adminBookConsultButton.setOnAction(event -> openBookMaintenanceWithSelection(screen, WindowFunction.CONSULT));
    }

    private void openUserMaintenanceWithSelection(DashboardView screen, WindowFunction function) {
        User selected = screen.adminUsersTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) {
            MessageUtil.warn("Seleção obrigatória", "Selecione um usuário para continuar.");
            return;
        }

        openUserMaintenance(function, selected.getId());
    }

    private void openUserMaintenance(WindowFunction function, Integer userId) {
        UserMaintenanceController.setFunction(function);
        UserMaintenanceController.setInitialUserId(userId);
        UserMaintenanceController.setReturnAfterAction(true);
        ScreenManager.changeTo(UserMaintenanceView.class);
    }

    private void openBookMaintenanceWithSelection(DashboardView screen, WindowFunction function) {
        Book selected = screen.adminBooksTable.getSelectionModel().getSelectedItem();
        Integer groupCode = selected == null ? null : selected.getGroupCode();
        if (selected == null || (groupCode == null && selected.getId() == null)) {
            MessageUtil.warn("Seleção obrigatória", "Selecione um livro para continuar.");
            return;
        }

        if (groupCode == null) {
            groupCode = selected.getId();
        }

        openBookMaintenance(function, groupCode);
    }

    private void openBookMaintenance(WindowFunction function, Integer bookId) {
        BookMaintenanceController.setFunction(function);
        BookMaintenanceController.setInitialBookId(bookId);
        BookMaintenanceController.setReturnAfterAction(true);
        ScreenManager.changeTo(BookMaintenanceView.class);
    }

    private void setupUserTable(DashboardView screen) {
        if (screen.userBorrowTable.getColumns().isEmpty()) {
            TableColumn<BorrowHistory, String> titleCol = new TableColumn<>("Livro");
            titleCol.setCellValueFactory(data -> new SimpleStringProperty(resolveBookTitle(data.getValue().getBookId())));

            TableColumn<BorrowHistory, String> borrowedCol = new TableColumn<>("Retirada");
            borrowedCol.setCellValueFactory(data -> new SimpleStringProperty(formatTimestamp(data.getValue().getBorrowedAt())));

            TableColumn<BorrowHistory, String> dueCol = new TableColumn<>("Previsto");
            dueCol.setCellValueFactory(data -> new SimpleStringProperty(formatTimestamp(data.getValue().getDueAt())));

            TableColumn<BorrowHistory, String> returnedCol = new TableColumn<>("Devolução");
            returnedCol.setCellValueFactory(data -> new SimpleStringProperty(formatTimestamp(data.getValue().getReturnedAt())));

            TableColumn<BorrowHistory, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(data -> new SimpleStringProperty(calculateStatus(data.getValue())));
            statusCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }

                    setText(item);
                    if ("Em atraso".equals(item)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            });

            screen.userBorrowTable.getColumns().addAll(titleCol, borrowedCol, dueCol, returnedCol, statusCol);
            screen.userBorrowTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }

    private void loadUserData(DashboardView screen) {
        User logged = UserData.LOGGED_USER;
        if (logged == null) {
            screen.userBorrowTable.setItems(FXCollections.emptyObservableList());
            screen.userBorrowSummary.setText("Nenhum usuário logado.");
            return;
        }

        String userId = logged.getId() == null ? null : logged.getId().toString();
        List<BorrowHistory> history = userId == null
            ? List.of()
            : borrowHistoryDao.findByUser(userId);
        if (history == null) {
            history = List.of();
        }

        bookCache.clear();
        ObservableList<BorrowHistory> items = FXCollections.observableArrayList(history);
        screen.userBorrowTable.setItems(items);

        long active = history.stream()
                .filter(entry -> entry.getReturnedAt() == null || StringUtils.isBlank(entry.getReturnedAt()))
                .count();
        long overdue = history.stream()
                .filter(entry -> "Em atraso".equals(calculateStatus(entry)))
                .count();

        screen.userBorrowSummary.setText(StringUtils.concat(
                "Empréstimos ativos: ", active,
                " | Em atraso: ", overdue));
    }

    private String resolveBookTitle(Integer bookId) {
        if (bookId == null) {
            return "";
        }

        Integer cacheKey = bookId;
        Book cached = bookCache.get(cacheKey);
        if (cached != null) {
            return nullSafe(cached.getTitle());
        }

        Book book = bookDao.find(bookId);
        if (book != null) {
            Integer key = book.getGroupCode() != null ? book.getGroupCode() : bookId;
            bookCache.put(key, book);
            if (!key.equals(cacheKey)) {
                bookCache.put(cacheKey, book);
            }
            return nullSafe(book.getTitle());
        }
        return StringUtils.concat("Livro ", bookId);
    }

    private String formatTimestamp(String timestamp) {
        if (StringUtils.isBlank(timestamp)) {
            return "-";
        }

        LocalDateTime parsed = parseTimestamp(timestamp);
        return parsed == null ? timestamp : DISPLAY_TIMESTAMP.format(parsed);
    }

    private String calculateStatus(BorrowHistory entry) {
        if (entry == null) {
            return "";
        }

        LocalDateTime dueDate = parseTimestamp(entry.getDueAt());
        LocalDateTime returnedDate = parseTimestamp(entry.getReturnedAt());

        if (returnedDate != null) {
            if (dueDate != null && returnedDate.isAfter(dueDate)) {
                return "Em atraso";
            }
            return "Devolvido";
        }

        LocalDateTime effectiveDue = dueDate;
        if (effectiveDue == null) {
            LocalDateTime borrowed = parseTimestamp(entry.getBorrowedAt());
            if (borrowed == null) {
                return "Em andamento";
            }
            effectiveDue = borrowed.plusDays(MAX_BORROW_DAYS);
        }

        return LocalDateTime.now().isAfter(effectiveDue) ? "Em atraso" : "Em andamento";
    }

    private LocalDateTime parseTimestamp(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim(), DB_TIMESTAMP);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
