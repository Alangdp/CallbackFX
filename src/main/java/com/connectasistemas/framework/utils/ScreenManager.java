package com.connectasistemas.framework.utils;

import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.utils.properties.PropertiesBinderGeneric;

/**
 * Gerenciador da tela
 */
public class ScreenManager {

    // Referência global do Stage atual
    private static Stage mainStage;

    // Referência atual da tela
    // OBS: é a referência de @Screen não do javaFX
    private static Object screenInstance;

    // Instância do binder de propriedades genéricas
    // OBS: Poderia ser estático, mas quero manter como interface pois alguns
    // elementos futuros terão de ser diferente
    private static final PropertiesBinderGeneric propertiesBinderGeneric = new PropertiesBinderGeneric();

    private static boolean changeInProgress;
    private static Class<?> deferredScreenClass;
    private static final Stack<Class<?>> screenHistory = new Stack<>();
    private static ScreenMetadata currentMetadata;
    private static final Map<Object, Stage> childStages = new WeakHashMap<>();

    // Armazena o stage na inicialização
    public static void init(Stage stage) {
        mainStage = stage;

        // Evento geral de fechamento da janela
        mainStage.setOnCloseRequest(e -> {
            // Limpa referência de elementos relacionados a tela antiga
            clearPreviousScreen();
        });

        // Inicializa a exibição da janela
        // OBS: Inicializar duas vezes causa problemas
        mainStage.show();

        // Tenta centralizar a janela na tela
        if (mainStage.getScene() != null && mainStage.getScene().getWindow() != null) {
            mainStage.getScene().getWindow().centerOnScreen();
        }
    }

    // Troca a tela para outra classe anotada com @Screen
    public static void changeTo(Class<?> screenClass) {
        if (screenClass == null) {
            return;
        }

        if (changeInProgress) {
            deferredScreenClass = screenClass;
            return;
        }

        changeInProgress = true;
        try {
            Class<?> nextScreen = screenClass;
            while (nextScreen != null) {
                deferredScreenClass = null;
                performScreenChange(nextScreen);
                nextScreen = deferredScreenClass;
            }
        } finally {
            changeInProgress = false;
        }
    }

    /**
     * Processa uma classe anotada com {@code @Screen} sem trocar o Stage atual,
     * devolvendo os elementos montados para uso como fragmento embutido.
     * 
     * @param screenClass Classe da tela a ser montada
     * @return {@link ScreenView} com instância, metadados e nó raiz
     */
    public static ScreenView renderFragment(Class<?> screenClass) {
        return renderFragment(screenClass, screenInstance);
    }

    public static ScreenView renderFragment(Class<?> screenClass, Object parentScreenInstance) {
        if (screenClass == null) {
            throw new IllegalArgumentException("Classe da tela não pode ser nula");
        }
        return ScreenAssembler.compose(screenClass, parentScreenInstance);
    }

    public static Region renderFragmentRoot(Class<?> screenClass) {
        return renderFragment(screenClass).root();
    }

    /**
     * Abre uma tela anotada com {@code @Screen} como sub janela do Stage principal.
     *
     * @param screenClass classe da tela secundária
     * @return {@link Stage} criado para a sub janela
     */
    public static Stage openChildWindow(Class<?> screenClass) {
        return openChildWindow(screenClass, screenInstance);
    }

    /**
     * Abre uma tela anotada com {@code @Screen} como sub janela, permitindo informar
     * explicitamente a instância pai para registro da hierarquia.
     *
     * @param screenClass           classe da tela secundária
     * @param parentScreenInstance  instância que servirá como pai lógico
     * @return {@link Stage} criado para a sub janela
     */
    public static Stage openChildWindow(Class<?> screenClass, Object parentScreenInstance) {
        if (screenClass == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Classe da tela não pode ser nula ao abrir sub janela"));
        }

        if (mainStage == null) {
            throw new IllegalStateException(StringUtils.concat(
                    "Stage principal não foi inicializado"));
        }

        ScreenView view = ScreenAssembler.compose(screenClass, parentScreenInstance);
        ScreenMetadata metadata = view.metadata();
        ScreenProperties screenProperties = view.screenProperties();

        Stage childStage = new Stage();
        childStage.initOwner(mainStage);
        childStage.setTitle(metadata.getTitle());
        childStage.setWidth(metadata.getWidth());
        childStage.setHeight(metadata.getHeight());

        if (screenProperties != null) {
            propertiesBinderGeneric.applyToStage(screenProperties, childStage);
        }

        Scene scene = new Scene(view.root());
        childStage.setScene(scene);

        childStages.put(view.screenInstance(), childStage);
        childStage.setOnHidden(event -> disposeScreenHierarchy(view.screenInstance()));

        childStage.show();
        childStage.centerOnScreen();

        return childStage;
    }

