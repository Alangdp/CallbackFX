package com.connectasistemas.framework.internal.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.internal.position.PositionBinderGeneric;
import com.connectasistemas.framework.internal.processor.AnnotationProcessor;
import com.connectasistemas.framework.internal.sizes.SizeBinderGeneric;
import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.internal.validation.ValidationBinderGeneric;
import com.connectasistemas.framework.utils.ElementManager;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.StringUtils;

/**
 * Responsável por transformar classes anotadas com {@code @Screen} em árvores
 * de
 * nós reutilizáveis, sem acoplar o processo à troca de
 * {@link javafx.stage.Stage}.
 */
public final class ScreenAssembler {

    private static final SizeBinderGeneric SIZE_BINDER = new SizeBinderGeneric();
    private static final PositionBinderGeneric POSITION_BINDER = new PositionBinderGeneric();
    private static final PropertiesBinderGeneric PROPERTIES_BINDER = new PropertiesBinderGeneric();
    private static final ValidationBinderGeneric VALIDATION_BINDER = new ValidationBinderGeneric();

    private ScreenAssembler() {
    }

    /**
     * Monta uma nova tela a partir da classe anotada informada.
     *
     * @param screenClass classe que representa a tela
     * @return {@link ScreenView} contendo a árvore construída
     */
    public static ScreenView compose(Class<?> screenClass) {
        return compose(screenClass, null);
    }

