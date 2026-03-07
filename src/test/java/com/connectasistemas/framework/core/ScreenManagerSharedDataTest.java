package com.connectasistemas.framework.core;

import com.connectasistemas.framework.internal.utils.ScreenManagerSharedData;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link ScreenManagerSharedData}.
 * Camada 1 — motor do framework.
 */
class ScreenManagerSharedDataTest {

    @BeforeAll
    static void initJavaFX() {
        JavaFXTestHelper.initToolkit();
    }

    @AfterEach
    void tearDown() {
        ScreenManagerSharedData.resetScreenData();
    }

    // ---- Cenário 1: setScreenData / getScreenData roundtrip ----

    @Test
    @DisplayName("setScreenData e getScreenData devem preservar o valor")
    void shouldPreserveDataRoundtrip() {
        Object screen = new Object();
        Label label = new Label("teste");

        ScreenManagerSharedData.setScreenData(screen, "campo1", label);
        Object resultado = ScreenManagerSharedData.getScreenData(screen, "campo1");

        assertSame(label, resultado, "getScreenData deve retornar o mesmo objeto armazenado");
    }

    // ---- Cenário 2: setScreenData com chave duplicada lança exceção ----

    @Test
    @DisplayName("setScreenData com chave duplicada deve lançar RuntimeException")
    void shouldThrowWhenDuplicateKey() {
        Object screen = new Object();
        Label label1 = new Label("teste1");
        Label label2 = new Label("teste2");

        ScreenManagerSharedData.setScreenData(screen, "campo1", label1);

        assertThrows(RuntimeException.class,
                () -> ScreenManagerSharedData.setScreenData(screen, "campo1", label2),
                "Chave duplicada deve lançar exceção");
    }

    // ---- Cenário 3: getScreenData com chave inexistente lança exceção ----

    @Test
    @DisplayName("getScreenData com chave inexistente deve lançar RuntimeException")
    void shouldThrowWhenKeyNotFound() {
        Object screen = new Object();
        // Cadastra o screen para ele existir no CACHE
        ScreenManagerSharedData.setScreenData(screen, "existe", new Label());

        assertThrows(RuntimeException.class,
                () -> ScreenManagerSharedData.getScreenData(screen, "naoExiste"),
                "Chave inexistente deve lançar exceção");
    }

    // ---- Cenário 4: resetScreenData limpa dados da instância ----

    @Test
    @DisplayName("resetScreenData deve limpar os dados da instância")
    void shouldResetScreenData() {
        Object screen = new Object();
        ScreenManagerSharedData.setScreenData(screen, "campo1", new Label());

        ScreenManagerSharedData.resetScreenData(screen);

        assertNull(ScreenManagerSharedData.getCache().get(screen),
                "Cache da tela deve ser nulo após resetScreenData");
    }

    // ---- Cenário 5: tela null lança exceção ----

    @Test
    @DisplayName("setScreenData com tela null deve lançar exceção")
    void shouldThrowWhenScreenIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> ScreenManagerSharedData.setScreenData(null, "campo", new Label()),
                "Tela nula deve lançar exceção");
    }

    @Test
    @DisplayName("getScreenData com tela null deve lançar exceção")
    void shouldThrowWhenGetWithNullScreen() {
        assertThrows(IllegalArgumentException.class,
                () -> ScreenManagerSharedData.getScreenData(null, "campo"),
                "Tela nula deve lançar exceção");
    }

    // ---- Cenário extra: setScreenData com valor null lança exceção ----

    @Test
    @DisplayName("setScreenData com valor null deve lançar exceção")
    void shouldThrowWhenValueIsNull() {
        Object screen = new Object();
        assertThrows(IllegalArgumentException.class,
                () -> ScreenManagerSharedData.setScreenData(screen, "campo", null),
                "Valor nulo deve lançar exceção");
    }
}
