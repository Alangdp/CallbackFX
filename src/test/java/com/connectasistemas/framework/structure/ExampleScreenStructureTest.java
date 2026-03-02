package com.connectasistemas.framework.structure;

import com.connectasistemas.framework.internal.examples.Example;
import com.connectasistemas.framework.internal.utils.ScreenAssembler;
import com.connectasistemas.framework.internal.utils.ScreenHierarchyRegistry;
import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;
import com.connectasistemas.framework.internal.utils.records.ScreenView;
import com.connectasistemas.framework.core.JavaFXTestHelper;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes estruturais da tela {@link Example}.
 * Camada 3 — verificação da árvore de nós sem interação.
 */
class ExampleScreenStructureTest {

    private ScreenView view;
    private Example screen;

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @BeforeEach
    void setUp() {
        view = ScreenAssembler.compose(Example.class);
        screen = (Example) view.screenInstance();
    }

    @AfterEach
    void tearDown() {
        ScreenManagerSharedData.resetScreenData();
        ScreenHierarchyRegistry.clear();
    }

    // ---- Cenário 1: Raiz é BorderPane ----

    @Test
    @DisplayName("Raiz da tela deve ser BorderPane")
    void shouldHaveBorderPaneAsRoot() {
        assertInstanceOf(BorderPane.class, screen.root,
                "root deve ser instância de BorderPane");
    }

    // ---- Cenário 2: Bottom contém Label de status ----

    @Test
    @DisplayName("Bottom da raiz deve conter Label de status")
    void shouldHaveStatusLabelAtBottom() {
        assertInstanceOf(Label.class, screen.root.getBottom(),
                "root.getBottom() deve ser uma Label");
    }

    // ---- Cenário 3: Center contém SplitPane ----

    @Test
    @DisplayName("Center da raiz deve conter SplitPane")
    void shouldHaveSplitPaneAtCenter() {
        assertInstanceOf(SplitPane.class, screen.root.getCenter(),
                "root.getCenter() deve ser SplitPane");
    }

    // ---- Cenário 4: SplitPane tem 2 itens ----

    @Test
    @DisplayName("SplitPane deve ter exatamente 2 itens")
    void shouldHaveTwoItemsInSplit() {
        assertEquals(2, screen.layoutSplit.getItems().size(),
                "layoutSplit deve ter 2 itens");
    }

    // ---- Cenário 5: Primeiro item do split é VBox ----

    @Test
    @DisplayName("Primeiro item do SplitPane deve ser VBox (explorer)")
    void shouldHaveVBoxAsFirstSplitItem() {
        assertInstanceOf(VBox.class, screen.layoutSplit.getItems().get(0),
                "Primeiro item deve ser VBox");
    }

    // ---- Cenário 6: Segundo item do split é TabPane ----

    @Test
    @DisplayName("Segundo item do SplitPane deve ser TabPane")
    void shouldHaveTabPaneAsSecondSplitItem() {
        assertInstanceOf(TabPane.class, screen.layoutSplit.getItems().get(1),
                "Segundo item deve ser TabPane");
    }

    // ---- Cenário 7: TabPane tem 3 abas ----

    @Test
    @DisplayName("TabPane deve ter 3 abas")
    void shouldHaveThreeTabs() {
        assertEquals(3, screen.contentTabs.getTabs().size(),
                "contentTabs deve ter 3 abas");
    }

    // ---- Cenário 8: Abas na ordem correta ----

    @Test
    @DisplayName("Abas devem estar na ordem: Visão geral, Detalhes, Insights")
    void shouldHaveTabsInCorrectOrder() {
        assertEquals("Visão geral", screen.contentTabs.getTabs().get(0).getText(),
                "Primeira aba deve ser 'Visão geral'");
        assertEquals("Detalhes", screen.contentTabs.getTabs().get(1).getText(),
                "Segunda aba deve ser 'Detalhes'");
        assertEquals("Insights", screen.contentTabs.getTabs().get(2).getText(),
                "Terceira aba deve ser 'Insights'");
    }

    // ---- Cenário 9: explorerPanel tem 4 filhos diretos ----

    @Test
    @DisplayName("explorerPanel deve ter 4 filhos diretos")
    void shouldHaveFourChildrenInExplorerPanel() {
        assertEquals(4, screen.explorerPanel.getChildren().size(),
                "explorerPanel deve ter 4 filhos: Header, filterInput, featureTree, addFolderButton");
    }

    // ---- Cenário 10: featureTree tem raiz "Framework" expandida ----

    @Test
    @DisplayName("featureTree deve ter raiz 'Framework' expandida")
    void shouldHaveExpandedFrameworkRoot() {
        assertNotNull(screen.featureTree.getRoot(),
                "featureTree deve ter uma raiz");
        assertEquals("Framework", screen.featureTree.getRoot().getValue(),
                "Raiz deve ter valor 'Framework'");
        assertTrue(screen.featureTree.getRoot().isExpanded(),
                "Raiz deve estar expandida");
    }

    // ---- Cenário 11: Raiz da árvore tem 7 filhos ----

    @Test
    @DisplayName("Raiz da árvore deve ter 7 filhos")
    void shouldHaveSevenChildrenInTreeRoot() {
        assertEquals(7, screen.featureRoot.getChildren().size(),
                "featureRoot deve ter 7 filhos");
    }

    // ---- Cenário 12: overviewContainer existe ----

