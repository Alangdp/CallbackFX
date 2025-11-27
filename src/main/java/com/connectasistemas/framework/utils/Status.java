package com.connectasistemas.framework.utils;

import com.connectasistemas.framework.enums.EventType;

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
}
