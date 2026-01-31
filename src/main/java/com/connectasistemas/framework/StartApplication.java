package com.connectasistemas.framework;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.utils.ScreenManager;

/**
 * Ponto de entrada JavaFX responsável por inicializar o {@link ScreenManager}
 * e delegar a construção da primeira tela.
 */
public class StartApplication extends Application {

    /**
     * Recebe o {@link Stage} criado pela plataforma e delega completamente a
     * gestão de telas ao {@link ScreenManager}.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Inicializa o gerenciador principal com o Stage criado pelo JavaFX
        ScreenManager.init(stage);

        // Solicita a troca imediata para o exemplo padrão ao subir a aplicação
        ScreenManager.changeTo(Example.class);

        // Exibe o Stage somente após a composição inicial
        stage.show();
    }

    /**
     * Método convencional de bootstrap, apenas encaminha para {@link #launch}.
     */
    public static void main(String[] args) {
        launch();
    }
}
