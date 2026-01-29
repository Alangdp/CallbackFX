package com.connectasistemas.framework.internal.examples;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import com.connectasistemas.framework.annotation.Screen;
import com.connectasistemas.framework.annotation.ScreenField;
import com.connectasistemas.framework.annotation.ScreenFieldPosition;
import com.connectasistemas.framework.annotation.ScreenFieldSize;
import com.connectasistemas.framework.annotation.ScreenProperties;
import com.connectasistemas.framework.annotation.ScreenValidation;
import com.connectasistemas.framework.enums.CursorType;
import com.connectasistemas.framework.enums.Position;
import com.connectasistemas.framework.enums.ValidationDataType;
import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.interfaces.CustomElement;
import com.connectasistemas.framework.utils.StringUtils;

/**
 * Tela de demonstração que exercita os recursos principais do CallbackFX.
 */
@Screen(
        title = "CallbackFX Showcase",
        width = 1280,
        height = 780,
        callbacks = ExampleController.class,
        region = BorderPane.class)
@ScreenProperties(resizable = true, styleClass = "callbackfx-example")
public class Example {

    @ScreenField(acronym = "root", order = 1)
    @ScreenFieldSize(vgrow = true, hgrow = true)
    public BorderPane root;

    @ScreenField(acronym = "statusBar", father = "root", position = Position.BOTTOM, literal = "Status: pronto", order = 2)
    @ScreenFieldSize(padding = { 6, 12, 6, 12 })
    @ScreenFieldPosition(alignment = Position.CENTER_LEFT)
    @ScreenProperties(focusTraversable = false, styleClass = "status-bar")
    public Label statusBar;

    @ScreenField(acronym = "layoutSplit", father = "root", position = Position.CENTER, order = 3)
    @ScreenFieldSize(vgrow = true, hgrow = true)
    public SplitPane layoutSplit;

    @ScreenField(acronym = "explorerPanel", father = "layoutSplit", order = 1)
    @ScreenFieldSize(padding = { 16, 16, 16, 16 }, spacing = 12)
    public VBox explorerPanel;

    @ScreenField(acronym = "explorerHeader", father = "explorerPanel", literal = "Catálogo do framework", order = 1)
    @ScreenProperties(styleClass = "section-title", wrapText = true)
    public Label explorerHeader;

    @ScreenField(acronym = "filterInput", father = "explorerPanel", literal = "Filtrar por nome ou tipo", order = 2)
    @ScreenFieldSize(width = 260)
    @ScreenValidation(maxLength = 50)
    public TextField filterInput;

    @ScreenField(acronym = "featureTree", father = "explorerPanel", order = 3)
    @ScreenFieldSize(vgrow = true, hgrow = true, minHeight = 260)
    public TreeView<String> featureTree;

    @ScreenField(acronym = "featureRoot", father = "featureTree", literal = "Framework", order = 1)
    @ScreenProperties(expanded = true)
    public TreeItem<String> featureRoot;

    @ScreenField(acronym = "layoutNode", father = "featureRoot", literal = "Layouts", order = 1)
    public TreeItem<String> layoutNode;

    @ScreenField(acronym = "eventsNode", father = "featureRoot", literal = "Eventos", order = 2)
    public TreeItem<String> eventsNode;

    @ScreenField(acronym = "validationNode", father = "featureRoot", literal = "Validações", order = 3)
    public TreeItem<String> validationNode;

    @ScreenField(acronym = "tablesNode", father = "featureRoot", literal = "Tabelas", order = 4)
    public TreeItem<String> tablesNode;

    @ScreenField(acronym = "treesNode", father = "featureRoot", literal = "Árvores", order = 5)
    public TreeItem<String> treesNode;

    @ScreenField(acronym = "navigationNode", father = "featureRoot", literal = "Navegação", order = 6)
    public TreeItem<String> navigationNode;

    @ScreenField(acronym = "utilsNode", father = "featureRoot", literal = "Utilitários", order = 7)
    public TreeItem<String> utilsNode;

    @ScreenField(acronym = "addFolderButton", father = "explorerPanel", literal = "Nova pasta", order = 4)
    @ScreenFieldSize(width = 200)
    @ScreenProperties(cursor = CursorType.HAND, tooltip = "Abre o editor de pastas")
    public Button addFolderButton;

    @ScreenField(acronym = "contentTabs", father = "layoutSplit", order = 2)
    @ScreenFieldSize(vgrow = true, hgrow = true)
    public TabPane contentTabs;

    @ScreenField(acronym = "overviewTab", father = "contentTabs", literal = "Visão geral", order = 1)
    @ScreenProperties(tooltip = "Resumo dos elementos")
    public Tab overviewTab;

    @ScreenField(acronym = "detailsTab", father = "contentTabs", literal = "Detalhes", order = 2)
    public Tab detailsTab;

    @ScreenField(acronym = "insightsTab", father = "contentTabs", literal = "Insights", order = 3)
    public Tab insightsTab;

    @ScreenField(acronym = "overviewContainer", father = "overviewTab", order = 1)
    @ScreenFieldSize(padding = { 16, 16, 16, 16 }, spacing = 12)
    public VBox overviewContainer;

    @ScreenField(acronym = "datasetSummary", father = "overviewContainer", literal = "Projetos monitorados", order = 1)
    @ScreenProperties(styleClass = "section-title")
    public Label datasetSummary;

    @ScreenField(acronym = "datasetTable", father = "overviewContainer", order = 2)
    @ScreenFieldSize(vgrow = true)
    public TableView<ExampleController.ExampleProject> datasetTable;

    @ScreenField(acronym = "projectNameColumn", father = "datasetTable", literal = "Nome", order = 1)
    public TableColumn<ExampleController.ExampleProject, String> projectNameColumn;

