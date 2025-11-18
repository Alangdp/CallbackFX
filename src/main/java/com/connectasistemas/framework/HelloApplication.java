package com.connectasistemas.framework;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.view.HomeView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Inicializa o ScreenManager com o Stage principal
        ScreenManager.init(stage);

        // Carrega a tela inicial automaticamente
        ScreenManager.changeTo(HomeView.class);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
