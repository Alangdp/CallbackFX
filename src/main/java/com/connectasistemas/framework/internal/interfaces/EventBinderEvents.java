package com.connectasistemas.framework.internal.interfaces;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.enums.FocusExitReason;
import com.connectasistemas.framework.fxelements.CheckEntryLabel;
import com.connectasistemas.framework.fxelements.TextEntryLabel;
import com.connectasistemas.framework.utils.EventContext;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.event.EventHandler;

import java.util.Collections;
import java.util.List;

/**
 * Interface abstrata para aplicar eventos em Nodes do JavaFX.
 * Cada instancia opera sobre um {@link EventContext} recebido no construtor,
 * eliminando o acesso direto a campos estaticos de Status.
 *
 * entcam            | callbackEntcamNome
 * saicam            | callbackSaicamNome
 * teclad            | callbackTecladNome
 * altcam            | callbackAltcamNome
 */
public abstract class EventBinderEvents {

    // Contexto de evento associado a esta instancia
    protected final EventContext eventContext;

    /**
     * Cria o binder com o contexto de evento informado.
     *
     * @param eventContext contexto que recebera as alteracoes de estado
     */
    protected EventBinderEvents(EventContext eventContext) {
        this.eventContext = eventContext != null ? eventContext : EventContext.getDefault();
    }

    /**
     * Construtor padrao que utiliza o contexto padrão.
     */
    protected EventBinderEvents() {
        this(EventContext.getDefault());
    }

    public abstract List<Runnable> applyEntcamSaicamEvent();
    public abstract List<Runnable> applyTecladEvent();
    public abstract List<Runnable> applyAltcamEvent();
    public List<Runnable> applyCustomEvents() {
        return Collections.emptyList();
    }

    /**
     * Publica o tipo de evento no contexto.
     *
     * @param eventType tipo de evento a publicar
     */
    protected void publishEvent(EventType eventType) {
        eventContext.setEvent(eventType);
    }

    /**
     * Altera o estado de validacao no contexto.
     *
     * @param valida novo estado
     */
    protected void changeValida(boolean valida) {
        eventContext.setValida(valida);
    }

    /**
     * Limpa o rastreamento de erros no contexto.
     */
    protected void resetErrorTracking() {
        eventContext.clearError();
    }

    /**
     * Consome o flag de skip de validacao do contexto.
     *
     * @return true se a validacao deveria ser ignorada
     */
    protected boolean consumeValidationSkip() {
        return eventContext.consumeValidationSkip();
    }

    /**
     * Se houver erro de validacao, foca no campo com erro ou no campo padrao.
     * O EventContext.node e setado quando e definido o erro no controller.
     * Esse metodo e executado somente em eventos de saida (entcam/saicam).
     *
     * @param defaultTarget alvo padrao caso nao haja campo com erro
     */
    protected void focusIfError(Object defaultTarget) {
        if (!eventContext.isValErrsim()) {
            return;
        }

        Object candidate = eventContext.getNode() != null ? eventContext.getNode() : defaultTarget;
        Node node = resolveFocusTarget(candidate);

        // Limpa o erro antes de focar
        eventContext.clearError();

        if (node == null) {
            return;
        }

        // Indica que nao valida
        eventContext.setValida(false);

        // Registra o motivo de saida como ESC
        eventContext.registerExitReason(FocusExitReason.ESC);

        // Indica que deve pular a proxima validacao
        // OBS: Isso ocorre pois ao sair de um campo com saicam e entrar em outro com saicam
        // o primeiro campo iria voltar o foco e dispararia o saicam do segundo campo,
        // ficando um loop infinito de foco
        eventContext.markValidationSkip();

        // Solicita o foco na proxima iteracao do JavaFX
        Platform.runLater(node::requestFocus);
    }

    /**
     * Resolve o Node correto para focar.
     *
     * @param candidate candidato a ser focado
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
     * Registra um rastreador de navegacao por teclado (TAB, SHIFT+TAB, ESC).
     *
     * @param node no alvo para registrar o rastreador
     * @return Runnable para remover o rastreador
     */
    protected Runnable registerNavigationTracker(Node node) {
        if (node == null) {
            return () -> {};
        }

        EventHandler<KeyEvent> tracker = e -> {
            if (e.getCode() == KeyCode.TAB) {
                if (e.isShiftDown()) {
                    eventContext.registerExitReason(FocusExitReason.SHIFT_TAB);
                } else {
                    eventContext.registerExitReason(FocusExitReason.TAB);
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                eventContext.registerExitReason(FocusExitReason.ESC);
            } else {
                eventContext.registerExitReason(FocusExitReason.OTHER);
            }
        };

        node.addEventFilter(KeyEvent.KEY_PRESSED, tracker);
        return () -> node.removeEventFilter(KeyEvent.KEY_PRESSED, tracker);
    }

    /**
     * Verifica se deve validar na saida do campo.
     *
     * @return true se deve validar, false caso contrario
     */
    protected boolean shouldValidateOnExit() {
        return eventContext.shouldValidateOnExit();
    }

    /**
     * Limpa a razao de saida do foco.
     */
    protected void clearExitReason() {
        eventContext.clearExitReason();
    }
}
