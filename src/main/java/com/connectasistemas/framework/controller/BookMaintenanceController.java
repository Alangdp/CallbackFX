package com.connectasistemas.framework.controller;

import com.connectasistemas.framework.dao.BookDao;
import com.connectasistemas.framework.dao.BorrowHistoryDao;
import com.connectasistemas.framework.dao.DaoFactory;
import com.connectasistemas.framework.enums.AcceptMode;
import com.connectasistemas.framework.enums.WindowFunction;
import com.connectasistemas.framework.models.Book;
import com.connectasistemas.framework.utils.BookCodeUtils;
import com.connectasistemas.framework.utils.BookCodeUtils.BookCodeParts;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.NumberUtils;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.view.BookMaintenanceView;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller da janela de manutenção de livros
 * OBS: os métodos aqui seguem a seguinte tabela de nomenclatura para os
 * callbacks:
 * Prefixo do evento | Callback gerado
 * entcam | callbackEntcamNome
 * saicam | callbackSaicamNome
 * teclad | callbackTecladNome
 * altcam | callbackAltcamNome
 * valida | callbackValidaNome
 * prebrw | callbackPrebrwNome
 * posbrw | callbackPosbrwNome
 * outace | callbackOutaceNome
 * ceplgr | callbackCeplgrNome
 * <p>
 * Os paragrafos geram o nome:
 * <p>
 * callback<Prefixo do evento><Nome da variável>
 */
public class BookMaintenanceController {
    private static WindowFunction function;
    private static AcceptMode status;
    private static Integer initialBookId;
    private static boolean returnAfterAction;

    private BookDao bookDao;
    private BorrowHistoryDao borrowHistoryDao;
    private Book maintenanceBook = new Book();

    /**
     * Seta o tipo de função da janela
     */
    public static void setFunction(WindowFunction function) {
        BookMaintenanceController.function = function;

        switch (function) {
            case REGISTER -> BookMaintenanceController.status = AcceptMode.DATA;
            case UPDATE, CONSULT, DELETE -> BookMaintenanceController.status = AcceptMode.KEY;
            default -> BookMaintenanceController.status = AcceptMode.DATA;
        }
    }

    public static void setInitialBookId(Integer bookId) {
        BookMaintenanceController.initialBookId = bookId;
    }

    public static void setReturnAfterAction(boolean shouldReturn) {
        BookMaintenanceController.returnAfterAction = shouldReturn;
    }

    /**
     * Callback de config da janela
     */
    public void callbackConfigBookMaintenanceView(BookMaintenanceView screen) {
        // Carrega o DAO de livros
        bookDao = DaoFactory.dao(BookDao.class);
        borrowHistoryDao = DaoFactory.dao(BorrowHistoryDao.class);

        switch (BookMaintenanceController.status) {
            case KEY -> acceptKey(screen);
            case DATA -> acceptData(screen);
        }

        switch (BookMaintenanceController.function) {
            case REGISTER -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Cadastro de livro");
            case UPDATE -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Atualização de livro");
            case DELETE -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Exclusão de livro");
            case CONSULT -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - Consulta de livro");
        }

        URL imageCover = getClass().getResource("/com/connectasistemas/framework/covers/empty_cover.png");
        screen.bookCover.setImage(new Image(imageCover.toExternalForm()));

        preloadBookIfNeeded(screen);
    }

    public void acceptKey(BookMaintenanceView screen) {
        // Desabilita todos os elementos da tela
        ScreenManager.disableWindow(BookMaintenanceView.class);

        // Habilita os botões de avançar e cancelar
        ScreenManager.enableNode(screen.advanceButton);
        ScreenManager.enableNode(screen.cancelButton);

        // Habilita os campos de chave
        ScreenManager.enableNode(screen.bookCode);
        screen.bookQuantity.setValue("");
    }

    public void acceptData(BookMaintenanceView screen) {
        // Carrega o próximo código disponivél no banco de dados
        int nextId = bookDao.nextId();
        screen.bookCode.setValue(String.valueOf(nextId));
        screen.bookQuantity.setValue("1");

        ScreenManager.disableNode(screen.bookCode);
    }

