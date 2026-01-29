package com.connectasistemas.framework.internal.utils;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.connectasistemas.framework.enums.FocusExitReason;
import com.connectasistemas.framework.utils.Status;

/**
 * Utilitário para exibir mensagens no JavaFX.
 * Fornece métodos rápidos para alerts já estilizados.
 */
public class MessageUtil {
    /**
     * Exibe um alerta de informação.
     *
     * @param title   Título da janela
     * @param message Mensagem do alerta
     */
    public static void info(String title, String message) {
        // Cria o alerta
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Exibe um alerta de aviso.
     *
     * @param title   Título da janela
     * @param message Mensagem do alerta
     */
    public static void warn(String title, String message) {
        // Cria o alerta
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Exibe um alerta de erro.
     *
     * @param title   Título da janela
     * @param message Mensagem do alerta
     */
    public static void error(String title, String message) {
        // Cria o alerta
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Exibe um alerta de confirmação com botões Confirmar e Cancelar.
     * O diálogo respeita a tecla ESC como atalho para cancelar.
     *
     * @param title   Título da janela
     * @param message Mensagem apresentada no corpo do alerta
     * @return true quando o usuário confirma; false caso contrário
     */
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType confirmButton = new ButtonType("Confirmar", ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        DialogPane dialogPane = alert.getDialogPane();
        Button cancelNode = (Button) dialogPane.lookupButton(cancelButton);
        // Trata a tecla ESC como ação de cancelamento explícita
        dialogPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Status.CONFIRMED_SELECTION = false;
                Status.registerExitReason(FocusExitReason.ESC);
                if (cancelNode != null) {
                    cancelNode.fire();
                } else {
                    alert.hide();
                }
                event.consume();
            }
        });

        Optional<ButtonType> result = alert.showAndWait();
        boolean confirmed = result.isPresent() && result.get() == confirmButton;
        Status.CONFIRMED_SELECTION = confirmed;
        if (Status.EXIT_REASON != FocusExitReason.ESC) {
            Status.registerExitReason(FocusExitReason.OTHER);
        }
        return confirmed;
    }
}