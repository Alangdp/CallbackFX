package com.connectasistemas.framework;

import com.connectasistemas.framework.controller.HomeController;
import com.connectasistemas.framework.enums.WindowFunction;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.view.HomeView;
import com.connectasistemas.framework.view.LoginView;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Inicializa o ScreenManager com o Stage principal
        ScreenManager.init(stage);

        HomeController.setFunction(WindowFunction.CONSULT);

        // Carrega a tela inicial automaticamente
        ScreenManager.changeTo(LoginView.class);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
