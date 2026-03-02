package com.connectasistemas.framework.util;

import com.connectasistemas.framework.utils.StringUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link StringUtils}.
 * Camada 1 — utilitários puros, sem dependência de JavaFX.
 */
class StringUtilsTest {

    // ---- concat ----

    @Test
    @DisplayName("concat com múltiplos argumentos deve concatenar corretamente")
    void shouldConcatMultipleArgs() {
        String resultado = StringUtils.concat("a", "b", "c");
        assertEquals("abc", resultado, "Concatenação simples de três strings");
    }

    @Test
    @DisplayName("concat sem argumentos deve retornar string vazia")
    void shouldReturnEmptyWhenNoArgs() {
        String resultado = StringUtils.concat();
        assertEquals("", resultado, "concat() sem argumentos deve retornar vazio");
    }

    @Test
    @DisplayName("concat com null entre os argumentos não deve quebrar")
    void shouldHandleNullInConcat() {
        // Espera-se que null seja convertido em "null" pelo StringBuilder
        String resultado = StringUtils.concat(null, "x");
        assertNotNull(resultado, "concat com null não deve retornar nulo");
        assertTrue(resultado.contains("x"), "concat com null deve conter 'x'");
    }

    @Test
    @DisplayName("concat com array null retorna string vazia")
    void shouldReturnEmptyWhenNullArray() {
        String resultado = StringUtils.concat((Object[]) null);
        assertEquals("", resultado, "concat com array null deve retornar vazio");
    }

    // ---- isEmpty ----

    @Test
    @DisplayName("isEmpty com null deve retornar true")
    void shouldReturnTrueWhenNullIsEmpty() {
        assertTrue(StringUtils.isEmpty(null), "isEmpty(null) deve ser true");
    }

    @Test
    @DisplayName("isEmpty com string vazia deve retornar true")
    void shouldReturnTrueWhenEmptyString() {
        assertTrue(StringUtils.isEmpty(""), "isEmpty(\"\") deve ser true");
    }

    @Test
    @DisplayName("isEmpty com string preenchida deve retornar false")
    void shouldReturnFalseWhenNonEmpty() {
        assertFalse(StringUtils.isEmpty("abc"), "isEmpty(\"abc\") deve ser false");
    }

    // ---- isBlank ----

    @Test
    @DisplayName("isBlank com espaços em branco deve retornar true")
    void shouldReturnTrueWhenOnlyWhitespace() {
        assertTrue(StringUtils.isBlank("   "), "isBlank(\"   \") deve ser true");
    }

    @Test
    @DisplayName("isBlank com null deve retornar true")
    void shouldReturnTrueWhenNullIsBlank() {
        assertTrue(StringUtils.isBlank(null), "isBlank(null) deve ser true");
    }

    @Test
    @DisplayName("isBlank com texto real deve retornar false")
    void shouldReturnFalseWhenNotBlank() {
        assertFalse(StringUtils.isBlank("abc"), "isBlank(\"abc\") deve ser false");
    }

    // ---- capitalize ----

    @Test
    @DisplayName("capitalize deve converter a primeira letra para maiúscula")
    void shouldCapitalizeFirstLetter() {
        assertEquals("Hello", StringUtils.capitalize("hello"), "capitalize(\"hello\") deve ser \"Hello\"");
    }

    @Test
    @DisplayName("capitalize com string vazia retorna a própria string")
    void shouldReturnSameWhenCapitalizeEmpty() {
        assertEquals("", StringUtils.capitalize(""), "capitalize(\"\") deve retornar vazio");
    }

    @Test
    @DisplayName("capitalize com null retorna null")
    void shouldReturnNullWhenCapitalizeNull() {
        assertNull(StringUtils.capitalize(null), "capitalize(null) deve retornar null");
    }

    @Test
    @DisplayName("capitalize com string de um caractere")
    void shouldCapitalizeSingleChar() {
        assertEquals("A", StringUtils.capitalize("a"), "capitalize(\"a\") deve ser \"A\"");
    }

    // ---- replaceParams ----

    @Test
    @DisplayName("replaceParams substitui marcadores posicionais corretamente")
    void shouldReplacePositionalParams() {
        String resultado = StringUtils.replaceParams("Nome: %1, Ativo: %2", "Proj", "Sim");
        assertEquals("Nome: Proj, Ativo: Sim", resultado, "Substituição posicional com dois parâmetros");
    }

    @Test
    @DisplayName("replaceParams com template null retorna null")
    void shouldReturnNullWhenTemplateNull() {
        assertNull(StringUtils.replaceParams(null, "a"), "replaceParams com template null deve retornar null");
    }

    @Test
    @DisplayName("replaceParams sem argumentos retorna o template original")
    void shouldReturnTemplateWhenNoArgs() {
        String template = "Texto sem marcadores";
        assertEquals(template, StringUtils.replaceParams(template), "Sem args deve retornar template");
    }

    // ---- trim ----

    @Test
    @DisplayName("trim remove espaços do início e do fim")
    void shouldTrimWhitespace() {
        assertEquals("abc", StringUtils.trim("  abc  "), "trim deve remover espaços");
    }

    @Test
    @DisplayName("trim com null retorna null")
    void shouldReturnNullWhenTrimNull() {
        assertNull(StringUtils.trim(null), "trim(null) deve retornar null");
    }

    // ---- lowerCase ----

    @Test
    @DisplayName("lowerCase converte para minúsculas")
    void shouldConvertToLowerCase() {
        assertEquals("abc", StringUtils.lowerCase("ABC"), "lowerCase(\"ABC\") deve ser \"abc\"");
    }

    @Test
    @DisplayName("lowerCase com null retorna string vazia")
    void shouldReturnEmptyWhenLowerCaseNull() {
        assertEquals("", StringUtils.lowerCase(null), "lowerCase(null) deve retornar vazio");
    }
}
