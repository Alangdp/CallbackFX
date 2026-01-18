package com.connectasistemas.framework.examples;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import com.connectasistemas.framework.utils.DateTimeUtils;
import com.connectasistemas.framework.utils.MessageUtil;
import com.connectasistemas.framework.utils.NumberUtils;
import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.utils.TableManager;
import com.connectasistemas.framework.utils.TreeManager;
import com.connectasistemas.framework.views.ProjectTreeEditor;
import com.connectasistemas.framework.views.ProjectTreeEditor.ProjectTreeEditorListener;

/**
 * Controller responsável por demonstrar o uso das principais utilidades do framework.
 */
public class ExampleController {

    private static final List<String> PROJECT_TYPES = List.of(
            "Layouts",
            "Eventos",
            "Validações",
            "Tabelas",
            "Árvores",
            "Utilitários");

    private final ObservableList<ExampleProject> projects = FXCollections.observableArrayList();
    private final FilteredList<ExampleProject> filteredProjects = new FilteredList<>(projects, project -> true);
    private final ObservableList<String> eventLog = FXCollections.observableArrayList();
    private final AtomicInteger folderSequence = new AtomicInteger(1);
    private final Comparator<ExampleProject> nameComparator = Comparator
            .comparing(project -> StringUtils.lowerCase(project.name()));

    public void callbackConfigExample(Example screen) {
        if (screen == null) {
            return;
        }

        ScreenManager.setTopMenu(screen.topMenu);
        configureLayout(screen);
        seedProjects(screen);
        configureTable(screen);
        configureTree(screen);
        configureForm(screen);
        configureInsights(screen);
        updateMetrics(screen);
        updateStatus(screen, "Ambiente carregado com exemplos dinâmicos");
        addEventLog(screen, "Tela inicial pronta para uso");
    }

    public void callbackAltcamFilterInput(Example screen) {
        applyFilter(screen);
    }

    public void callbackTecladFilterInput(Example screen, KeyEvent event) {
        if (screen == null || event == null || event.getCode() != KeyCode.ENTER) {
            return;
        }
        applyFilter(screen);
        addEventLog(screen, StringUtils.concat("Filtro aplicado com ENTER: ", screen.filterInput.getText()));
    }

    public void callbackAltcamRefreshButton(Example screen) {
        seedProjects(screen);
        updateMetrics(screen);
        updateStatus(screen, "Dados recarregados a partir da fonte padrão");
        addEventLog(screen, "Tabela atualizada manualmente");
    }

    public void callbackAltcamExportButton(Example screen) {
        if (screen == null) {
            return;
        }
        MessageUtil.info("Exportação", "Este botão demonstra como acionar diálogos do framework.");
        addEventLog(screen, "Exportação simulada para os registros visíveis");
    }

    public void callbackAltcamAddFolderButton(Example screen) {
        openFolderDialog(screen);
    }

    public void callbackAltcamFeatureTree(Example screen) {
        handleTreeSelection(screen, TreeManager.getSelectedItem(screen.featureTree));
    }

    public void callbackClickFeatureTree(Example screen, MouseEvent event, TreeItem<?> selected) {
        if (screen == null) {
            return;
        }
        handleTreeSelection(screen, selected);
        if (selected != null) {
            addEventLog(screen, StringUtils.concat(
                    "Item selecionado: ",
                    TreeManager.describe(screen, selected)));
        }
    }

    public void callbackDoubleClickFeatureTree(Example screen, MouseEvent event, TreeItem<?> selected) {
        if (screen == null || selected == null) {
            return;
        }
        String path = TreeManager.getPath(selected);
        MessageUtil.info("Árvore", StringUtils.concat("Abrindo fluxo para ", path));
        addEventLog(screen, StringUtils.concat("Duplo clique em ", path));
    }

    public void callbackAltcamDatasetTable(Example screen) {
        applySelectionToForm(screen, getSelectedProject(screen));
    }

    public void callbackClickDatasetTable(Example screen, MouseEvent event, Object selectedRow,
            TablePosition<?, ?> selectedCell) {
        if (screen == null) {
            return;
        }
        ExampleProject project = selectedRow instanceof ExampleProject row ? row : getSelectedProject(screen);
        if (project == null) {
            return;
        }
        updateStatus(screen, StringUtils.concat("Selecionado: ", project.name()));
    }

