package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.processor.AnnotationProcessor;
import com.connectasistemas.framework.utils.position.PositionBinderGeneric;
import com.connectasistemas.framework.utils.sizes.SizeBinderGeneric;
import com.connectasistemas.framework.utils.validation.ValidationBinderGeneric;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gerenciador da tela
 */
public class ScreenManager {

    // Referência global do Stage atual
    private static Stage mainStage;

    // Referência atual da tela
    // OBS: é a referência de @Screen não do javaFX
    private static Object screenInstance;

    // Instância do binder de tamanho
    // OBS: Poderia ser estático, mas quero manter como interface pois alguns
    // elementos futuros terão de ser diferente
    private static final SizeBinderGeneric sizeBinderGeneric = new SizeBinderGeneric();

    // Instância do binder de posição
    // OBS: Poderia ser estático, mas quero manter como interface pois alguns
    // elementos futuros terão de ser diferente
    private static final PositionBinderGeneric positionBinderGeneric = new PositionBinderGeneric();

    private static final ValidationBinderGeneric validationBinderGeneric = new ValidationBinderGeneric();

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
            AnnotationProcessor ap = new AnnotationProcessor();
            ScreenMetadata meta = ap.processScreen(screenClass);

            // Atualiza título e tamanho
            mainStage.setTitle(meta.getTitle());
            mainStage.setWidth(meta.getWidth());
            mainStage.setHeight(meta.getHeight());

            // Monta layout básico
            Region root = meta.root();

            meta.getFields().forEach((acronym, field) -> {
                field.setAccessible(true);

                // Tipo declarado
                Class<?> type = field.getType();

                // Obtém a anotação
                ScreenField f = field.getAnnotation(ScreenField.class);

                // Cria Node
                ElementManager.setLiteral(f.literal());
                Node node = ElementManager.createElement(type);

                // Atualiza a referência da propriedade da tela
                if (!type.isInstance(node)) {
                    throw new RuntimeException(StringUtils.concat(
                            "Tipo incompatível ao criar elemento para o campo ",
                            acronym));
                }

                try {
                    field.set(screenInstance, node);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(StringUtils.concat(
                            "Não foi possível atribuir o elemento ao campo ", acronym), e);
                }

                // Adiciona elemento a lista de cache
                // OBS: usada para facilitar futuras manipulações via Acronym
                ScreenManagerSharedData.setScreenData(screenClass, acronym, node);

                // Aplica eventos
                EventBinder.attach(acronym, node, screenInstance, meta.callbackInstance());

                // Adiciona apenas elementos raiz diretamente ao container principal
                if (f.father().isEmpty()) {
                    ElementManager.addChild(root, node, f.position());
                }
            });

            // Aplica posição em relação ao elemento pai
            meta.getFields().forEach((key, field) -> {
                // Obtém a anotação
                ScreenField f = field.getAnnotation(ScreenField.class);
                ScreenFieldSize s = field.getAnnotation(ScreenFieldSize.class);
                ScreenFieldPosition p = field.getAnnotation(ScreenFieldPosition.class);
                ScreenValidation v = field.getAnnotation(ScreenValidation.class);

                // Carrega o node do cache
                Node node = ScreenManagerSharedData.getScreenData(screenClass, key);

                // Se tem um elemento pai
                if (!f.father().isEmpty()) {
                    // Adiciona o elemento ao Pai
                    Region father = ScreenManagerSharedData.getScreenDataAsRegion(screenClass, f.father());
                    ElementManager.addChild(father, node, f.position());
                }

                // Se tem anotação de tamanho no elemento
                if (s != null) {
                    sizeBinderGeneric.applyAll(s, node);
                }

                // Se tem anotação de posição no elemento
                if (p != null) {
                    positionBinderGeneric.applyAll(p, node);
                }

                if (v != null) {
                    validationBinderGeneric.applyAll(v, node, key, screenInstance, meta.callbackInstance());
                }
            });

            // Garante que a ordem dos filhos respeite o order configurado nas anotações
            reorderChildrenByOrder(screenClass, meta, root);

