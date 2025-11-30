package com.connectasistemas.framework.utils;

/**
 * Utilitário para conversão segura de números
 */
public class NumberUtils {

    /**
     * Converte String para int de forma segura
     * Retorna 0 se for null, vazia ou inválida
     */
    public static int toInt(String value) {
        if (value == null) {
            return 0;
        }

        value = value.trim();
        if (value.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
