package com.connectasistemas.framework.internal.examples.views;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.Status;
import com.connectasistemas.framework.utils.StringUtils;

/**
 * Callbacks da janela {@link ProjectTreeEditor}.
 */
public class ProjectTreeEditorCallbacks {

    public void callbackConfigProjectTreeEditor(ProjectTreeEditor screen) {
        updateStatus(screen, "Escolha o nome da pasta a ser criada");
        if (screen != null && screen.folderNameInput != null) {
            screen.folderNameInput.setText("");
        }
    }

    public void callbackAltcamConfirmButton(ProjectTreeEditor screen) {
        if (screen == null) {
            return;
        }

        String folderName = screen.folderNameInput != null ? screen.folderNameInput.getText() : "";
        if (StringUtils.isBlank(folderName)) {
            updateStatus(screen, "Informe um nome válido");
            Status.markError(screen.folderNameInput);
            return;
        }

        ProjectTreeEditor.ProjectTreeEditorListener listener = screen.getListener();
        if (listener == null) {
            updateStatus(screen, "Nenhuma ação configurada para o editor");
            return;
        }

        String normalizedName = StringUtils.trim(folderName);
        listener.onFolderCreated(screen, normalizedName);
        updateStatus(screen, StringUtils.concat("Adicionado: ", normalizedName));
        ScreenManager.closeChildWindow(screen);
    }

    public void callbackAltcamCancelButton(ProjectTreeEditor screen) {
        if (screen != null) {
            ProjectTreeEditor.ProjectTreeEditorListener listener = screen.getListener();
            if (listener != null) {
                listener.onCancel(screen);
            }
        }
        ScreenManager.closeChildWindow(screen);
    }

    private void updateStatus(ProjectTreeEditor screen, String message) {
        if (screen == null || screen.statusLabel == null) {
            return;
        }
        screen.statusLabel.setText(message);
    }
}
