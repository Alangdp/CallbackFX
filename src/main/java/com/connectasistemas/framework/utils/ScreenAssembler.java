package com.connectasistemas.framework.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.processor.AnnotationProcessor;
import com.connectasistemas.framework.utils.position.PositionBinderGeneric;
import com.connectasistemas.framework.utils.properties.PropertiesBinderGeneric;
import com.connectasistemas.framework.utils.sizes.SizeBinderGeneric;
import com.connectasistemas.framework.utils.validation.ValidationBinderGeneric;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Responsável por transformar classes anotadas com {@code @Screen} em árvores de
 * nós reutilizáveis, sem acoplar o processo à troca de {@link javafx.stage.Stage}.
 */
public final class ScreenAssembler {

    private static final SizeBinderGeneric SIZE_BINDER = new SizeBinderGeneric();
    private static final PositionBinderGeneric POSITION_BINDER = new PositionBinderGeneric();
    private static final PropertiesBinderGeneric PROPERTIES_BINDER = new PropertiesBinderGeneric();
    private static final ValidationBinderGeneric VALIDATION_BINDER = new ValidationBinderGeneric();

    private ScreenAssembler() {
    }

    public static ScreenView compose(Class<?> screenClass) {
        return compose(screenClass, null);
    }

    public static ScreenView compose(Class<?> screenClass, Object parentInstance) {
        try {
            Object screenInstance = screenClass.getDeclaredConstructor().newInstance();
            AnnotationProcessor processor = new AnnotationProcessor();
            ScreenMetadata metadata = processor.processScreen(screenClass);
            ScreenProperties screenProperties = screenClass.getAnnotation(ScreenProperties.class);

            if (screenInstance instanceof Region regionInstance) {
                metadata.overrideRoot(regionInstance);
            }

            ScreenView view = new ScreenView(screenClass, screenInstance, metadata, screenProperties);
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

    private static void build(ScreenView view) {
        applyRootProperties(view);
        createNodes(view);
        applyFieldMetadata(view);
        reorderChildren(view);
    }

    private static void applyRootProperties(ScreenView view) {
        ScreenProperties properties = view.screenProperties();
        if (properties != null) {
            PROPERTIES_BINDER.applyAll(properties, view.root());
        }
    }

    private static void createNodes(ScreenView view) {
        ScreenMetadata metadata = view.metadata();
        Object screenInstance = view.screenInstance();

        metadata.getFields().forEach((acronym, field) -> {
            field.setAccessible(true);
            ScreenField definition = field.getAnnotation(ScreenField.class);

            Node node = instantiateFieldNode(view, field, definition);
            ensureAssignable(field, node, acronym);
            assignField(field, screenInstance, node, acronym);

            ScreenManagerSharedData.setScreenData(screenInstance, acronym, node);
            EventBinder.attach(acronym, node, screenInstance, metadata.callbackInstance());

            if (definition.father().isEmpty()) {
                ElementManager.addChild(view.root(), node, definition);
            }
        });
    }

    private static Node instantiateFieldNode(ScreenView view, Field field, ScreenField definition) {
        Class<?> type = field.getType();

        if (type.isAnnotationPresent(Screen.class)) {
            return composeNestedScreen(view, type);
        }

        ElementManager.setLiteral(definition.literal());
        return ElementManager.createElement(type);
    }

    private static Node composeNestedScreen(ScreenView parentView, Class<?> nestedClass) {
        if (!Region.class.isAssignableFrom(nestedClass)) {
            throw new IllegalStateException(StringUtils.concat(
                    "O elemento ", nestedClass.getSimpleName(),
                    " precisa estender Region para ser usado como parte de outra tela."));
        }

        ScreenView childView = compose(nestedClass, parentView.screenInstance());
        return childView.root();
    }

    private static void ensureAssignable(Field field, Node node, String acronym) {
        if (!field.getType().isInstance(node)) {
            throw new RuntimeException(StringUtils.concat(
                    "Tipo incompatível ao criar elemento para o campo ", acronym));
        }
    }

    private static void assignField(Field field, Object target, Node value, String acronym) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(StringUtils.concat(
                    "Não foi possível atribuir o elemento ao campo ", acronym), e);
        }
    }

    private static void applyFieldMetadata(ScreenView view) {
        ScreenMetadata metadata = view.metadata();
        Object screenInstance = view.screenInstance();

        metadata.getFields().forEach((acronym, field) -> {
            ScreenField definition = field.getAnnotation(ScreenField.class);
            ScreenFieldSize size = field.getAnnotation(ScreenFieldSize.class);
            ScreenFieldPosition position = field.getAnnotation(ScreenFieldPosition.class);
            ScreenProperties props = field.getAnnotation(ScreenProperties.class);
            ScreenValidation validation = field.getAnnotation(ScreenValidation.class);

            Node node = ScreenManagerSharedData.getScreenData(screenInstance, acronym);

            if (!definition.father().isEmpty()) {
                Region father = ScreenManagerSharedData.getScreenDataAsRegion(screenInstance, definition.father());
                ElementManager.addChild(father, node, definition);
            }

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
        });
    }

    private static void reorderChildren(ScreenView view) {
        Map<String, Node> cachedNodes = ScreenManagerSharedData.getCache().get(view.screenInstance());
        if (cachedNodes == null) {
            return;
        }

        Map<Region, List<NodeOrder>> grouped = new HashMap<>();

        view.metadata().getFields().forEach((acronym, field) -> {
            ScreenField definition = field.getAnnotation(ScreenField.class);
            Region parent = definition.father().isEmpty()
                    ? view.root()
                    : ScreenManagerSharedData.getScreenDataAsRegion(view.screenInstance(), definition.father());

            Node node = cachedNodes.get(acronym);
            if (node == null) {
                return;
            }

            grouped.computeIfAbsent(parent, key -> new ArrayList<>())
                    .add(new NodeOrder(definition.order(), acronym, node));
        });

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

    private static int normalizedOrder(int order) {
        return order <= 0 ? Integer.MAX_VALUE : order;
    }

    private record NodeOrder(int order, String acronym, Node node) {
    }

    private static void invokeScreenConfig(ScreenView view) {
        Object callbacksInstance = view.metadata().callbackInstance();
        if (callbacksInstance == null) {
            return;
        }

        CallbackInvoker.call(callbacksInstance, view.screenInstance(), "config", view.screenClass().getSimpleName());
    }
}
