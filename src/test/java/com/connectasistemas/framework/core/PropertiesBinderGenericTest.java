package com.connectasistemas.framework.core;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link com.connectasistemas.framework.internal.utils.PropertiesBinderGeneric}.
 * Camada 1 — verificação de propriedades visuais após compose.
 */
class PropertiesBinderGenericTest {

    private ScreenView view;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        view = ScreenAssembler.compose(Example.class);
    }

    @AfterEach
    void tearDown() {
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- Cenário 1: focusTraversable = false em statusBar ----

    @Test
    @DisplayName("statusBar deve ter focusTraversable = false")
    void shouldDisableFocusTraversableOnStatusBar() {
        Node statusBar = ScreenManagerSharedData.getScreenDataAsNode(
                view.screenInstance(), "statusBar");

        assertFalse(statusBar.isFocusTraversable(),
                "statusBar.isFocusTraversable() deve ser false");
    }

    // ---- Cenário 2: styleClass = "status-bar" em statusBar ----

    @Test
    @DisplayName("statusBar deve ter styleClass 'status-bar'")
    void shouldApplyStyleClassToStatusBar() {
        Node statusBar = ScreenManagerSharedData.getScreenDataAsNode(
                view.screenInstance(), "statusBar");

        assertTrue(statusBar.getStyleClass().contains("status-bar"),
                "statusBar deve conter styleClass 'status-bar'");
    }

    // ---- Cenário 3: cursor = HAND em addFolderButton ----

    @Test
    @DisplayName("addFolderButton deve ter cursor = HAND")
    void shouldApplyHandCursorToButton() {
        Node addFolderButton = ScreenManagerSharedData.getScreenDataAsNode(
                view.screenInstance(), "addFolderButton");

        assertEquals(Cursor.HAND, addFolderButton.getCursor(),
                "addFolderButton deve ter cursor HAND");
    }

    // ---- Cenário 4: tooltip em addFolderButton ----

    @Test
    @DisplayName("addFolderButton deve ter tooltip 'Abre o editor de pastas'")
    void shouldApplyTooltipToButton() {
        Button addFolderButton = (Button) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "addFolderButton");

        assertNotNull(addFolderButton.getTooltip(),
                "addFolderButton deve ter tooltip definido");
        assertEquals("Abre o editor de pastas", addFolderButton.getTooltip().getText(),
                "Texto do tooltip deve ser 'Abre o editor de pastas'");
    }

    // ---- Cenário 5: wrapText = true em explorerHeader ----

    @Test
    @DisplayName("explorerHeader deve ter wrapText = true")
    void shouldApplyWrapTextToLabel() {
        Label explorerHeader = (Label) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "explorerHeader");

        assertTrue(explorerHeader.isWrapText(),
                "explorerHeader.isWrapText() deve ser true");
    }

    // ---- Cenário 6: expanded = true em featureRoot ----

    @Test
    @DisplayName("featureRoot deve estar expandido")
    void shouldExpandTreeItem() {
        @SuppressWarnings("unchecked")
        TreeItem<String> featureRoot = (TreeItem<String>) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "featureRoot");

        assertTrue(featureRoot.isExpanded(),
                "featureRoot.isExpanded() deve ser true");
    }

    // ---- Cenário 8: tooltip em overviewTab ----

    @Test
    @DisplayName("overviewTab deve ter tooltip 'Resumo dos elementos'")
    void shouldApplyTooltipToTab() {
        Tab overviewTab = (Tab) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "overviewTab");

        assertNotNull(overviewTab.getTooltip(),
                "overviewTab deve ter tooltip");
        assertEquals("Resumo dos elementos", overviewTab.getTooltip().getText(),
                "Texto do tooltip da aba deve ser 'Resumo dos elementos'");
    }

    // ---- Cenário extra: focusTraversable = false em eventLogList ----

    @Test
    @DisplayName("eventLogList deve ter focusTraversable = false")
    void shouldDisableFocusOnEventLogList() {
        Node eventLogList = ScreenManagerSharedData.getScreenDataAsNode(
                view.screenInstance(), "eventLogList");

        assertFalse(eventLogList.isFocusTraversable(),
                "eventLogList.isFocusTraversable() deve ser false");
    }

    // ---- Cenário extra: styleClass em explorerHeader ----

    @Test
    @DisplayName("explorerHeader deve ter styleClass 'section-title'")
    void shouldApplyStyleClassToExplorerHeader() {
        Node explorerHeader = ScreenManagerSharedData.getScreenDataAsNode(
                view.screenInstance(), "explorerHeader");

        assertTrue(explorerHeader.getStyleClass().contains("section-title"),
                "explorerHeader deve conter styleClass 'section-title'");
    }
}
