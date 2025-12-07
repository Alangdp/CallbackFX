package com.connectasistemas.framework.interfaces;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.enums.FocusExitReason;
import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.utils.Status;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.event.EventHandler;

import java.util.Collections;
import java.util.List;

/**
 * Interface abstrata para aplicar eventos em Nodes do JavaFX
 * entcam            | callbackEntcamNome
 * saicam            | callbackSaicamNome
 * teclad            | callbackTecladNome
 * altcam            | callbackAltcamNome
 */
public abstract class EventBinderEvents {

    public abstract List<Runnable> applyEntcamSaicamEvent();
    public abstract List<Runnable> applyTecladEvent();
    public abstract List<Runnable> applyAltcamEvent();
    public List<Runnable> applyCustomEvents() {
        return Collections.emptyList();
    }

    protected void publishEvent(EventType eventType) {
        Status.EVENT = eventType;
    }

    protected void changeValida(boolean valida) {
        Status.VALIDA = valida;
    }

    protected void resetErrorTracking() {
        Status.clearError();
    }

    /**
     * Se houver erro de validação, foca no campo com erro ou no campo padrão
     * OBS: O Status.NODE é setado quando é definido o erro no controller
     * OBS: Esse método é executado somente em eventos de saída (entcam/saicam)
     * @param defaultTarget Alvo padrão caso não haja campo com erro
     */
    protected void focusIfError(Object defaultTarget) {
        if (!Status.VAL_ERRSIM) {
            return;
        }

        Object candidate = Status.NODE != null ? Status.NODE : defaultTarget;
        Node node = resolveFocusTarget(candidate);

        // Limpa o erro antes de focar
        Status.clearError();

        if (node == null) {
            return;
        }

        // Indica que não valida
        Status.VALIDA = false;
        
        // Registra o motivo de saída como ESC
        Status.registerExitReason(FocusExitReason.ESC);

        // Indica que deve pular a próxima validação
        // OBS: Isso ocorre pois ao sair de um campo com saicam e entrar em outro com saicam ...
        // ...O primeiro campo iria voltar o foco e dispararia o saicam do segundo campo...
        // ...Ficando um loop infinito de foco
        Status.markValidationSkip();

        // Solicita o foco na próxima iteração do JavaFX
        Platform.runLater(node::requestFocus);
    }

    /**
     * Resolve o Node correto para focar
     * @param candidate Candidato a ser focado
     * @return Node a ser focado ou null
     */
    private Node resolveFocusTarget(Object candidate) {
        if (candidate instanceof TextEntryLabel label) {
            return label.getTextField();
        }

        if (candidate instanceof CheckEntryLabel entry) {
            return entry.getCheckBox();
        }

        if (candidate instanceof Node node) {
            return node;
        }

        return null;
    }

    /**
     * Registra um rastreador de navegação por teclado (TAB, SHIFT+TAB, ESC)
     * @param node Nó alvo para registrar o rastreador
     * @return Runnable para remover o rastreador
     */
    protected Runnable registerNavigationTracker(Node node) {
        if (node == null) {
            return () -> {};
        }

        EventHandler<KeyEvent> tracker = e -> {
            if (e.getCode() == KeyCode.TAB) {
                if (e.isShiftDown()) {
                    Status.registerExitReason(FocusExitReason.SHIFT_TAB);
                } else {
                    Status.registerExitReason(FocusExitReason.TAB);
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                Status.registerExitReason(FocusExitReason.ESC);
            } else {
                Status.registerExitReason(FocusExitReason.OTHER);
            }
        };

        node.addEventFilter(KeyEvent.KEY_PRESSED, tracker);
        return () -> node.removeEventFilter(KeyEvent.KEY_PRESSED, tracker);
    }
    
    /**
     * Verifica se deve validar na saída do campo
     * @return true se deve validar, false caso contrário
     */
    protected boolean shouldValidateOnExit() {
        return Status.shouldValidateOnExit();
    }

    /**
     * Limpa a razão de saída do foco
     */
    protected void clearExitReason() {
        Status.clearExitReason();
    }
}