    @ScreenField(acronym = "projectTypeColumn", father = "datasetTable", literal = "Tipo", order = 2)
    public TableColumn<ExampleController.ExampleProject, String> projectTypeColumn;

    @ScreenField(acronym = "projectStatusColumn", father = "datasetTable", literal = "Status", order = 3)
    public TableColumn<ExampleController.ExampleProject, String> projectStatusColumn;

    @ScreenField(acronym = "projectUpdatedColumn", father = "datasetTable", literal = "Atualizado em", order = 4)
    public TableColumn<ExampleController.ExampleProject, String> projectUpdatedColumn;

    @ScreenField(acronym = "tableActions", father = "overviewContainer", order = 3)
    @ScreenFieldSize(spacing = 8)
    public HBox tableActions;

    @ScreenField(acronym = "refreshButton", father = "tableActions", literal = "Recarregar dados", order = 1)
    @ScreenFieldSize(width = 180)
    @ScreenProperties(cursor = CursorType.HAND, tooltip = "Atualiza o conteúdo da tabela")
    public Button refreshButton;

    @ScreenField(acronym = "exportButton", father = "tableActions", literal = "Exportar", order = 2)
    @ScreenFieldSize(width = 120)
    @ScreenProperties(cursor = CursorType.HAND, tooltip = "Exporta os registros visíveis")
    public Button exportButton;

    @ScreenField(acronym = "detailsContainer", father = "detailsTab", order = 1)
    @ScreenFieldSize(padding = { 16, 16, 16, 16 }, spacing = 10)
    public VBox detailsContainer;

    @ScreenField(acronym = "formHeader", father = "detailsContainer", literal = "Cadastro do recurso", order = 1)
    @ScreenProperties(styleClass = "section-title")
    public Label formHeader;

    @ScreenField(acronym = "projectNameField", father = "detailsContainer", literal = "Nome do recurso", order = 2)
    @ScreenFieldSize(width = 360)
    @ScreenValidation(required = true, maxLength = 40, showMessage = true, validateOn = "saveButton")
    public TextEntryLabel projectNameField;

    @ScreenField(acronym = "ownerField", father = "detailsContainer", literal = "Responsável", order = 3)
    @ScreenFieldSize(width = 360)
    @ScreenValidation(required = true, maxLength = 30, showMessage = true, validateOn = "saveButton")
    public TextEntryLabel ownerField;

    @ScreenField(acronym = "versionField", father = "detailsContainer", literal = "Versão (1-99)", order = 4)
    @ScreenFieldSize(width = 200)
    @ScreenValidation(required = true, dataType = ValidationDataType.INTEGER, minValue = 1, maxValue = 99, showMessage = true, validateOn = "saveButton")
    public TextEntryLabel versionField;

    @ScreenField(acronym = "activeToggle", father = "detailsContainer", literal = "Ativo", order = 6)
    public CheckEntryLabel activeToggle;

    @ScreenField(acronym = "descriptionInput", father = "detailsContainer", literal = "Descrição", order = 7)
    @ScreenFieldSize(width = 420, height = 140)
    @ScreenValidation(maxLength = 160, showMessage = true, validateOn = "saveButton")
    public TextArea descriptionInput;

    @ScreenField(acronym = "formActions", father = "detailsContainer", order = 8)
    @ScreenFieldSize(spacing = 10)
    public HBox formActions;

    @ScreenField(acronym = "saveButton", father = "formActions", literal = "Salvar", order = 1)
    @ScreenFieldSize(width = 140)
    @ScreenProperties(cursor = CursorType.HAND, tooltip = "Grava as alterações", focusOnLoad = false)
    public Button saveButton;

    @ScreenField(acronym = "clearButton", father = "formActions", literal = "Limpar", order = 2)
    @ScreenFieldSize(width = 120)
    @ScreenProperties(cursor = CursorType.HAND, tooltip = "Limpa o formulário")
    public Button clearButton;

    @ScreenField(acronym = "insightsContainer", father = "insightsTab", order = 1)
    @ScreenFieldSize(padding = { 16, 16, 16, 16 }, spacing = 12)
    public VBox insightsContainer;

    @ScreenField(acronym = "insightHeader", father = "insightsContainer", literal = "Monitoramento em tempo real", order = 1)
    @ScreenProperties(styleClass = "section-title")
    public Label insightHeader;

    @ScreenField(acronym = "metricsCard", father = "insightsContainer", order = 2, custom = true)
    @ScreenFieldSize(width = 420)
    public MetricsCard metricsCard;

    @ScreenField(acronym = "eventLogSection", father = "insightsContainer", order = 3)
    @ScreenFieldSize(spacing = 6)
    public VBox eventLogSection;

    @ScreenField(acronym = "eventLogLabel", father = "eventLogSection", literal = "Histórico de eventos", order = 1)
    public Label eventLogLabel;

    @ScreenField(acronym = "eventLogList", father = "eventLogSection", order = 2)
    @ScreenFieldSize(height = 220, vgrow = true)
    @ScreenProperties(focusTraversable = false)
    public ListView<String> eventLogList;

    /**
     * Cartão simples para destacar métricas dinâmicas.
     */
    public static final class MetricsCard extends VBox implements CustomElement {

        private final Label title = new Label("Painel de métricas");
        private final Label primaryValue = new Label();
        private final Label secondaryValue = new Label();

        public MetricsCard() {
            setSpacing(6);
            getChildren().addAll(title, primaryValue, secondaryValue);
        }

        @Override
        public Class<? extends Region> getType() {
            return VBox.class;
        }

        public void updateMetrics(String projectsLabel, String syncLabel) {
            primaryValue.setText(projectsLabel);
            secondaryValue.setText(StringUtils.concat("Última sincronização: ", syncLabel));
        }
    }
}
