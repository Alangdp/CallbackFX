package com.connectasistemas.framework.views;

import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.views.ChildCallbacksDemo;
import com.connectasistemas.framework.views.ProjectTreeEditor;
import com.connectasistemas.framework.views.components.ProjectSummaryCard;

/**
 * Callbacks da tela {@link Example}.
 */
public class ExampleCallbacks {

    /**
     * Abre o resumo de projeto atual como sub janela quando o usuário confirma a criação.
     *
     * @param screen instância da tela principal
     */
    public void callbackAltcamAdvanceButton(Example screen) {
        ScreenManager.openChildWindow(ProjectSummaryCard.class, screen);
    }

    /**
     * Abre uma child window dedicada a demonstrar os callbacks aplicados em filhos.
     */
    public void callbackAltcamTestChildCallbacksButton(Example screen) {
        ScreenManager.openChildWindow(ChildCallbacksDemo.class, screen);
    }

    /**
     * Torna a aba adicional visível e a seleciona quando o usuário clica no botão.
     */
    public void callbackAltcamShowAddPageButton(Example screen) {
        ScreenManager.setNodeVisibility(screen.addPageTab.getId(), true);
        screen.projectTabs.getSelectionModel().select(screen.addPageTab);
        screen.showAddPageButton.setDisable(true);
    }

    public void callbackAltcamOpenTreeEditorButton(Example screen) {
        ScreenManager.openChildWindow(ProjectTreeEditor.class, screen);
    }

    /**
     * Exibe mensagem ao entrar na aba "No pages".
     */
    public void callbackEntcamNoPagesTab(Example screen) {
        if (!hasTabStatusLabel(screen) || screen.noPagesTab == null) {
            return;
        }

        screen.tabEventStatus.setText(StringUtils.concat("Entrou na aba: ", tabTitle(screen.noPagesTab)));
    }

    /**
     * Exibe mensagem ao sair da aba "No pages".
     */
    public void callbackSaicamNoPagesTab(Example screen) {
        if (!hasTabStatusLabel(screen) || screen.noPagesTab == null) {
            return;
        }

        screen.tabEventStatus.setText(StringUtils.concat("Saiu da aba: ", tabTitle(screen.noPagesTab)));
    }

    /**
     * Confirma seleção da aba "Nova página" quando ela fica ativa.
     */
    public void callbackAltcamAddPageTab(Example screen) {
        if (!hasTabStatusLabel(screen) || screen.addPageTab == null) {
            return;
        }

        screen.tabEventStatus.setText(StringUtils.concat("Selecionou a aba: ", tabTitle(screen.addPageTab)));
    }

    /**
     * Inicializa a TreeView de estrutura e seleciona o primeiro nó.
     */
    public void callbackConfigProjectStructureTree(Example screen) {
        if (screen == null || screen.projectStructureTree == null) {
            return;
        }

        updateTreeStatus(screen, "Tree pronta para uso");
        if (screen.projectTreeFolderJava != null) {
            screen.projectStructureTree.getSelectionModel().select(screen.projectTreeFolderJava);
        }
    }

    public void callbackEntcamProjectStructureTree(Example screen) {
        updateTreeStatus(screen, StringUtils.concat("Focus em TreeView -> ", currentTreeValue(screen)));
    }

    public void callbackSaicamProjectStructureTree(Example screen) {
        updateTreeStatus(screen, "Saiu da TreeView");
    }

    public void callbackAltcamProjectStructureTree(Example screen) {
        updateTreeStatus(screen, StringUtils.concat("Selecionou: ", currentTreeValue(screen)));
    }

    public void callbackClickProjectStructureTree(Example screen, MouseEvent event, TreeItem<?> selected) {
        updateTreeStatus(screen, StringUtils.concat("Click em: ", readableValue(selected)));
    }

    public void callbackDoubleClickProjectStructureTree(Example screen, MouseEvent event, TreeItem<?> selected) {
        updateTreeStatus(screen, StringUtils.concat("Double click em: ", readableValue(selected)));
    }

    /**
     * Permite que outras telas adicionem nós na árvore principal.
     */
    public void addExternalFolder(Example screen, String folderName) {
        if (screen == null || screen.projectTreeFolderJava == null || StringUtils.isBlank(folderName)) {
            return;
        }

        TreeItem<String> newNode = new TreeItem<>(folderName.trim());
        screen.projectTreeFolderJava.getChildren().add(newNode);
        screen.projectStructureTree.getSelectionModel().select(newNode);
        updateTreeStatus(screen, StringUtils.concat("Adicionado via child: ", folderName));
    }

    private boolean hasTabStatusLabel(Example screen) {
        return screen != null && screen.tabEventStatus != null;
    }

    private String tabTitle(Tab tab) {
        return tab != null ? tab.getText() : "";
    }

    private void updateTreeStatus(Example screen, String message) {
        if (screen == null || screen.projectTreeStatus == null) {
            return;
        }

        screen.projectTreeStatus.setText(message);
    }

    private String currentTreeValue(Example screen) {
        if (screen == null || screen.projectStructureTree == null || screen.projectStructureTree.getSelectionModel() == null) {
            return "";
        }

        TreeItem<String> selected = screen.projectStructureTree.getSelectionModel().getSelectedItem();
        return readableValue(selected);
    }

    private String readableValue(TreeItem<?> item) {
        Object value = item != null ? item.getValue() : null;
        return value != null ? value.toString() : "";
    }
}
