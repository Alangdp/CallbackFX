package com.connectasistemas.framework.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;

/**
 * Utilitário para exibir mensagens no JavaFX.
 * Fornece métodos rápidos para alerts já estilizados.
 */
public class MessageUtil {

    // Estilo simples aplicado no Alert
    private static final String DEFAULT_STYLE =
            "-fx-font-size: 14px;" +
                    "-fx-background-color: #2b2b2b;" +
                    "-fx-text-fill: #ffffff;";

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
        applyStyle(alert);
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
        applyStyle(alert);
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
        applyStyle(alert);
        alert.showAndWait();
    }

    /**
     * Aplica o estilo padrão ao Alert.
     *
     * @param alert Alert a estilizar
     */
    private static void applyStyle(Alert alert) {
        // Aplica CSS diretamente no DialogPane
        DialogPane pane = alert.getDialogPane();
        pane.setStyle(DEFAULT_STYLE);

        // Garante que botões também recebem o tema
        pane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        pane.lookupButton(javafx.scene.control.ButtonType.OK)
                .setStyle("-fx-background-color: #444; -fx-text-fill: white;");
    }
}
