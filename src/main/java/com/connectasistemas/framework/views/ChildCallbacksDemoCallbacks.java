package com.connectasistemas.framework.views;

import java.util.Arrays;
import java.util.List;

import javafx.scene.input.KeyEvent;

import com.connectasistemas.framework.utils.ScreenManager;
import com.connectasistemas.framework.utils.StringUtils;
import com.connectasistemas.framework.views.Example;
import com.connectasistemas.framework.views.ExampleCallbacks;

/**
 * Controller da tela ChildCallbacksDemo responsável pelos callbacks de teste.
 */
public class ChildCallbacksDemoCallbacks {

    private static final List<ValidationSample> VALIDATION_SAMPLES = Arrays.asList(
            new ValidationSample(
                    "[OK] Entrada válida",
                    "Dashboard Principal",
                    "dashboardMain",
                    "Cumpre os limites de 3 a 50 caracteres em ambos os campos.",
                    true),
            new ValidationSample(
                    "[ERRO] Título curto",
                    "Ui",
                    "pageShortTitle",
                    "O título possui menos de 3 caracteres.",
                    false),
            new ValidationSample(
                    "[ERRO] Nome curto",
                    "Página Genérica",
                    "id",
                    "O nome interno não alcança o mínimo de 3 caracteres.",
                    false),
            new ValidationSample(
                    "[ERRO] Título longo",
                    "Página extremamente detalhada para validação de caracteres 123",
                    "longTitlePage",
                    "O título supera o máximo de 50 caracteres permitido.",
                    false),
            new ValidationSample(
                    "[ERRO] Nome longo",
                    "Catálogo Principal",
                    "nomeInternoQueUltrapassaOMaximoPermitidoDeCaracteres",
                    "O identificador interno excede 50 caracteres.",
                    false));

    private void updateStatus(ChildCallbacksDemo screen, String message) {
        if (screen == null || screen.statusLabel == null) {
            return;
        }
        screen.statusLabel.setText(message);
    }

    public void callbackConfigValidationExamplesList(ChildCallbacksDemo screen) {
        configureValidationSamples(screen);
    }

    public void callbackConfigStatusLabel(ChildCallbacksDemo screen) {
        updateStatus(screen, "Interaja com os campos para ver os eventos.");
    }

    public void callbackEntcamChildNameInput(ChildCallbacksDemo screen) {
        updateStatus(screen, StringUtils.concat("entcam -> ", "Nome da página"));
    }

    public void callbackSaicamChildNameInput(ChildCallbacksDemo screen) {
        updateStatus(screen, StringUtils.concat("saicam -> ", "Nome da página"));
    }

    public void callbackAltcamChildContentInput(ChildCallbacksDemo screen) {
        updateStatus(screen, StringUtils.concat("altcam -> Conteúdo: ", screen.childContentInput.getValue()));
    }

    public void callbackTecladChildNotesInput(ChildCallbacksDemo screen, KeyEvent event) {
        if (event == null) {
            updateStatus(screen, "teclad -> evento nulo");
            return;
        }
        updateStatus(screen, StringUtils.concat("teclad -> ", event.getCode().getName()));
    }

    public void callbackAltcamConfirmChildButton(ChildCallbacksDemo screen) {
        String summary = StringUtils.concat(
                "Confirmado para ",
                screen.childNameInput.getTextField().getText(),
                " com conteúdo: ",
                screen.childContentInput.getTextField().getText());
        updateStatus(screen, summary);

        ExampleCallbacks parentCallbacks = ScreenManager.getControllerReference(Example.class);
        Example parentScreen = ScreenManager.getScreenReference(Example.class);

        if (parentCallbacks != null && parentScreen != null) {
            String folderName = screen.childNameInput.getTextField().getText();
            parentCallbacks.addExternalFolder(parentScreen, folderName);
        }
    }

    public void callbackAltcamCloseChildButton(ChildCallbacksDemo screen) {
        if (screen == null) {
            return;
        }
        ScreenManager.closeChildWindow(screen);
    }

    public void callbackAltcamGoBackButton(ChildCallbacksDemo screen) {
        if (screen == null) {
            return;
        }
        if (!ScreenManager.canGoBack()) {
            updateStatus(screen, "Nenhuma tela anterior disponível para goBack.");
            return;
        }
        updateStatus(screen, "Voltando para a tela anterior...");
        ScreenManager.closeChildWindow(screen);
        ScreenManager.goBack();
    }

    private void configureValidationSamples(ChildCallbacksDemo screen) {
        if (screen == null || screen.validationExamplesList == null) {
            return;
        }
        if (Boolean.TRUE.equals(screen.validationExamplesList.getUserData())) {
            return;
        }
        screen.validationExamplesList.getItems().clear();
        for (ValidationSample sample : VALIDATION_SAMPLES) {
            screen.validationExamplesList.getItems().add(sample.getLabel());
        }
        screen.validationExamplesList.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex == null) {
                return;
            }
            applySample(screen, newIndex.intValue());
        });
        screen.validationExamplesList.getSelectionModel().selectFirst();
        screen.validationExamplesList.setUserData(Boolean.TRUE);
    }

    private void applySample(ChildCallbacksDemo screen, int index) {
        if (screen == null || index < 0 || index >= VALIDATION_SAMPLES.size()) {
            return;
        }
        ValidationSample sample = VALIDATION_SAMPLES.get(index);
        if (screen.pageTitleInput != null) {
            screen.pageTitleInput.setValue(sample.getPageTitle());
        }
        if (screen.pageNameInput != null) {
            screen.pageNameInput.setValue(sample.getPageName());
        }
        String statusPrefix = sample.isValid() ? "Caso válido" : "Caso inválido";
        updateStatus(screen, StringUtils.concat(statusPrefix, ": ", sample.getDescription()));
    }

    private static final class ValidationSample {
        private final String label;
        private final String pageTitle;
        private final String pageName;
        private final String description;
        private final boolean valid;

        private ValidationSample(String label, String pageTitle, String pageName, String description, boolean valid) {
            this.label = label;
            this.pageTitle = pageTitle;
            this.pageName = pageName;
            this.description = description;
            this.valid = valid;
        }

        private String getLabel() {
            return label;
        }

        private String getPageTitle() {
            return pageTitle;
        }

        private String getPageName() {
            return pageName;
        }

        private String getDescription() {
            return description;
        }

        private boolean isValid() {
            return valid;
        }
    }
}