    private void preloadBookIfNeeded(BookMaintenanceView screen) {
        if (initialBookId == null) {
            return;
        }

        screen.bookCode.setValue(initialBookId.toString());

        if (BookMaintenanceController.function != WindowFunction.REGISTER) {
            Book book = bookDao.find(initialBookId);
            if (book == null) {
                MessageUtil.warn("Livro não encontrado", "Não foi possível localizar o livro informado.");
            } else {
                maintenanceBook = book;
                moveDad(screen);
                adjustAccessForFunction(screen);
            }
        }

        initialBookId = null;
    }

    private void adjustAccessForFunction(BookMaintenanceView screen) {
        screen.bookCode.getTextField().setDisable(false);
        screen.bookCode.getTextField().setEditable(false);

        switch (BookMaintenanceController.function) {
            case UPDATE -> {
                ScreenManager.enableAll(screen.contentContainer);
                ScreenManager.enableAll(screen.rightVBox);
                ScreenManager.enableNode(screen.advanceButton);
                ScreenManager.enableNode(screen.cancelButton);
            }
            case DELETE, CONSULT -> {
                ScreenManager.disableAll(screen.contentContainer);
                ScreenManager.disableAll(screen.rightVBox);
                ScreenManager.enableNode(screen.bookCode);
                ScreenManager.enableNode(screen.advanceButton);
                ScreenManager.enableNode(screen.cancelButton);
            }
            default -> {
            }
        }
    }

