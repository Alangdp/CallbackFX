package com.connectasistemas.framework.views;

import javafx.scene.control.Tab;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.views.ChildCallbacksDemo;
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

    private boolean hasTabStatusLabel(Example screen) {
        return screen != null && screen.tabEventStatus != null;
    }

    private String tabTitle(Tab tab) {
        return tab != null ? tab.getText() : "";
    }
}
