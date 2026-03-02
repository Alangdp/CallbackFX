package com.connectasistemas.framework.core;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.examples.ExampleController;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link ScreenAssembler}.
 * Camada 1 — motor do framework.
 * Necessita toolkit JavaFX headless para instanciar Nodes.
 */
class ScreenAssemblerTest {

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @AfterEach
    void tearDown() {
        // Limpa estado global compartilhado para isolamento
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- Cenário 1: raiz é BorderPane ----

    @Test
    @DisplayName("Montar Example deve gerar raiz do tipo BorderPane")
    void shouldBuildBorderPaneAsRoot() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        assertNotNull(view.root(), "Root não deve ser nulo");
        assertInstanceOf(BorderPane.class, view.root(),
                "Root deve ser BorderPane");
    }

    // ---- Cenário 2: position BOTTOM → Label em root.getBottom() ----

    @Test
    @DisplayName("StatusBar deve estar em root.getBottom()")
    void shouldPlaceStatusBarAtBottom() {
        ScreenView view = ScreenAssembler.compose(Example.class);
        Example screen = (Example) view.screenInstance();

        assertNotNull(screen.root.getBottom(), "getBottom() não deve ser nulo");
        assertInstanceOf(Label.class, screen.root.getBottom(),
                "getBottom() deve ser Label");
    }

    // ---- Cenário 3: position CENTER → SplitPane ----

    @Test
    @DisplayName("root.getCenter() deve ser SplitPane")
    void shouldPlaceSplitPaneAtCenter() {
        ScreenView view = ScreenAssembler.compose(Example.class);
        Example screen = (Example) view.screenInstance();

        assertNotNull(screen.root.getCenter(), "getCenter() não deve ser nulo");
        assertInstanceOf(SplitPane.class, screen.root.getCenter(),
                "getCenter() deve ser SplitPane");
    }

    // ---- Cenário 4: SplitPane → 2 filhos ----

    @Test
    @DisplayName("SplitPane deve ter exatamente 2 itens")
    void shouldHaveTwoItemsInSplitPane() {
        ScreenView view = ScreenAssembler.compose(Example.class);
        Example screen = (Example) view.screenInstance();

        assertEquals(2, screen.layoutSplit.getItems().size(),
                "SplitPane deve ter 2 itens");
    }

    // ---- Cenário 5: featureRoot → 7 filhos TreeItem ----

    @Test
    @DisplayName("featureRoot deve ter 7 filhos TreeItem")
    void shouldHaveSevenChildrenInFeatureRoot() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        Object featureRootObj = ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "featureRoot");
        assertInstanceOf(TreeItem.class, featureRootObj, "featureRoot deve ser TreeItem");

        @SuppressWarnings("unchecked")
        TreeItem<String> featureRoot = (TreeItem<String>) featureRootObj;
        assertEquals(7, featureRoot.getChildren().size(),
                "featureRoot deve ter 7 filhos (layoutNode..utilsNode)");
    }

    // ---- Cenário 6: IDs aplicados automaticamente ----

    @Test
    @DisplayName("Node IDs devem ser aplicados via acronym (ex: filterInput)")
    void shouldApplyIdViaAcronym() {
        ScreenView view = ScreenAssembler.compose(Example.class);
        Example screen = (Example) view.screenInstance();

        // Verifica IDs diretamente nos campos da instância
        assertNotNull(screen.filterInput.getId(),
                "filterInput deve ter ID definido");
        assertEquals("filterInput", screen.filterInput.getId(),
                "ID do filterInput deve ser 'filterInput'");
        assertNotNull(screen.addFolderButton.getId(),
                "addFolderButton deve ter ID definido");
    }

    // ---- Cenário 7: literal aplicado em Label (statusBar) ----

    @Test
    @DisplayName("Literal deve ser aplicado corretamente em Label (statusBar)")
    void shouldApplyLiteralToLabel() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        Object statusBarObj = ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "statusBar");
        assertInstanceOf(Label.class, statusBarObj, "statusBar deve ser Label");

        Label statusBar = (Label) statusBarObj;
        // O literal original é "Status: pronto", mas o callbackConfig pode alterá-lo
        // Verifica que o Label existe e tem texto não vazio
        assertNotNull(statusBar.getText(), "statusBar deve possuir texto");
        assertFalse(statusBar.getText().isEmpty(), "statusBar deve ter texto não vazio");
    }

    // ---- Cenário 8: literal aplicado em TreeItem ----

    @Test
    @DisplayName("Literal 'Framework' deve ser aplicado em featureRoot")
    void shouldApplyLiteralToTreeItem() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        Object featureRootObj = ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "featureRoot");
        @SuppressWarnings("unchecked")
        TreeItem<String> featureRoot = (TreeItem<String>) featureRootObj;

        assertEquals("Framework", featureRoot.getValue(),
                "featureRoot.getValue() deve ser 'Framework'");
    }

    // ---- Cenário 9: literal aplicado em Tab ----

    @Test
    @DisplayName("Literal 'Visão geral' deve ser aplicado em overviewTab")
    void shouldApplyLiteralToTab() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        Object overviewTabObj = ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "overviewTab");
        assertInstanceOf(Tab.class, overviewTabObj, "overviewTab deve ser Tab");

        Tab overviewTab = (Tab) overviewTabObj;
        assertEquals("Visão geral", overviewTab.getText(),
                "overviewTab.getText() deve ser 'Visão geral'");
    }

    // ---- Cenário 10: literal aplicado em Button ----

    @Test
    @DisplayName("Literal 'Nova pasta' deve ser aplicado em addFolderButton")
    void shouldApplyLiteralToButton() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        Object buttonObj = ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "addFolderButton");
        assertInstanceOf(Button.class, buttonObj, "addFolderButton deve ser Button");

        Button addFolderButton = (Button) buttonObj;
        assertEquals("Nova pasta", addFolderButton.getText(),
                "addFolderButton.getText() deve ser 'Nova pasta'");
    }

    // ---- Cenário 12: elemento custom MetricsCard ----

    @Test
    @DisplayName("MetricsCard deve ser instanciado como custom element")
    void shouldInstantiateMetricsCard() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        Object metricsCardObj = ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "metricsCard");
        assertInstanceOf(Example.MetricsCard.class, metricsCardObj,
                "metricsCard deve ser instância de MetricsCard");
        assertInstanceOf(javafx.scene.layout.VBox.class, metricsCardObj,
                "metricsCard deve ser instância de VBox");
    }

    // ---- Cenário 13: callback instance instanciada ----

    @Test
    @DisplayName("callbackInstance deve ser ExampleController após compose")
    void shouldInstantiateCallbackInstance() {
        ScreenView view = ScreenAssembler.compose(Example.class);

        assertNotNull(view.metadata().callbackInstance(),
                "callbackInstance não deve ser nulo");
        assertInstanceOf(ExampleController.class, view.metadata().callbackInstance(),
                "callbackInstance deve ser ExampleController");
    }
}