    public void callbackSaicamBookCode(BookMaintenanceView screen) {
        if (!Status.VALIDA) {
            return;
        }

        String rawCode = screen.bookCode.getValue();
            BookCodeParts parts = BookCodeUtils.split(rawCode);

        if (parts == null || parts.groupCode() <= 0) {
            MessageUtil.warn("Atenção", "Código inválido.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

            Book baseBook = bookDao.findByGroupAndSequence(parts.groupCode(), parts.sequence());
            if (baseBook == null && parts.sequence() == 0) {
                baseBook = bookDao.find(parts.groupCode());
            }

        if (baseBook == null) {
            MessageUtil.warn("Atenção", "Livro não encontrado.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        boolean needsPersistence = false;
        if (baseBook.getGroupCode() == null) {
            baseBook.setGroupCode(parts.groupCode());
            needsPersistence = true;
        }
        if (baseBook.getSequence() == null) {
            baseBook.setSequence(parts.sequence());
            needsPersistence = true;
            } else if (parts.sequence() == 0 && baseBook.getSequence() != 0) {
                baseBook.setSequence(0);
                needsPersistence = true;
        }
        if (needsPersistence) {
            bookDao.update(baseBook);
        }

        maintenanceBook = baseBook;
        moveDad(screen);
    }

    /**
     * Saída do campo Ano
     */
    public void callbackSaicamBookYear(BookMaintenanceView screen) {
        String val = screen.bookYear.getValue();
        if (!Status.VALIDA) {
            return;
        }

        int year = NumberUtils.toInt(val);

        if (val == null || val.isEmpty()) {
            MessageUtil.warn("Ano obrigatório", "Preencha o ano.");
            screen.bookYear.setValue("");
            Status.markError(screen.bookYear.getTextField());
            return;
        }

        if (year == 0 && !val.equals("0")) {
            MessageUtil.warn("Ano inválido", "O ano deve ser numérico.");
            screen.bookYear.setValue("");
            Status.markError(screen.bookYear.getTextField());
            return;
        }

        if (year < 0 || year > 2100) {
            MessageUtil.warn("Ano inválido", "O ano deve estar entre 0 e 2100.");
            screen.bookYear.setValue("");
            Status.markError(screen.bookYear.getTextField());
        }
    }

    /**
     * Saída do campo Páginas
     */
    public void callbackSaicamBookPages(BookMaintenanceView screen) {
        String val = screen.bookPages.getValue();
        if (!Status.VALIDA) {
            return;
        }

        int pages = NumberUtils.toInt(val);

        if (val == null || val.isEmpty()) {
            MessageUtil.warn("Páginas obrigatórias", "Preencha a quantidade de páginas.");
            screen.bookPages.setValue("");
            Status.markError(screen.bookPages.getTextField());
            return;
        }

        if (pages == 0) {
            MessageUtil.warn("Páginas inválidas", "Digite apenas números.");
            screen.bookPages.setValue("");
            Status.markError(screen.bookPages.getTextField());
            return;
        }

        if (pages < 1 || pages > 10000) {
            MessageUtil.warn("Páginas inválidas", "O valor deve estar entre 1 e 10000.");
            screen.bookPages.setValue("");
            Status.markError(screen.bookPages.getTextField());
        }
    }

    /**
     * Saída do campo ISBN
     */
    public void callbackSaicamBookIsbn(BookMaintenanceView screen) {
        String val = screen.bookIsbn.getValue();

        if (!Status.VALIDA) {
            return;
        }

        if (val == null || val.isEmpty()) {
            MessageUtil.warn("ISBN obrigatório", "Preencha o ISBN.");
            screen.bookIsbn.setValue("");
            Status.markError(screen.bookIsbn.getTextField());
            return;
        }

        if (!val.matches("\\d+")) {
            MessageUtil.warn("ISBN inválido", "Use apenas números no ISBN.");
            screen.bookIsbn.setValue("");
            Status.markError(screen.bookIsbn.getTextField());
        }
    }

    public void moveDad(BookMaintenanceView screen) {
        Integer groupCode = maintenanceBook.getGroupCode() != null
                ? maintenanceBook.getGroupCode()
                : maintenanceBook.getId();
        screen.bookCode.setValue(groupCode == null ? "" : groupCode.toString());

        if (groupCode != null) {
            int quantity = bookDao.countByGroupCode(groupCode);
                if (quantity <= 0) {
                    quantity = 1;
                }
                screen.bookQuantity.setValue(String.valueOf(quantity));
        } else {
            screen.bookQuantity.setValue("1");
        }

        // Título
        screen.bookTitle.setValue(maintenanceBook.getTitle());
        // Autor
        screen.bookAuthor.setValue(maintenanceBook.getAuthor());
        // Editora
        screen.bookPublisher.setValue(maintenanceBook.getPublisher());
        // Ano
        screen.bookYear.setValue(
                maintenanceBook.getYear() == null ? "" : maintenanceBook.getYear().toString());
        // ISBN
        screen.bookIsbn.setValue(maintenanceBook.getIsbn());
        // Páginas
        screen.bookPages.setValue(
                maintenanceBook.getPages() == null ? "" : maintenanceBook.getPages().toString());
        // Caminho da capa
        screen.bookCoverPath.setValue(maintenanceBook.getCoverPath());
        // Disponível
        screen.bookAvailable.setValue(
                maintenanceBook.getAvailable() == null ? "" : maintenanceBook.getAvailable().toString());
        // Capa (se existir caminho)
        screen.bookCover.setImage(null);
        if (maintenanceBook.getCoverPath() != null && !maintenanceBook.getCoverPath().isEmpty()) {
            URL imageUrl = getClass().getResource("/com/connectasistemas/framework/" + maintenanceBook.getCoverPath());
            if (imageUrl != null) {
                screen.bookCover.setImage(
                        new Image(imageUrl.toExternalForm()));
            } else {
                URL imageCover = getClass().getResource("/com/connectasistemas/framework/covers/empty_cover.png");
                screen.bookCover.setImage(new Image(imageCover.toExternalForm()));
            }
        }
    }

    // Move da tela para o objeto maintenanceBook
    public void retDad(BookMaintenanceView screen) {
        // Título
        maintenanceBook.setTitle(screen.bookTitle.getValue());
        // Autor
        maintenanceBook.setAuthor(screen.bookAuthor.getValue());
        // Editora
        maintenanceBook.setPublisher(screen.bookPublisher.getValue());
        // Ano
        maintenanceBook.setYear(
                screen.bookYear.getValue() == null || screen.bookYear.getValue().isEmpty()
                        ? null
                        : Integer.valueOf(screen.bookYear.getValue()));
        // ISBN
        maintenanceBook.setIsbn(screen.bookIsbn.getValue());
        // Páginas
        maintenanceBook.setPages(
                screen.bookPages.getValue() == null || screen.bookPages.getValue().isEmpty()
                        ? null
                        : Integer.valueOf(screen.bookPages.getValue()));
        // Caminho da capa
        maintenanceBook.setCoverPath(screen.bookCoverPath.getValue());
        // Disponível
        maintenanceBook.setAvailable(screen.bookAvailable.isSelected());
    }

    private int parseQuantity(BookMaintenanceView screen) {
        String quantityValue = screen.bookQuantity.getValue();
        int quantity = NumberUtils.toInt(quantityValue);
        if (quantity <= 0) {
            return 1;
        }
        return Math.min(quantity, 999);
    }

    private Book cloneFromTemplate(Book template, int groupCode, int sequence) {
        Book copy = new Book();
        copy.setGroupCode(groupCode);
        copy.setSequence(sequence);
        copy.setTitle(template.getTitle());
        copy.setAuthor(template.getAuthor());
        copy.setPublisher(template.getPublisher());
        copy.setYear(template.getYear());
        copy.setIsbn(template.getIsbn());
        copy.setPages(template.getPages());
        copy.setCoverPath(template.getCoverPath());
        Boolean available = template.getAvailable();
        copy.setAvailable(available == null ? Boolean.TRUE : available);
        return copy;
    }

    private void copyMetadata(Book source, Book target, boolean keepAvailability) {
        target.setTitle(source.getTitle());
        target.setAuthor(source.getAuthor());
        target.setPublisher(source.getPublisher());
        target.setYear(source.getYear());
        target.setIsbn(source.getIsbn());
        target.setPages(source.getPages());
        target.setCoverPath(source.getCoverPath());
        if (!keepAvailability) {
            target.setAvailable(source.getAvailable());
        }
    }

    public void callbackAltcamAdvanceButton(BookMaintenanceView screen) {
        switch (BookMaintenanceController.function) {
            case REGISTER -> handleRegister(screen);
            case UPDATE -> handleUpdate(screen);
            case CONSULT -> handleConsult(screen);
            case DELETE -> handleDelete(screen);
        }
    }

    private void handleRegister(BookMaintenanceView screen) {
        if (!MessageUtil.confirm("Confirmação", "Deseja cadastrar o livro informado?")) {
            return;
        }

        try {
            retDad(screen);
            int quantity = parseQuantity(screen);
            int groupCode = bookDao.nextId();
            boolean available = maintenanceBook.getAvailable() == null || maintenanceBook.getAvailable();

            for (int sequence = 0; sequence < quantity; sequence++) {
                Book copy = cloneFromTemplate(maintenanceBook, groupCode, sequence);
                copy.setAvailable(available);
                bookDao.insert(copy);
            }

            MessageUtil.info("Sucesso", quantity > 1
                    ? "Exemplares cadastrados com sucesso."
                    : "Livro cadastrado com sucesso.");
            resetScreen(screen);
        } catch (Exception e) {
            MessageUtil.error("Erro", "Não foi possível cadastrar o livro.");
            e.printStackTrace();
        }
    }

    private void handleUpdate(BookMaintenanceView screen) {
        String codeValue = screen.bookCode.getValue();
        BookCodeParts parts = BookCodeUtils.split(codeValue);
        if (parts == null || parts.groupCode() <= 0) {
            MessageUtil.warn("Código inválido", "Informe um código válido para atualizar.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        int groupCode = parts.groupCode();
        List<Book> copies = bookDao.findByGroupCode(groupCode);
        if (copies == null || copies.isEmpty()) {
            Book fallback = bookDao.find(groupCode);
            if (fallback != null) {
                if (fallback.getSequence() == null) {
                    fallback.setSequence(0);
                }
                fallback.setGroupCode(groupCode);
                copies = new ArrayList<>();
                copies.add(fallback);
            }
        }
        if (copies == null || copies.isEmpty()) {
            MessageUtil.warn("Livro não encontrado", "Não foi possível localizar o livro informado.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        if (!MessageUtil.confirm("Confirmação", "Deseja atualizar o livro selecionado?")) {
            return;
        }

        try {
            retDad(screen);
            maintenanceBook.setGroupCode(groupCode);
            maintenanceBook.setSequence(0);

            int desiredQuantity = parseQuantity(screen);
            int existingQuantity = copies.size();
            int removableNeeded = Math.max(0, existingQuantity - desiredQuantity);

            List<Book> removableCandidates = new ArrayList<>();
            for (Book copy : copies) {
                Integer sequence = copy.getSequence();
                boolean baseCopy = sequence == null || sequence == 0;
                if (baseCopy) {
                    continue;
                }

                boolean available = Boolean.TRUE.equals(copy.getAvailable());
                int activeBorrows = borrowHistoryDao.countActiveByBook(copy.getId());
                if (available && activeBorrows == 0) {
                    removableCandidates.add(copy);
                }
            }

            removableCandidates.sort(Comparator.comparingInt(copy -> copy.getSequence() == null ? 0 : copy.getSequence()));

            if (removableNeeded > removableCandidates.size()) {
                MessageUtil.warn("Ajuste de quantidade", "NAO APAGAR SE NAO CHEGAR NA QUANTIDADE");
                return;
            }

            for (Book copy : copies) {
                boolean keepAvailability = borrowHistoryDao.countActiveByBook(copy.getId()) > 0;
                copyMetadata(maintenanceBook, copy, keepAvailability);
                copy.setGroupCode(groupCode);
                if (copy.getSequence() == null) {
                    copy.setSequence(0);
                }
                bookDao.update(copy);
            }

            if (desiredQuantity > existingQuantity) {
                int maxSequence = bookDao.maxSequence(groupCode);
                maxSequence = Math.max(maxSequence, copies.stream()
                        .map(Book::getSequence)
                        .filter(seq -> seq != null)
                        .max(Integer::compareTo)
                        .orElse(0));

                int toCreate = desiredQuantity - existingQuantity;
                boolean available = maintenanceBook.getAvailable() == null || maintenanceBook.getAvailable();
                for (int i = 1; i <= toCreate; i++) {
                    int nextSequence = maxSequence + i;
                    Book newCopy = cloneFromTemplate(maintenanceBook, groupCode, nextSequence);
                    newCopy.setAvailable(available);
                    bookDao.insert(newCopy);
                }
            } else if (removableNeeded > 0) {
                for (int i = 0; i < removableNeeded; i++) {
                    Book copy = removableCandidates.get(removableCandidates.size() - 1 - i);
                    try {
                        bookDao.delete(copy.getId());
                    } catch (Exception ex) {
                        MessageUtil.warn("Ajuste de quantidade", "NAO APAGAR SE NAO CHEGAR NA QUANTIDADE");
                        return;
                    }
                }
            }

            MessageUtil.info("Sucesso", "Livro atualizado com sucesso.");
            resetScreen(screen);
        } catch (Exception e) {
            MessageUtil.error("Erro", "Não foi possível atualizar o livro.");
            e.printStackTrace();
        }
    }

    private void handleConsult(BookMaintenanceView screen) {
        String codeValue = screen.bookCode.getValue();
        BookCodeParts parts = BookCodeUtils.split(codeValue);
        if (parts == null || parts.groupCode() <= 0) {
            MessageUtil.warn("Código inválido", "Informe um código válido para consultar.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        Book persisted = bookDao.findByGroupAndSequence(parts.groupCode(), parts.sequence());
        if (persisted == null) {
            persisted = bookDao.find(parts.groupCode());
        }

        if (persisted == null) {
            MessageUtil.warn("Livro não encontrado", "Não foi possível localizar o livro informado.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        if (persisted.getGroupCode() == null) {
            persisted.setGroupCode(parts.groupCode());
        }
        if (persisted.getSequence() == null) {
            persisted.setSequence(parts.sequence());
        }

        maintenanceBook = persisted;
        moveDad(screen);
        ScreenManager.disableNode(screen.advanceButton);
    }

    private void handleDelete(BookMaintenanceView screen) {
        String codeValue = screen.bookCode.getValue();
        BookCodeParts parts = BookCodeUtils.split(codeValue);
        if (parts == null || parts.groupCode() <= 0) {
            MessageUtil.warn("Código inválido", "Informe um código válido para excluir.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        int groupCode = parts.groupCode();
        List<Book> copies = bookDao.findByGroupCode(groupCode);
        if (copies == null || copies.isEmpty()) {
            Book fallback = bookDao.find(groupCode);
            if (fallback != null) {
                if (fallback.getSequence() == null) {
                    fallback.setSequence(0);
                }
                fallback.setGroupCode(groupCode);
                copies = new ArrayList<>();
                copies.add(fallback);
            }
        }
        if (copies == null || copies.isEmpty()) {
            MessageUtil.warn("Livro não encontrado", "Não foi possível localizar o livro informado.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        maintenanceBook = copies.get(0);
        moveDad(screen);

        boolean hasBorrowed = copies.stream()
                .anyMatch(copy -> borrowHistoryDao.countActiveByBook(copy.getId()) > 0);
        if (hasBorrowed) {
            MessageUtil.warn("Exclusão bloqueada", "NAO APAGAR SE NAO CHEGAR NA QUANTIDADE");
            return;
        }

        if (!MessageUtil.confirm("Confirmação", "Deseja excluir todos os exemplares selecionados?")) {
            return;
        }

        try {
            copies.sort((a, b) -> Integer.compare(b.getSequence() == null ? 0 : b.getSequence(),
                    a.getSequence() == null ? 0 : a.getSequence()));
            for (Book copy : copies) {
                try {
                    bookDao.delete(copy.getId());
                } catch (Exception ex) {
                    MessageUtil.warn("Exclusão bloqueada", "NAO APAGAR SE NAO CHEGAR NA QUANTIDADE");
                    return;
                }
            }
            MessageUtil.info("Sucesso", "Exemplares excluídos com sucesso.");
            resetScreen(screen);
        } catch (Exception e) {
            MessageUtil.error("Erro", "Não foi possível excluir o livro.");
            e.printStackTrace();
        }
    }

    public void callbackAltcamCancelButton(BookMaintenanceView screen) {
        ScreenManager.goBack();
    }

    private void resetScreen(BookMaintenanceView screen) {
        clearForm(screen);
        maintenanceBook = new Book();
        Status.clearError();
        Status.clearExitReason();
        Status.CONFIRMED_SELECTION = false;

        if (BookMaintenanceController.status == AcceptMode.KEY) {
            screen.bookCode.setValue("");
            acceptKey(screen);
        } else {
            acceptData(screen);
            ScreenManager.enableNode(screen.advanceButton);
            ScreenManager.enableNode(screen.cancelButton);
        }
    }

    private void clearForm(BookMaintenanceView screen) {
        screen.bookTitle.setValue("");
        screen.bookAuthor.setValue("");
        screen.bookPublisher.setValue("");
        screen.bookYear.setValue("");
        screen.bookIsbn.setValue("");
        screen.bookPages.setValue("");
        screen.bookCoverPath.setValue("");
        screen.bookAvailable.setValue("");
        screen.bookQuantity.setValue("1");

        URL imageCover = getClass().getResource("/com/connectasistemas/framework/covers/empty_cover.png");
        screen.bookCover.setImage(new Image(imageCover.toExternalForm()));

        screen.bookCode.getTextField().setEditable(true);
        screen.bookCode.getTextField().setDisable(false);

        if (returnAfterAction) {
            returnAfterAction = false;
            ScreenManager.goBack();
        }
    }
}
