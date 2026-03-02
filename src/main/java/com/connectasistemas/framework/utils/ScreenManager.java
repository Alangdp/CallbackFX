package com.connectasistemas.framework.utils;

import java.util.Deque;
import java.util.Map;

import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.internal.properties.TabVisibilityManager;
import com.connectasistemas.framework.internal.utils.EventBinder;
import com.connectasistemas.framework.internal.utils.PropertiesBinderGeneric;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.ScreenMetadata;
import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.internal.utils.ScreenControllerRegistry;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Orquestra o ciclo de vida das telas anotadas com {@code @Screen}, cuidando da
 * troca do {@link Stage} principal, gerenciamento de sub-janelas, historico de
 * navegacao e controle de elementos armazenados em cache.
 * <p>
 * O estado de navegacao e armazenado em {@link ScreenRuntimeContext}.
 * Todos os metodos estaticos delegam para a instancia padrao do contexto;
 * novas integracoes devem obter o contexto via {@link #getDefault()} e
 * operar diretamente sobre ele quando possivel.
 */
public class ScreenManager {

    // Contexto padrao compartilhado (substitui os antigos campos static)
    private static final ScreenRuntimeContext ctx = ScreenRuntimeContext.getDefault();

    // Binder de propriedades genericas (stateless, pode permanecer static)
    private static final PropertiesBinderGeneric propertiesBinderGeneric = new PropertiesBinderGeneric();

    /**
     * Retorna o {@link ScreenRuntimeContext} padrao utilizado pelos metodos
     * estaticos desta classe.
     *
     * @return contexto de navegacao padrao
     */
    public static ScreenRuntimeContext getDefault() {
        return ctx;
    }

    /**
     * Armazena o {@link Stage} principal e aplica os listeners básicos de ciclo de
     * vida.
     *
     * @param stage instância criada pelo JavaFX
     */
    public static void init(Stage stage) {
        // Guarda a referencia inicial uma unica vez
        ctx.setMainStage(stage);

        // Evento geral de fechamento da janela
        stage.setOnCloseRequest(e -> {
            // Limpa referencia de elementos relacionados a tela antiga
            clearPreviousScreen();
        });

        // Inicializa a exibicao da janela
        // OBS: Inicializar duas vezes causa problemas
        stage.show();

        // Tenta centralizar a janela na tela
        if (stage.getScene() != null && stage.getScene().getWindow() != null) {
            stage.getScene().getWindow().centerOnScreen();
        }
    }

    /**
     * Troca a tela ativa para uma classe anotada com {@code @Screen}, evitando
     * alterações concorrentes quando múltiplas chamadas acontecem em sequência.
     *
     * @param screenClass classe da nova tela
     */
    public static void changeTo(Class<?> screenClass) {
        if (screenClass == null) {
            return;
        }

        if (ctx.isChangeInProgress()) {
            // Memoriza a proxima tela para processar apos a troca atual
            ctx.setDeferredScreenClass(screenClass);
            return;
        }

        // Evita reentrancia enquanto a troca esta em andamento
        ctx.setChangeInProgress(true);
        try {
            Class<?> nextScreen = screenClass;
            while (nextScreen != null) {
                // Zera referencia antes de cada iteracao
                ctx.setDeferredScreenClass(null);
                // Gera a tela e aplica no Stage
                performScreenChange(nextScreen);
                // Caso algum callback solicite outra tela, processamos na sequencia
                nextScreen = ctx.getDeferredScreenClass();
            }
        } finally {
            // Libera o bloqueio mesmo em caso de excecao
            ctx.setChangeInProgress(false);
        }
    }

    /**
     * Processa uma classe anotada com {@code @Screen} sem trocar o Stage atual,
     * devolvendo os elementos montados para uso como fragmento embutido.
     * 
     * @param screenClass Classe da tela a ser montada
     * @return {@link ScreenView} com instância, metadados e nó raiz
     */
    /**
     * Constrói um {@link ScreenView} reutilizável sem alterar o Stage atual.
     *
     * @param screenClass classe da tela embutida
     * @return view contendo instância, metadados e root
     */
    private static ScreenView renderFragment(Class<?> screenClass) {
        return renderFragment(screenClass, ctx.getScreenInstance());
    }

    /**
     * Varre uma classe anotada com {@code @Screen} e registra o controller,
     * vinculando opcionalmente a uma tela pai.
     *
     * @param screenClass          classe que será montada
     * @param parentScreenInstance instância da tela pai
     * @return {@link ScreenView} pronto para uso em composições
     */
    private static ScreenView renderFragment(Class<?> screenClass, Object parentScreenInstance) {
        if (screenClass == null) {
            throw new IllegalArgumentException("Classe da tela não pode ser nula");
        }
        ScreenView view = ScreenAssembler.compose(screenClass, parentScreenInstance);
        ScreenControllerRegistry.register(view);
        return view;
    }

    /**
     * Retorna apenas o nó raiz renderizado para uso direto dentro de outros
     * layouts.
     *
     * @param screenClass classe anotada com {@code @Screen}
     * @return {@link Region} raiz renderizada
     */
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
        return openChildWindow(screenClass, ctx.getScreenInstance());
    }

    /**
     * Abre uma tela anotada com {@code @Screen} como sub janela, permitindo
     * informar
     * explicitamente a instância pai para registro da hierarquia.
     *
     * @param screenClass          classe da tela secundária
     * @param parentScreenInstance instância que servirá como pai lógico
     * @return {@link Stage} criado para a sub janela
     */
    public static Stage openChildWindow(Class<?> screenClass, Object parentScreenInstance) {
        if (screenClass == null) {
            throw new IllegalArgumentException(StringUtils.concat(
                    "Classe da tela não pode ser nula ao abrir sub janela"));
        }

        Stage mainStage = ctx.getMainStage();
        if (mainStage == null) {
            throw new IllegalStateException(StringUtils.concat(
                    "Stage principal não foi inicializado"));
        }

        // Composicao da tela filha reaproveitando o assembler padrao
        ScreenView view = ScreenAssembler.compose(screenClass, parentScreenInstance);
        ScreenControllerRegistry.register(view);

        ScreenMetadata metadata = view.metadata();
        ScreenProperties screenProperties = view.screenProperties();

        // Cria um Stage independente para a sub janela
        Stage childStage = new Stage();
        childStage.initOwner(mainStage);
        childStage.setTitle(metadata.getTitle());
        childStage.setWidth(metadata.getWidth());
        childStage.setHeight(metadata.getHeight());

        if (screenProperties != null) {
            // Reaproveita o binder generico para aplicar propriedades no Stage filho
            propertiesBinderGeneric.applyToStage(screenProperties, childStage);
        }

        // A cena herda o root montado pelo assembler
        Scene scene = new Scene(view.root());
        childStage.setScene(scene);

        // Controla o ciclo de vida da sub janela para liberar recursos automaticamente
        ctx.getChildStages().put(view.screenInstance(), childStage);
        childStage.setOnHidden(event -> disposeScreenHierarchy(view.screenInstance()));

        childStage.show();
        childStage.centerOnScreen();

        return childStage;
    }

    /**
     * Fecha uma sub janela previamente aberta via {@link #openChildWindow(Class)}.
     *
     * @param childInstance instância anotada com {@code @Screen} usada na sub
     *                      janela
     */
    public static void closeChildWindow(Object childInstance) {
        if (childInstance == null) {
            return;
        }

        Stage childStage = ctx.getChildStages().remove(childInstance);
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
            Stage mainStage = ctx.getMainStage();

            // Guarda referencia para empilhar historico posteriormente
            Object previousInstance = ctx.getScreenInstance();

            // Limpa referencia de elementos relacionados a tela antiga
            clearPreviousScreen();
            // Monta a nova tela e registra controller
            ScreenView view = ScreenAssembler.compose(screenClass);
            ScreenControllerRegistry.register(view);
            ctx.setScreenInstance(view.screenInstance());
            ScreenMetadata meta = view.metadata();
            ctx.setCurrentMetadata(meta);
            ScreenProperties screenProperties = view.screenProperties();

            // Atualiza titulo e tamanho
            mainStage.setTitle(meta.getTitle());
            mainStage.setWidth(meta.getWidth());
            mainStage.setHeight(meta.getHeight());

            // Aplica propriedades da tela diretamente no Stage
            if (screenProperties != null) {
                propertiesBinderGeneric.applyToStage(screenProperties, mainStage);
            }

            // Cria a cena com o novo root antes de verificar centralizacao
            Scene scene = new Scene(view.root());

            if (ctx.getDeferredScreenClass() != null) {
                return;
            }

            if (previousInstance != null && previousInstance.getClass() != screenClass) {
                // Empilha o historico somente quando a classe realmente mudou
                ctx.getScreenHistory().push(previousInstance.getClass());
            }

            // Tenta centralizar a janela na tela
            if (scene.getWindow() != null) {
                scene.getWindow().centerOnScreen();
            }

            // Aplica a cena recem-criada no Stage principal
            mainStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica se é possível retornar para a tela anterior no histórico.
     */
    public static boolean canGoBack() {
        return !ctx.getScreenHistory().isEmpty();
    }

    /**
     * Retorna para a tela anterior no historico, se possivel.
     */
    public static void goBack() {
        // Caso nao haja tela anterior, encerra a aplicacao
        if (!canGoBack()) {
            Stage mainStage = ctx.getMainStage();
            if (mainStage != null) {
                mainStage.close();
            }
            return;
        }

        Class<?> previous = ctx.getScreenHistory().pop();
        changeTo(previous);
    }

    /**
     * Desfaz toda a estrutura associada à tela atual (eventos, cache e hierarquia),
     * preparando o Stage para receber uma nova composição.
     */
    private static void clearPreviousScreen() {
        if (ctx.getScreenInstance() == null) {
            return;
        }

        disposeScreenHierarchy(ctx.getScreenInstance());
        ctx.setScreenInstance(null);
        ctx.setCurrentMetadata(null);
    }

    /**
     * Remove recursivamente os recursos registrados para uma instância, incluindo
     * filhos, eventos e sub-janelas.
     *
     * @param instance instância anotada com {@code @Screen}
     */
    private static void disposeScreenHierarchy(Object instance) {
        if (instance == null) {
            return;
        }

        // Remove o controller associado a esta instância
        ScreenControllerRegistry.unregister(instance);

        Stage childStage = ctx.getChildStages().remove(instance);
        if (childStage != null && childStage.isShowing()) {
            // Garante que o listener de fechamento nao tente dupla liberacao
            childStage.setOnHidden(null);
            childStage.close();
        }

        // Percorre todos os filhos registrados para liberar em cascata
        ScreenHierarchyRegistry.detachChildren(instance).forEach(ScreenManager::disposeScreenHierarchy);
        // Remove eventos registrados via EventBinder
        EventBinder.deleteEvents(instance);
        // Limpa o cache de elementos referentes à tela
        ScreenManagerSharedData.resetScreenData(instance);
    }

    /**
     * Desativa todos os nós mantidos no cache quando a tela corresponde à classe
     * informada. Útil para bloquear interações temporariamente.
     */
    public static void disableWindow(Class<?> screenClass) {
        Object instance = ctx.getScreenInstance();
        if (screenClass == null || instance == null || instance.getClass() != screenClass) {
            return;
        }

        Map<String, Object> cachedNodes = ScreenManagerSharedData.getCache().get(instance);
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

        // Desabilita o nó atual
        disableNode(node);

        // Se for um container que possui filhos
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                disableAll(child);
            }
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
     * @param acronym identificador configurado em {@code @ScreenField}
     * @param visible define se o elemento deve permanecer visível e gerenciado
     */
    public static void setNodeVisibility(String acronym, boolean visible) {
        if (StringUtils.isBlank(acronym)) {
            return;
        }

        if (ctx.getScreenInstance() == null) {
            return;
        }

        Object element = ScreenManagerSharedData.getScreenData(ctx.getScreenInstance(), acronym);
        setElementVisibility(element, visible);
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

    /**
     * Retorna a instância do controller registrada para a tela informada.
     *
     * @param screenClass classe anotada com {@code @Screen}
     * @return controller associado ou {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> T getControllerReference(Class<?> screenClass) {
        return ScreenControllerRegistry.getControllerReference(screenClass);
    }

    /**
     * Retorna a instância da tela registrada para a classe informada.
     *
     * @param screenClass classe anotada com {@code @Screen}
     * @return instância atual da tela ou {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> T getScreenReference(Class<T> screenClass) {
        return ScreenControllerRegistry.getScreenReference(screenClass);
    }

    /**
     * Aplica a visibilidade no elemento apropriado, abstraindo o tipo concreto.
     *
     * @param element elemento recuperado do cache
     * @param visible estado desejado
     */
    private static void setElementVisibility(Object element, boolean visible) {
        if (element instanceof Node node) {
            setNodeVisibility(node, visible);
            return;
        }

        if (element instanceof Tab tab) {
            TabVisibilityManager.setVisible(tab, visible);
        }
    }

    /**
     * Reabilita todos os ancestrais para que o nó atual possa voltar a responder a
     * eventos.
     */
    private static void enableAncestors(Parent parent) {
        Parent current = parent;
        while (current != null) {
            current.setDisable(false);
            current = current.getParent();
        }
    }

    /**
     * Caminha pela subárvore garantindo que os descendentes também sejam
     * habilitados.
     */
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
        Stage mainStage = ctx.getMainStage();
        if (mainStage != null) {
            mainStage.setTitle(title);
        }
    }

    /**
     * Retorna o titulo atual da janela
     */
    public static String getWindowTitle() {
        Stage mainStage = ctx.getMainStage();
        return mainStage != null ? mainStage.getTitle() : "";
    }

    /**
     * Retorna o Stage principal da aplicacao
     */
    public static Stage getMainStage() {
        return ctx.getMainStage();
    }

    /**
     * Retorna o Scene atual do Stage principal, ou {@code null} se nao houver cena
     * definida.
     */
    public static Scene getMainScene() {
        Stage mainStage = ctx.getMainStage();
        return mainStage != null ? mainStage.getScene() : null;
    }

    /**
     * Tenta aplicar o menu superior a janela atual
     * 
     * @param topMenu menu superior a ser aplicado
     */
    @SuppressWarnings("unused")
    public static void setTopMenu(MenuBar topMenu) {
        if (topMenu == null) {
            return;
        }

        // Descobre qual tela está sendo composta no momento
        ScreenCompositionMetadata metadata = resolveTopMenuMetadata();
        MenuBar pendingTopMenu;
        if (metadata == null) {
            // Sem metadados ainda, aguarda até que o root esteja pronto
            pendingTopMenu = topMenu;
            return;
        }

        if (applyTopMenu(topMenu, metadata.root())) {
            if (metadata == ctx.getCurrentMetadata()) {
                // Menu aplicado diretamente a tela atual
                pendingTopMenu = null;
            }
            return;
        }

        if (metadata == ctx.getCurrentMetadata()) {
            // Registra para reaplicar assim que o layout suportar
            pendingTopMenu = topMenu;
        }
    }

    /**
     * Retorna o root através do Cache
     * 
     * @return root atual da tela
     */
    public static Node getRoot() {
        ScreenMetadata meta = ctx.getCurrentMetadata();
        return meta != null ? meta.root() : null;
    }

    /**
     * Retorna um descritor imutavel com as informacoes principais da tela atual.
     * Serve para expor dados basicos sem permitir acesso direto ao
     * {@code ScreenMetadata} interno.
     *
     * @return descritor da tela atual ou {@code null} quando nao ha tela carregada
     */
    public static ScreenDescriptor getCurrentScreenDescriptor() {
        ScreenMetadata meta = ctx.getCurrentMetadata();
        if (meta == null) {
            return null;
        }

        return ScreenDescriptor.from(meta, ctx.getScreenInstance());
    }

    /**
     * Retorna o root do elemento pai de um {@link Region}, se existir.
     * Útil para navegação e manipulação de layouts aninhados.
     *
     * @param region elemento filho
     * @return root do elemento pai ou {@code null} se não houver
     */
    public static Region getParentRoot(Region region) {
        if (region == null) {
            return null;
        }
        Parent parent = region.getParent();
        if (parent instanceof Region parentRegion) {
            return parentRegion;
        }
        return null;
    }

    private static boolean applyTopMenu(MenuBar topMenu, Region root) {
        if (topMenu == null || root == null) {
            return false;
        }

        if (root instanceof BorderPane borderPane) {
            borderPane.setTop(topMenu);
            return true;
        }

        if (root instanceof Pane pane) {
            pane.getChildren().add(0, topMenu);
            return true;
        }

        return false;
    }

    /**
     * Empilha metadados de composição enquanto telas aninhadas estão sendo
     * processadas.
     */
    public static void pushCompositionMetadata(ScreenCompositionMetadata metadata) {
        if (metadata == null) {
            return;
        }

        ctx.getComposingMetadataStack().push(metadata);
    }

    /**
     * Remove o metadado informado do topo (ou da pilha) para manter o contexto
     * consistente.
     */
    public static void popCompositionMetadata(ScreenCompositionMetadata metadata) {
        Deque<ScreenCompositionMetadata> stack = ctx.getComposingMetadataStack();
        if (stack.isEmpty()) {
            return;
        }

        if (stack.peek() == metadata) {
            stack.pop();
        } else {
            stack.remove(metadata);
        }

        ctx.clearComposingMetadata();
    }

    /**
     * Resolve qual metadado deve receber o menu superior (pilha ou tela atual).
     */
    private static ScreenCompositionMetadata resolveTopMenuMetadata() {
        Deque<ScreenCompositionMetadata> stack = ctx.getComposingMetadataStack();
        if (!stack.isEmpty()) {
            return stack.peek();
        }

        return ctx.getCurrentMetadata();
    }
}
