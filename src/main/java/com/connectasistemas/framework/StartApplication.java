package com.connectasistemas.framework;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.view.WithdrawView;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Define o tema da aplicação
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // Inicializa o ScreenManager com o Stage principal
        ScreenManager.init(stage);

        // Carrega a tela inicial automaticamente
        ScreenManager.changeTo(WithdrawView.class);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
