package com.connectasistemas.framework.core;

import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.utils.ElementManager;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link ElementManager}.
 * Camada 1 — motor do framework.
 */
class ElementManagerTest {

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    // ---- Cenário 1: createElement para TextField ----

    @Test
    @DisplayName("createElement(TextField.class) deve retornar instância de TextField")
    void shouldCreateTextField() {
        Object element = ElementManager.createElement(TextField.class);
        assertInstanceOf(TextField.class, element, "Deve retornar TextField");
    }

    // ---- Cenário 2: createElement para Label com literal ----

    @Test
    @DisplayName("createElement(Label.class) com literal deve aplicar texto")
    void shouldCreateLabelWithLiteral() {
        Object element = ElementManager.createElement(Label.class, "Texto teste");

        assertInstanceOf(Label.class, element, "Deve retornar Label");
        assertEquals("Texto teste", ((Label) element).getText(),
                "Label deve ter o texto definido via literal");
    }

    // ---- Cenário 3: createElement para Button com literal ----

    @Test
    @DisplayName("createElement(Button.class) com literal deve aplicar texto")
    void shouldCreateButtonWithLiteral() {
        Object element = ElementManager.createElement(Button.class, "Clique aqui");

        assertInstanceOf(Button.class, element, "Deve retornar Button");
        assertEquals("Clique aqui", ((Button) element).getText(),
                "Button deve ter o texto definido via literal");
    }

    // ---- Cenário 4: addChild em BorderPane com Position.TOP ----

    @Test
    @DisplayName("addChild em BorderPane com Position.TOP deve colocar no topo")
    void shouldAddChildToBorderPaneTop() throws NoSuchFieldException {
        BorderPane borderPane = new BorderPane();
        Label child = new Label("Topo");

        // Usa reflexão para obter um ScreenField com position=TOP
        ScreenField metadata = CampoPosicaoTop.class
                .getDeclaredField("campo")
                .getAnnotation(ScreenField.class);

        ElementManager.addChild(borderPane, child, metadata);

        assertSame(child, borderPane.getTop(),
                "Label deve ser colocado no topo do BorderPane");
    }

    // ---- Cenário 5: addChild em TabPane com Tab ----

    @Test
    @DisplayName("addChild em TabPane com Tab deve adicionar a aba")
    void shouldAddTabToTabPane() {
        TabPane tabPane = new TabPane();
        Tab tab = new Tab("Aba 1");

        ElementManager.addChild(tabPane, tab, null);

        assertTrue(tabPane.getTabs().contains(tab),
                "TabPane deve conter a aba adicionada");
    }

    // ---- Cenário 6: addChild em TreeView com TreeItem ----

    @Test
    @DisplayName("addChild em TreeView com TreeItem deve definir como root")
    void shouldSetTreeItemAsRoot() {
        TreeView<String> treeView = new TreeView<>();
        TreeItem<String> root = new TreeItem<>("Raiz");

        ElementManager.addChild(treeView, root, null);

        assertSame(root, treeView.getRoot(),
                "TreeItem deve ser definido como root da TreeView");
    }

    // ---- Cenário 7: addChild em Pane genérico ----

    @Test
    @DisplayName("addChild em Pane genérico deve adicionar à lista de filhos")
    void shouldAddChildToPane() {
        VBox pane = new VBox();
        Label child = new Label("Filho");

        ElementManager.addChild(pane, child, null);

        assertTrue(pane.getChildren().contains(child),
                "Pane genérico deve conter o filho adicionado");
    }

    // ---- Cenário 8: createElement com MetricsCard (custom=true) ----

    @Test
    @DisplayName("createElement(MetricsCard.class) deve retornar instância de MetricsCard")
    void shouldCreateCustomElement() {
        Object element = ElementManager.createElement(Example.MetricsCard.class);

        assertInstanceOf(Example.MetricsCard.class, element,
                "Deve retornar MetricsCard");
        assertInstanceOf(VBox.class, element,
                "MetricsCard deve ser VBox");
    }

    // ---- Cenário 9: tipo desconhecido lança exceção ----

    @Test
    @DisplayName("createElement com tipo desconhecido deve lançar RuntimeException")
    void shouldThrowForUnknownType() {
        assertThrows(RuntimeException.class,
                () -> ElementManager.createElement(UnknownWidget.class),
                "Tipo desconhecido deve lançar exceção");
    }

    // ---- Classes auxiliares ----

    // Widget fictício não registrado
    static class UnknownWidget extends javafx.scene.canvas.Canvas {
    }

    // Campo com position TOP para teste
    static class CampoPosicaoTop {
        @ScreenField(acronym = "campo", position = Position.TOP)
        public Label campo;
    }
}
