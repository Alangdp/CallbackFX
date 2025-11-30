package com.connectasistemas.framework.controller;

import com.connectasistemas.framework.dao.BookDao;
import com.connectasistemas.framework.dao.DaoFactory;
import com.connectasistemas.framework.enums.AcceptMode;
import com.connectasistemas.framework.enums.WindowFunction;
import com.connectasistemas.framework.models.Book;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.NumberUtils;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.view.HomeView;
import javafx.scene.image.Image;

import java.net.URL;

/**
 * Controller da janela Home
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
public class HomeController {
    private static WindowFunction function;
    private static AcceptMode status;

    private BookDao bookDao;
    private Book maintenanceBook = new Book();

    /**
     * Seta o tipo de função da janela
     */
    public static void setFunction(WindowFunction function) {
        HomeController.function = function;

        switch (function) {
            case REGISTER -> HomeController.status = AcceptMode.DATA;
            case UPDATE, CONSULT, DELETE -> HomeController.status = AcceptMode.KEY;
            default -> HomeController.status = AcceptMode.DATA;
        }
    }

    /**
     * Callback de config da janela
     */
    public void callbackConfigHomeView(HomeView screen) {
        // Carrega o DAO de livros
        bookDao = DaoFactory.dao(BookDao.class);

        switch (HomeController.status) {
            case KEY -> acceptKey(screen);
            case DATA -> acceptData(screen);
        }

        switch (HomeController.function) {
            case REGISTER -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - cadastro de livro");
            case UPDATE -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - atualização de livro");
            case DELETE -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - exclusão de livro");
            case CONSULT -> ScreenManager.setWindowTitle(ScreenManager.getWindowTitle() + " - consulta de livro");
        }
    }

    public void callbackSaicamBookCode(HomeView screen) {
        if (!Status.VALIDA) {
            return;
        }

        int bookCode = Integer.parseInt(screen.bookCode.getValue());

        // Se o código está zerado
        if (bookCode == 0) {
            return;
        }

        maintenanceBook = bookDao.find(bookCode);
        if (maintenanceBook == null) {
            // Livro não encontrado
            MessageUtil.warn("Atenção", "Livro não encontrado.");
            Status.markError(screen.bookCode.getTextField());
            return;
        }

        moveDad(screen);
    }

    /**
     * Saída do campo Ano
     */
    public void callbackSaicamBookYear(HomeView screen) {
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
    public void callbackSaicamBookPages(HomeView screen) {
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
    public void callbackSaicamBookIsbn(HomeView screen) {
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

    /**
     * Saída do campo Disponível (true/false)
     */
    public void callbackSaicamBookAvailable(HomeView screen) {
        String val = screen.bookAvailable.getValue();

        if (!Status.VALIDA) {
            return;
        }

        if (val == null || val.isEmpty()) {
            MessageUtil.warn("Disponibilidade obrigatória", "Preencha com true ou false.");
            screen.bookAvailable.setValue("");
            Status.markError(screen.bookAvailable.getCheckBox());
            return;
        }

        if (!val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false")) {
            MessageUtil.warn("Valor inválido", "Use true ou false.");
            screen.bookAvailable.setValue("");
            Status.markError(screen.bookAvailable.getCheckBox());
        }
    }

    public void acceptKey(HomeView screen) {
        // Desabilita todos os elementos da tela
        ScreenManager.disableWindow(HomeView.class);

        // Habilita os botões de avançar e cancelar
        ScreenManager.enableNode(screen.advanceButton);
        ScreenManager.enableNode(screen.cancelButton);

        // Habilita os campos de chave
        ScreenManager.enableNode(screen.bookCode);
    }

    public void acceptData(HomeView screen) {
        // Carrega o próximo código disponivél no banco de dados
        int nextId = bookDao.nextId();
        screen.bookCode.setValue(String.valueOf(nextId));

        ScreenManager.disableNode(screen.bookCode);

    }

    public void moveDad(HomeView screen) {
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
                screen.bookCover.setImage(null);
            }
        }
    }

    // Move da tela para o objeto maintenanceBook
    public void retDad(HomeView screen) {
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
        maintenanceBook.setAvailable(
                screen.bookAvailable.getValue() == null || screen.bookAvailable.getValue().isEmpty()
                        ? null
                        : Boolean.valueOf(screen.bookAvailable.getValue()));
    }

    public void callbackAltcamAdvanceButton(HomeView screen) {
        // Se for consulta de registro
        if (HomeController.function == WindowFunction.REGISTER) {
            retDad(screen);
            bookDao.insert(maintenanceBook);
        }

        if (HomeController.function == WindowFunction.UPDATE) {
            bookDao.update(maintenanceBook);
        }

        if (HomeController.function == WindowFunction.CONSULT) {
            maintenanceBook = bookDao.find(maintenanceBook.getId());
            moveDad(screen);

            ScreenManager.disableNode(screen.advanceButton);
        }

        if (HomeController.function == WindowFunction.DELETE) {
            bookDao.delete(maintenanceBook.getId());
        }
    } 
}
