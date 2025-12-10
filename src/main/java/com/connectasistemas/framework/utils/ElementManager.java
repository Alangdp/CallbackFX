package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.interfaces.CustomElement;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.utils.position.BorderPanePosition;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Gerencia a criação e modificação de elementos do tipo Node em tela
 */
public class ElementManager {

    // Registro de tipos suportados
    private static final Map<Class<?>, Supplier<Object>> registry = new HashMap<>();
    private static final BorderPanePosition borderPanePosition = new BorderPanePosition();
    private static String literal = "";

    static {
        // Registro padrão
        registry.put(TextField.class, TextField::new);
        registry.put(Label.class, Label::new);
        registry.put(CheckBox.class, CheckBox::new);
        registry.put(PasswordField.class, PasswordField::new);
        registry.put(Button.class,  Button::new);
        registry.put(TableView.class, TableView::new);
        registry.put(Tab.class, Tab::new);

        // Registros personalizados
        registry.put(TextEntryLabel.class, () -> new TextEntryLabel(literal));
        registry.put(CheckEntryLabel.class, () -> new CheckEntryLabel(literal));
        registry.put(ImageView.class, ImageView::new);

        // Criação de Region
        registry.put(Region.class, Region::new);
        registry.put(BorderPane.class, BorderPane::new);
        registry.put(VBox.class, VBox::new);
        registry.put(HBox.class, HBox::new);
        registry.put(SplitPane.class, SplitPane::new);

        // Lista/Tabelas
        registry.put(ListView.class, ListView::new);
        registry.put(TreeView.class, TreeView::new);
        
        // TabPane
        registry.put(TabPane.class, TabPane::new);
    }

    /**
     * Cria um elemento vazio do tipo recebido
     *
     * @param type Classe do tipo que deseja criar o elemento
     * @return Elemento do tipo recebido
     */
    public static Object createElement(Class<?> type) {
        // Cria o node vazio
        Object node = createRegisteredElement(type);

        // Aplica literal ao elemento (Quando aplicável)
        ElementManager.applyLiteral(node, ElementManager.literal);

        // Retorna o elemento criado
        return node;
    }

    /**
     * Cria um elemento vazio do tipo recebido
     *
     * @param type Classe do tipo que deseja criar o elemento
     * @return Elemento do tipo recebido
     */
    private static Object createRegisteredElement(Class<?> type) {
        Supplier<Object> creator = registry.get(type);
        if (creator != null) {
            return creator.get();
        }

        if (CustomElement.class.isAssignableFrom(type)) {
            return createCustomElement(type);
        }

        throw new RuntimeException(StringUtils.concat("Tipo inválido: ", type.getName()));
    }

    /**
     * Adiciona um filho a um Region conforme o tipo do Region
     *
     * @param parent   Container pai (Region ou Tab)
     * @param child    Elemento filho (Node ou Tab, conforme o pai)
     */
    public static void addChild(Object parent, Object child, ScreenField metadata) {
        if (parent instanceof Tab tab) {
            addChildToTab(tab, child);
            return;
        }

        if (!(parent instanceof Region region)) {
            throw new RuntimeException(StringUtils.concat(
                    "Tipo não permitido para adicionar elementos: ",
                    parent != null ? parent.getClass().getName() : "null"));
        }

        Position position = metadata != null ? metadata.position() : Position.CENTER;

        if (region instanceof BorderPane borderPane) {
            Node nodeChild = requireNodeChild(child, borderPane.getClass().getSimpleName());
            borderPanePosition.apply(borderPane, nodeChild, position);
            return;
        }

        if (region instanceof TabPane tabPane) {
            addChildToTabPane(tabPane, child, metadata);
            return;
        }

        if (region instanceof Pane pane) {
            Node nodeChild = requireNodeChild(child, pane.getClass().getSimpleName());
            pane.getChildren().add(nodeChild);
            return;
        }

        if (region instanceof SplitPane splitPane) {
            Node nodeChild = requireNodeChild(child, splitPane.getClass().getSimpleName());
            splitPane.getItems().add(nodeChild);
            return;
        }

        throw new RuntimeException(StringUtils.concat(
                "Tipo não permitido para adicionar elementos: ",
                region.getClass().getName()));
    }

    public static void setLiteral(String literal) {
        ElementManager.literal = literal;
    }

    /**
     * Aplica o literal (texto) ao componente conforme seu tipo
     */
    private static void applyLiteral(Object element, String literal) {
        if (literal == null || literal.isEmpty() || element == null) {
            ElementManager.literal = "";
            return;
        }

        if (element instanceof Labeled labeled) {
            labeled.setText(literal);
        } else if (element instanceof TextInputControl textInput) {
            textInput.setPromptText(literal);
        } else if (element instanceof Tab tab) {
            tab.setText(literal);
        }

        ElementManager.literal = "";
    }

    /**
     * Cria um elemento customizado a partir de uma classe
     * OBS: Segue algumas regras.
     * - Deve ser um Node
     * - Deve implementar CustomElement
     * - Deve ser um Region
     * 
     * @param type Classe do elemento customizado
     * @return Instância do elemento customizado
     */
    private static Node createCustomElement(Class<?> type) {
        try {
            Object instance = type.getDeclaredConstructor().newInstance();
            if (!(instance instanceof Node node)) {
                throw new RuntimeException("Elementos customizados precisam estender Node: " + type.getName());
            }

            if (!(instance instanceof CustomElement customElement)) {
                throw new RuntimeException("Elemento customizado inválido: " + type.getName());
            }

            if (!(node instanceof Region regionNode)) {
                throw new RuntimeException("Elementos customizados precisam estender Region: " + type.getName());
            }

            Class<? extends Region> declaredType = customElement.getType();
            if (declaredType != null && !declaredType.isAssignableFrom(node.getClass())) {
                throw new RuntimeException(StringUtils.concat(
                    "O elemento customizado ", type.getName(),
                    " deve estender ", declaredType.getName(), "."));
            }

            customElement.onElementCreated(regionNode);
            return node;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.concat(
                    "Não foi possível criar o elemento customizado: ", type.getName()), e);
        }
    }

    /**
     * Adiciona um filho a um TabPane conforme o tipo do TabPane
     * 
     * @param tabPane TabPane pai
     * @param child   Node filho
     * @param metadata Metadados do campo da tela
     */
    private static void addChildToTabPane(TabPane tabPane, Object child, ScreenField metadata) {
        Tab tab;

        if (child instanceof Tab childTab) {
            tab = childTab;
        } else {
            Node nodeChild = requireNodeChild(child, tabPane.getClass().getSimpleName());
            tab = new Tab();
            tab.setContent(nodeChild);
            tab.setClosable(false);
        }

        if (metadata != null) {
            String title = StringUtils.isBlank(metadata.literal()) ? metadata.acronym() : metadata.literal();
            if (StringUtils.isBlank(tab.getText())) {
                tab.setText(title);
            }
        }

        tabPane.getTabs().add(tab);
    }

    private static void addChildToTab(Tab tab, Object child) {
        Node nodeChild = requireNodeChild(child, Tab.class.getSimpleName());

        if (tab.getContent() != null) {
            throw new RuntimeException(StringUtils.concat(
                    "Tab já possui conteúdo definido: ", tab.getText()));
        }

        tab.setContent(nodeChild);
    }

    private static Node requireNodeChild(Object child, String parentType) {
        if (child instanceof Node node) {
            return node;
        }

        throw new RuntimeException(StringUtils.concat(
                "Apenas Nodes podem ser filhos de ", parentType, ": ",
                child != null ? child.getClass().getName() : "null"));
    }
}