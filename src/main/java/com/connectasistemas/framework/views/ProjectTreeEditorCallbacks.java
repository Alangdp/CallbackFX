package com.connectasistemas.framework.views;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.StringUtils;

/**
 * Callbacks da janela {@link ProjectTreeEditor}.
 */
public class ProjectTreeEditorCallbacks {

    public void callbackAltcamConfirmButton(ProjectTreeEditor screen) {
        if (screen == null) {
            return;
        }

        ExampleCallbacks parentCallbacks = ScreenManager.getControllerReference(Example.class);
        Example parentScreen = ScreenManager.getScreenReference(Example.class);

        if (parentCallbacks == null || parentScreen == null) {
            updateStatus(screen, "Tela principal não encontrada");
            return;
        }

        String folderName = screen.folderNameInput != null ? screen.folderNameInput.getText() : "";
        if (StringUtils.isBlank(folderName)) {
            updateStatus(screen, "Informe um nome válido");
            return;
        }

        parentCallbacks.addExternalFolder(parentScreen, folderName);
        updateStatus(screen, StringUtils.concat("Adicionado: ", folderName));
        ScreenManager.closeChildWindow(screen);
    }

    public void callbackAltcamCancelButton(ProjectTreeEditor screen) {
        ScreenManager.closeChildWindow(screen);
    }

    private void updateStatus(ProjectTreeEditor screen, String message) {
        if (screen == null || screen.statusLabel == null) {
            return;
        }
        screen.statusLabel.setText(message);
    }
}
