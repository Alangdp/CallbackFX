package com.connectasistemas.framework.contract;

import com.connectasistemas.framework.internal.examples.ExampleController;
import com.connectasistemas.framework.internal.utils.CallbackInvoker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes parametrizados de completude do {@link ExampleController}.
 * Camada 4 — garante que todos os callbacks documentados existem no controller.
 */
class ExampleControllerContractTest {

    private ExampleController controller;

    @BeforeEach
    void setUp() {
        controller = new ExampleController();
    }

    // ---- Verifica a existência de cada callback esperado ----

    @ParameterizedTest(name = "callback{1}{2} deve existir no controller")
    @DisplayName("Todos os callbacks documentados devem existir no ExampleController")
    @CsvSource({
            "config, Config, Example",
            "altcam, Altcam, FilterInput",
            "teclad, Teclad, FilterInput",
            "altcam, Altcam, RefreshButton",
            "altcam, Altcam, ExportButton",
            "altcam, Altcam, AddFolderButton",
            "altcam, Altcam, FeatureTree",
            "click,  Click,  FeatureTree",
            "doubleClick, DoubleClick, FeatureTree",
            "altcam, Altcam, DatasetTable",
            "click,  Click,  DatasetTable",
            "doubleClick, DoubleClick, DatasetTable",
            "altcam, Altcam, SaveButton",
            "altcam, Altcam, ClearButton"
    })
    void shouldHaveCallback(String event, String eventCapitalized, String acronym) {
        // Converte acronym para começar com minúscula
        String acLower = acronym.substring(0, 1).toLowerCase() + acronym.substring(1);
        assertTrue(CallbackInvoker.exists(controller, event, acLower),
                "callback" + eventCapitalized + acronym + " deve existir no ExampleController");
    }

    // ---- Verifica que callbacks inexistentes retornam false ----

    @ParameterizedTest(name = "callback{0}{1} não deve existir")
    @DisplayName("Callbacks não documentados devem retornar false")
    @CsvSource({
            "altcam, campoInexistente",
            "entcam, naoExiste",
            "valida, fantasma"
    })
    void shouldNotHaveUndocumentedCallback(String event, String acronym) {
        assertFalse(CallbackInvoker.exists(controller, event, acronym),
                "callback inexistente não deve ser encontrado");
    }
}
