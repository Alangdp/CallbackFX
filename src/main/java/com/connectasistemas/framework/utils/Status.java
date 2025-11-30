package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.enums.EventType;
import com.connectasistemas.framework.enums.FocusExitReason;

/**
 * Classe usada para status globalmente, Deve ser alterada com cuidado
 */
public class Status {
    // Valida 
    // OBS: O valida fica true quando é executado um avanço de tela
    // Ao voltar usando ESC, o valida deve ser false
    public static boolean VALIDA;

    // Tipo de evento atual
    public static EventType EVENT;

    // Razão da saída
    public static FocusExitReason EXIT_REASON;

    // Indica se houve erro
    public static boolean VAL_ERRSIM = false;

    // Último campo processado de tela
    public static Object NODE;

    private static boolean SKIP_VALIDATION;

    /**
     * Marca a ocorrência de erro sem um alvo específico. O último campo processado
     * será usado como fallback para o foco.
     */
    public static void markError() {
        VAL_ERRSIM = true;
        NODE = null;
    }

    /**
     * Marca a ocorrência de erro e registra um alvo preferencial para receber o foco.
     *
     * @param node Instância que deve recuperar o foco. Pode ser um Node ou um componente que contenha um Node focável.
     */
    public static void markError(Object node) {
        VAL_ERRSIM = true;
        NODE = node;
    }

    /**
     * Limpa o estado de erro global.
     */
    public static void clearError() {
        VAL_ERRSIM = false;
        NODE = null;
    }

    public static void markValidationSkip() {
        SKIP_VALIDATION = true;
    }

    public static boolean consumeValidationSkip() {
        boolean skip = SKIP_VALIDATION;
        SKIP_VALIDATION = false;
        return skip;
    }

    /**
     * Registra o motivo da saída do foco usado para decidir se deve validar.
     *
     * @param reason razão da navegação; null limpa o motivo atual
     */
    public static void registerExitReason(FocusExitReason reason) {
        EXIT_REASON = reason;
    }

    /**
     * Limpa o motivo registrado da última navegação.
     */
    public static void clearExitReason() {
        EXIT_REASON = null;
    }

    /**
     * Indica se a saída atual deve disparar validação dos campos.
     *
     * @return true quando é avanço padrão; false para Shift+Tab, ESC e casos equivalentes
     */
    public static boolean shouldValidateOnExit() {
        return EXIT_REASON != FocusExitReason.SHIFT_TAB && EXIT_REASON != FocusExitReason.ESC;
    }
}