    public void callbackDoubleClickDatasetTable(Example screen, MouseEvent event, Object selectedRow,
            TablePosition<?, ?> selectedCell) {
        if (screen == null) {
            return;
        }
        ExampleProject project = selectedRow instanceof ExampleProject row ? row : getSelectedProject(screen);
        if (project == null) {
            return;
        }
        MessageUtil.info("Detalhes", StringUtils.concat(
                "Projeto ", project.name(),
                " (versão ", project.version(), ") mantido por ", project.owner()));
        addEventLog(screen, StringUtils.concat("Inspeção de projeto: ", project.name()));
    }

    public void callbackAltcamSaveButton(Example screen) {
        saveProject(screen);
    }

    public void callbackAltcamClearButton(Example screen) {
        clearForm(screen);
    }

    private void configureLayout(Example screen) {
        screen.layoutSplit.setDividerPositions(0.28);
        screen.datasetTable.setPlaceholder(new Label("Nenhum registro encontrado"));
        screen.eventLogList.setItems(eventLog);
    }

    private void seedProjects(Example screen) {
        projects.clear();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (String type : PROJECT_TYPES) {
            int version = random.nextInt(1, 8);
            boolean active = random.nextBoolean();
            String name = StringUtils.concat("Componente de ", type, " ", version);
            String owner = StringUtils.concat("Equipe de ", type);
            int days = random.nextInt(0, 7);
            String updatedAt = DateTimeUtils.addDays(DateTimeUtils.currentTimestamp(), -days);
            String description = StringUtils.concat(
                    "Exemplo demonstrando ", type,
                    " com foco na versão ", version, ".");
            projects.add(new ExampleProject(name, owner, type, active, version, updatedAt, description));
        }
        FXCollections.sort(projects, nameComparator);
        filteredProjects.setPredicate(project -> true);
        if (screen.datasetTable != null) {
            screen.datasetTable.setItems(filteredProjects);
            screen.datasetTable.getSelectionModel().selectFirst();
        }
        updateDatasetSummary(screen);
    }

