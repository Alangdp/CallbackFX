package com.connectasistemas.framework;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.utils.ScreenManager;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Inicializa o ScreenManager com o Stage principal
        ScreenManager.init(stage);

        ScreenManager.changeTo(Example.class);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