    /**
     * Monta uma nova tela e mantém o vínculo com o pai para o registro de
     * hierarquia.
     *
     * @param screenClass    classe que representa a tela
     * @param parentInstance instância do pai para relacionamento, pode ser
     *                       {@code null}
     * @return {@link ScreenView} com os metadados e elementos criados
     */
    public static ScreenView compose(Class<?> screenClass, Object parentInstance) {
        try {
            // Cria a instância da tela e obtém metadados antes de qualquer processamento.
            Object screenInstance = screenClass.getDeclaredConstructor().newInstance();
            AnnotationProcessor processor = new AnnotationProcessor();
            ScreenMetadata metadata = processor.processScreen(screenClass);
            ScreenProperties screenProperties = screenClass.getAnnotation(ScreenProperties.class);

            // Se a tela for um Region, utiliza o próprio nó como raiz.
            if (screenInstance instanceof Region regionInstance) {
                metadata.overrideRoot(regionInstance);
            }

            // Embala instância, metadados e propriedades
            ScreenView view = new ScreenView(screenClass, screenInstance, metadata, screenProperties);
            // Mantém a relação pai-filho
            ScreenHierarchyRegistry.registerChild(parentInstance, screenInstance);

            build(view);
            invokeScreenConfig(view);
            return view;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.concat(
                    "Não foi possível montar a tela ", screenClass.getName()), e);
        }
    }

    /**
     * Organiza o pipeline de montagem, garantindo que propriedades, nós e metadados
     * sejam aplicados na ordem correta.
     *
     * @param view visão que encapsula a tela em construção
     */
    private static void build(ScreenView view) {
        // Ajusta atributos declarados na anotação da tela
        applyRootProperties(view);
        // Instancia os componentes definidos pelos campos
        createNodes(view);
        // Aplica tamanho, posição, validações e propriedades
        applyFieldMetadata(view);
        // Garante a ordem configurada nos campos
        reorderChildren(view);
    }

    /**
     * Aplica propriedades declaradas diretamente na tela raiz.
     *
     * @param view visão contendo a tela e seus metadados
     */
    private static void applyRootProperties(ScreenView view) {
        ScreenProperties properties = view.screenProperties();
        if (properties != null) {
            PROPERTIES_BINDER.applyAll(properties, view.root());
        }
    }

    /**
     * Instancia todos os elementos definidos pelos campos anotados e garante o
     * vínculo com o gerenciador de eventos.
     *
     * @param view visão com os dados necessários para criação dos nós
     */
    private static void createNodes(ScreenView view) {
        ScreenMetadata metadata = view.metadata();
        Object screenInstance = view.screenInstance();

        metadata.getFields().forEach((acronym, field) -> {
            // Habilita acesso a campos privados
            field.setAccessible(true);
            // Recupera a anotação principal do campo
            ScreenField definition = field.getAnnotation(ScreenField.class);

            // Cria o elemento associado ao campo
            Object element = instantiateFieldElement(view, field, definition);
            // Confere compatibilidade de tipos
            ensureAssignable(field, element, acronym);
            // Injeta o elemento na instância da tela
            assignField(field, screenInstance, element, acronym);

            // Cacheia o elemento para uso posterior
            ScreenManagerSharedData.setScreenData(screenInstance, acronym, element);

            if (element instanceof Node node) {
                // Conecta eventos do Node
                EventBinder.attach(acronym, node, screenInstance, metadata.callbackInstance());
            } else if (element instanceof Tab tab) {
                // Conecta eventos da Tab
                EventBinder.attach(acronym, tab, screenInstance, metadata.callbackInstance());
            }

            if (definition.father().isEmpty()) {
                // Sem pai explícito, adiciona direto na raiz
                ElementManager.addChild(view.root(), element, definition);
            }
        });
    }

    /**
     * Resolve o elemento correspondente ao campo, lidando com telas aninhadas
     * quando necessário.
     *
     * @param view       visão da tela atual
     * @param field      campo analisado
     * @param definition metadados do campo
     * @return instância criada para o campo
     */
    private static Object instantiateFieldElement(ScreenView view, Field field, ScreenField definition) {
        Class<?> type = field.getType();

        if (type.isAnnotationPresent(Screen.class)) {
            // Campos com anotação @Screen representam telas aninhadas.
            return composeNestedScreen(view, type);
        }

        ElementManager.setLiteral(definition.literal());
        return ElementManager.createElement(type);
    }

    /**
     * Monta uma tela aninhada a partir do campo anotado e devolve o nó raiz.
     *
     * @param parentView  visão da tela pai
     * @param nestedClass classe da tela filha
     * @return nó raiz da tela filha
     */
    private static Node composeNestedScreen(ScreenView parentView, Class<?> nestedClass) {
        if (!Region.class.isAssignableFrom(nestedClass)) {
            throw new IllegalStateException(StringUtils.concat(
                    "O elemento ", nestedClass.getSimpleName(),
                    " precisa estender Region para ser usado como parte de outra tela."));
        }

        ScreenView childView = compose(nestedClass, parentView.screenInstance());
        return childView.root();
    }

    /**
     * Verifica se o elemento gerado pode ser atribuído ao campo correspondente.
     *
     * @param field   campo de destino
     * @param element elemento gerado
     * @param acronym acrônimo do campo para mensagens
     */
    private static void ensureAssignable(Field field, Object element, String acronym) {
        if (!field.getType().isInstance(element)) {
            throw new RuntimeException(StringUtils.concat(
                    "Tipo incompatível ao criar elemento para o campo ", acronym));
        }
    }

    /**
     * Associa o elemento instanciado ao campo da tela.
     *
     * @param field   campo a ser preenchido
     * @param target  instância da tela
     * @param value   elemento criado
     * @param acronym acrônimo do campo para mensagens
     */
    private static void assignField(Field field, Object target, Object value, String acronym) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(StringUtils.concat(
                    "Não foi possível atribuir o elemento ao campo ", acronym), e);
        }
    }

    /**
     * Aplica metadados de tamanho, posição, propriedades e validações para cada
     * campo já instanciado.
     *
     * @param view visão contendo a instância e os metadados
     */
    private static void applyFieldMetadata(ScreenView view) {
        ScreenMetadata metadata = view.metadata();
        Object screenInstance = view.screenInstance();

        metadata.getFields().forEach((acronym, field) -> {
            ScreenField definition = field.getAnnotation(ScreenField.class);
            ScreenFieldSize size = field.getAnnotation(ScreenFieldSize.class);
            ScreenFieldPosition position = field.getAnnotation(ScreenFieldPosition.class);
            ScreenProperties props = field.getAnnotation(ScreenProperties.class);
            ScreenValidation validation = field.getAnnotation(ScreenValidation.class);

            Object element = ScreenManagerSharedData.getScreenData(screenInstance, acronym);
            applyDefaultId(element, acronym);

            if (!definition.father().isEmpty()) {
                Object parentElement = ScreenManagerSharedData.getScreenData(screenInstance, definition.father());
                ElementManager.addChild(parentElement, element, definition);
            }

            if (element instanceof Node node) {
                if (size != null) {
                    SIZE_BINDER.applyAll(size, node);
                }

                if (position != null) {
                    POSITION_BINDER.applyAll(position, node);
                }

                if (validation != null) {
                    VALIDATION_BINDER.applyAll(validation, node, acronym, screenInstance, metadata.callbackInstance());
                }

                if (props != null) {
                    PROPERTIES_BINDER.applyAll(props, node);
                }
            }

            if (element instanceof Tab tab && props != null) {
                PROPERTIES_BINDER.applyToTab(props, tab);
            }

            if (element instanceof TreeItem<?> treeItem && props != null) {
                PROPERTIES_BINDER.applyToTreeItem(props, treeItem);
            }

            if (element instanceof TableColumn<?, ?> column && props != null) {
                PROPERTIES_BINDER.applyToTableColumn(props, column);
            }
        });
    }

    /**
     * Reordena os nós filhos conforme o atributo {@code order} dos campos, quando
     * suportado pelo container.
     *
     * @param view visão da tela em processamento
     */
    private static void reorderChildren(ScreenView view) {
        Map<String, Object> cachedNodes = ScreenManagerSharedData.getCache().get(view.screenInstance());
        if (cachedNodes == null) {
            return;
        }

        Map<Region, List<NodeOrder>> grouped = new HashMap<>();

        view.metadata().getFields().forEach((acronym, field) -> {
            ScreenField definition = field.getAnnotation(ScreenField.class);
            Object parentElement = definition.father().isEmpty()
                    ? view.root()
                    : ScreenManagerSharedData.getScreenData(view.screenInstance(), definition.father());

            if (!(parentElement instanceof Region parent)) {
                // Apenas containers do tipo Region permitem reordenação.
                return;
            }

            Object storedNode = cachedNodes.get(acronym);
            if (!(storedNode instanceof Node node)) {
                return;
            }

            grouped.computeIfAbsent(parent, key -> new ArrayList<>())
                    // Guarda as combinações para posterior ordenação.
                    .add(new NodeOrder(definition.order(), acronym, node));
        });

        grouped.forEach((parent, nodes) -> {
            if (parent instanceof BorderPane) {
                // BorderPane já define posições fixas por região, não reordenamos.
                return;
            }

            if (parent instanceof Pane pane) {
                nodes.sort(Comparator
                        .comparingInt((NodeOrder nodeOrder) -> normalizedOrder(nodeOrder.order()))
                        .thenComparing(NodeOrder::acronym));

                List<Node> orderedNodes = nodes.stream()
                        .map(NodeOrder::node)
                        .toList();
                // Substitui a lista de filhos para garantir a ordem coerente.
                pane.getChildren().setAll(orderedNodes);
            }
        });
    }

    /**
     * Normaliza valores de ordenação, empurrando índices não positivos para o fim
     * da lista.
     *
     * @param order valor original informado no campo
     * @return valor ajustado para comparação
     */
    private static int normalizedOrder(int order) {
        return order <= 0 ? Integer.MAX_VALUE : order;
    }

    /**
     * Estrutura auxiliar para armazenar informações de ordenação de nós.
     */
    private record NodeOrder(int order, String acronym, Node node) {
    }

    /**
     * Invoca o método de configuração da tela, respeitando o contexto de metadados.
     *
     * @param view visão da tela pronta para configuração
     */
    private static void invokeScreenConfig(ScreenView view) {
        Object callbacksInstance = view.metadata().callbackInstance();
        if (callbacksInstance == null) {
            return;
        }

        // Garantimos que o ScreenManager conheça o contexto atual antes da
        // configuração.
        ScreenManager.pushCompositionMetadata(view.metadata());
        try {
            CallbackInvoker.call(callbacksInstance, view.screenInstance(), "config",
                    view.screenClass().getSimpleName());
        } finally {
            // Restaura o contexto anterior mesmo em caso de exceção.
            ScreenManager.popCompositionMetadata(view.metadata());
        }
    }

    /**
     * Aplica um ID padrão ao elemento, baseado no acrônimo do campo.
     */
    private static void applyDefaultId(Object element, String acronym) {
        if (StringUtils.isBlank(acronym) || element == null) {
            return;
        }

        if (element instanceof Node node) {
            if (node.getId() == null || node.getId().isBlank()) {
                node.setId(acronym);
            }
            return;
        }

        if (element instanceof Tab tab) {
            if (tab.getId() == null || tab.getId().isBlank()) {
                tab.setId(acronym);
            }
        }
    }
}
