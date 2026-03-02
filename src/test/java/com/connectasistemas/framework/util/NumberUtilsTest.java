package com.connectasistemas.framework.util;

import com.connectasistemas.framework.utils.NumberUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link NumberUtils}.
 * Camada 1 — utilitários puros, sem dependência de JavaFX.
 */
class NumberUtilsTest {

    @Test
    @DisplayName("toInt com valor numérico válido deve retornar o inteiro correspondente")
    void shouldParseValidInt() {
        assertEquals(42, NumberUtils.toInt("42"), "toInt(\"42\") deve retornar 42");
    }

    @Test
    @DisplayName("toInt com texto não numérico deve retornar 0")
    void shouldReturnZeroWhenInvalidText() {
        assertEquals(0, NumberUtils.toInt("abc"), "toInt(\"abc\") deve retornar 0");
    }

    @Test
    @DisplayName("toInt com null deve retornar 0")
    void shouldReturnZeroWhenNull() {
        assertEquals(0, NumberUtils.toInt(null), "toInt(null) deve retornar 0");
    }

    @Test
    @DisplayName("toInt com string vazia deve retornar 0")
    void shouldReturnZeroWhenEmpty() {
        assertEquals(0, NumberUtils.toInt(""), "toInt(\"\") deve retornar 0");
    }

    @Test
    @DisplayName("toInt com espaços ao redor deve parsear corretamente")
    void shouldTrimAndParse() {
        assertEquals(7, NumberUtils.toInt("  7  "), "toInt(\" 7 \") deve retornar 7");
    }

    @Test
    @DisplayName("toInt com número negativo deve parsear corretamente")
    void shouldParseNegativeNumber() {
        assertEquals(-5, NumberUtils.toInt("-5"), "toInt(\"-5\") deve retornar -5");
    }
}