    private void configureTable(Example screen) {
        screen.projectNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().name()));
        screen.projectTypeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().type()));
        screen.projectStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().status()));
        screen.projectUpdatedColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().updatedAt()));

        TableManager.setHeader(screen, "projectUpdatedColumn", "Atualizado");
        TableManager.setSortable(screen, "projectStatusColumn", false);
        TableManager.setStyle(screen.projectStatusColumn, "-fx-alignment: CENTER;");
        TableManager.setStyleClasses(screen.projectTypeColumn, "highlight-column");
    }

    private void configureTree(Example screen) {
        screen.featureTree.setShowRoot(true);
        screen.layoutNode.getChildren().setAll(
                new TreeItem<>("BorderPane"),
                new TreeItem<>("SplitPane"));
        screen.eventsNode.getChildren().setAll(
                new TreeItem<>("Buttons"),
                new TreeItem<>("TextFields"));
        screen.validationNode.getChildren().setAll(
                new TreeItem<>("Regras"),
                new TreeItem<>("Mensagens"));
        screen.tablesNode.getChildren().setAll(
                new TreeItem<>("TableView"),
                new TreeItem<>("ListView"));
        screen.treesNode.getChildren().setAll(
                new TreeItem<>("TreeView"),
                new TreeItem<>("TreeItem"));
        screen.navigationNode.getChildren().setAll(
                new TreeItem<>("ScreenManager"),
                new TreeItem<>("Fragments"));
        screen.utilsNode.getChildren().setAll(
                new TreeItem<>("StringUtils"),
                new TreeItem<>("DateTimeUtils"));
        expandNode(screen.featureRoot);
        screen.featureTree.getSelectionModel().select(screen.featureRoot);
    }

    private void configureForm(Example screen) {
        screen.typeCombo.getItems().setAll(PROJECT_TYPES);
        screen.typeCombo.getSelectionModel().selectFirst();
        screen.projectNameField.setValue("Componente demonstrativo");
        screen.ownerField.setValue("Time padrão");
        screen.versionField.setValue("1");
        screen.activeToggle.setValue(true);
        screen.descriptionInput.setText("Use este formulário para testar validações e eventos.");
    }

    private void configureInsights(Example screen) {
        eventLog.clear();
        screen.metricsCard.updateMetrics("Projetos: 0", DateTimeUtils.currentTimestamp());
        toggleEventLogSection(screen, true);
        addEventLog(screen, "Painel de insights inicializado");
    }

    private void openFolderDialog(Example screen) {
        if (screen == null) {
            return;
        }
        ScreenManager.openChildWindow(ProjectTreeEditor.class, screen);
        ProjectTreeEditor editor = ScreenManager.getScreenReference(ProjectTreeEditor.class);
        if (editor == null) {
            MessageUtil.error("Editor", "Não foi possível abrir o editor de pastas.");
            return;
        }
        if (editor.folderNameInput != null && StringUtils.isBlank(editor.folderNameInput.getText())) {
            editor.folderNameInput.setText(StringUtils.concat("Nova pasta ", folderSequence.getAndIncrement()));
        }
        editor.setListener(new FolderDialogListener(screen));
        updateStatus(screen, "Editor de pastas aberto");
    }

    private void applyFilter(Example screen) {
        if (screen == null) {
            return;
        }
        String rawFilter = screen.filterInput != null ? screen.filterInput.getText() : "";
        String normalized = StringUtils.lowerCase(StringUtils.trim(rawFilter));
        if (StringUtils.isBlank(normalized)) {
            filteredProjects.setPredicate(project -> true);
            updateStatus(screen, "Exibindo todos os registros");
            return;
        }
        filteredProjects.setPredicate(project -> {
            String lowerName = StringUtils.lowerCase(project.name());
            String lowerType = StringUtils.lowerCase(project.type());
            String lowerOwner = StringUtils.lowerCase(project.owner());
            return lowerName.contains(normalized)
                || lowerType.contains(normalized)
                || lowerOwner.contains(normalized);
        });
        updateStatus(screen, StringUtils.concat("Filtrando por ", rawFilter));
    }

    private void handleTreeSelection(Example screen, TreeItem<?> selected) {
        if (screen == null || selected == null) {
            return;
        }
        String key = TreeManager.getCaseKey(screen, selected);
        boolean shouldFocusOnLog = "eventsNode".equals(key) || "validationNode".equals(key);
        toggleEventLogSection(screen, shouldFocusOnLog);
        String path = TreeManager.getPath(selected);
        updateStatus(screen, StringUtils.concat("Explorando: ", path));
    }

    private void toggleEventLogSection(Example screen, boolean visible) {
        if (screen == null || screen.eventLogSection == null) {
            return;
        }
        ScreenManager.setNodeVisibility(screen.eventLogSection, visible);
    }

    private ExampleProject getSelectedProject(Example screen) {
        if (screen == null || screen.datasetTable == null) {
            return null;
        }
        return screen.datasetTable.getSelectionModel().getSelectedItem();
    }

    private void applySelectionToForm(Example screen, ExampleProject project) {
        if (screen == null || project == null) {
            return;
        }
        screen.projectNameField.setValue(project.name());
        screen.ownerField.setValue(project.owner());
        screen.versionField.setValue(Integer.toString(project.version()));
        screen.typeCombo.getSelectionModel().select(project.type());
        screen.activeToggle.setValue(project.active());
        screen.descriptionInput.setText(project.description());
    }

    private void saveProject(Example screen) {
        if (!validateForm(screen)) {
            return;
        }

        String name = StringUtils.trim(screen.projectNameField.getValue());
        String owner = StringUtils.trim(screen.ownerField.getValue());
        String type = screen.typeCombo.getSelectionModel().getSelectedItem();
        boolean active = screen.activeToggle.isSelected();
        int version = NumberUtils.toInt(screen.versionField.getValue());
        String description = StringUtils.trim(screen.descriptionInput.getText());
        String updatedAt = DateTimeUtils.currentTimestamp();

        ExampleProject newProject = new ExampleProject(name, owner, type, active, version, updatedAt, description);
        projects.removeIf(project -> project.name().equalsIgnoreCase(name));
        projects.add(newProject);
        FXCollections.sort(projects, nameComparator);
        screen.datasetTable.refresh();
        updateDatasetSummary(screen);
        updateMetrics(screen);
        updateStatus(screen, StringUtils.concat("Projeto salvo: ", name));
        addEventLog(screen, StringUtils.concat("Registro atualizado: ", name));
        Status.clearError();
    }

    private void clearForm(Example screen) {
        if (screen == null) {
            return;
        }
        boolean confirmed = MessageUtil.confirm("Limpar formulário", "Deseja realmente limpar os dados?");
        if (!confirmed) {
            return;
        }
        screen.projectNameField.setValue("");
        screen.ownerField.setValue("");
        screen.versionField.setValue("1");
        screen.typeCombo.getSelectionModel().selectFirst();
        screen.activeToggle.setValue(false);
        screen.descriptionInput.clear();
        updateStatus(screen, "Formulário limpo");
        addEventLog(screen, "Campos retornaram ao estado inicial");
    }

    private boolean validateForm(Example screen) {
        if (screen == null) {
            return false;
        }

        String name = StringUtils.trim(screen.projectNameField.getValue());
        if (StringUtils.isBlank(name)) {
            Status.markError(screen.projectNameField.getTextField());
            MessageUtil.warn("Validação", "Informe o nome do recurso.");
            return false;
        }

        String owner = StringUtils.trim(screen.ownerField.getValue());
        if (StringUtils.isBlank(owner)) {
            Status.markError(screen.ownerField.getTextField());
            MessageUtil.warn("Validação", "Informe o responsável pelo recurso.");
            return false;
        }

        int version = NumberUtils.toInt(screen.versionField.getValue());
        if (version <= 0 || version > 99) {
            Status.markError(screen.versionField.getTextField());
            MessageUtil.warn("Validação", "Versão deve estar entre 1 e 99.");
            return false;
        }

        String type = screen.typeCombo.getSelectionModel().getSelectedItem();
        if (StringUtils.isBlank(type)) {
            MessageUtil.warn("Validação", "Selecione um tipo para o recurso.");
            return false;
        }

        return true;
    }

    private void updateMetrics(Example screen) {
        if (screen == null) {
            return;
        }
        String projectsLabel = StringUtils.concat("Projetos: ", projects.size());
        screen.metricsCard.updateMetrics(projectsLabel, DateTimeUtils.currentTimestamp());
    }

    private void updateDatasetSummary(Example screen) {
        if (screen == null || screen.datasetSummary == null) {
            return;
        }
        screen.datasetSummary.setText(StringUtils.concat(projects.size(), " projetos catalogados"));
    }

    private void updateStatus(Example screen, String message) {
        if (screen == null || screen.statusBar == null) {
            return;
        }
        screen.statusBar.setText(StringUtils.concat("[", DateTimeUtils.currentTimestamp(), "] ", message));
    }

    private void addEventLog(Example screen, String message) {
        eventLog.add(0, StringUtils.concat("[", DateTimeUtils.currentTimestamp(), "] ", message));
        if (eventLog.size() > 80) {
            eventLog.remove(eventLog.size() - 1);
        }
        toggleEventLogSection(screen, true);
    }

    private void expandNode(TreeItem<?> node) {
        if (node == null) {
            return;
        }
        node.setExpanded(true);
        if (node.getChildren() == null) {
            return;
        }
        node.getChildren().forEach(this::expandNode);
    }

    private void addExternalFolder(Example screen, String folderName) {
        if (screen == null) {
            return;
        }
        TreeItem<?> selected = TreeManager.getSelectedItem(screen.featureTree);
        TreeItem<String> target = selected instanceof TreeItem<?> item
                ? castTreeItem(item)
                : screen.featureRoot;
        if (target == null) {
            target = screen.featureRoot;
        }
        TreeItem<String> newFolder = new TreeItem<>(folderName);
        target.getChildren().add(newFolder);
        target.setExpanded(true);
        updateStatus(screen, StringUtils.concat(
                "Pasta adicionada em ", TreeManager.getPath(target)));
        addEventLog(screen, StringUtils.concat("Nova pasta criada: ", folderName));
    }

    @SuppressWarnings("unchecked")
    private TreeItem<String> castTreeItem(TreeItem<?> item) {
        return (TreeItem<String>) item;
    }

    private final class FolderDialogListener implements ProjectTreeEditorListener {
        private final Example screen;

        private FolderDialogListener(Example screen) {
            this.screen = screen;
        }

        @Override
        public void onFolderCreated(ProjectTreeEditor child, String folderName) {
            addExternalFolder(screen, folderName);
        }

        @Override
        public void onCancel(ProjectTreeEditor child) {
            addEventLog(screen, "Criação de pasta cancelada");
        }
    }

    public record ExampleProject(
            String name,
            String owner,
            String type,
            boolean active,
            int version,
            String updatedAt,
            String description) {

        public String status() {
            return active ? "Ativo" : "Inativo";
        }
    }
}
