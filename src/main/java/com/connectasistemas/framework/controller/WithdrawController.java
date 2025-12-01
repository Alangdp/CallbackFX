package com.connectasistemas.framework.controller;

import com.connectasistemas.framework.dao.BookDao;
import com.connectasistemas.framework.dao.BorrowHistoryDao;
import com.connectasistemas.framework.dao.DaoFactory;
import com.connectasistemas.framework.models.Book;
import com.connectasistemas.framework.models.BorrowHistory;
import com.connectasistemas.framework.models.User;
import com.connectasistemas.framework.utils.BookCodeUtils;
import com.connectasistemas.framework.utils.BookCodeUtils.BookCodeParts;
import com.connectasistemas.framework.utils.DateTimeUtils;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.NumberUtils;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.utils.UserData;
import com.connectasistemas.framework.view.LoginView;
import com.connectasistemas.framework.view.WithdrawView;
import com.connectasistemas.framework.view.DashboardView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller da janela de retirada de livros
 */
public class WithdrawController {
    private static final int BORROW_PERIOD_DAYS = 14;
    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static String pendingBookCode;

    private BookDao bookDao;
    private BorrowHistoryDao borrowHistoryDao;

    public void callbackConfigWithdrawView(WithdrawView screen) {
        ensureDaos();

        screen.bookCode.getTextField().setPrefColumnCount(13);
        screen.bookCode.getTextField().requestFocus();

        if (!StringUtils.isBlank(pendingBookCode) && UserData.LOGGED_USER != null) {
            String code = pendingBookCode;
            pendingBookCode = null;
            screen.bookCode.setValue(code);
            handleBookCode(screen, code);
        } else {
            screen.bookCode.setValue("");
        }
    }

    public void callbackSaicamBookCode(WithdrawView screen) {
        if (!Status.VALIDA) {
            return;
        }

        String code = StringUtils.trim(screen.bookCode.getValue());
        if (StringUtils.isBlank(code)) {
            MessageUtil.warn("Código obrigatório", "Informe o código do livro.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        handleBookCode(screen, code);
    }

    public void callbackAltcamAccountButton(WithdrawView screen) {
        if (UserData.LOGGED_USER == null) {
            ScreenManager.changeTo(LoginView.class);
        } else {
            ScreenManager.changeTo(DashboardView.class);
        }
    }

    private void handleBookCode(WithdrawView screen, String code) {
        Book book = resolveBook(screen, code);
        if (book == null) {
            return;
        }

        BorrowHistory activeBorrow = borrowHistoryDao.findActiveBorrowByBook(book.getId());

        if (activeBorrow != null) {
            handleExistingBorrow(screen, code, book, activeBorrow);
            return;
        }

        if (UserData.LOGGED_USER == null) {
            pendingBookCode = code;
            MessageUtil.info("Autenticação necessária", "Faça login para registrar a retirada.");
            ScreenManager.changeTo(LoginView.class);
            return;
        }

        processBorrow(screen, book);
    }

    private Book resolveBook(WithdrawView screen, String code) {
        Book book = null;

        BookCodeParts parts = BookCodeUtils.split(code);
        if (parts != null) {
            book = bookDao.findByGroupAndSequence(parts.groupCode(), parts.sequence());
            if (book == null && parts.sequence() == 0) {
                book = bookDao.find(parts.groupCode());
            }
        }

        if (book == null) {
            int bookId = NumberUtils.toInt(code);
            book = bookId > 0 ? bookDao.find(bookId) : null;
        }

        if (book == null) {
            book = bookDao.findByIsbn(code);
        }

        if (book == null) {
            MessageUtil.warn("Livro não encontrado", "Não há registro para o código informado.");
            Status.markError(screen.bookCode.getTextField());
            return null;
        }

        if (book.getId() == null) {
            MessageUtil.error("Livro inválido", "O registro do livro está incompleto e não possui código interno.");
            return null;
        }

        return book;
    }

    private void processBorrow(WithdrawView screen, Book book) {
        User user = UserData.LOGGED_USER;
        if (user == null || user.getId() == null) {
            MessageUtil.error("Usuário inválido", "Não foi possível identificar o usuário logado.");
            return;
        }

        try {
            String timestamp = DateTimeUtils.currentTimestamp();
            String dueDate = DateTimeUtils.timestampAfterDays(BORROW_PERIOD_DAYS);
            borrowHistoryDao.insert(String.valueOf(user.getId()), String.valueOf(book.getId()), timestamp, dueDate, null);

            book.setAvailable(false);
            bookDao.update(book);

            String friendlyDue = formatDisplayTimestamp(dueDate);
            MessageUtil.info("Retirada registrada", StringUtils.concat(
                    "A retirada do livro foi registrada com sucesso. Devolução prevista até ",
                    friendlyDue,
                    "."));
            screen.bookCode.setValue("");
            screen.bookCode.getTextField().requestFocus();
        } catch (Exception ex) {
            MessageUtil.error("Erro", "Não foi possível registrar a retirada do livro.");
            ex.printStackTrace();
        }
    }

    private void handleExistingBorrow(WithdrawView screen, String code, Book book, BorrowHistory activeBorrow) {
        if (UserData.LOGGED_USER == null) {
            pendingBookCode = code;
            MessageUtil.info("Autenticação necessária", "Faça login para gerenciar o empréstimo deste livro.");
            ScreenManager.changeTo(LoginView.class);
            return;
        }

        Integer loggedUserId = UserData.LOGGED_USER.getId();
        if (loggedUserId != null && loggedUserId.equals(activeBorrow.getUserId())) {
            if (MessageUtil.confirm("Confirmar devolução", "Você retirou este livro. Deseja registrar a devolução?")) {
                processReturn(screen, book, activeBorrow);
            } else {
                screen.bookCode.getTextField().requestFocus();
            }
            return;
        }

        MessageUtil.warn("Livro indisponível", "O livro informado já está emprestado.");
        Status.markError(screen.bookCode.getTextField());
    }

    private void processReturn(WithdrawView screen, Book book, BorrowHistory activeBorrow) {
        try {
            String timestamp = DateTimeUtils.currentTimestamp();
            borrowHistoryDao.markReturned(activeBorrow.getId(), timestamp);

            book.setAvailable(true);
            bookDao.update(book);

            MessageUtil.info("Devolução registrada", "A devolução do livro foi registrada com sucesso.");
            screen.bookCode.setValue("");
            screen.bookCode.getTextField().requestFocus();
        } catch (Exception ex) {
            MessageUtil.error("Erro", "Não foi possível registrar a devolução do livro.");
            ex.printStackTrace();
        }
    }

    private String formatDisplayTimestamp(String timestamp) {
        if (StringUtils.isBlank(timestamp)) {
            return "-";
        }

        try {
            LocalDateTime parsed = LocalDateTime.parse(timestamp.trim(), DB_FORMAT);
            return DISPLAY_FORMAT.format(parsed);
        } catch (DateTimeParseException ex) {
            return timestamp;
        }
    }

    private void ensureDaos() {
        if (bookDao == null) {
            bookDao = DaoFactory.dao(BookDao.class);
        }
        if (borrowHistoryDao == null) {
            borrowHistoryDao = DaoFactory.dao(BorrowHistoryDao.class);
        }
    }
}
