package com.connectasistemas.framework.core;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link com.connectasistemas.framework.internal.sizes.SizeBinderGeneric}.
 * Camada 1 — verificação de tamanhos aplicados após compose.
 */
class SizeBinderGenericTest {

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

    // ---- Cenário 1: width = 260 aplicado em filterInput ----

    @Test
    @DisplayName("filterInput deve ter prefWidth = 260")
    void shouldApplyWidthToFilterInput() {
        TextField filterInput = (TextField) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "filterInput");

        assertEquals(260, filterInput.getPrefWidth(), 0.1,
                "prefWidth do filterInput deve ser 260");
    }

    // ---- Cenário 2: vgrow = true em featureTree ----

    @Test
    @DisplayName("featureTree deve ter VBox.vgrow = ALWAYS")
    void shouldApplyVgrowToFeatureTree() {
        Node featureTree = ScreenManagerSharedData.getScreenDataAsNode(
                view.screenInstance(), "featureTree");

        assertEquals(Priority.ALWAYS, VBox.getVgrow(featureTree),
                "VBox.getVgrow(featureTree) deve ser ALWAYS");
    }

    // ---- Cenário 4: padding = {16,16,16,16} em explorerPanel ----

    @Test
    @DisplayName("explorerPanel deve ter padding de 16 em todos os lados")
    void shouldApplyPaddingToExplorerPanel() {
        Region explorerPanel = ScreenManagerSharedData.getScreenDataAsRegion(
                view.screenInstance(), "explorerPanel");

        Insets expected = new Insets(16, 16, 16, 16);
        assertEquals(expected, explorerPanel.getPadding(),
                "Padding do explorerPanel deve ser (16,16,16,16)");
    }

    // ---- Cenário 5: spacing = 12 em explorerPanel (VBox) ----

    @Test
    @DisplayName("explorerPanel (VBox) deve ter spacing = 12")
    void shouldApplySpacingToExplorerPanel() {
        VBox explorerPanel = (VBox) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "explorerPanel");

        assertEquals(12, explorerPanel.getSpacing(), 0.1,
                "Spacing do explorerPanel deve ser 12");
    }

    // ---- Cenário 6: height = 220 em eventLogList ----

    @Test
    @DisplayName("eventLogList deve ter prefHeight = 220")
    void shouldApplyHeightToEventLogList() {
        ListView<?> eventLogList = (ListView<?>) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "eventLogList");

        assertEquals(220, eventLogList.getPrefHeight(), 0.1,
                "prefHeight do eventLogList deve ser 220");
    }

    // ---- Cenário 7: minHeight = 260 em featureTree ----

    @Test
    @DisplayName("featureTree deve ter minHeight = 260")
    void shouldApplyMinHeightToFeatureTree() {
        TreeView<?> featureTree = (TreeView<?>) ScreenManagerSharedData.getScreenData(
                view.screenInstance(), "featureTree");

        assertEquals(260, featureTree.getMinHeight(), 0.1,
                "minHeight do featureTree deve ser 260");
    }

    // ---- Cenário extra: statusBar padding ----

    @Test
    @DisplayName("statusBar deve ter padding de (6,12,6,12)")
    void shouldApplyPaddingToStatusBar() {
        Region statusBar = ScreenManagerSharedData.getScreenDataAsRegion(
                view.screenInstance(), "statusBar");

        Insets expected = new Insets(6, 12, 6, 12);
        assertEquals(expected, statusBar.getPadding(),
                "Padding do statusBar deve ser (6,12,6,12)");
    }
}
