package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.processor.AnnotationProcessor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.lang.reflect.Field;

/**
 * Gerenciador da tela
 */
public class ScreenManager {

    // Referência global do Stage atual
    private static Stage mainStage;

    // Armazena o stage na inicialização
    public static void init(Stage stage) {
        mainStage = stage;
    }

    // Troca a tela para outra classe anotada com @Screen
    public static void changeTo(Class<?> screenClass) {
        try {
            Object screen = screenClass.getDeclaredConstructor().newInstance();

            // Processa anotações
            AnnotationProcessor p = new AnnotationProcessor();
            ScreenMetadata meta = p.processScreen(screenClass);

            // Atualiza título e tamanho
            mainStage.setTitle(meta.getTitle());
            mainStage.setWidth(meta.getWidth());
            mainStage.setHeight(meta.getHeight());

            // Monta layout básico
            HBox root = new HBox(10);

            for (var entry : meta.getFields().entrySet()) {

                String acronym = entry.getKey();
                Field field = entry.getValue();
                field.setAccessible(true);

                // tipo declarado
                Class<?> type = field.getType();

                // cria Node
                Node node = ElementManager.createElement(type);

                // aplica eventos
                EventBinder.attach(acronym, node, screen, meta.callbackInstance());

                // Adiciona o elemento na tela
                // TODO: Rever lógica para considerar posicionamento e o tipo de elemento pai tipo:
                // @ScreenElement(acronym = "teste", child = borderPane, position = BorderPanePosition.TOP)
                root.getChildren().add(node);
            }

            // Troca a cena
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
