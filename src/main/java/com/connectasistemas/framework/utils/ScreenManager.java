package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.processor.AnnotationProcessor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Gerenciador da tela
 */
public class ScreenManager {

    // Referência global do Stage atual
    private static Stage mainStage;

    // Referência atual da tela
    // OBS: é a referência de @Screen não do javaFX
    private static Object screenInstance;

    // Armazena o stage na inicialização
    public static void init(Stage stage) {
        mainStage = stage;

        // Evento geral de fechamento da janela
        mainStage.setOnCloseRequest(e -> {
            // Limpa referência de elementos relacionados a tela antiga
            clearPreviousScreen();
        });
    }

    // Troca a tela para outra classe anotada com @Screen
    public static void changeTo(Class<?> screenClass) {
        try {
            // Limpa referência de elementos relacionados a tela antiga
            clearPreviousScreen();

            screenInstance = screenClass.getDeclaredConstructor().newInstance();

            // Processa anotações
            AnnotationProcessor p = new AnnotationProcessor();
            ScreenMetadata meta = p.processScreen(screenClass);

            // Atualiza título e tamanho
            mainStage.setTitle(meta.getTitle());
            mainStage.setWidth(meta.getWidth());
            mainStage.setHeight(meta.getHeight());

            // Monta layout básico
            HBox root = new HBox(10);

            meta.getFields().forEach((acronym, field) -> {
                field.setAccessible(true);

                // Tipo declarado
                Class<?> type = field.getType();

                // Cria Node
                Node node = ElementManager.createElement(type);

                // Adiciona elemento a lista de cache
                // OBS: usada para facilitar futuras manipulações via Acronym
                ScreenManagerSharedData.setScreenData(screenClass, acronym, node);

                // aplica eventos
                EventBinder.attach(acronym, node, screenInstance, meta.callbackInstance());

                // Adiciona o elemento na tela
                // TODO: Rever lógica para considerar posicionamento e o tipo de elemento pai tipo:
                // @ScreenElement(acronym = "teste", child = "borderPane", position = BorderPanePosition.TOP)
                root.getChildren().add(node);
            });

            // Troca a cena
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Limpa referência de elementos relacionados a tela antiga
     * OBS: isso cria uma limitação de haver 2 telas sobreposta, entretanto no momento não foi pensado nesse caso
     * TODO: Revisar isso para poder haver 2 telas, talvez usando alguma flag em @Screen, nesse caso deve salvar a...
     * ...Referência da tela pai
     */
    private static void clearPreviousScreen() {
        if (screenInstance != null) {
            EventBinder.deleteEvents(screenInstance);
            ScreenManagerSharedData.resetScreenData(screenInstance);
        }
    }
}