    /**
     * Fecha uma sub janela previamente aberta via {@link #openChildWindow(Class)}.
     *
     * @param childInstance instância anotada com {@code @Screen} usada na sub janela
     */
    public static void closeChildWindow(Object childInstance) {
        if (childInstance == null) {
            return;
        }

        Stage childStage = childStages.remove(childInstance);
        if (childStage == null) {
            return;
        }

        childStage.setOnHidden(null);
        if (childStage.isShowing()) {
            childStage.close();
        }
        disposeScreenHierarchy(childInstance);
    }

    /**
     * Realiza a troca de tela para a classe especificada.
     * 
     * @param screenClass Classe da tela para a qual se deseja trocar.
     */
    private static void performScreenChange(Class<?> screenClass) {
        try {
            // Limpa referência de elementos relacionados a tela antiga
            clearPreviousScreen();

            Object previousInstance = screenInstance;
            ScreenView view = ScreenAssembler.compose(screenClass);
            screenInstance = view.screenInstance();
            ScreenMetadata meta = currentMetadata = view.metadata();
            ScreenProperties screenProperties = view.screenProperties();

            // Atualiza título e tamanho
            mainStage.setTitle(meta.getTitle());
            mainStage.setWidth(meta.getWidth());
            mainStage.setHeight(meta.getHeight());

            // Aplica propriedades da tela diretamente no Stage
            if (screenProperties != null) {
                propertiesBinderGeneric.applyToStage(screenProperties, mainStage);
            }

            Scene scene = new Scene(view.root());

            if (deferredScreenClass != null) {
                return;
            }

            if (previousInstance != null && previousInstance.getClass() != screenClass) {
                screenHistory.push(previousInstance.getClass());
            }

            // Tenta centralizar a janela na tela
            if (scene.getWindow() != null) {
                scene.getWindow().centerOnScreen();
            }

            mainStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica se é possível retornar para a tela anterior no histórico.
     */
    public static boolean canGoBack() {
        return !screenHistory.isEmpty();
    }

    /**
     * Retorna para a tela anterior no histórico, se possível.
     */
    public static void goBack() {
        // Caso não haja tela anterior, encerra a aplicação
        if (!canGoBack()) {
            if (mainStage != null) {
                mainStage.close();
            }
            return;
        }

        Class<?> previous = screenHistory.pop();
        changeTo(previous);
    }

    /**
     * Limpa referência de elementos relacionados à tela do Stage principal.
     * Use {@link #openChildWindow(Class)} para manter sub janelas coexistindo com a principal.
     */
    private static void clearPreviousScreen() {
        if (screenInstance == null) {
            return;
        }

        disposeScreenHierarchy(screenInstance);
        screenInstance = null;
        currentMetadata = null;
    }

    private static void disposeScreenHierarchy(Object instance) {
        if (instance == null) {
            return;
        }

        Stage childStage = childStages.remove(instance);
        if (childStage != null && childStage.isShowing()) {
            childStage.setOnHidden(null);
            childStage.close();
        }

        ScreenHierarchyRegistry.detachChildren(instance).forEach(ScreenManager::disposeScreenHierarchy);
        EventBinder.deleteEvents(instance);
        ScreenManagerSharedData.resetScreenData(instance);
    }

    /**
     * Desativa todos os nós mantidos no cache da tela informada. Serve como um
     */
    public static void disableWindow(Class<?> screenClass) {
        if (screenClass == null || screenInstance == null || screenInstance.getClass() != screenClass) {
            return;
        }

        Map<String, Object> cachedNodes = ScreenManagerSharedData.getCache().get(screenInstance);
        if (cachedNodes == null) {
            return;
        }

        cachedNodes.values().stream()
                .filter(Node.class::isInstance)
                .map(Node.class::cast)
                .forEach(ScreenManager::disableAll);
    }

    /**
     * Desativa o nó recebido e todos os seus descendentes, garantindo que nenhum
     * componente dentro dessa subárvore responda a eventos.
     */
    public static void disableAll(Node node) {
        if (node == null) {
            return;
        }

        disableNode(node);

        if (node instanceof Pane pane) {
            pane.getChildren().forEach(ScreenManager::disableAll);
        }
    }

    /**
     * Ativa o nó recebido e todos os seus descendentes, garantindo que nenhum
     * componente dentro dessa subárvore responda a eventos.
     */
    public static void enableAll(Node node) {
        if (node == null) {
            return;
        }

        enableNode(node);

        if (node instanceof Pane pane) {
            pane.getChildren().forEach(ScreenManager::enableAll);
        }
    }

    /**
     * Ativa apenas o nó recebido, sem afetar os descendentes. Útil quando se deseja
     * reabilitar um container específico mas preservar o estado desabilitado de
     * seus filhos.
     */
    public static void enableNode(Node node) {
        if (node == null) {
            return;
        }

        node.setDisable(false);
        if (node instanceof Parent parent) {
            enableDescendants(parent);
        }
        enableAncestors(node.getParent());
    }

    /**
     * Ativa apenas o nó recebido, sem afetar os descendentes. Útil quando se deseja
     * reabilitar um container específico mas preservar o estado desabilitado de
     * seus filhos.
     */
    public static void disableNode(Node node) {
        if (node != null) {
            node.setDisable(true);
        }
    }

    /**
     * Ajusta a visibilidade de um elemento identificado pelo acronym.
     * Quando invisível, o elemento também deixa de ser gerenciado pelo layout.
     *
     * @param screenClass classe da view anotada com {@code @Screen}
     * @param acronym     identificador configurado em {@code @ScreenField}
     * @param visible     define se o elemento deve permanecer visível e gerenciado
     */
    public static void setNodeVisibility(Class<?> screenClass, String acronym, boolean visible) {
        if (screenClass == null || StringUtils.isBlank(acronym)) {
            return;
        }

        if (screenInstance == null || screenInstance.getClass() != screenClass) {
            return;
        }

        Node node = ScreenManagerSharedData.getScreenDataAsNode(screenInstance, acronym);
        setNodeVisibility(node, visible);
    }

    /**
     * Ajusta a visibilidade de um elemento já referenciado.
     * Quando invisível, deixa de ser gerenciado pelo layout.
     *
     * @param node    elemento JavaFX alvo
     * @param visible define se o elemento deve permanecer visível e gerenciado
     */
    public static void setNodeVisibility(Node node, boolean visible) {
        if (node == null) {
            return;
        }

        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static void enableAncestors(Parent parent) {
        Parent current = parent;
        while (current != null) {
            current.setDisable(false);
            current = current.getParent();
        }
    }

    private static void enableDescendants(Parent parent) {
        if (parent == null) {
            return;
        }

        for (Node child : parent.getChildrenUnmodifiable()) {
            child.setDisable(false);
            if (child instanceof Parent childParent) {
                enableDescendants(childParent);
            }
        }
    }

    /**
     * Altera o título da janela atual
     */
    public static void setWindowTitle(String title) {
        if (mainStage != null) {
            mainStage.setTitle(title);
        }
    }

    /**
     * Retorna o título atual da janela
     */
    public static String getWindowTitle() {
        return mainStage != null ? mainStage.getTitle() : "";
    }

    /**
     * Retorna o Stage principal da aplicação
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /**
     * Tenta aplicar o menu superior a janela atual
     * 
     * @param topMenu menu superior a ser aplicado
     */
    public static void setTopMenu(MenuBar topMenu) {
        if (topMenu == null || currentMetadata == null) {
            return;
        }

        Region root = currentMetadata.root();
        if (root == null) {
            return;
        }

        if (root instanceof BorderPane borderPane) {
            borderPane.setTop(topMenu);
            return;
        }

        if (root instanceof Pane pane) {
            pane.getChildren().add(0, topMenu);
        }
    }

    /**
     * Retorna o root através do Cache
     * 
     * @return root atual da tela
     */
    public static Node getRoot() {
        return currentMetadata != null ? currentMetadata.root() : null;
    }
}
