package com.connectasistemas.framework.enums;

/**
 * Razao do evento de saida de foco.
 * Determina se a validacao deve ser disparada ao navegar entre campos.
 */
public enum FocusExitReason {
    TAB,
    SHIFT_TAB,
    ESC,
    OTHER
}