    @Test
    @DisplayName("overviewContainer deve existir dentro da aba Visão geral")
    void shouldHaveOverviewContainer() {
        assertNotNull(screen.overviewContainer,
                "overviewContainer deve estar presente");
        assertInstanceOf(VBox.class, screen.overviewContainer,
                "overviewContainer deve ser VBox");
    }

    // ---- Cenário 13: datasetTable tem 4 colunas ----

    @Test
    @DisplayName("datasetTable deve ter 4 colunas")
    void shouldHaveFourColumnsInTable() {
        assertEquals(4, screen.datasetTable.getColumns().size(),
                "datasetTable deve ter 4 colunas");
    }

    // ---- Cenário 14: Textos das colunas ----

    @Test
    @DisplayName("Colunas da tabela devem ter textos corretos")
    void shouldHaveCorrectColumnTexts() {
        assertEquals("Nome", screen.projectNameColumn.getText(),
                "Primeira coluna deve ser 'Nome'");
        assertEquals("Tipo", screen.projectTypeColumn.getText(),
                "Segunda coluna deve ser 'Tipo'");
        assertEquals("Status", screen.projectStatusColumn.getText(),
                "Terceira coluna deve ser 'Status'");
    }

    // ---- Cenário 15: detailsContainer existe ----

    @Test
    @DisplayName("detailsContainer deve estar presente na aba Detalhes")
    void shouldHaveDetailsContainer() {
        assertNotNull(screen.detailsContainer,
                "detailsContainer deve estar presente");
        assertInstanceOf(VBox.class, screen.detailsContainer,
                "detailsContainer deve ser VBox");
    }

    // ---- Cenário 16: Formulário contém campos esperados ----

    @Test
    @DisplayName("Formulário de detalhes deve conter todos os campos esperados")
    void shouldHaveAllFormFields() {
        assertNotNull(screen.projectNameField, "projectNameField deve existir");
        assertNotNull(screen.ownerField, "ownerField deve existir");
        assertNotNull(screen.versionField, "versionField deve existir");
        assertNotNull(screen.activeToggle, "activeToggle deve existir");
        assertNotNull(screen.descriptionInput, "descriptionInput deve existir");
        assertNotNull(screen.saveButton, "saveButton deve existir");
        assertNotNull(screen.clearButton, "clearButton deve existir");
    }

    // ---- Cenário 17: insightsContainer existe ----

    @Test
    @DisplayName("insightsContainer deve estar presente na aba Insights")
    void shouldHaveInsightsContainer() {
        assertNotNull(screen.insightsContainer,
                "insightsContainer deve estar presente");
    }

    // ---- Cenário 18: metricsCard é MetricsCard e VBox ----

    @Test
    @DisplayName("metricsCard deve ser instância de MetricsCard e VBox")
    void shouldHaveMetricsCardAsCustomElement() {
        assertInstanceOf(Example.MetricsCard.class, screen.metricsCard,
                "metricsCard deve ser MetricsCard");
        assertInstanceOf(VBox.class, screen.metricsCard,
                "MetricsCard deve estender VBox");
    }

    // ---- Cenário 19: eventLogList dentro de eventLogSection ----

    @Test
    @DisplayName("eventLogList deve estar dentro de eventLogSection")
    void shouldHaveEventLogListInsideSection() {
        assertNotNull(screen.eventLogSection, "eventLogSection deve existir");
        assertNotNull(screen.eventLogList, "eventLogList deve existir");
        assertTrue(screen.eventLogSection.getChildren().contains(screen.eventLogList),
                "eventLogList deve ser filho de eventLogSection");
    }

    // ---- Cenário 20: Todos os campos possuem ID via acronym ----

    @Test
    @DisplayName("Todos os campos de node devem ter ID definido via acronym")
    void shouldHaveIdSetOnAllNodeFields() {
        // Verifica campos que são Node
        assertNotNull(screen.root.getId(), "root deve ter ID");
        assertNotNull(screen.statusBar.getId(), "statusBar deve ter ID");
        assertNotNull(screen.layoutSplit.getId(), "layoutSplit deve ter ID");
        assertNotNull(screen.explorerPanel.getId(), "explorerPanel deve ter ID");
        assertNotNull(screen.filterInput.getId(), "filterInput deve ter ID");
        assertNotNull(screen.featureTree.getId(), "featureTree deve ter ID");
        assertNotNull(screen.addFolderButton.getId(), "addFolderButton deve ter ID");
        assertNotNull(screen.contentTabs.getId(), "contentTabs deve ter ID");
        assertNotNull(screen.datasetTable.getId(), "datasetTable deve ter ID");
    }

    // ---- Cenário 21: delimiterPreviewPane é StackPane ----

    @Test
    @DisplayName("delimiterPreviewPane deve ser StackPane")
    void shouldHaveStackPaneForDelimiterPreview() {
        assertInstanceOf(StackPane.class, screen.delimiterPreviewPane,
                "delimiterPreviewPane deve ser StackPane");
    }

    // ---- Cenário 22: tableActions contém 2 botões ----

    @Test
    @DisplayName("tableActions deve conter 2 botões (refresh + export)")
    void shouldHaveTwoButtonsInTableActions() {
        assertInstanceOf(HBox.class, screen.tableActions,
                "tableActions deve ser HBox");
        assertEquals(2, screen.tableActions.getChildren().size(),
                "tableActions deve ter 2 filhos");
        assertInstanceOf(Button.class, screen.tableActions.getChildren().get(0),
                "Primeiro filho de tableActions deve ser Button");
        assertInstanceOf(Button.class, screen.tableActions.getChildren().get(1),
                "Segundo filho de tableActions deve ser Button");
    }
}
