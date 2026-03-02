package com.connectasistemas.framework.util;

import com.connectasistemas.framework.utils.DateTimeUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link DateTimeUtils}.
 * Camada 1 — utilitários puros, sem dependência de JavaFX.
 */
class DateTimeUtilsTest {

    // Padrão esperado: yyyy-MM-dd HH:mm:ss
    private static final String TIMESTAMP_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    @Test
    @DisplayName("currentTimestamp deve retornar formato yyyy-MM-dd HH:mm:ss")
    void shouldReturnCurrentTimestampInExpectedFormat() {
        String resultado = DateTimeUtils.currentTimestamp();
        assertNotNull(resultado, "currentTimestamp() não deve ser nulo");
        assertTrue(resultado.matches(TIMESTAMP_REGEX),
                "Formato esperado: yyyy-MM-dd HH:mm:ss, recebido: " + resultado);
    }

    @Test
    @DisplayName("format com LocalDateTime deve retornar string formatada")
    void shouldFormatLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 3, 1, 10, 30, 0);
        String resultado = DateTimeUtils.format(dateTime);
        assertEquals("2026-03-01 10:30:00", resultado, "format deve formatar o LocalDateTime corretamente");
    }

    @Test
    @DisplayName("format com null deve retornar null")
    void shouldReturnNullWhenFormatNull() {
        assertNull(DateTimeUtils.format(null), "format(null) deve retornar null");
    }

    @Test
    @DisplayName("addDays deve somar dias ao timestamp informado")
    void shouldAddDaysToTimestamp() {
        String resultado = DateTimeUtils.addDays("2026-03-01 00:00:00", 5);
        assertEquals("2026-03-06 00:00:00", resultado, "addDays deve somar 5 dias corretamente");
    }

    @Test
    @DisplayName("addDays com valor negativo deve subtrair dias")
    void shouldSubtractDays() {
        String resultado = DateTimeUtils.addDays("2026-03-10 12:00:00", -3);
        assertEquals("2026-03-07 12:00:00", resultado, "addDays com valor negativo deve subtrair dias");
    }

    @Test
    @DisplayName("normalizeTimestamp com epoch millis deve converter para o formato padrão")
    void shouldNormalizeEpochMillis() {
        // 2026-03-01T00:00:00 no fuso local seria representado como epoch millis
        // Testamos apenas que o resultado segue o padrão
        long epochMillis = LocalDateTime.of(2026, 3, 1, 0, 0, 0)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        String resultado = DateTimeUtils.normalizeTimestamp(String.valueOf(epochMillis));
        assertNotNull(resultado, "normalizeTimestamp não deve retornar null");
        assertTrue(resultado.matches(TIMESTAMP_REGEX),
                "Formato esperado: yyyy-MM-dd HH:mm:ss, recebido: " + resultado);
    }

    @Test
    @DisplayName("normalizeTimestamp com formato ISO deve converter para padrão do banco")
    void shouldNormalizeIsoFormat() {
        String resultado = DateTimeUtils.normalizeTimestamp("2026-03-01T14:30:00");
        assertEquals("2026-03-01 14:30:00", resultado,
                "normalizeTimestamp com ISO deve converter para o formato do banco");
    }

    @Test
    @DisplayName("normalizeTimestamp com null deve retornar null")
    void shouldReturnNullWhenNormalizeNull() {
        assertNull(DateTimeUtils.normalizeTimestamp(null),
                "normalizeTimestamp(null) deve retornar null");
    }

    @Test
    @DisplayName("normalizeTimestamp com string vazia deve retornar string vazia")
    void shouldReturnEmptyWhenNormalizeEmpty() {
        assertEquals("", DateTimeUtils.normalizeTimestamp("  "),
                "normalizeTimestamp com espaços deve retornar vazio");
    }

    @Test
    @DisplayName("timestampAfterDays deve retornar timestamp futuro no formato padrão")
    void shouldReturnFutureTimestamp() {
        String resultado = DateTimeUtils.timestampAfterDays(1);
        assertNotNull(resultado, "timestampAfterDays não deve retornar null");
        assertTrue(resultado.matches(TIMESTAMP_REGEX),
                "Formato esperado: yyyy-MM-dd HH:mm:ss, recebido: " + resultado);
    }
}