            // Troca a cena
            Scene scene = new Scene(root);
            invokeScreenInitializationCallback(screenClass, meta);
            mainStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reordena os nós filhos de regiões (Pane) de acordo com a informação de ordem
     * presente
     * nas anotações de campo da tela (ScreenField).
     * <p>
     * Para cada campo descrito em {@code meta}, obtém o nó correspondente no cache
     * de nós
     * associado a {@code screenClass} (via
     * {@link ScreenManagerSharedData#getCache()}).
     * Cada nó é agrupado por seu pai (definido pela propriedade {@code father} da
     * anotação
     * {@code ScreenField}; se vazio, usa-se o {@code root} passado como parâmetro).
     * Campos
     * sem nó correspondente no cache são ignorados.
     * <p>
     * Após agrupar por pai, para cada grupo:
     * - Se o pai for uma instância de {@link BorderPane}, o grupo é
     * ignorado (nenhuma alteração é feita).
     * - Se o pai for uma instância de {@link Pane}, os nós do grupo
     * são ordenados primeiro pelo valor normalizado de ordem (obtido via
     * {@code normalizedOrder(...)}) e, em caso de empate, pelo acrônimo do campo.
     * Em seguida os filhos do {@code Pane} são substituídos pela lista ordenada.
     * <p>
     * Efeitos colaterais:
     * - Modifica diretamente a lista de filhos das regiões do tipo {@link Pane}.
     * - Lê dados e resolve regiões/recursos via {@link ScreenManagerSharedData}.
     *
     * @param screenClass classe da tela cujos nós/metadata serão reorderados
     * @param meta        metadados da tela que contêm os campos (acrônimo ->
     *                    campo/annotação)
     * @param root        região raiz usada como pai padrão quando {@code father}
     *                    estiver vazio
     */
    private static void reorderChildrenByOrder(Class<?> screenClass, ScreenMetadata meta, Region root) {
        Map<String, Node> cachedNodes = ScreenManagerSharedData.getCache().get(screenClass);
        if (cachedNodes == null) {
            return;
        }

        // Agrupa de nós por pai
        Map<Region, List<NodeOrder>> grouped = new HashMap<>();

        // Adiciona os nós ao grupo conforme o pai
        meta.getFields().forEach((acronym, field) -> {
            ScreenField f = field.getAnnotation(ScreenField.class);
            Region parent = f.father().isEmpty()
                    ? root
                    : ScreenManagerSharedData.getScreenDataAsRegion(screenClass, f.father());

            Node node = cachedNodes.get(acronym);
            if (node == null) {
                return;
            }

            grouped.computeIfAbsent(parent, k -> new ArrayList<>())
                    .add(new NodeOrder(f.order(), acronym, node));
        });

        // Vare os nós agrupados por pai
        grouped.forEach((parent, nodes) -> {
            if (parent instanceof BorderPane) {
                return;
            }

            if (parent instanceof Pane pane) {
                nodes.sort(Comparator
                        .comparingInt((NodeOrder nodeOrder) -> normalizedOrder(nodeOrder.order()))
                        .thenComparing(NodeOrder::acronym));

                List<Node> orderedNodes = nodes.stream()
                        .map(NodeOrder::node)
                        .toList();
                pane.getChildren().setAll(orderedNodes);
            }
        });
    }

    // Normaliza o order para facilitar a ordenação
    // OBS: valores <= 0 são considerados como Integer.MAX_VALUE ou seja, vão para o
    // final
    private static int normalizedOrder(int order) {
        return order <= 0 ? Integer.MAX_VALUE : order;
    }

    // Estrutura para facilitar o agrupamento e ordenação dos nós
    private record NodeOrder(int order, String acronym, Node node) {
    }

    private static void invokeScreenInitializationCallback(Class<?> screenClass, ScreenMetadata meta) {
        Object callbacksInstance = meta.callbackInstance();
        if (callbacksInstance == null) {
            return;
        }

        CallbackInvoker.call(callbacksInstance, screenInstance, "config", screenClass.getSimpleName());
    }

    /**
     * Limpa referência de elementos relacionados a tela antiga
     * OBS: isso cria uma limitação de haver 2 telas sobreposta, entretanto no
     * momento não foi pensado nesse caso
     * TODO: Revisar isso para poder haver 2 telas, talvez usando alguma flag
     * em @Screen, nesse caso deve salvar a...
     * ...Referência da tela pai
     */
    private static void clearPreviousScreen() {
        if (screenInstance != null) {
            EventBinder.deleteEvents(screenInstance);
            ScreenManagerSharedData.resetScreenData(screenInstance);
        }
    }

    /**
     * Desativa todos os nós mantidos no cache da tela informada. Serve como um
     */
    public static void disableWindow(Class<?> screenClass) {
        Map<String, Node> cachedNodes = ScreenManagerSharedData.getCache().get(screenClass);
        if (cachedNodes == null) {
            return;
        }

        cachedNodes.values().forEach(ScreenManager::disableAll);
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
     * reabilitar um container específico mas preservar o estado desabilitado de seus filhos.
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
     * reabilitar um container específico mas preservar o estado desabilitado de seus filhos.
     */
    public static void disableNode(Node node) {
        if (node != null) {
            node.setDisable(true);
        }
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
}
