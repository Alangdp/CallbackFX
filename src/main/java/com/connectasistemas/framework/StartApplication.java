package com.connectasistemas.framework;

import com.connectasistemas.framework.utils.ScreenManager;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Inicializa o ScreenManager com o Stage principal
        ScreenManager.init(stage);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
