package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.enums.FocusExitReason;

/**
 * Contexto de execucao de um evento.
 * Cada pipeline de evento pode operar sobre sua propria instancia, eliminando
 * colisoes entre janelas ou testes paralelos.
 */
public class EventContext {

    // Instancia padrao usada pelo fluxo principal
    private static final EventContext DEFAULT = new EventContext();

    // Indica se a validacao esta ativa
    private boolean valida;

    // Tipo de evento atual
    private EventType event;

    // Razao da saida do foco
    private FocusExitReason exitReason;

    // Indica se houve erro de validacao
    private boolean valErrsim;

    // Ultimo campo processado de tela (alvo para foco em caso de erro)
    private Object node;

    // Indica se o usuario confirmou uma selecao de dialogo
    private boolean confirmedSelection;

    // Indica se a validacao deve ser ignorada na proxima verificacao
    private boolean skipValidation;

    // --- Getters e Setters ---

    /**
     * Retorna se a validacao esta ativa.
     */
    public boolean isValida() {
        return valida;
    }

    /**
     * Define o estado de validacao.
     *
     * @param valida novo estado
     */
    public void setValida(boolean valida) {
        this.valida = valida;
    }

    /**
     * Retorna o tipo de evento atual.
     */
    public EventType getEvent() {
        return event;
    }

    /**
     * Define o tipo de evento atual.
     *
     * @param event tipo do evento
     */
    public void setEvent(EventType event) {
        this.event = event;
    }

    /**
     * Retorna a razao da saida do foco.
     */
    public FocusExitReason getExitReason() {
        return exitReason;
    }

    /**
     * Define a razao da saida do foco.
     *
     * @param exitReason razao da navegacao; null limpa o motivo atual
     */
    public void setExitReason(FocusExitReason exitReason) {
        this.exitReason = exitReason;
    }

    /**
     * Indica se houve erro de validacao.
     */
    public boolean isValErrsim() {
        return valErrsim;
    }

    /**
     * Retorna o ultimo campo processado (alvo preferencial para foco em erro).
     */
    public Object getNode() {
        return node;
    }

    /**
     * Define o ultimo campo processado.
     *
     * @param node instancia que deve recuperar o foco
     */
    public void setNode(Object node) {
        this.node = node;
    }

    /**
     * Indica se o usuario confirmou a selecao de dialogo.
     */
    public boolean isConfirmedSelection() {
        return confirmedSelection;
    }

    /**
     * Define o estado de confirmacao de selecao.
     *
     * @param confirmedSelection novo estado
     */
    public void setConfirmedSelection(boolean confirmedSelection) {
        this.confirmedSelection = confirmedSelection;
    }

    // --- Metodos de conveniencia ---

    /**
     * Marca a ocorrencia de erro sem um alvo especifico.
     */
    public void markError() {
        valErrsim = true;
        node = null;
    }

    /**
     * Marca a ocorrencia de erro e registra um alvo preferencial para receber o foco.
     *
     * @param node instancia que deve recuperar o foco
     */
    public void markError(Object node) {
        valErrsim = true;
        this.node = node;
    }

    /**
     * Limpa o estado de erro.
     */
    public void clearError() {
        valErrsim = false;
        node = null;
    }

    /**
     * Marca que a proxima validacao deve ser ignorada.
     */
    public void markValidationSkip() {
        skipValidation = true;
    }

    /**
     * Consome o flag de skip de validacao, retornando se estava ativo.
     *
     * @return true se a validacao deveria ser ignorada
     */
    public boolean consumeValidationSkip() {
        boolean skip = skipValidation;
        skipValidation = false;
        return skip;
    }

    /**
     * Registra o motivo da saida do foco.
     *
     * @param reason razao da navegacao; null limpa o motivo atual
     */
    public void registerExitReason(FocusExitReason reason) {
        exitReason = reason;
    }

    /**
     * Limpa o motivo registrado da ultima navegacao.
     */
    public void clearExitReason() {
        exitReason = null;
    }

    /**
     * Indica se a saida atual deve disparar validacao dos campos.
     *
     * @return true quando e avanco padrao; false para Shift+Tab, ESC e casos equivalentes
     */
    public boolean shouldValidateOnExit() {
        return exitReason != FocusExitReason.SHIFT_TAB && exitReason != FocusExitReason.ESC;
    }

    /**
     * Retorna a instancia padrao do contexto de evento.
     */
    public static EventContext getDefault() {
        return DEFAULT;
    }
}
