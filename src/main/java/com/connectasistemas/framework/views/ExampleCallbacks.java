package com.connectasistemas.framework.views;

import com.connectasistemas.framework.utils.ScreenManager;
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
}
